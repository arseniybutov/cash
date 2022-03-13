package ru.crystals.pos.fiscalprinter.de.fcc.ber;

/**
 *
 * @author dalex
 *
 * "Beleg" log ASN.1 types
 * classes defined in "Technical Guideline BSI TR-03151"
 * Used document version Version 1.0.1 20. December 2018
 */
public enum BerLogType implements BerTag {
    OPERATION_TYPE(0x80, BerType.PRINTABLE_STRING),
    CLIENT_ID(0x81, BerType.PRINTABLE_STRING),
    PROCESS_DATA(0x82, BerType.PRINTABLE_STRING),
    PROCESS_TYPE(0x83, BerType.PRINTABLE_STRING),
    ADDITIONAL_EXTERNAL_DATA(0x84, BerType.OCTET_STRING),
    TRANSACTION_NUMBER(0x85, BerType.INTEGER),
    ADDITIONAL_INTERNAL_DATA(0x86, BerType.OCTET_STRING),;

    private int tag;
    private BerType type;

    private BerLogType(int tag, BerType type) {
        this.tag = tag;
        this.type = type;
    }

    public int getTag() {
        return tag;
    }

    public BerType getType() {
        return type;
    }

    @Override
    public Object read(BerTagData dataTag) {
        return type.read(dataTag);
    }
}
