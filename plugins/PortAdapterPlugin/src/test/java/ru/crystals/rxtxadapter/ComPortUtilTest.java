package ru.crystals.rxtxadapter;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ComPortUtilTest {

    private static final String S0 = "/dev/ttyS0";
    private static final String S1 = "/dev/ttyS1";
    private static final String S2 = "/dev/ttyS2";
    private static final String S3 = "/dev/ttyS3";
    private static final String S4 = "/dev/ttyS4";
    private static final String S5 = "/dev/ttyS5";
    private static final String COM1 = "COM1";
    private static final String COM2 = "COM2";
    private static final String COM3 = "COM3";
    private static final String COM4 = "COM4";
    private static final String COM5 = "COM5";
    private static final String COM6 = "COM6";

    /**
     * Порты здорового человека: COM(x) = ttyS(x-1)
     */
    private static final Map<String, String> NORMAL_PORTS = new LinkedHashMap<>();
    /**
     * Порты курильщика: COM(x) = ttyS(x-1) только для x [0, 1]
     */
    private static final Map<String, String> ABNORMAL_PORTS = new LinkedHashMap<>();

    static {
        NORMAL_PORTS.put(COM1, S0);
        NORMAL_PORTS.put(COM2, S1);
        NORMAL_PORTS.put(COM3, S2);
        NORMAL_PORTS.put(COM4, S3);
        NORMAL_PORTS.put(COM5, S4);
        NORMAL_PORTS.put(COM6, S5);

        ABNORMAL_PORTS.put(COM1, S0);
        ABNORMAL_PORTS.put(COM2, S1);
        ABNORMAL_PORTS.put(COM3, S4);
        ABNORMAL_PORTS.put(COM4, S5);
    }

    @Test
    public void normalPortsMapping() {
        assertPortsEquals("Порты здорового человека, все порты свободны", NORMAL_PORTS, Arrays.asList(S0, S1, S2, S3, S4, S5));
        assertPortsEquals("Порты здорового человека, ttyS0 занят", NORMAL_PORTS, Arrays.asList(S1, S2, S3, S4, S5));
        assertPortsEquals("Порты здорового человека, ttyS1 занят", NORMAL_PORTS, Arrays.asList(S0, S2, S3, S4, S5));
        assertPortsEquals("Порты здорового человека, ttyS0 и ttyS1 заняты", NORMAL_PORTS, Arrays.asList(S2, S3, S4, S5));
    }
    @Test
    public void abnormalPortMapping() {
        assertPortsEquals("Порты курильщика, все порты свободны", ABNORMAL_PORTS, Arrays.asList(S0, S1, S4, S5));
        assertPortsEquals("Порты курильщика, ttyS0 занят", ABNORMAL_PORTS, Arrays.asList(S0, S4, S5));
        assertPortsEquals("Порты курильщика, ttyS1 занят", ABNORMAL_PORTS, Arrays.asList(S1, S4, S5));
        assertPortsEquals("Порты курильщика, ttyS0 и ttyS1 заняты", ABNORMAL_PORTS, Arrays.asList(S4, S5));
    }

    private void assertPortsEquals(String message, Map<String, String> expectedByConfiguredPort, List<String> availablePorts) {
        expectedByConfiguredPort.forEach((configuredPort, expectedPort) -> {
            assertPortEquals(message, configuredPort, expectedPort, availablePorts);
        });
    }

    private void assertPortEquals(String message, String configuredPort, String expected, List<String> availablePorts) {
        final String errorMessage = String.format("For given available ports (%s) configured port %s should be mapped as %s (%s)",
                availablePorts, configuredPort, expected, message);
        assertEquals(errorMessage, expected, ComPortUtil.getRealSystemPortName(configuredPort, availablePorts));
    }
}