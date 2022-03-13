package ru.crystals.pos.scale.shtrih.slim200;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ShtrihSlimDoubleImpl extends AbstractScalePluginImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ShtrihSlimDoubleImpl.class);
    private int minimalWeight = -1; // считывается из конфига, если в конфиге нет, то из весов.
    private static final String SCALE_SYMLINK_DIRECTORY = "/dev/";
    private static final String SCALE_SYMLINK_NAME = "usbSLIM";
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private ShtrihSlim200ScaleServiceImpl firstScale;
    private ShtrihSlim200ScaleServiceImpl secondScale;
    private static final int WEIGHT_NOT_ACCEPTED_STATUS = -666;

    @Override
    public void start() {
        reconnectDevicesIfNeeded();
        Thread watcher = new Thread(new ScalesWatcher());
        watcher.setPriority(Thread.MIN_PRIORITY);
        watcher.start();
    }

    @Override
    public void stop() {
        //DO NOTHING
    }

    @Override
    public synchronized int getWeight() throws ScaleException {
        boolean firstAvailable = checkScaleAvailable(firstScale);
        boolean secondAvailable = checkScaleAvailable(secondScale);
        if (firstAvailable && secondAvailable) {
            try {
                List<GetWeightCommand> commandList = Arrays.asList(new GetWeightCommand(firstScale), new GetWeightCommand(secondScale));
                List<Future<Integer>> futures = executor.invokeAll(commandList);
                Integer firstValue = futures.get(0).get();
                Integer secondValue = futures.get(1).get();
                if (firstValue == null || secondValue == null) {
                    throw new ScaleException("Can't get Weight from at least one scale!");
                } else if (!(firstValue.equals(0) || secondValue.equals(0)) || (firstValue.compareTo(0) < 0 || secondValue.compareTo(0) < 0)) {
                    LOG.error("Weight on all scales, or at least one scales is on error state! " + firstValue + "g and " + secondValue + "g!");
                    return -1;
                } else {
                    return Math.max(firstValue, secondValue);
                }
            } catch (Exception e) {
                throw new ScaleException(e.getMessage());
            }
        } else if (firstAvailable) {
            return firstScale.getWeight();
        } else if (secondAvailable) {
            return secondScale.getWeight();
        } else {
            throw new ScaleException("Scales is not online");
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return firstScale.moduleCheckState() && secondScale.moduleCheckState();
    }

    private boolean checkScaleAvailable(ShtrihSlim200ScaleServiceImpl scale) {
        try {
            return scale != null && scale.validateStatus();
        } catch (Exception e) {
            return false;
        }
    }

    public int getMinimalWeight() {
        return minimalWeight;
    }

    public void setMinimalWeight(int minimalWeight) {
        this.minimalWeight = minimalWeight;
    }

    /**
     * Создает, настраивает и запускает плагин весов
     *
     * @param port
     */
    ShtrihSlim200ScaleServiceImpl createAndStartScalePlugin(File port) {
        ShtrihSlim200ScaleServiceImpl result = null;
        if (port.exists()) {
            String realPort;
            try {
                realPort = port.getCanonicalPath();
            } catch (Exception e) {
                return null;
            }
            result = new ShtrihSlimInnerImpl();
            result.setPort(realPort, false);
            result.setMinimalWeight(minimalWeight);
            try {
                result.start();
                result.getWeight();
            } catch (Exception e) {
                return null;
            }
        }
        return result;
    }

    class GetWeightCommand implements Callable<Integer> {
        private ShtrihSlim200ScaleServiceImpl scaleInstance;

        public GetWeightCommand(ShtrihSlim200ScaleServiceImpl scaleInstance) {
            this.scaleInstance = scaleInstance;
        }

        @Override
        public Integer call() {
            try {
                return scaleInstance.getWeight();
            } catch (Exception e) {
                LOG.error("Exception on getWeight command from scale on " + scaleInstance.getPort() + "port", e);
                return null;
            }
        }
    }

    private class ShtrihSlimInnerImpl extends ShtrihSlim200ScaleServiceImpl {
        @Override
        protected synchronized int parseWeightData(WeightData weightData, int totalMinimalWeight) {
            int weight = 0;
            if ((weightData != null) && (weightData.getErrorCode() == 0)) {
                if (!weightData.isZeroOnStartError() && !weightData.isOverloadScales() && !weightData.isMeasureError() && !weightData.isLittleWeight() && !weightData
                        .isNoAnswerADP() && weightData.isScalesIsStable()) {
                    // Если нет перегрузки и нет ошибок, то
                    /*
                     * правильный код с точки зрения логики, так как вес
                     * минимальный гарантированный вес не должен считываться из
                     * конфига, а должен браться непосредственно из самих весов
                     * if (weightData.isScalesIsStable() && weightData.isWeightIsFixed()) {
                     */
                    if (((weightData.getTaraWeight() + weightData.getWeight()) >= totalMinimalWeight)) {
                        // Если вес зафиксирован, то получаем вес
                        weight = weightData.getWeight();
                    } else if (weightData.isTarePresent()) {
                        // Если весы стабилизированы и присутствует тара, но вес
                        // меньше minimalWeight (вес не зафиксирован)
                        if ((weightData.getTaraWeight() + weightData.getWeight()) >= totalMinimalWeight) {
                            // Если сумма веса тары и веса товара >= НмПВ, то
                            // получаем вес
                            weight = weightData.getWeight();
                        }
                    }
                } else {
                    //Вес слишком мал, нестабилен, или возникла ошибка? В данных весах проверим, что вес больший чем указанный минимальный пришел от весов. В таком
                    // случае выкинем адский вес :)
                    // который потом обработаем в плагине. SRTD-332
                    return weightData.getWeight() >= totalMinimalWeight ? WEIGHT_NOT_ACCEPTED_STATUS : 0;
                }
            }
            return Math.max(weight, 0);
        }
    }

    private class ScalesWatcher implements Runnable {

        private static final long WATCH_INTERVAL_MILLIS = 3000L;

        @Override
        public void run() {
            Path devicesLocation = new File(SCALE_SYMLINK_DIRECTORY).toPath();
            try {
                WatchService service = devicesLocation.getFileSystem().newWatchService();
                devicesLocation.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    try {
                        WatchKey poll = service.take();
                        boolean foundScale = false;
                        if (poll != null) {
                            List<WatchEvent<?>> events = poll.pollEvents();
                            if (CollectionUtils.isNotEmpty(events)) {
                                for (WatchEvent<?> watchEvent : events) {
                                    WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                                    if (event.context().getFileName().toString().startsWith(SCALE_SYMLINK_NAME)) {
                                        foundScale = true;
                                    }
                                }
                            }
                            poll.reset();
                        }
                        if (foundScale) {
                            reconnectDevicesIfNeeded();
                        }
                    } catch (Exception e) {
                        //DO NOTHING
                    } finally {
                        sleep();
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to start scale watching service", e);
            }
        }

        private void sleep() {
            try {
                Thread.sleep(WATCH_INTERVAL_MILLIS);
            } catch (Exception e) {
                //DO NOTHING
            }
        }
    }

    private synchronized void reconnectDevicesIfNeeded() {
        boolean firstAvailable = checkScaleAvailable(firstScale);
        boolean secondAvailable = checkScaleAvailable(secondScale);
        if (!firstAvailable || !secondAvailable) {
            File dir = new File(SCALE_SYMLINK_DIRECTORY);
            File[] symlinks = dir.listFiles((dir1, name) -> name.startsWith(SCALE_SYMLINK_NAME));
            if (symlinks != null && symlinks.length > 0) {
                for (File symlink : symlinks) {
                    ShtrihSlim200ScaleServiceImpl newScale = createAndStartScalePlugin(symlink);
                    if (newScale != null) {
                        if (!firstAvailable) {
                            firstScale = newScale;
                            firstAvailable = true;
                        } else if (!secondAvailable) {
                            secondScale = newScale;
                            secondAvailable = true;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

}
