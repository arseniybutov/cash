package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class Confirmation extends Result {
    // 55h
    public Confirmation(Response response) {
        super(response);
    }
}
