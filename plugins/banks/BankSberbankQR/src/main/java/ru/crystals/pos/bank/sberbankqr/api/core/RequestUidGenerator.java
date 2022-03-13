package ru.crystals.pos.bank.sberbankqr.api.core;

import java.util.UUID;

public class RequestUidGenerator {

    String generateRqUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
