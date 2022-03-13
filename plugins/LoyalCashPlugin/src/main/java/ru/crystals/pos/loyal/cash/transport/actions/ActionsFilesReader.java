package ru.crystals.pos.loyal.cash.transport.actions;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.transport.ActionFileInfo;
import ru.crystals.discounts.transport.IAdvertiseActionsFileTransferRemote;
import ru.crystals.httpclient.TransportDisabledException;
import ru.crystals.loyal.deserializer.EntityDeserializer;
import ru.crystals.loyal.deserializer.actions.AdvertisingActionEntityDeserializer;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.loyal.LastDiscountIDEntity;
import ru.crystals.pos.loyal.cash.service.LoyalServiceImpl;
import ru.crystals.pos.loyal.cash.transport.persistence.ActionsTransportAuxiliariesDao;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.salemetrics.JmxMetrics;
import ru.crystals.pos.transport.ConnectionState;
import ru.crystals.pos.transport.ModuleConnection;
import ru.crystals.transport.TransferObject;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Класс, отвечающий за проверку, получение и прочтение файлов со скидками на сервере (транспорт рекламных акций сервер-касса).
 *
 * @author AlekseyOF
 * @author Anton Martynov
 */
public class ActionsFilesReader extends ModuleConnection implements Runnable, ConnectionState {
    private static final Short LASTID_VERSION = 1;
    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(ActionsFilesReader.class);

    @Autowired
    private InternalCashPoolExecutor executor;

    /**
     * Надстройка над таблицей sales_management_properties, позволяющая читать оттуда данные.
     */
    @Autowired
    protected PropertiesManager propertiesManager;

    /**
     * Название серверного модуля
     */
    private static final String SERVER_MODULE_NAME = "LOY";

    // injected
    /**
     * Через эту штуку будем сохранять в БД инфу о ходе импорта объектов лояльности (РА + настройки)
     */
    private ActionsTransportAuxiliariesDao trsAuxDao;

    private IAdvertiseActionsFileTransferRemote discountsMan;

    private boolean workWithFiles = false;

    private LoyalServiceImpl service;

    private boolean firstRequestAfterStart = false;

    public void start() {
        // получение пути из реестра модулей для данного модуля
        setURL();

        discountsMan = getHttpConnect().find(IAdvertiseActionsFileTransferRemote.class, "java:app/SET-Discounts/SET/DiscountsFileTransferBean!ru.crystals.discounts" +
                ".transport.IAdvertiseActionsFileTransferRemote");
        firstRequestAfterStart = true;
        scheduleDeferredTask();
    }

    protected void scheduleDeferredTask() {
        log.debug("Scheduling next call of data type \"{}\" after {} seconds.", SERVER_MODULE_NAME, getPollInterval());
        executor.schedule(this, getPollInterval(), TimeUnit.SECONDS);
    }

    public void stop() {
    }

    private Collection<ActionFileInfo> getNewFiles(Long id) {
        try {
            Collection<ActionFileInfo> files = discountsMan.getNewAdvertiseActionFileInfoForCash(getOrCreateAddress(), id, firstRequestAfterStart);
            if (files != null) {
                firstRequestAfterStart = false;
            }
            return files;
        } catch (Exception e) {
            if (e instanceof TransportDisabledException) {
                log.warn(e.toString());
            } else {
                log.error("Could not get files list for cash.", e);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Обработка файла рекламных акций
     *
     * @param fileName путь к файлу
     * @return результат обратоки файла (обработан / не обработан)
     */
    private boolean readNewFile(String fileName, List<Long> readActions) {
        long stopWatch = System.currentTimeMillis();
        String shortFileName = (fileName.lastIndexOf('/') > 0) ? fileName.substring(fileName.lastIndexOf('/')) : fileName;

        try {
            URL url = new URL(fileName);

            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setUseCaches(false);

            List<TransferObject> objects = new ArrayList<>();
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(conn.getInputStream()))) {
                while (true) {
                    try {
                        Object o = ois.readObject();
                        if (o instanceof TransferObject) {
                            objects.add((TransferObject) o);
                        }
                    } catch (EOFException e) {
                        // it's ok
                        break;
                    }
                }
            }
            // 1. расшифруем эти транспортные сообщения - поймем что вообще прислали:
            DeserializedLoyalObjects dlo = deserializeTransferObjects(objects);

            // 2. а теперь скормим что нам прислали кому надо:
            // 2.1. обработка присланных РА:
            // 2.1.1. GUID'ы всех присланных РА надо зафигачить в discounts:
            for (AdvertisingActionEntity aae : dlo.getActions()) {
                readActions.add(aae.getGuid());
            } // for aae

            // 2.2.2. Сохранить присланные РА в БД и обновить кэш РА:
            service.actionsImportedEvent(dlo.getActions());
            return true;
        } catch (Exception mue) {
            log.error("File " + shortFileName + " has NOT been read", mue);
            return false;
        } finally {
            JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.ACTIONS_PACKAGE_SAVE, System.currentTimeMillis() - stopWatch);
        }

    }

