package ru.crystals.pos.visualization.payments.bonussberbank;

import ru.crystals.pos.visualization.admin.components.ReportComponent;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.PaymentInputPanel;
import ru.crystals.pos.visualization.payments.ExtendedPaymentPanel;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.bonussberbank.BonusSberbankPaymentContainer.State;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.FormatHelper;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.math.BigDecimal;

import static ru.crystals.pos.check.BigDecimalConverter.convertMoney;

public class BonusSberbankPaymentComponent extends VisualPanel implements PaymentComponent {

    private static final long serialVersionUID = 1L;

    private ExtendedPaymentPanel paymentPanel;
    private VisualPanel dataExchange;
    private ReportComponent reportComponent;
    private PaymentInputPanel bonusCardAmount = null;
    private JLabel jPaid = new JLabel();

    public BonusSberbankPaymentComponent() {
        initialize();
    }

    private void initialize() {
        paymentPanel = new ExtendedPaymentPanel();
        paymentPanel.setPaymentType(ResBundlePaymentBonusSberbank.getString("SBERBANK_PAYMENT"));
        bonusCardAmount = new PaymentInputPanel();
        dataExchange = new VisualPanel();
        dataExchange.setPreferredSize(Size.middlePanel);

        setPreferredSize(Size.middlePanel);
        add(paymentPanel);
        add(bonusCardAmount);
        add(dataExchange);

        JPanel enterPayment = getEnterPaymentPanel();
        paymentPanel.add(enterPayment);

        setState(State.DATA_EXCHANGE);
    }

    public void setSumma(Long summa) {
        paymentPanel.setSumma(summa);
    }

    public void setChange(boolean change) {
        if (change) {
            paymentPanel.setPaymentType(ResBundlePaymentBonusSberbank.getString("CHANGE"));
        } else {
            paymentPanel.setPaymentType(ResBundlePaymentBonusSberbank.getString("CARD_PAYMENT"));
        }
    }

    public void setState(State state) {
        switch (state) {
            case ENTER_AMOUNT:
                paymentPanel.setVisible(true);
                dataExchange.setVisible(false);
                bonusCardAmount.setVisible(true);
                validate();
                break;
            case DATA_EXCHANGE:
                paymentPanel.setVisible(false);
                bonusCardAmount.setVisible(false);
                dataExchange.setVisible(true);
                validate();
                dataExchange.validate();
                break;
            default:
                break;
        }
    }

    @Override
    public void setPayment(Long payment) {
        bonusCardAmount.getInputField().setPresetValue(convertMoney(payment));
    }

    @Override
    public void setPaymentType(String paymentType) {
        paymentPanel.setPaymentType(paymentType);
    }

    @Override
    public void setOperation(String operation) {
        paymentPanel.setOperation(operation);
    }

    @Override
    public void setSummaLabel(String label) {
        bonusCardAmount.setLabel(label);
    }

    @Override
    public void setCurrency(String symbol, String name) {
        paymentPanel.setCurrency(symbol, name);
    }

    public VisualPanel getDataExchange() {
        return dataExchange;
    }

    public ReportComponent getReportComponent() {
        return reportComponent;
    }

    private JPanel getEnterPaymentPanel() {
        Style.setPaidStyle(jPaid);
        JLabel jPaidLabel = new JLabel();
        jPaidLabel.setText(ResBundlePaymentBonusSberbank.getString("PAID"));
        Style.setPaidLabelStyle(jPaidLabel);
        JPanel jPerUnitPanel = new JPanel();
        jPerUnitPanel.setLayout(this.flowLayout);
        jPerUnitPanel.setBackground(Color.greyBackground);
        jPerUnitPanel.setPreferredSize(new ScaleDimension(150, 80));
        jPerUnitPanel.add(jPaidLabel, null);
        jPerUnitPanel.add(jPaid, null);

        return jPerUnitPanel;
    }

    public void setReportComponent(ReportComponent reportComponent) {
        this.reportComponent = reportComponent;
        dataExchange.removeAll();
        if (reportComponent != null) {
            reportComponent.setPreferredSize(Size.middlePanel);
            dataExchange.add(reportComponent);
            reportComponent.setVisible(true);
        }
    }

    @Override
    public void setPaid(String paid) {
        paymentPanel.setJLabel1Text(paid);
    }

    @Override
    public void setPaid(Long paid) {
        paymentPanel.setMoney(paid);
        jPaid.setText(FormatHelper.formatSumma(paid / 100.0));
    }

    public InputFieldInterface<BigDecimal> getInputField() {
        return bonusCardAmount.getInputField();
    }

    public void setBonus(double bonus) {
        paymentPanel.setJPanel2(ResBundlePaymentBonusSberbank.getString("BONUS_ON_CARD"), bonus);
    }

    public void setBonusLabelText(String value) {
        paymentPanel.setJLabel2Text(value);
    }

}