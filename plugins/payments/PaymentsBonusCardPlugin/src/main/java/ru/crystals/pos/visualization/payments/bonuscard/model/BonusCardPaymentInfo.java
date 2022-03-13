package ru.crystals.pos.visualization.payments.bonuscard.model;

import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;
import ru.crystals.wsclient.cards.internal.BonusAccountVO;
import ru.crystals.wsclient.cards.internal.InternalCardInformationVO;

import java.math.BigDecimal;


public class BonusCardPaymentInfo extends DefaultPaymentInfo {
    private String cardNumber;
    private String exceptionText;
    private InternalCardInformationVO cardInfo;
    private BonusAccountVO bonusAccount;
    private BigDecimal availableBalance;
    /**
     * сумма, доступная к оплате данным типом оплаты
     */
    private long paymentTypeSurcharge;
    /**
     * чек
     */
    private PurchaseEntity purchase;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public InternalCardInformationVO getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(InternalCardInformationVO cardInfo) {
        this.cardInfo = cardInfo;
    }

    public BonusAccountVO getBonusAccount() {
        return bonusAccount;
    }

    public void setBonusAccount(BonusAccountVO bonusAccount) {
        this.bonusAccount = bonusAccount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public void clearCustomData() {
        cardNumber = null;
        exceptionText = null;
        cardInfo = null;
        bonusAccount = null;
        availableBalance = null;
    }

    public long getPaymentTypeSurcharge() {
        return paymentTypeSurcharge;
    }

    public void setPaymentTypeSurcharge(long paymentTypeSurcharge) {
        this.paymentTypeSurcharge = paymentTypeSurcharge;
    }

    public PurchaseEntity getPurchase() {
        return purchase;
    }

    public void setPurchase(PurchaseEntity purchase) {
        this.purchase = purchase;
    }
}
