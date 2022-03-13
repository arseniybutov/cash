package ru.crystals.pos.fiscalprinter.az.airconn.commands;

public class VerifySoftChecksumCommand extends BaseCommand<Object, Object> {
    public VerifySoftChecksumCommand() {
        getRequest().setOperationId("verifySoftChecksum");
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
