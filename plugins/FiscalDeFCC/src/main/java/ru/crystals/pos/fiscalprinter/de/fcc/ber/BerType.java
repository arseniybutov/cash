package ru.crystals.pos.fiscalprinter.de.fcc.ber;

import org.apache.commons.codec.binary.Base64;

import static ru.crystals.pos.fiscalprinter.de.fcc.BelegTools.bytesToLong;

/**
 *
 * @author dalex
 *
 * Base ASN.1 types
 *
 * This class contains "beleg" log message types only
 */
public enum BerType implements BerTag {
    SEQUENCE(0x30, dataTag -> dataTag.getData().length),
    INTEGER(0x02, dataTag -> (int) bytesToLong(dataTag.getData())),
    OCTET_STRING(0x04, dataTag -> {
        return Base64.encodeBase64String(dataTag.getData());
    }),
    OBJECT_IDENTIFIER(0x06, BerTagData::getData),
    PRINTABLE_STRING(dataTag -> {
        return new String(dataTag.getData());
    }),
    UTC_TIME(0x17, dataTag -> {
        return bytesToLong(dataTag.getData());
    }),
    GENERALIZED_TIME(0x18, dataTag -> {
        return bytesToLong(dataTag.getData());
    }),
    UNKNOWN(BerTagData::getData);

    private int tag;
    private BerTag belegTagReader;

    BerType(BerTag belegTagReader) {
        tag = -1;
        this.belegTagReader = belegTagReader;
    }

    BerType(int tag, BerTag belegTagReader) {
        this.tag = tag;
        this.belegTagReader = belegTagReader;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public Object read(BerTagData dataTag) {
        return belegTagReader.read(dataTag);
    }
}
