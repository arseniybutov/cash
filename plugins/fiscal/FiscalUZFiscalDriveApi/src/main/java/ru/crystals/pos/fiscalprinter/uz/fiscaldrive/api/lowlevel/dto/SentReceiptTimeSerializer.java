package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.FiscalDriveAPI;

import java.time.format.DateTimeFormatter;

public class SentReceiptTimeSerializer extends LocalDateTimeSerializer {

    public SentReceiptTimeSerializer() {
        super(DateTimeFormatter.ofPattern(FiscalDriveAPI.TIMESTAMP_FORMAT));
    }
}
