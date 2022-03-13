package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import java.util.Arrays;

public class CloseSection extends ReportDataSection {
    protected static final int SIZE = 17;
    private long sum;           // 8  Сумма
    private DateSection date;        // 7  Дата Структура  типа “Data”

    public CloseSection(byte[] data) {
        super(data);
        this.sum = getLong8Bytes(data, 1);
        date = new DateSection(Arrays.copyOfRange(data, 9, 9 + DateSection.SIZE));
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public long getSum() {
        return sum;
    }

    public DateSection getDate() {
        return date;
    }
}
