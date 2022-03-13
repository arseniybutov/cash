package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import java.util.Map;

public class DrawerStatusCommand extends SimpleCommand {

    private final boolean definedStatus;
    private final ByteSequence expectedValue;

    public DrawerStatusCommand(ByteSequence commandDefinition, Map<Boolean, ByteSequence> statusMap) {
        super(commandDefinition.getCommand());

        final Map.Entry<Boolean, ByteSequence> status = statusMap.entrySet().iterator().next();
        expectedValue = status.getValue();
        definedStatus = status.getKey();
    }

    public int getResponseLength() {
        return expectedValue.getCommand().length;
    }

    public Boolean parseResult(byte[] result) {
        if (expectedValue.matchesToResult(result)) {
            return definedStatus;
        }
        return !definedStatus;
    }
}
