package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;

import java.util.Map;

public class StatusCommand extends SimpleCommand {

    private final int resultSize;
    private final Map<ByteSequence, StatusFP.Status> statusMapping;

    public StatusCommand(ByteSequence commandDefinition, Map<ByteSequence, StatusFP.Status> statusMap) {
        super(commandDefinition.getCommand());

        resultSize = statusMap.keySet().stream().map(c -> c.getCommand().length).findFirst().orElse(1);

        statusMapping = statusMap;
    }

    public int getResponseLength() {
        return resultSize;
    }

    public StatusFP.Status parseResult(byte[] result) {
        for (Map.Entry<ByteSequence, StatusFP.Status> statusEntry : statusMapping.entrySet()) {
            if (statusEntry.getKey().matchesToResult(result)) {
                return statusEntry.getValue();
            }
        }
        return StatusFP.Status.NORMAL;
    }
}
