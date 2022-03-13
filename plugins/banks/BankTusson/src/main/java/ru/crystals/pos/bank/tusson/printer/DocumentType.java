package ru.crystals.pos.bank.tusson.printer;

public enum DocumentType {
    UNDEFINED(0x00),
    READY_CHECKING(0x30),
    SERVICE(0x31),
    BANK_SLIP(0x32),
    CLIENT_SLIP(0x33),
    SHIFT_REPORT(0x34),
    CHEQUE_REGISTRY(0x35),
    CASHIER_NOTIFY(0x36);

    private int documentTypeValue;

    private DocumentType(int documentTypeValue) {
        this.documentTypeValue = documentTypeValue;
    }

    public static DocumentType getTypeByCode(Number code) {
        int codeInt = code.intValue();
        for (DocumentType documentType : values()) {
            if (documentType.documentTypeValue == codeInt) {
                return documentType;
            }
        }
        return UNDEFINED;
    }
}
