package ru.crystals.pos.visualization.payments.creditcontract;

import ru.crystals.pos.payments.CreditContractPaymentEntity;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;

public class CreditContractPaymentContainer extends PaymentContainer
        implements NumberEventListener, EscEventListener, EnterEventListener, DotEventListener {
    enum CreditContractState {ENTER_NUMBER, ENTER_AMOUNT}

    ;

    private CreditContractPaymentComponent visualPanel = null;
    private String agreementNumber = "";
    private CreditContractState state = CreditContractState.ENTER_NUMBER;


    @Override
    public String getChargeName() {
        return ResBundleCreditContractPayment.getString("TO_CHARGE");
    }

    @Override
    public String getPaymentBase() {
        return CreditContractPaymentEntity.class.getName();
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return getVisualPanel();
    }

    @Override
    public String getPaymentType() {
        if (isRefund()) {
            return ResBundleCreditContractPayment.getString("REFUND_AGREEMENT_PAYMENT");
        } else {
            return ResBundleCreditContractPayment.getString("CREDIT_AGREEMENT");
        }
    }

    @Override
    public CreditContractPaymentComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new CreditContractPaymentComponent(getFactory());
        }
        return visualPanel;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public void enter() {
        switch (state) {
            case ENTER_NUMBER:
                if (agreementNumber.length() == 11) {
                    state = CreditContractState.ENTER_AMOUNT;
                    updateDisplay(getSurcharge(), getSurcharge());
                    getVisualPanel().setState(state);
                }
                break;
            case ENTER_AMOUNT:
                if (!isChange()) {
                    if (getSummaHelper().isSummaAvailable()) {
                        processPayment();
                    } else {
                        getFactory().getTechProcess().error();
                    }
                } else {
                    dispatchCloseEvent();
                }
                break;
            default:
                break;
        }
    }


    public void esc() {
        if (getPayment().getSumPay() != null) {
            dispatchCloseEvent();
        } else {
            super.esc();
            switch (state) {
                case ENTER_NUMBER:
                    reset();
                    break;
                case ENTER_AMOUNT:
                    if (!getSummaHelper().isSummaAvailable() || isSummaReset()) {
                        state = CreditContractState.ENTER_NUMBER;
                        getVisualPanel().setState(state);
                    } else {
                        getSummaHelper().reset();
                        getVisualPanel().setPayment(getSurcharge());
                        updateDisplay(getSurcharge(), getSurcharge());
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void number(Byte num) {
        setReset(false);
        switch (state) {
            case ENTER_NUMBER:
                if (agreementNumber.length() < 11) {
                    agreementNumber += num.toString();
                    getVisualPanel().setAgreementNumber(agreementNumber);
                }
                break;
            case ENTER_AMOUNT:
                enterAmount(num);
                break;
            default:
                break;
        }
    }

    @Override
    public void reset() {
        state = CreditContractState.ENTER_NUMBER;
        setReset(true);
        setChange(false);
        getSummaHelper().reset();
        agreementNumber = "";
        getVisualPanel().setAgreementNumber(agreementNumber);
        getVisualPanel().setAgreementNumberBehaviour(false);
        resetPanels();
        getVisualPanel().setState(state);
    }

    @Override
    public void dot() {
        switch (state) {
            case ENTER_AMOUNT:
                if (getPayment().getPaymentType().getDimension() > 0) {
                    getSummaHelper().dot();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String getTitlePaymentType() {
        if (isRefund()) {
            return ResBundleCreditContractPayment.getString("REFUND_AGREEMENT_PAYMENT");
        } else {
            return ResBundleCreditContractPayment.getString("AGREEMENT_PAYMENT");
        }
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundleCreditContractPayment.getString("RETURN_PAYMENT_CREDIT_CONTRACT");
    }

    @Override
    public void setPaymentFields() {
        CreditContractPaymentEntity payment = (CreditContractPaymentEntity) getPayment();
        payment.setContractNumber(agreementNumber);
    }

}
