package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;

public class PiritAgentRB extends PiritAgent {

    public PiritAgentRB(PiritConnector pc) {
        super(pc);
    }

    @Override
    public String getCashierName(Cashier cashier) {
        return StringUtils.left(formatCashierName(cashier), CASHIER_NAME_MAX_LENGTH);
    }

    private String formatCashierName(Cashier cashier) {
        return StringUtils.leftPad(String.valueOf(cashier.getTabNumLong()), 2, "0") + cashier.getNullSafeName();
    }
}
