package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * [ Header, Len, Cmd, Data, Crc ]
 * <p>
 * Ответное сообщение по протоколу 100 состоит из: <p>
 * 1. Header : 0xF8, 0x55, 0xCE - первые три байта любого ответа. <br>
 * 2. Length : два байта в порядке little-endian, считается из длины Command + Data. <br>
 * 3. Command : байт, уникальный для каждой команды. <br>
 * 4. Data : собственно, данные, поставляемые командой. <br>
 * 5. Crc : два байта в порядке big-endian. <br>
 * <p>
 * Если в блоке Data параметр типа byte или int, то его длина заранее известна (и порядок вероятнее всего LE).
 * Если текст в виде char[], то каждый параметр оканчивается разделителем 0x0D 0x0A (\r\n)
 */
public abstract class Response {

    private final byte NACK = (byte) 0xF0;
    private final byte[] header = new byte[]{(byte) 0xF8, 0x55, (byte) 0xCE};
    private final byte command;
    protected final byte[] answer;
    protected final int headerIndex;
    protected final int lengthIndex;
    protected final int cmdIndex;
    protected final int dataIndex;
    protected final int crcIndex;

    protected Response(int cmd, byte[] answer) throws Protocol100Exception {
        this.command = (byte) cmd;
        this.answer = Arrays.copyOf(answer, answer.length);
        this.headerIndex = 0;
        this.lengthIndex = 3;
        this.cmdIndex = 5;
        this.dataIndex = 6;
        this.crcIndex = answer.length - 2;
        check(answer);
    }

    protected void check(byte[] answer) throws Protocol100Exception {
        checkHeader(answer);
        byte cmd = answer[cmdIndex];
        checkNack(cmd);
        checkError(cmd);
        checkCommand(cmd);
    }

    protected void checkHeader(byte[] answer) throws Protocol100Exception {
        byte[] received = Arrays.copyOfRange(answer, headerIndex, lengthIndex);
        if (!Arrays.equals(this.header, received)) {
            throw new Protocol100Exception("Header is not correct: " + Arrays.toString(received));
        }
    }

    protected void checkCommand(byte received) throws Protocol100Exception {
        if (this.command != received) {
            throw new Protocol100Exception("Unexpected command in answer: " + Long.toHexString(received));
        }
    }

    protected void checkNack(byte received) throws Protocol100Exception {
        if (received == NACK) {
            throw new Protocol100Exception("Received NACK from device");
        }
    }

    protected void checkError(byte received) throws Protocol100Exception {
        if (received == Error.getCommand()) {
            throw new Protocol100Exception(Error.of(answer[dataIndex]));
        }
    }

    protected int parseInt(int from) {
        return ByteBuffer.wrap(Arrays.copyOfRange(answer, from, from + 4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    protected int parseShort(int from) {
        return ByteBuffer.wrap(Arrays.copyOfRange(answer, from, from + 2))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    protected boolean parseBoolean(int from) {
        return answer[from] != 0;
    }

    protected String parseString(int from) {
        return new String(Arrays.copyOfRange(answer, from, crcIndex))
                .split("\r\n")[0];
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", "
                + "cmd=0x" + Long.toHexString(command);
    }
}
