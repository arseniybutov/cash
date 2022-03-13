package ru.crystals.pos.scale.massak.protocol100.request;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;
import ru.crystals.util.crccalc.CrcCalcFactory;
import ru.crystals.util.crccalc.CrcCalculator;

public abstract class Request {

    private final byte[] header = new byte[]{(byte) 0xF8, 0x55, (byte) 0xCE};
    private final byte command;
    private final byte[] data;
    private final CrcCalculator calculator;

    protected Request(int cmd) {
        this(cmd, new byte[0]);
    }

    protected Request(int cmd, byte[] data) {
        this.command = (byte) cmd;
        this.data = data;
        this.calculator = CrcCalcFactory.createStandartCrc16Calc(CrcCalcFactory.Crc16.CRC_16_AUG_CCITT);
    }

    public byte[] constructBytes() throws Protocol100Exception {
        try {
            String header = Hex.encodeHexString(this.header);
            String cmd = Hex.encodeHexString(new byte[]{this.command});
            String data = Hex.encodeHexString(this.data);
            String frame = header + calcLength() + cmd + data + calcCrc();
            return Hex.decodeHex(frame.toCharArray());
        } catch (DecoderException e) {
            throw new Protocol100Exception(e.getMessage());
        }
    }

    private String calcLength() {
        String mask = "0000";
        String hex = Long.toHexString(this.data.length + 1);
        if (hex.length() > mask.length()) {
            throw new IllegalArgumentException("data is too big! length should not exceed 2 bytes");
        }
        String withLeadingZeroes = (mask + hex).substring(hex.length());
        return withLeadingZeroes.substring(2) + withLeadingZeroes.substring(0, 2);
    }

    private String calcCrc() {
        byte[] bytes = new byte[this.data.length + 1];
        bytes[0] = this.command;
        System.arraycopy(data, 0, bytes, 1, data.length);
        return Long.toHexString(calculator.calc(bytes)).toUpperCase();
    }

}
