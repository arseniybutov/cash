package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.FiscalDriveAPI;

import java.time.format.DateTimeFormatter;

public class SentReceiptTimeDeserializer extends LocalDateTimeDeserializer {

    public SentReceiptTimeDeserializer() {
        super(DateTimeFormatter.ofPattern(FiscalDriveAPI.TIMESTAMP_FORMAT));
    }
}
