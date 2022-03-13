package ru.crystals.pos.bank.ucs.messages.requests;


public class FinalizeDayTotalsRequest extends Request {

    public FinalizeDayTotalsRequest() {
        super(RequestType.FINALIZE_DAY_TOTALS);
    }

    @Override
    protected String getDataToString() {
        return "";
    }
}