    /**
     * Десериализует/извлечет данные из указанных {@link TransferObject транспортных сообщений} в понятном нам виде.
     * <p/>
     * Implementation Note: не впихнет <code>null</code> ни в одну из полей коллекций: ни в {@link DeserializedLoyalObjects#getActions() список РА}.
     * <p/>
     * Implementation Note2: ВСЕ РА, что попадут в {@link DeserializedLoyalObjects#getActions() список РА} будут иметь не-NULL'евые
     * {@link AdvertisingActionEntity#getGuid() GUID'ы}: присланная РА без этого поля будет тупо забракована.
     *
     * @param objects отсюда надо извлечь информацию
     * @return никогда не вернет <code>null</code> - в крайнем случае вернет пустой объект
     */
    private static DeserializedLoyalObjects deserializeTransferObjects(List<TransferObject> objects) {
        DeserializedLoyalObjects result = new DeserializedLoyalObjects();

        log.trace("entering deserializeTransferObjects(List). The argument's size is: {}", objects == null ? "(NULL)" : objects.size());

        if (objects != null && !objects.isEmpty()) {
            for (TransferObject to : objects) {
                if (to != null && to.serializableObject instanceof byte[]) {
                    EntityDeserializer deserializer = EntityDeserializer.getActionInstanse((byte[]) to.serializableObject, to.objectType);
                    if (deserializer instanceof AdvertisingActionEntityDeserializer) {
                        // полезной нагрузкой этого сообщения является РА
                        log.trace("Deserializing advertising action...");

                        AdvertisingActionEntityDeserializer userEntityDeserializer = (AdvertisingActionEntityDeserializer) deserializer;
                        AdvertisingActionEntity advertisingActionEntity = userEntityDeserializer.getAdvertisingActionEntity();
                        if (advertisingActionEntity != null) {
                            log.trace("the deserialized action is: {}", advertisingActionEntity);

                            if (advertisingActionEntity.getGuid() != null) {
                                result.getActions().add(advertisingActionEntity);
                            } else {
                                log.error("deserializeTransferObjects(List): the deserialized action [{}] lacks GUID! It will be removed from further processing!",
                                        advertisingActionEntity);
                            }
                        } else {
                            log.warn("deserializeTransferObjects(List): failed to deserialize adv-action!");
                        }
                    } else {
                        // х.з. что прислали. десериэлайзер не смогли определить. Просто проигнорим
                        log.error("deserializeTransferObjects(List): unknown payload was attached to this TO [object-type: {}; deserializer-class: {}]!",
                                to.objectType, deserializer == null ? "(NULL)" : deserializer.getClass().getCanonicalName());
                    }
                } else {
                    log.warn("deserializeTransferObjects(List): an illegal transfer-object was received: either NULL, " +
                                    "or its payload is of a wrong type [class: {}] - not an array of bytes!",
                            to == null ? "(NULL)" : to.serializableObject == null ? "nULL" : to.serializableObject.getClass().getCanonicalName());
                }
            } // for to
        } else {
            log.warn("deserializeTransferObjects(List): The argument is EMPTY!");
        }
        log.trace("leaving deserializeTransferObjects(List). The result is: {}", result);

        return result;
    }

    /**
     * Отправка информации об обработке файла рекламных акций
     *
     * @param id      идентификатор файла рекламных акций
     * @param success успешность обработки файла
     * @param items   список идентификаторов рекламных акций
     * @return успешность отправки отчёта на сервер
     */
    private boolean sendReport(Long id, boolean success, List<Long> items) {
        try {
            discountsMan.acknowledgeAdvertiseActions(getOrCreateAddress(), id, success, items);
            return true;
        } catch (Exception e) {
            log.error("Could not send ack to server", e);
            return false;
        }
    }

    public void setService(LoyalServiceImpl service) {
        this.service = service;
    }

    public LoyalServiceImpl getService() {
        return service;
    }

    /**
     * Сохранение списка идентификаторов рекламных акций
     *
     * @param discounts список рекламных акций
     */
    private void saveDiscounts(List<Long> discounts) {
        trsAuxDao.saveDiscounts(discounts);
    }

    private List<Long> getDiscounts() {
        return trsAuxDao.getProcessedActionsGuids();
    }

