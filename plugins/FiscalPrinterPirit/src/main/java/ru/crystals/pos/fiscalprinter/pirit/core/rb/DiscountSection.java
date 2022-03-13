package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public class DiscountSection extends ReportDataSection {

    private int type;              // 1   Признак скидки/наценки на позицию
    private int typeFor;           // 1   Признак абсолютной скидки/наценки
    private int correctionFlag;    // 1   Признак коррекции
    private String nameDiscount;   // 39  Наименование
    private long percent;          // 8   Процент
    private long sum;              // 8   Сумма
    protected static final int SIZE = 60;

    public DiscountSection(byte[] data) {
        super(data);
        this.type = getByte(data[1]);
        this.typeFor = getByte(data[2]);
        this.correctionFlag = getByte(data[3]);
        this.nameDiscount = getStringBytes(data, 4, 39);
        this.percent = getLong8Bytes(data, 43);
        this.sum = getLong8Bytes(data, 51);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public int getType() {
        return type;
    }

    public int getTypeFor() {
        return typeFor;
    }

    public int getCorrectionFlag() {
        return correctionFlag;
    }

    public String getNameDiscount() {
        return nameDiscount;
    }

    public long getPercent() {
        return percent;
    }

    public long getSum() {
        return sum;
    }
}
