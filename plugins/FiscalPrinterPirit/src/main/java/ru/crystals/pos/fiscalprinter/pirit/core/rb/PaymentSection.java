package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public class PaymentSection extends ReportDataSection {

    private String namePayment; // 45  Наименование оплаты
    private String typePayment; // 19  Тип оплаты
    private long sum;           // 8   Сумма
    protected static final int SIZE = 74;

    public PaymentSection(byte[] data) {
        super(data);
        this.namePayment = getStringBytes(data, 1, 45);
        this.typePayment = getStringBytes(data, 46, 19);
        this.sum = getLong8Bytes(data, 65);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public String getNamePayment() {
        return namePayment;
    }

    public String getTypePayment() {
        return typePayment;
    }

    public long getSum() {
        return sum;
    }
}
