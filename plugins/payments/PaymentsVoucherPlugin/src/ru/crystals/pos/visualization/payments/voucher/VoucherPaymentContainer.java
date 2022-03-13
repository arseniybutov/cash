package ru.crystals.pos.visualization.payments.voucher;


import ru.crystals.pos.payments.VoucherPaymentEntity;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;

public class VoucherPaymentContainer extends PaymentContainer
        implements NumberEventListener, EscEventListener, EnterEventListener, DotEventListener {
    enum VoucherState {ENTER_NUMBER, ENTER_AMOUNT}

    ;

    private VoucherPaymentComponent visualPanel = null;
    private String voucherNumber = "";
    private VoucherState state = VoucherState.ENTER_NUMBER;

    @Override
    public String getChargeName() {
        return ResBundlePaymentVoucher.getString("TO_CHARGE");
    }

    @Override
    public String getPaymentBase() {
        return VoucherPaymentEntity.class.getName();
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return getVisualPanel();
    }

    @Override
    public String getPaymentType() {
        if (isRefund()) {
            return ResBundlePaymentVoucher.getString("REFUND_VOUCHER_PAYMENT");
        } else {
            return ResBundlePaymentVoucher.getString("VOUCHER");
        }
    }

    @Override
    public VoucherPaymentComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new VoucherPaymentComponent(getFactory());
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
                if (voucherNumber.length() == 19) {
                    voucherNumber = voucherNumber.substring(0, 4) + voucherNumber.substring(5, 9) + voucherNumber.substring(10, 14) + voucherNumber.substring(15, 19);
                    state = VoucherState.ENTER_AMOUNT;
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

    @Override
    public void esc() {
        if (getPayment().getSumPay() != null) {
            dispatchCloseEvent();
            return;
        }
        super.esc();
        switch (state) {
            case ENTER_NUMBER:
                reset();
                break;
            case ENTER_AMOUNT:
                if (!getSummaHelper().isSummaAvailable() || isSummaReset()) {
                    voucherNumber =
                            voucherNumber.substring(0, 4) + " " + voucherNumber.substring(4, 8) + " " + voucherNumber.substring(8, 12) + " " + voucherNumber.substring(12, 16);
                    state = VoucherState.ENTER_NUMBER;
                    getVisualPanel().setState(state);
                    updateDisplay(0L, getSurcharge());
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

    @Override
    public void number(Byte num) {
        setReset(false);
        switch (state) {
            case ENTER_NUMBER:
                if (voucherNumber.length() < 19) {
                    if (voucherNumber.length() == 4 || voucherNumber.length() == 9 || voucherNumber.length() == 14) {
                        voucherNumber += " ";
                    }
                    voucherNumber += num.toString();
                    getVisualPanel().setVoucherNumber(voucherNumber);
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
        state = VoucherState.ENTER_NUMBER;
        setReset(true);
        setChange(false);
        getSummaHelper().reset();
        voucherNumber = "";
        getVisualPanel().setVoucherNumber(voucherNumber);
        getVisualPanel().setVoucherNumberBehaviour(false);
        resetPanels();
        getVisualPanel().setState(state);
    }

    @Override
    public void dot() {
        if (state == VoucherState.ENTER_AMOUNT && getPayment().getPaymentType().getDimension() > 0) {
            getSummaHelper().dot();
        }
    }

    @Override
    public String getTitlePaymentType() {
        if (isRefund()) {
            return ResBundlePaymentVoucher.getString("REFUND_VOUCHER_PAYMENT");
        } else {
            return ResBundlePaymentVoucher.getString("VOUCHER_PAYMENT");
        }
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentVoucher.getString("RETURN_PAYMENT_VOUCHER");
    }

    @Override
    public void setPaymentFields() {
        VoucherPaymentEntity payment = (VoucherPaymentEntity) getPayment();
        payment.setVoucherNumber(voucherNumber);
    }
}
