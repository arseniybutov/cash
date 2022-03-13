package ru.crystals.rxtxadapter;

import gnu.io.CommPortIdentifier;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComPortUtil {

    private final static Logger LOG = LoggerFactory.getLogger(ComPortUtil.class);
    private static final String S0_PORT = "/dev/ttyS0";
    private static final String S1_PORT = "/dev/ttyS1";

    private static final Path CONFIG_PATH = Paths.get("serial_port.properties");
    private static Properties properties;


    private static List<String> ports = null;

    private static void initPorts() {
        ports = new ArrayList<>();
        Enumeration<?> portIdentifierEnum = CommPortIdentifier.getPortIdentifiers();
        while (portIdentifierEnum.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) portIdentifierEnum.nextElement();
            if (commPortIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL &&
                    (commPortIdentifier.getName().contains("ttyS") || commPortIdentifier.getName().toLowerCase().contains("com"))) {
                ports.add(commPortIdentifier.getName());
            }
        }

        Collections.sort(ports, new Comparator<String>() {
            private final Pattern p = Pattern.compile("\\d+$");

            @Override
            public int compare(String o1, String o2) {
                Matcher m1 = p.matcher(o1);
                Matcher m2 = p.matcher(o2);
                return m1.find() && m2.find() ? Integer.valueOf(m1.group()).compareTo(Integer.valueOf(m2.group())) : o1.compareTo(o2);
            }
        });
    }

    /**
     * Преобразует номер порта (com1, com2, ...)
     * в адрес порта используемый в системе
     * нужно для преобразования в Linux системе
     * <p>
     * Работает только для портов начинающихся с com (регистр не важен)
     * Остальные порты возвращаются в том-же виде что и пришли
     * <p>
     * правило:
     * /dev/ttyS0 (активен)    - com1
     * /dev/ttyS1 (активен)    - com2
     * /dev/ttyS2 (не активен) - не соотвертствует никакому com
     * /dev/ttyS3 (не активен) - не соотвертствует никакому com
     * /dev/ttyS4 (активен)    - com3
     * /dev/ttyS5 (активен)    - com4
     *
     * @param port
     * @return
     */
    public static String getRealSystemPortName(String port) {
        if (port == null || SystemUtils.IS_OS_WINDOWS) {
            return port;
        }
        if (!port.toLowerCase().startsWith("com")) {
            return port;
        }
        if (ports == null) {
            initPorts();
            setProperty("allPorts", String.join(",", ports));
        }
        final String realPortResult = getRealSystemPortName(port, ports);
        setProperty(port, realPortResult);
        LOG.warn("Real system port for {} is {}", port, realPortResult);
        return realPortResult;
    }

    static String getRealSystemPortName(String port, List<String> ports) {
        try {
            final String portNum = port.substring(3, 4);
            final int portIndex = Integer.parseInt(portNum) - 1;
            final ArrayList<String> extendedPorts = new ArrayList<>(ports);
            boolean hasS0 = ports.contains(S0_PORT);
            boolean hasS1 = ports.contains(S1_PORT);
            if (!hasS0) {
                extendedPorts.add(0, S0_PORT);
            }
            if (!hasS1) {
                extendedPorts.add(1, S1_PORT);
            }
            return extendedPorts.get(portIndex);
        } catch (Exception ex) {
            LOG.error("Cannot parse port num {}", port, ex);
            return port;
        }
    }

    private static void setProperty(String key, String value) {
        try {
            initConfig();
            properties.setProperty(key, value);
            writeProperties();
        } catch (Exception e) {
            LOG.error("Error on write properties", e);
        }
    }

    private static void initConfig() {
        if (properties != null) {
            return;
        }
        loadProperties();
    }

    private static void loadProperties() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                if (CONFIG_PATH.getParent() != null) {
                    Files.createDirectories(CONFIG_PATH.getParent());
                }
                Files.createFile(CONFIG_PATH);
            }
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                Properties loaded = new Properties();
                loaded.load(in);
                properties = loaded;
            }
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    private static void writeProperties() {
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, null);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }
}
