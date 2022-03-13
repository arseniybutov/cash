package ru.crystals.pos.visualization.payments.externalbankterminal.forms;

import ru.crystals.pos.visualization.payments.common.panels.AbstractUnitPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentUnitPanel;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentInfo;
import ru.crystals.pos.visualization.styles.Color;

import java.awt.BorderLayout;

public class ExtBankPayment2UnitPanel extends AbstractUnitPanel<ExtBankTerminalPaymentInfo> {
    private static final long serialVersionUID = 1L;
    private CommonPaymentUnitPanel panel1;
    private CommonPaymentUnitPanel panel2;

    public ExtBankPayment2UnitPanel(CommonPaymentUnitPanel panel1, CommonPaymentUnitPanel panel2) {
        this.panel1 = panel1;
        this.panel2 = panel2;

        this.setBackground(Color.greyBackground);
        this.setLayout(new BorderLayout());
        if (panel1 != null) {
            this.add(panel1, BorderLayout.NORTH);
        }
        if (panel2 != null) {
            this.add(panel2, BorderLayout.SOUTH);
        }

    }

    public void setPaid(ExtBankTerminalPaymentInfo info) {
        if (panel1 != null) {
            panel1.setPaid(info.getLastDigits());
        }
        if (panel2 != null) {
            panel2.setPaid(info.getAuthCode());
        }
    }

    public void setRefund(boolean refund) {
        //
    }
}