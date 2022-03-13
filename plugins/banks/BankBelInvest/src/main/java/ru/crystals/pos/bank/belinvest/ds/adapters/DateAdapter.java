package ru.crystals.pos.bank.belinvest.ds.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Created by Tatarinov Eduard on 25.11.16.
 */
public class DateAdapter extends XmlAdapter<String, LocalDateTime> {

    private final DateTimeFormatter secondaryFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Override
    public LocalDateTime unmarshal(String v) {
        if (v == null) {
            return null;
        }
        try {
            // по умолчанию парсим дату в формате ISO_LOCAL_DATE_TIME (2011-12-03T10:15:30)
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            // если не смогли, то второй вариант
            return LocalDateTime.parse(v, secondaryFormatter);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String marshal(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
