package ru.crystals.pos.customerdisplay.kraftway.cdk3100;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.simple.SerialPortConfiguration;
import ru.crystals.pos.utils.simple.SimplePortAdapter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CustomerDisplayKraftwayCDK3100PluginImplTest {
    @Mock
    private SimplePortAdapter adapter;
    @InjectMocks
    private CustomerDisplayKraftwayCDK3100PluginImpl pluginImpl;
    private byte[] clearText = {0x1B, 0x0C};
    private String test = "тест";


    @Before
    public void setUp() {
        pluginImpl.setEncoding("cp866");
        pluginImpl.setPort("/dev/ttyCSCD0");
        pluginImpl.setBaudRate(9660);
        pluginImpl.setParity(9660);
        pluginImpl.setStopBits(1);
        pluginImpl.setDataBits(8);
    }

    @Test
    public void testOpen() throws Exception {
        pluginImpl.open();

        SerialPortConfiguration expectedConfig = SerialPortConfiguration.builder()
                .port("/dev/ttyCSCD0")
                .baudRate(9660)
                .parity(9660)
                .stopBits(1)
                .dataBits(8)
                .build();
        verify(adapter).setConfiguration(expectedConfig);
        verify(adapter).openPort();
    }

    @Test
    public void testDisplayTextAt() throws PortAdapterException, CustomerDisplayPluginException {
        pluginImpl.setEncoding("cp866");
        pluginImpl.open();

        pluginImpl.displayTextAt(0, 0, test);

        verify(adapter).write(test.getBytes(Charset.forName("cp866")));
    }

    @Test
    public void testClearText() throws PortAdapterException {
        pluginImpl.clearText();
        verify(adapter).write(clearText);
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testGetAdapterWithException() throws Exception {
        doThrow(PortAdapterException.class).when(adapter).openPort();
        pluginImpl.open();
    }

    @Test
    public void testExecuteCommandWithoutEncoding() throws Exception {
        pluginImpl.setEncoding(null);
        pluginImpl.open();
        pluginImpl.executeCommand(test);
        verify(adapter).write(test.getBytes((StandardCharsets.UTF_8)));
    }

    @Test
    public void testExecuteCommandWithEmptyEncoding() throws Exception {
        pluginImpl.setEncoding("");
        pluginImpl.open();
        pluginImpl.executeCommand(test);
        verify(adapter).write(test.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testExecuteCommandWithCP866() throws Exception {
        pluginImpl.setEncoding("cp866");
        pluginImpl.open();
        pluginImpl.executeCommand(test);
        verify(adapter).write(test.getBytes(Charset.forName("cp866")));
    }

    @Test
    public void testExecuteCommandWithIOException() throws Exception {
        doThrow(new PortAdapterException("")).when(adapter).write(anyString().getBytes());
        pluginImpl.executeCommand(test);
    }
}
