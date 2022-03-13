package ru.crystals.pos.nfc.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.cash.settings.ModuleConfigDocument;
import ru.crystals.cash.settings.PropertyDocument;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.listeners.XMSRListener;
import ru.crystals.pos.nfc.acr122.ACR122SReader;
import ru.crystals.pos.nfc.acr122.NfcReaderACR122SPlugin;
import ru.crystals.test.MockInjectors;


public class NfcReaderACR122SPluginTest {

    private static final String PROPERTY_NAME_POLL_DELAY_MS = "pollDelayMs";
    private static final String PROPERTY_NAME_RESULT_FORMAT = "resultFormat";

    private static final long TEST_MAX_TIME = 10000L;

    private static final byte[] GUID_1_BYTES = new byte[] {0x66, 0x2A, 0x0C, 0x37};

    private ACR122SReader reader = mock(ACR122SReader.class);

    private NfcReaderACR122SPlugin plugin;

    @Before
    public void before() throws Exception {
        plugin = spy(new NfcReaderACR122SPlugin());
        MockInjectors.injectField(plugin, reader, "reader");
        doNothing().when(reader).open(anyString(), anyInt());
    }


    @Test
    public void testPlugin() throws Exception {
        PropertyDocument.Property delayProperty = mock(PropertyDocument.Property.class);
        doReturn(PROPERTY_NAME_POLL_DELAY_MS).when(delayProperty).getKey();
        doReturn("5").when(delayProperty).getValue();
        PropertyDocument.Property formatProperty = mock(PropertyDocument.Property.class);
        doReturn(PROPERTY_NAME_RESULT_FORMAT).when(formatProperty).getKey();
        doReturn("DECIMAL").when(formatProperty).getValue();

        ModuleConfigDocument.ModuleConfig mconfig = mock(ModuleConfigDocument.ModuleConfig.class);
        PropertyDocument.Property[] props = new PropertyDocument.Property[] {delayProperty, formatProperty};
        doReturn(props).when(mconfig).getPropertyArray();

        Queue<byte[]> bytes = new LinkedList<>(Arrays.asList(GUID_1_BYTES, null));

        doAnswer(invocationOnMock -> bytes.poll()).when(reader).poll();

        AtomicBoolean notInterrupted = new AtomicBoolean(true);

        XMSRListener xmsrListener = mock(XMSRListener.class);
        doAnswer(invocationOnMock -> {
            try {
                String track2 = (String) invocationOnMock.getArguments()[1];
                if ("HEX".equals(formatProperty.getValue())) {
                    assertEquals("662a0c37", track2);
                    notInterrupted.set(false);
                    return true;
                } else {
                    assertEquals("102042012055", track2);
                    doReturn("HEX").when(formatProperty).getValue();
                    plugin.init(mconfig);
                    bytes.add(GUID_1_BYTES);
                    return true;
                }
            } finally {
                return false;
            }
        }).when(xmsrListener).eventMSR(anyString(), anyString(), anyString(), anyString());
        CashEventSource.getInstance().addMSRListener(xmsrListener);
        MockInjectors.injectField(CashEventSource.getInstance(), Thread.currentThread(), "currentThread");

        plugin.init(mconfig);

        plugin.start();

        long startTime = System.currentTimeMillis();
        while (notInterrupted.get() && (System.currentTimeMillis() - startTime < TEST_MAX_TIME)) {
            CashEventSource.getInstance().processEventManually();
        }
        plugin.stop();

        verify(xmsrListener).eventMSR(eq(""), eq("102042012055"), eq(""), eq(""));
        verify(xmsrListener).eventMSR(eq(""), eq("662a0c37"), eq(""), eq(""));
    }

}
