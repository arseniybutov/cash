package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public class CashInOutSection extends ReportDataSection {

    private String namePayment; // 45  Наименование оплаты
    private long sum;           // 8   Сумма
    protected static final int SIZE = 55;

    public CashInOutSection(byte[] data) {
        super(data);
        this.namePayment = getStringBytes(data, 1, 45);
        this.sum = getLong8Bytes(data, 46);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public String getNamePayment() {
        return namePayment;
    }

    public long getSum() {
        return sum;
    }
}
