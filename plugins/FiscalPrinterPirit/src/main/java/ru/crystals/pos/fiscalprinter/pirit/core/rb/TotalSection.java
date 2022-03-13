package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public class TotalSection extends ReportDataSection {
    private int typeDoc;        // 1  Тип документа
    private long sum;           // 8  Сумма
    protected static final int SIZE = 11;

    public TotalSection(byte[] data) {
        super(data);
        this.typeDoc = getByte(data[1]);
        this.sum = getLong8Bytes(data, 2);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public int getTypeDoc() {
        return typeDoc;
    }

    public long getSum() {
        return sum;
    }
}
