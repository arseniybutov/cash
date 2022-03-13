package ru.crystals.pos.fiscalprinter.az.airconn.commands;

public class SaveSoftChecksumCommand extends BaseCommand<Object, Object> {
    public SaveSoftChecksumCommand() {
        getRequest().setOperationId("saveSoftChecksum");
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
