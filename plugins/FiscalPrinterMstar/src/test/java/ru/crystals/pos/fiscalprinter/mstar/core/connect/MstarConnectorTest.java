package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MstarConnectorTest {

    private static final int STX = 0x02;

    /**
     * Метод  MstarConnector.readPacketByCommand(int cmd) не должен зависать, если в ответе не пришёл ETX
     */
    @Test
    public void shouldReadPacketWOETC() throws IOException, IllegalArgumentException, IllegalAccessException, InterruptedException {
        final MstarConnector sut = new MstarConnector();
        long read_time_out = 3000;

        final FakeStreamReader fake = new FakeStreamReader(new int[]{STX, 0x30, 0x31, 0x32, 0x30, 0x31, 0x32, 0x30, 0x31, 0x32, 0, 0x32, 0x32});
        for (Field f : BaseConnector.class.getDeclaredFields()) {
            if (f.getName().equals("isr")) {
                f.setAccessible(true);
                f.set(sut, fake);
            } else if (f.getName().equals("is")) {
                f.setAccessible(true);
                InputStream is = mock(InputStream.class);
                when(is.available()).thenReturn(0);
                f.set(sut, is);
            } else if (f.getName().equals("os")) {
                f.setAccessible(true);
                OutputStream os = mock(OutputStream.class);
                doAnswer((Answer<Object>) invocation -> {
                    Object[] args = invocation.getArguments();
                    if (args != null && args.length > 0 && args[0] instanceof Integer && (Integer) args[0] == 5) {
                        fake.newPacket(new int[]{0x06});
                    }
                    return null;
                }).when(os).write(Mockito.anyByte());
                f.set(sut, os);
            }
        }
        for (Field f : MstarConnector.class.getDeclaredFields()) {
            if (f.getName().equals("READ_TIME_OUT")) {
                f.setAccessible(true);
                read_time_out = 3000 * f.getLong(sut);
            }
        }

        final CountDownLatch cdl = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                sut.readPacketByCommand(0x00, 0x30);
                cdl.countDown();
            } catch (FiscalPrinterException e) {
                cdl.countDown();
                e.printStackTrace();
            }
        });
        t.start();
        cdl.await(read_time_out, TimeUnit.MILLISECONDS);
        assertEquals("Test failed because MstarConnector go to infinite loop !", 0, cdl.getCount());
    }

    class FakeStreamReader extends InputStreamReader {

        private int[] packet;
        private int index = 0;

        public FakeStreamReader(int[] packet) {
            super(mock(InputStream.class));
            this.packet = packet;
        }

        public void newPacket(int[] packet) {
            this.packet = packet;
            index = 0;
        }


        @Override
        public boolean ready() {
            return index < packet.length;
        }

        @Override
        public int read() throws IOException {
            if (index < packet.length) {
                return packet[index++];
            } else {
                throw new IOException("No data in FakeInputStream");
            }
        }

    }
}
