package ru.crystals.pos.fiscalprinter.retailforce;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RandomSource {

    public String getUuid() {
        return UUID.randomUUID().toString();
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
