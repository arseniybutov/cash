package ru.crystals.pos.bank.ucs.messages.requests;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.datastruct.SaleData;

public class SaleRequest extends Request {

    private long amount;

    public SaleRequest(SaleData saleData) {
        super(RequestType.SALE);
        this.amount = saleData.getAmount();
    }

    @Override
    protected String getDataToString() {
        return StringUtils.leftPad(String.valueOf(amount), 12, '0');
    }

    @Override
    protected void setLoggableFields() {
        getLoggerUtil().add("amount", amount);
    }
}
