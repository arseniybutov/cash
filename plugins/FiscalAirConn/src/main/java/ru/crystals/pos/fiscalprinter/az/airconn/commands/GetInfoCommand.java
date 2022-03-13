package ru.crystals.pos.fiscalprinter.az.airconn.commands;

import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.InfoData;

public class GetInfoCommand extends BaseCommand<Object, InfoData> {

    public GetInfoCommand() {
        getRequest().setOperationId("getInfo");
    }

    public Class<InfoData> getResponseDataClass() {
        return InfoData.class;
    }
}
