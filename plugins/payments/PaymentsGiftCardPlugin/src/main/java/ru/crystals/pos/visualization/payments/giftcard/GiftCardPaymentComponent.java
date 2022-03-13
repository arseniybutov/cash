package ru.crystals.pos.visualization.payments.giftcard;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.admin.components.ReportComponent;
import ru.crystals.pos.visualization.components.PanelWithConfirmation;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.NumberInputPanel;
import ru.crystals.pos.visualization.input.PaymentInputPanel;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentPanel;
import ru.crystals.pos.visualization.payments.giftcard.GiftCardPaymentContainer.GiftCardState;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import java.math.BigDecimal;

/**
 * Пытается отвечать за view оплат картой, у него это не всегда получается
 */
public class GiftCardPaymentComponent extends VisualPanel implements PaymentComponent {

    private static final long serialVersionUID = 1L;

    private PaymentPanel paymentPanel = null;

    private NumberInputPanel giftCardNumber = null;

    private PaymentInputPanel giftCardAmount = null;

    private ReportComponent reportComponent = null;

    private VisualPanel dataExchange = null;

    private PanelWithConfirmation wPanel = null;

    public GiftCardPaymentComponent() {
        initialize();
    }

    private void initialize() {
        wPanel = new PanelWithConfirmation(ResBundlePaymentGiftCard.getString("WARN_CANCELED"), ResBundlePaymentGiftCard.getString("WARN_CONFIRM"), true);
        wPanel.setPreferredSize(Size.middlePanel);
        paymentPanel = new PaymentPanel();
        paymentPanel.setPaymentType(ResBundlePaymentGiftCard.getString("GIFT_PAYMENT"));
        giftCardNumber = new NumberInputPanel(ResBundlePaymentGiftCard.getString("ENTER_CARD_NUMBER"), 3);
        giftCardAmount = new PaymentInputPanel(CurrencyUtil.getFractionDigits());
        dataExchange = new VisualPanel();
        dataExchange.setPreferredSize(Size.middlePanel);
        this.setPreferredSize(Size.middlePanel);

        this.add(paymentPanel, null);
        this.add(giftCardNumber, null);
        this.add(giftCardAmount, null);
        this.add(dataExchange, null);
        this.add(wPanel, null);

        setState(GiftCardState.ENTER_NUMBER);
    }

    public void setPaid(Long paid) {
        paymentPanel.setPaid(paid);
    }

    public void setSumma(Long summa) {
        paymentPanel.setSumma(summa);
    }

    public void setChange(boolean change) {
        if (change) {
            paymentPanel.setPaymentType(ResBundlePaymentGiftCard.getString("CHANGE"));
        } else {
            paymentPanel.setPaymentType(ResBundlePaymentGiftCard.getString("GIFT_PAYMENT"));
        }
    }

    public void setState(GiftCardState state) {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        switch (state) {
            case ENTER_NUMBER:
                wPanel.setVisible(false);
                giftCardAmount.setVisible(false);
                paymentPanel.setVisible(true);
                giftCardNumber.setVisible(true);
                validate();
                giftCardNumber.validate();
                dataExchange.setVisible(false);

                break;

            case ENTER_AMOUNT:
                paymentPanel.setVisible(true);
                giftCardAmount.setVisible(true);
                giftCardNumber.setVisible(false);
                wPanel.setVisible(false);
                dataExchange.setVisible(false);
                validate();
                giftCardAmount.validate();

                break;

            case DATA_EXCHANGE:
                wPanel.setVisible(false);
                paymentPanel.setVisible(false);
                giftCardNumber.setVisible(false);
                giftCardAmount.setVisible(false);
                dataExchange.setVisible(true);
                validate();
                dataExchange.validate();
                break;

            case SHOW_WARNING_BEEP:
                paymentPanel.setVisible(false);
                giftCardNumber.setVisible(false);
                giftCardAmount.setVisible(false);
                dataExchange.setVisible(false);
                if (wPanel.isCommitButtonSelected()) {
                    //выделяем кнопку "Отмена"
                    wPanel.changeSelection();
                }
                wPanel.setVisible(true);
                validate();
                dataExchange.validate();
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                break;
            default:
                break;
        }
    }

    private void changeSelectionIfNeed(GiftCardState state) {
        if (state == GiftCardState.SHOW_WARNING || state == GiftCardState.SHOW_WARNING_BEEP) {
            wPanel.changeSelection();
        }
    }

    public void left(GiftCardState state) {
        changeSelectionIfNeed(state);
    }

    public void right(GiftCardState state) {
        changeSelectionIfNeed(state);
    }

    public void setPayment(Long payment) {
        giftCardAmount.getInputField().setPresetValue(CurrencyUtil.convertMoney(payment));
    }

    public void setPaymentEditable(boolean editable) {
        InputFieldInterface<BigDecimal> inputField = giftCardAmount.getInputField();
        if (!editable) {
            inputField.setFontColor(Color.greyText);
        }
        inputField.setEnabled(editable);
    }

    public InputFieldInterface<BigDecimal> getPaymentInputField() {
        return giftCardAmount.getInputField();
    }

    public long getPaymentSum() {
        return CurrencyUtil.convertMoney(giftCardAmount.getInputField().getValue());
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
        giftCardAmount.setLabel(label);
    }

    @Override
    public void setCurrency(String symbol, String name) {
        paymentPanel.setCurrency(symbol, name);
    }

    @Override
    public void setPaid(String paid) {
        paymentPanel.setPaid(paid);
    }

    public ReportComponent getReportComponent() {
        return reportComponent;
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

    public InputFieldInterface<String> getCardNumberInputField() {
        return giftCardNumber.getInputField();
    }

    public void setWarnText(String text) {
        wPanel.setText(text);
        wPanel.selectCancel(true);
    }

    protected boolean isCardUsingConfirm() {
        return wPanel.isCommitButtonSelected();
    }
}
