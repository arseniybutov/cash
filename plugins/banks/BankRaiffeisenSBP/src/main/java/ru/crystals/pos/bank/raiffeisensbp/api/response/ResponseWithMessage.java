package ru.crystals.pos.bank.raiffeisensbp.api.response;

public interface ResponseWithMessage {

    String getMessage();

    ResponseStatusCode getCode();
}
