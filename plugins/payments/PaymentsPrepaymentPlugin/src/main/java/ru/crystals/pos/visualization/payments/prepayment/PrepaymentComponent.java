package ru.crystals.pos.visualization.payments.prepayment;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.spi.ui.forms.MessageForm;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.PaymentInputPanel;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentPanel;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import java.math.BigDecimal;

/**
 * Отвечает за view зачета предоплаты
 */
public class PrepaymentComponent extends VisualPanel implements PaymentComponent {

    private static final long serialVersionUID = 1L;

    private PaymentPanel paymentPanel = null;

    private PaymentInputPanel amount = null;

    private MessageForm messageForm;

    public PrepaymentComponent() {
        initialize();
    }

    private void initialize() {
        paymentPanel = new PaymentPanel();
        paymentPanel.setPaymentType(ResBundlePrepayment.getString("PREPAYMENT"));
        amount = new PaymentInputPanel(CurrencyUtil.getFractionDigits());
        messageForm = new MessageForm();
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.LOADING_BIG));
        messageForm.setMessage(ResBundlePrepayment.getString("PREPAYMENT"));
        messageForm.setModal(true);
        this.setPreferredSize(Size.middlePanel);

        this.add(paymentPanel, null);
        this.add(amount, null);
        this.add(messageForm, null);
    }

    public void showPaymentForm() {
        paymentPanel.setVisible(true);
        amount.setVisible(true);
        messageForm.setVisible(false);
    }

    public void showSpinner() {
        paymentPanel.setVisible(false);
        amount.setVisible(false);
        messageForm.setVisible(true);
    }

    public void setPaid(Long paid) {
        paymentPanel.setPaid(paid);
    }

    public void setSumma(Long summa) {
        paymentPanel.setSumma(summa);
    }

    public void setChange(boolean change) {
        if (change) {
            paymentPanel.setPaymentType(ResBundlePrepayment.getString("CHANGE"));
        } else {
            paymentPanel.setPaymentType(ResBundlePrepayment.getString("PREPAYMENT"));
        }
    }


    public void setPayment(Long payment) {
        amount.getInputField().setPresetValue(CurrencyUtil.convertMoney(payment));
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
        amount.setLabel(label);
    }

    @Override
    public void setCurrency(String symbol, String name) {
        paymentPanel.setCurrency(symbol, name);
    }

    @Override
    public void setPaid(String paid) {
        paymentPanel.setPaid(paid);
    }

    public InputFieldInterface<BigDecimal> getInputField() {
        return amount.getInputField();
    }

    public PaymentInputPanel getPaymentInputPanel() {
        return amount;
    }
}
