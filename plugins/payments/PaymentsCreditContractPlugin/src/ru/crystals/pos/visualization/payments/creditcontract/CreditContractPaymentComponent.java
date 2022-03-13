package ru.crystals.pos.visualization.payments.creditcontract;

import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.InputPanel;
import ru.crystals.pos.visualization.input.InputPanel.InputStyle;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentPanel;
import ru.crystals.pos.visualization.payments.creditcontract.CreditContractPaymentContainer.CreditContractState;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.utils.FormatHelper;

public class CreditContractPaymentComponent extends VisualPanel implements
        PaymentComponent {

    private static final long serialVersionUID = 1L;
    private PaymentPanel paymentPanel = null;
    private InputPanel numberPanel = null;
    private InputPanel amountPanel = null;

    public CreditContractPaymentComponent(Factory factory) {
        super(factory);
        initialize();
    }

    private void initialize() {
        paymentPanel = new PaymentPanel(getFactory());
        paymentPanel.setPaymentType(ResBundleCreditContractPayment.getString("AGREEMENT_PAYMENT"));
        numberPanel = new InputPanel(getFactory(),
                ResBundleCreditContractPayment.getString("AGREEMENT_NUMBER"),
                ResBundleCreditContractPayment.getString("ENTER_AGREEMENT_NUMBER"), InputStyle.STRING);
        amountPanel = new InputPanel(getFactory(),
                ResBundleCreditContractPayment.getString("CHARGE_AMOUNT"),
                ResBundleCreditContractPayment.getString("ENTER_PAYMENT"), InputStyle.STRING);
        this.setPreferredSize(Size.middlePanel);
        this.add(paymentPanel, null);
        this.add(numberPanel, null);
        this.add(amountPanel, null);
        setState(CreditContractState.ENTER_NUMBER);
    }

    public void setPaid(Long paid) {
        paymentPanel.setPaid(paid);
    }

    public void setSumma(Long summa) {
        paymentPanel.setSumma(summa);
    }

    public void setChange(boolean change) {
        if (change) {
            paymentPanel.setPaymentType(ResBundleCreditContractPayment.getString("CHANGE"));
        } else {
            paymentPanel.setPaymentType(ResBundleCreditContractPayment.getString("AGREEMENT_PAYMENT"));
        }
    }

    public void setState(CreditContractState state) {
        switch (state) {
            case ENTER_NUMBER:
                numberPanel.setVisible(true);
                amountPanel.setVisible(false);
                validate();
                numberPanel.validate();
                break;
            case ENTER_AMOUNT:
                numberPanel.setVisible(false);
                amountPanel.setVisible(true);
                validate();
                amountPanel.validate();
                break;
            default:
                break;
        }
    }

    public void setAgreementNumber(String cardNumber) {
        numberPanel.setField(cardNumber);
    }

    public void setAgreementNumberBehaviour(boolean behaviour) {
        numberPanel.changeBehaviour(behaviour);
    }

    public void setPayment(Long payment) {
        if (payment > 0L) {
            amountPanel.setField(FormatHelper.formatInputSumma(payment / 100.0));
        } else {
            amountPanel.changeBehaviour(false);
        }
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
        amountPanel.setLabel(label);
    }

    @Override
    public void setCurrency(String symbol, String name) {
        paymentPanel.setCurrency(symbol, name);
    }

    @Override
    public void setPaid(String paid) {
        paymentPanel.setPaid(paid);
    }
}
