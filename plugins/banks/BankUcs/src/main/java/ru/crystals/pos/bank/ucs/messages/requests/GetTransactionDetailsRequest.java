package ru.crystals.pos.bank.ucs.messages.requests;

import org.apache.commons.lang.StringUtils;

public class GetTransactionDetailsRequest extends Request {
    private String uniqueReferenceNumber;

    public GetTransactionDetailsRequest(String uniqueReferenceNumber) {
        super(RequestType.GET_TRANSACTION_DETAILS);
        this.uniqueReferenceNumber = uniqueReferenceNumber;
    }

    @Override
    protected String getDataToString() {
        return StringUtils.leftPad(uniqueReferenceNumber, 12, '0');
    }

    @Override
    protected void setLoggableFields() {
        getLoggerUtil().add("uniqueReferenceNumber", uniqueReferenceNumber);
    }
}
