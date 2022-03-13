package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateSection extends AbstractSection {
    public static final int SIZE = 7;
    private int day;   // 1
    private int month; // 1
    private int year;  // 2
    private int hour;  // 1
    private int min;   // 1
    private int sec;   // 1
    private static final String DATE_FORMATTER_PATTERN = "ddMMyyyy hh:mm:ss";
    private static final String STRING_FORMATTER_PATTERN = "%1$02d%2$02d%3$04d %4$02d:%5$02d:%6$02d";


    public DateSection(byte[] data) {
        super(data);
        this.day = getByte(data[0]);
        this.month = getByte(data[1]);
        this.year = getByte(data[3]) * 256 + getByte(data[2]);
        this.hour = getByte(data[4]);
        this.min = getByte(data[5]);
        this.sec = getByte(data[6]);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public Date getDate() {
        try {
            return new SimpleDateFormat(DATE_FORMATTER_PATTERN).parse(String.format(STRING_FORMATTER_PATTERN, day, month, year, hour, min, sec));
        } catch (Exception e) {
            LOG.error("Failed to parse date: day={}, month={}, year={}, hour={}, min={}, sec={}", day, month, year, hour, min, sec, e);
            return new Date();
        }
    }
}