    @Override
    public boolean getConnectionState() {
        return true;
    }

    @Override
    public void run() {
        if (workWithFiles) {
            scheduleDeferredTask();
            return;
        }

        workWithFiles = true;
        try {
            workWithFiles();
        } finally {
            workWithFiles = false;
            scheduleDeferredTask();
        }
    }

    private void workWithFiles() {
        try {
            LastDiscountIDEntity lastDisountsFile = trsAuxDao.getLastDiscountId();

            if (lastDisountsFile != null) {
                if (!lastDisountsFile.getSendToServer()) {
                    // информация о последнем полученном файле не была отправлена на сервер
                    List<Long> discounts = getDiscounts();

                    if (sendReport(lastDisountsFile.getDiscountId(), lastDisountsFile.getSaved(), discounts)) {
                        lastDisountsFile.setSendToServer(true);

                        trsAuxDao.saveLastDiscountId(lastDisountsFile);
                    } else {
                        saveDiscounts(discounts);
                    }

                    return;
                }
            }

            Long lastid = 0L;
            if (lastDisountsFile != null) {
                lastid = lastDisountsFile.getDiscountId();
            }

            // получение списка файлов для данной кассы
            Collection<ActionFileInfo> files = getNewFiles(lastid);
            if (files == null || files.isEmpty()) {
                if (log.isTraceEnabled()) {
                    log.trace("no new files; last id = " + lastid);
                }
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace("size of list of new files: " + files.size());
            }

            // обработка файлов (по возрастанию идентификаторов)
            files = files.stream()
                    .sorted(Comparator.comparing(ActionFileInfo::getId))
                    .collect(Collectors.toList());

            for (ActionFileInfo actionFileInfo : files) {
                LastDiscountIDEntity discountId = new LastDiscountIDEntity();
                discountId.setVersion(LASTID_VERSION);
                discountId.setGuid(actionFileInfo.getId());
                discountId.setDiscountId(actionFileInfo.getId());
                discountId.setSendToServer(false);

                List<Long> readActions = new LinkedList<>();
                boolean successRead = readNewFile(actionFileInfo.getFilePath(), readActions);
                if (!Integer.valueOf(readActions.size()).equals(actionFileInfo.getObjectsCount())) {
                    log.error("Attention! Lost of actions detected in file {}! Expected {} actions, but read only {}",
                            actionFileInfo.getFilePath(), actionFileInfo.getObjectsCount(), readActions.size());
                    tryDumpFile(actionFileInfo.getFilePath());
                }
                discountId.setSaved(successRead);

                trsAuxDao.saveLastDiscountId(discountId);

                if (sendReport(actionFileInfo.getId(), successRead, readActions)) {
                    if (successRead) {
                        discountId.setSendToServer(true);

                        trsAuxDao.saveLastDiscountId(discountId);
                    } else {
                        saveDiscounts(readActions);
                        break;
                    }
                } else {
                    saveDiscounts(readActions);
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("Loyal transport error", ex);
        }
    }

    /**
     * Попытается сохранить проблемный файл в директории для проблемных файлов
     */
    private void tryDumpFile(String filePath) {
        try {
            String fileName = filePath.lastIndexOf("/") > 0 ? filePath.substring(filePath.lastIndexOf("/") + 1) : filePath;
            String pathForFails = System.getProperty("user.dir") + "/failFiles/";
            FileUtils.copyURLToFile(new URL(filePath), new File(pathForFails + fileName));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getServerModuleName() {
        return SERVER_MODULE_NAME;
    }

    public ActionsTransportAuxiliariesDao getTrsAuxDao() {
        return trsAuxDao;
    }

    public void setTrsAuxDao(ActionsTransportAuxiliariesDao trsAuxDao) {
        this.trsAuxDao = trsAuxDao;
    }

    private int getPollInterval() {
        if (ModuleConnection.isDebugMode()) {
            return ModuleConnection.SERVER_POLLING_INTERVAL_DEBUG_SEC;
        }
        try {
            String pi = propertiesManager.getProperty(
                    ModuleConnection.PROPERTIES_MODULE_NAME_SERVER_POLLING,
                    null,
                    "cash.server.poll.interval.loyalty",
                    null
            );
            if (pi == null) {
                return ModuleConnection.SERVER_POLLING_INTERVAL_PRODUCTION_DEFAULT_SEC;
            }
            return Integer.parseInt(pi);
        } catch (Exception ex) {
            log.error("Failed to read poll interval for data type \"{}\"", SERVER_MODULE_NAME, ex);
            return ModuleConnection.SERVER_POLLING_INTERVAL_PRODUCTION_DEFAULT_SEC;
        }
    }
}
