package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.junit.Test;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author dalex
 */
public class CommandStackTest {

    class ThreadIdSetter {

        private long threadId = 0;

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

    }

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
        sut.addRequestPacket(new PiritRequestPacket(1, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(2, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(3, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(4, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(20, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(21, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(22, new byte[0]));
        sut.addRequestPacket(new PiritRequestPacket(48, new byte[0]));

        //then
        assertEquals("Stack size", 5, sut.getStackSize());

        List<PiritRequestPacket> testPacks = new ArrayList<>();
        testPacks.add(new PiritRequestPacket(4, new byte[0]));
        testPacks.add(new PiritRequestPacket(20, new byte[0]));
        testPacks.add(new PiritRequestPacket(21, new byte[0]));
        testPacks.add(new PiritRequestPacket(22, new byte[0]));
        testPacks.add(new PiritRequestPacket(48, new byte[0]));
        assertEquals(testPacks, sut.getStackBody());
    }

    @Test
    public void shouldRemoveRightRespondedExtendedCommand() throws FiscalPrinterException {
        //given
        final ThreadIdSetter threadIdSetter = new ThreadIdSetter();
        CommandStack sut = new CommandStack(5) {
            @Override
            protected long getCurrentThreadID() {
                return threadIdSetter.getThreadId();
            }
        };
        final PacketId packetId = new PacketId(1, ExtendedCommand.GET_INFO_INN);

        //when
        sut.addRequestPacket(new PiritRequestPacket(3, ExtendedCommand.GET_INFO_FW_ID, new DataPacket()));
        sut.addRequestPacket(new PiritRequestPacket(1, ExtendedCommand.GET_INFO_INN, new DataPacket()));
        sut.addRequestPacket(new PiritRequestPacket(2, ExtendedCommand.GET_INFO_MODEL_ID, new DataPacket()));
        final DataPacket data = new DataPacket();
        data.putIntValue(ExtendedCommand.GET_INFO_INN.getSubCmd());
        data.putStringValue("12345678");
        PiritResponsePacket resp = new PiritResponsePacket(new PacketId(1, ExtendedCommand.GET_INFO_INN), data);
        resp = sut.getResponsePacket(resp, packetId);

        //then
        assertNotNull(resp);
        assertEquals(packetId, resp.getPacketId());
        assertEquals("12345678", resp.getData().getStringValue(1));
    }

    @Test
    public void shouldRemoveRightRespondedCommand() throws FiscalPrinterException {
        //given
        final ThreadIdSetter threadIdSetter = new ThreadIdSetter();
        CommandStack sut = new CommandStack(5) {
            @Override
            protected long getCurrentThreadID() {
                return threadIdSetter.getThreadId();
            }
        };
        final PacketId packetId = new PacketId(1, PiritCommand.ADD_DISCOUNT);

        //when
        sut.addRequestPacket(new PiritRequestPacket(1, PiritCommand.ADD_DISCOUNT, "TEST"));
        sut.addRequestPacket(new PiritRequestPacket(2, PiritCommand.ADD_DISCOUNT, "TEST"));
        PiritResponsePacket resp = new PiritResponsePacket(1, PiritCommand.ADD_DISCOUNT, new byte[0]);
        resp = sut.getResponsePacket(resp, packetId);

        //then
        assertNotNull(resp);
        assertEquals(packetId, resp.getPacketId());
    }

    @Test
    public void shouldRightRespondedCommandByThreadId() throws FiscalPrinterException {
        //given
        final ThreadIdSetter threadIdSetter = new ThreadIdSetter();
        CommandStack sut = new CommandStack(5) {
            @Override
            protected long getCurrentThreadID() {
                return threadIdSetter.getThreadId();
            }
        };
        final PacketId packetId = new PacketId(1, PiritCommand.ADD_DISCOUNT);

        //when
        PiritRequestPacket testPacket = new PiritRequestPacket(1, PiritCommand.ADD_DISCOUNT, "TEST");
        threadIdSetter.setThreadId(20);
        sut.addRequestPacket(testPacket);
        threadIdSetter.setThreadId(-1);
        sut.addRequestPacket(new PiritRequestPacket(2, PiritCommand.ADD_DISCOUNT, "TEST"));
        PiritResponsePacket resp = new PiritResponsePacket(1, PiritCommand.ADD_DISCOUNT, new byte[0]);
        resp = sut.getResponsePacket(resp, new PacketId(3, PiritCommand.ADD_DISCOUNT));

        //then
        assertNull(resp);

        resp = new PiritResponsePacket(1, PiritCommand.ADD_DISCOUNT, new byte[0]);
        threadIdSetter.setThreadId(20);
        resp = sut.getResponsePacket(resp, packetId);

        assertEquals(packetId, resp.getPacketId());
    }

    @Test(expected = FiscalPrinterCommunicationException.class)
    public void shouldRightErrorCommandByThreadId() throws FiscalPrinterException {
        //given
        final ThreadIdSetter threadIdSetter = new ThreadIdSetter();
        CommandStack sut = new CommandStack(5) {
            @Override
            protected long getCurrentThreadID() {
                return threadIdSetter.getThreadId();
            }
        };
        final PacketId packetId = new PacketId(1, PiritCommand.ADD_DISCOUNT);

        //when
        PiritRequestPacket testPacket = new PiritRequestPacket((byte) 1, PiritCommand.ADD_DISCOUNT, "TEST");
        threadIdSetter.setThreadId(20);
        sut.addRequestPacket(testPacket);
        threadIdSetter.setThreadId(-1);
        sut.addRequestPacket(new PiritRequestPacket((byte) 2, PiritCommand.ADD_DISCOUNT, "TEST"));
        PiritResponsePacket resp = new PiritResponsePacket(1, PiritCommand.ADD_DISCOUNT, new byte[0]);
        resp.setErrorCode(10);
        resp = sut.getResponsePacket(resp, packetId);

        //then
        assertNull(resp);

        resp = new PiritResponsePacket(1, PiritCommand.ADD_DISCOUNT, new byte[0]);
        threadIdSetter.setThreadId(20);

        sut.getResponsePacketBeforeRead(packetId);

    }

    @Test
    public void testDataPacket() throws Exception {
        byte[] testPacket = new byte[]{0x02, 0x32, 0x30, 0x30, 0x30, 0x30, 0x30, 0x1c, 0x34, 0x1c, 0x30, 0x1c, 0x03, 0x31, 0x39};
        PiritResponsePacket resp = new PiritResponsePacket(testPacket);
        DataPacket dataPacket = resp.getData();

        long p1 = dataPacket.getLongValue(0);
        long p2 = dataPacket.getLongValue(1);
        long p3 = dataPacket.getLongValue(2);

        assertEquals(3, dataPacket.getCountValue());

        assertEquals(0L, p1);
        assertEquals(4L, p2);
        assertEquals(0L, p3);
    }

}
