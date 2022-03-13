package ru.crystals.pos.fiscalprinter.de.fcc.ber;

/**
 *
 * @author dalex
 *
 * ASN.1 serialized object
 */
public class BerTagData {

    /**
     * ASN.1 tag
     */
    private int tag;

    /**
     * ASN.1 object data
     */
    private byte[] data;

    public BerTagData() {
    }

    public BerTagData(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
