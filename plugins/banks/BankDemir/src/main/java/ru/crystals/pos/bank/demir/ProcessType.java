package ru.crystals.pos.bank.demir;

public enum ProcessType {

    SALES("01"),
    END_OF_DAY("91");

    private final String code;

    ProcessType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
