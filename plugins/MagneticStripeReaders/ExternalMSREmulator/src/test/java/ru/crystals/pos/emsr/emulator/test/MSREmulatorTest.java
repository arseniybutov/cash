package ru.crystals.pos.emsr.emulator.test;


import org.junit.Test;
import ru.crystals.pos.emsr.emulator.MSREmulatorClient;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MSREmulatorTest {

    @Test
    public void testParseArguments1() throws Exception {
        String[] args = {
                "--ip", "127.0.0.1",
                "--port", "6502",
                "--track", "1", "2", "3", "4",
                "--gui"
        };
        Map<String, String> result = MSREmulatorClient.parseArguments(args);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_IP));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_PORT));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_GUI));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "1"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "2"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "3"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "4"));
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_IP), "127.0.0.1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_PORT), "6502");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_GUI), "true");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "1"), "1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "2"), "2");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "3"), "3");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "4"), "4");
    }

    @Test
    public void testParseArguments2() throws Exception {
        String[] args = {
                "--ip", "127.0.0.1",
                "--port", "6502",
                "--track", "1", "2"
        };
        Map<String, String> result = MSREmulatorClient.parseArguments(args);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_IP));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_PORT));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "1"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "2"));
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_IP), "127.0.0.1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_PORT), "6502");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "1"), "1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "2"), "2");
    }

    @Test
    public void testParseArguments3() throws Exception {
        String[] args = {
                "--ip", "127.0.0.1",
                "--track", "1", "", "", "4",
                "--port", "6502",
                "--gui"
        };
        Map<String, String> result = MSREmulatorClient.parseArguments(args);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_IP));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_PORT));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_GUI));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "1"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "2"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "3"));
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_TRACK + "4"));
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_IP), "127.0.0.1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_PORT), "6502");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_GUI), "true");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "1"), "1");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "2"), "");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "3"), "");
        assertEquals(result.get(MSREmulatorClient.PARAM_NAME_TRACK + "4"), "4");
    }

    @Test
    public void testParseArguments4() throws Exception {
        String[] args = {
                "127.0.0.1",
                "--track", "1", "4",
                "--port", "6502",
                "--gui"
        };
        Map<String, String> result = MSREmulatorClient.parseArguments(args);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseArgument5() throws Exception {
        String[] args = {
                "--ip", "172.16.2.16", "--track", "", "433", "--gui"
        };
        Map<String, String> result = MSREmulatorClient.parseArguments(args);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertTrue(result.containsKey(MSREmulatorClient.PARAM_NAME_GUI));
    }
}
