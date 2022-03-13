package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;

public class PiritAgentFN100 extends PiritAgent {

    private static final int CASHIER_NAME_MAX_LENGTH = 64;

    public PiritAgentFN100(PiritConnector pc) {
        super(pc);
    }

    @Override
    public String getCashierName(Cashier cashier) {
        String result = StringUtils.left(cashier.getCashierStringForOFDTag1021(), CASHIER_NAME_MAX_LENGTH);
        if (cashier.getInn() != null) {
            return cashier.getInn().concat("&").concat(result);
        }
        return result;
    }
}
