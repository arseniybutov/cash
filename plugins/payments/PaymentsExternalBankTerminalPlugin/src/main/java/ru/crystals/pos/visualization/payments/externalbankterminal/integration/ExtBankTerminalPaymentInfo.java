package ru.crystals.pos.visualization.payments.externalbankterminal.integration;

import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

public class ExtBankTerminalPaymentInfo extends DefaultPaymentInfo {
    private String lastDigits;
    private Long checkNumber;
    private String authCode;
    private PaymentEntity prePayment;

    public ExtBankTerminalPaymentInfo() {

    }

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public Long getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(Long checkNumber) {
        this.checkNumber = checkNumber;
    }

    public PaymentEntity getPrePayment() {
        return prePayment;
    }

    public void setPrePayment(PaymentEntity prePayment) {
        this.prePayment = prePayment;
    }

    @Override
    public String toString() {
        return "ExtBankTerminalPaymentInfo{" +
                "lastDigits='" + lastDigits + '\'' +
                ", checkNumber=" + checkNumber +
                ", authCode='" + authCode + '\'' +
                '}';
    }
}
