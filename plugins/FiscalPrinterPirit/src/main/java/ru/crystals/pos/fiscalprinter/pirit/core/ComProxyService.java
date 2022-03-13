package ru.crystals.pos.fiscalprinter.pirit.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.datastruct.info.ProxySoftwareInfo;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Сервис для запуска/останов службы ComProxy, а также для получения информации о ее версии
 */
public class ComProxyService {

    private static final Logger log = LoggerFactory.getLogger(ComProxyService.class);

    private static final String COM_PROXY_JAR_NAME = "ComProxy.jar";
    private static final String SW_INFO_NAME = "ComProxy";

    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";

    private static final String LINUX_INSTALL_PATH = "/usr/local/comproxy";
    private String serviceName = SystemUtils.IS_OS_WINDOWS ? "ComProxy" : "comproxy.sh";
    private String servicePath = "/home/tc/storage/comproxy";
    private boolean requireComProxyStart = true;
    private boolean addHookShutdownComProxy = true;
    private long runServiceTimeout = 5000;

    private Path actualJarPath;


    public void start(boolean ofdDevice) throws FiscalPrinterException {
        if (!ofdDevice) {
            try {
                controlService(STOP_CMD);
            } catch (FiscalPrinterException e) {
                log.debug("Unable to stop ComProxy for non-OFD device: {}", e.getMessage());
            }
            return;
        }
        if (!requireComProxyStart) {
            return;
        }
        controlService(START_CMD);
        //Останов при перезагрузке кассы
        if (addHookShutdownComProxy) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    controlService(STOP_CMD);
                } catch (FiscalPrinterException e) {
                    log.error(e.getMessage());
                }
            }));
            addHookShutdownComProxy = false;
        }
        requireComProxyStart = false;
    }

    /**
     * Запуск и останов сервиса ComProxy для Пиритов с фискальным накопителем
     */
    private void controlService(final String command) throws FiscalPrinterException {
        log.debug("ComProxy service: {} ", command);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> task = executor.submit(() -> {
                String[] commandForRun;
                if (SystemUtils.IS_OS_WINDOWS) {
                    commandForRun = new String[]{"sc", command, serviceName};
                } else {
                    commandForRun = new String[]{"sudo", servicePath + "/" + serviceName, command};
                }
                return Runtime.getRuntime()
                        .exec(commandForRun)
                        .waitFor();
            });
            if (getRunServiceTimeout() > 0) {
                task.get(getRunServiceTimeout(), TimeUnit.MILLISECONDS);
            } else {
                task.get();
            }
            log.debug("ComProxy service: {} (complete)", command);
        } catch (TimeoutException te) {
            log.error("Error on start ComProxy service", te);
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("SERVICE_TIMEOUT"));
        } catch (Exception e) {
            log.error("Error on start ComProxy service", e);
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("SERVICE_ERROR"));
        }
    }

    public void stopService() throws FiscalPrinterException {
        requireComProxyStart = true;
        controlService(STOP_CMD);
    }

    public void configure(PiritPluginConfig config) {
        Optional.ofNullable(config.getRunServiceTimeout()).ifPresent(this::setRunServiceTimeout);
        Optional.ofNullable(config.getServiceName()).ifPresent(this::setServiceName);
        Optional.ofNullable(config.getServicePath()).ifPresent(this::setServicePath);
    }

    public void setRunServiceTimeout(long runServiceTimeout) {
        this.runServiceTimeout = runServiceTimeout;
    }

    public long getRunServiceTimeout() {
        return runServiceTimeout;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public ProxySoftwareInfo getInfo() {
        if (actualJarPath == null) {
            actualJarPath = getActualJarPath();
        }
        if (actualJarPath == null || !Files.exists(actualJarPath)) {
            return new ProxySoftwareInfo(SW_INFO_NAME, ProxySoftwareInfo.UNAVAILABLE_VERSION);
        }
        return getInfoFromJar(actualJarPath);
    }

    protected Path getActualJarPath() {
        try {
            String installationPath = getInstallationPath();
            if (installationPath == null) {
                return null;
            }
            final Path comProxyJar = Paths.get(installationPath, COM_PROXY_JAR_NAME);
            log.debug("ComProxy jar path: {}", comProxyJar);
            if (Files.exists(comProxyJar)) {
                return comProxyJar;
            }
        } catch (Exception e) {
            log.debug("Unable to get ComProxy jar path", e);
        }
        return null;
    }

    private String getInstallationPath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return LINUX_INSTALL_PATH;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder();
            // Получение информации о службе, включая путь к exe службы
            builder.command("sc", "qc", serviceName);
            Process process = builder.start();
            process.waitFor(5, TimeUnit.SECONDS);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("cp866")))) {
                String s;
                while ((s = br.readLine()) != null) {
                    if (isBinaryPath(s)) {
                        return extractInstallPath(s);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to get ComProxy installation path", e);
        }
        return null;
    }

    static String extractInstallPath(String scQcResultLine) {
        return StringUtils.strip(StringUtils.substringBetween(scQcResultLine, ": ", "\\ComProxySrv.exe"), "\"");
    }

    static boolean isBinaryPath(String scQcResultLine) {
        return StringUtils.containsIgnoreCase(scQcResultLine, "Имя_двоичного_файла")
                || StringUtils.containsIgnoreCase(scQcResultLine, "BINARY_PATH_NAME");
    }

    /**
     * Достает версию из манифеста ComProxy.jar
     */
    static ProxySoftwareInfo getInfoFromJar(Path comProxyJarPath) {
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(comProxyJarPath))) {
            final ProxySoftwareInfo info = Optional.ofNullable(jarStream.getManifest())
                    .map(Manifest::getMainAttributes)
                    .map(attrs -> attrs.getValue("ComProxyVersion"))
                    .map(version -> new ProxySoftwareInfo(SW_INFO_NAME, version))
                    .orElseGet(() -> new ProxySoftwareInfo(SW_INFO_NAME, ProxySoftwareInfo.INVALID_VERSION));
            log.info("ComProxy version: {}", info.getVersion());
            return info;
        } catch (Exception e) {
            log.debug("Unable to read ComProxy version from {}: {}", comProxyJarPath, e.getMessage());
        }
        return new ProxySoftwareInfo(SW_INFO_NAME, ProxySoftwareInfo.UNAVAILABLE_VERSION);
    }
}
