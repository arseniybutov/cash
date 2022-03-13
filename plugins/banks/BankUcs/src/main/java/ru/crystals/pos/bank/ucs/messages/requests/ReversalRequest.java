package ru.crystals.pos.bank.ucs.messages.requests;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.datastruct.ReversalData;

public class ReversalRequest extends Request {
    private long amount;
    private String uniqueReferenceNumber;

    public ReversalRequest(ReversalData reversalData) {
        super(RequestType.REVERSAL);
        this.amount = reversalData.getAmount();
        this.uniqueReferenceNumber = reversalData.getRefNumber();
    }

    @Override
    protected String getDataToString() {
        return uniqueReferenceNumber + StringUtils.leftPad(String.valueOf(amount), 12, '0');
    }

    @Override
    protected void setLoggableFields() {
        getLoggerUtil().add("amount", amount);
        getLoggerUtil().add("uniqueReferenceNumber", uniqueReferenceNumber);
    }

}
