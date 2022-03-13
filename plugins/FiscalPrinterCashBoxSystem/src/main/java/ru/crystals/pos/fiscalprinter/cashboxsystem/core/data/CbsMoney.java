package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание цены/суммы в коммандах CBS.
 */
public class CbsMoney {

    /**
     * Целая часть
     */
    @JsonProperty("bills")
    private Long bills = 0L;
    /**
     * Дробная часть (копейки) (по умолчанию 0)
     */
    @JsonProperty("coins")
    private Long coins = 0L;
    /**
     * Количество копеек в одной целой части (по умолчанию 100)
     */
    @JsonProperty("coins_in_bills")
    private Long coinsInBills = 100L;

    public Long getBills() {
        return bills;
    }

    public void setBills(Long bills) {
        this.bills = bills;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public Long getCoinsInBills() {
        return coinsInBills;
    }

    public void setCoinsInBills(Long coinsInBills) {
        this.coinsInBills = coinsInBills;
    }

    public void addMoneyFromLong(Long sum) {
        bills += sum / coinsInBills;
        coins += sum % coinsInBills;
    }

    public Long getLongFromMoney() {
        return (bills * coinsInBills) + coins;
    }

    public static CbsMoney getMoneyFromLong(Long sum) {
        CbsMoney money = new CbsMoney();
        money.setBills(sum / money.coinsInBills);
        money.setCoins(sum % money.coinsInBills);
        return money;
    }
}
