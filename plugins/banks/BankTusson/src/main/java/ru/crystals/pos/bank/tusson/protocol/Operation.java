package ru.crystals.pos.bank.tusson.protocol;

import ru.crystals.pos.bank.tusson.printer.DocumentType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Описывает банковские операции
 */
public enum Operation {
    SALE((byte) 0x00),
    REVERSAL((byte) 0x04),
    REFUND((byte) 0x01),
    DAILY_LOG((byte) 0xF0),
    UNKNOWN((byte) 0x50);
    private byte operationCode;
    /**
     * Типы документов, которые мы ожидаем при успешном завершении операции
     */
    public static final Collection<DocumentType> SUCCESS_DOCUMENTS = Arrays.asList(DocumentType.CLIENT_SLIP, DocumentType.BANK_SLIP);
    /**
     * Типы документов, которые мы ожидаем если операция провалилась
     */
    public static final Collection<DocumentType> FAILED_DOCUMENTS = Collections.singletonList(DocumentType.CLIENT_SLIP);

    public static final Collection<DocumentType> DAILY_LOG_DOCUMENTS = Collections.singletonList(DocumentType.SHIFT_REPORT);

    Operation(byte operationCode) {
        this.operationCode = operationCode;
    }

    public static Operation getOperationByCode(byte operationCode) {
        for (Operation operation : values()) {
            if (operation.operationCode == operationCode) {
                return operation;
            }
        }
        return UNKNOWN;
    }

    public byte getOperationCode() {
        return operationCode;
    }
}
