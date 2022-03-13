package ru.crystals.pos.visualization.payments.externalbankterminal.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentController;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentState;

public class ExtBankTerminalPaymentView extends AbstractPaymentView<ExtBankTerminalPaymentController> {
    private ExtBankTerminalPaymentEnterAuthCodeForm enterAuthCodeForm = null;
    private ExtBankTerminalPaymentEnterLastDigitsForm enterLastDigitsForm = null;
    private ExtBankTerminalPaymentEnterSumForm enterSumForm = null;
    private ExtBankTerminalPaymentEnterReceiptNumberForm enterReceiptNumberForm = null;
    private Logger LOG = LoggerFactory.getLogger(ExtBankTerminalPaymentView.class);

    /**
     * В конструкторе создаем все формы представления и добавляем их на него
     */
    public ExtBankTerminalPaymentView() {
        enterAuthCodeForm = new ExtBankTerminalPaymentEnterAuthCodeForm(this);
        enterLastDigitsForm = new ExtBankTerminalPaymentEnterLastDigitsForm(this);
        enterSumForm = new ExtBankTerminalPaymentEnterSumForm(this);
        enterReceiptNumberForm = new ExtBankTerminalPaymentEnterReceiptNumberForm(this);

        this.add(enterAuthCodeForm, enterAuthCodeForm.getClass().getName());
        this.add(enterLastDigitsForm, enterLastDigitsForm.getClass().getName());
        this.add(enterReceiptNumberForm, enterReceiptNumberForm.getClass().getName());
        this.add(enterSumForm, enterSumForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (ExtBankTerminalPaymentController) controller;
        enterAuthCodeForm.setController(controller);
        enterLastDigitsForm.setController(controller);
        enterReceiptNumberForm.setController(controller);
        enterSumForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        LOG.info("lastEvent: {}", lastEvent);
        LOG.info("event: {}", event);
        lastEvent = event;
        switch ((ExtBankTerminalPaymentState) lastEvent.getState()) {
            case ENTER_SUM:
                setCurrentForm(enterSumForm);
                break;
            case NOT_SET:
                break;
            case REFUND:
                break;
            case ENTER_LAST_4_DIGITS:
                setCurrentForm(enterLastDigitsForm);
                break;
            case ENTER_AUTH_CODE:
                setCurrentForm(enterAuthCodeForm);
                break;
            case ENTER_RECEIPT_NUMBER:
                setCurrentForm(enterReceiptNumberForm);
                break;
            default:
                break;
        }
    }
}
