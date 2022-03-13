package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import org.junit.Test;
import ru.crystals.comportemulator.mstar.MstarRequestPacket;
import ru.crystals.comportemulator.mstar.MstarResponsePacket;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandStackTest {

    @Test
    public void shouldRemoveOldCommands() throws FiscalPrinterException {
        //given
        final ThreadIdSetter threadIdSetter = new ThreadIdSetter();
        CommandStack sut = new CommandStack(5) {
            @Override
            protected long getCurrentThreadID() {
                return threadIdSetter.getThreadId();
            }
        };

        //when
        sut.addRequestPacket(new MstarRequestPacket(1, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(2, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(3, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(4, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(20, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(21, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(22, new byte[0]));
        sut.addRequestPacket(new MstarRequestPacket(48, new byte[0]));

        //then
        assertTrue("Stack size: " + sut.getStackSize() + " != 5", sut.getStackSize() == 5);

        List<MstarRequestPacket> testPacks = new ArrayList<>();
        testPacks.add(new MstarRequestPacket(4, new byte[0]));
        testPacks.add(new MstarRequestPacket(20, new byte[0]));
        testPacks.add(new MstarRequestPacket(21, new byte[0]));
        testPacks.add(new MstarRequestPacket(22, new byte[0]));
        testPacks.add(new MstarRequestPacket(48, new byte[0]));
        assertEquals(testPacks, sut.getStackBody());
    }

    @Test
    public void testDataPacket() throws Exception {
        byte[] testPacket = new byte[]{0x02, 0x32, 0x30, 0x30, 0x30, 0x30, 0x30, 0x1c, 0x34, 0x1c, 0x30, 0x1c, 0x03, 0x31, 0x39};
        MstarResponsePacket resp = new MstarResponsePacket(testPacket);
        DataPacket dataPacket = resp.getDataPacket();

        Long p1 = dataPacket.getLongValue(0);
        Long p2 = dataPacket.getLongValue(1);
        Long p3 = dataPacket.getLongValue(2);

        assertTrue(4 == dataPacket.getCountValue());

        assertTrue(0L == p1);
        assertTrue(4L == p2);
        assertTrue(0L == p3);
    }

    class ThreadIdSetter {

        private long threadId = 0;

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

    }

}
