package ru.crystals.pos.bank.ucs.messages.requests;

public enum RequestType {

    LOGIN("30"),
    FINALIZE_DAY_TOTALS("21"),
    GET_REPORT("25"),
    SALE("10"),
    CREDIT("14"),
    REVERSAL("1A"),
    GET_TRANSACTION_DETAILS("20");

    private String classAndCode;

    RequestType(String classAndCode) {
        this.classAndCode = classAndCode;
    }

    public String getClassAndCode() {
        return classAndCode;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + classAndCode + ")";
    }
}
