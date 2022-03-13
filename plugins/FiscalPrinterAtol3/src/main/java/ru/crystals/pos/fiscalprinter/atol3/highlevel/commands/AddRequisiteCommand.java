package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class AddRequisiteCommand extends Command {
    private static final int CODE = 0xE8;

    private final int flag = 1;
    private final int totalBlockNumber;
    private final int blockNumber;
    private final int tag;
    private final String data;

    public AddRequisiteCommand(int totalBlockNumber, int blockNumber, int tag, String data) {
        super(CODE);
        this.totalBlockNumber = totalBlockNumber;
        this.blockNumber = blockNumber;
        this.tag = tag;
        this.data = data;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flag);
        stream.write(totalBlockNumber);
        stream.write(blockNumber);
        stream.write(tag & 0xFF);
        stream.write(tag >>> 8);
        byte[] buf = encode(data);
        stream.write(buf.length & 0xFF);
        stream.write(buf.length >>> 8);
        stream.write(buf);
    }
}
