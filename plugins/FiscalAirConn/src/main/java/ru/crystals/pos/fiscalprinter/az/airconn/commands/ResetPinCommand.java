package ru.crystals.pos.fiscalprinter.az.airconn.commands;

import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.ResetPinParameters;

public class ResetPinCommand extends BaseCommand<ResetPinParameters, Object> {

    public ResetPinCommand(ResetPinParameters resetPinParameters) {
        getRequest().setOperationId("resetPin");
        setParameters(resetPinParameters);
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
