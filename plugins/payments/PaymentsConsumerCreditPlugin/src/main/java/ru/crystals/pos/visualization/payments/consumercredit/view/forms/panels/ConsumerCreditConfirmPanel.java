package ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels;

import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.payments.common.panels.AbstractHeaderPanel;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * @author d_alex
 */
public class ConsumerCreditConfirmPanel extends AbstractHeaderPanel {

    private JLabel creditAmount = new JLabel();
    private JLabel bankLabel = new JLabel();
    private JLabel bankProductLabel = new JLabel();
    private JLabel fioLabel = new JLabel();
    private JLabel contractNumLabel = new JLabel();

    private JPanel centerPanel = new JPanel(new GridLayout(5, 2));
    private JPanel southPanel = new JPanel(new BorderLayout(16, 16));

    public ConsumerCreditConfirmPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.greyBackground);
        centerPanel.setBackground(Color.greyBackground);
        southPanel.setBackground(Color.greyBackground);

        southPanel.setPreferredSize(new ScaleDimension(400, 30));
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(southPanel, BorderLayout.NORTH);
        addLabel(new JLabel(ResBundlePaymentConsumerCredit.getString("CREDIT_SUM") + " : "));
        addValueLabel(creditAmount);
        addLabel(new JLabel(ResBundlePaymentConsumerCredit.getString("BANK_NAME") + " : "));
        addValueLabel(bankLabel);
        addLabel(new JLabel(ResBundlePaymentConsumerCredit.getString("BANK_PRODUCT_NAME") + " : "));
        addValueLabel(bankProductLabel);
        addLabel(new JLabel(ResBundlePaymentConsumerCredit.getString("FIO") + " : "));
        addValueLabel(fioLabel);
        addLabel(new JLabel(ResBundlePaymentConsumerCredit.getString("CONTRACT_NUMBER_NAME") + " : "));
        addValueLabel(contractNumLabel);
    }

    private void addLabel(JLabel label) {
        label.setFont(MyriadFont.getItalic(22F));
        label.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        label.setHorizontalAlignment(JLabel.RIGHT);
        centerPanel.add(label);
    }

    private void addValueLabel(JLabel label) {
        label.setFont(MyriadFont.getRegular(22F));
        centerPanel.add(label);
    }

    public void setCreditAmountLabel(String creditAmount) {
        this.creditAmount.setText(creditAmount);
    }

    public void setBankLabel(String bankLabel) {
        this.bankLabel.setText(bankLabel);
    }

    public void setBankProductLabel(String bankProductLabel) {
        this.bankProductLabel.setText(bankProductLabel);
    }

    public void setContractNumLabel(String contractNumLabel) {
        this.contractNumLabel.setText(contractNumLabel);
    }

    public void setFioLabel(String fioLabel) {
        this.fioLabel.setText(fioLabel);
    }

    @Override
    public void setHeaderInfo(PaymentEntity payment) {
        //
    }

    @Override
    public void setRefund(boolean refund) {
        //
    }
}
