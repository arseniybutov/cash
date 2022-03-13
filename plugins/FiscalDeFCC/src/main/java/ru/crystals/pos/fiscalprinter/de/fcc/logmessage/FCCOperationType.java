package ru.crystals.pos.fiscalprinter.de.fcc.logmessage;

/**
 *
 * @author dalex
 *
 * Beleg log operation types
 */
public enum FCCOperationType {

    TRANSACTION_OPERATION_TYPE_START_TRANSACTION("StartTransaction"),
    TRANSACTION_OPERATION_TYPE_UPDATE_TRANSACTION("UpdateTransaction"),
    TRANSACTION_OPERATION_TYPE_FINISH_TRANSACTION("FinishTransaction"),;

    private final String exportName;

    private FCCOperationType(String operationTypeExportName) {
        this.exportName = operationTypeExportName;
    }

    public String getExportName() {
        return this.exportName;
    }
}
