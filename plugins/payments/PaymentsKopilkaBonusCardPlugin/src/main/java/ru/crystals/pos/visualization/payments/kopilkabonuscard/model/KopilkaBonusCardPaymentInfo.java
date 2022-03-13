package ru.crystals.pos.visualization.payments.kopilkabonuscard.model;

import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Простой класс для хранения промежуточной инфы, сопутствующей оплате.
 * Данные попадают сюда из адаптера, используем где надо на визуализации.
 */
public class KopilkaBonusCardPaymentInfo extends DefaultPaymentInfo {
    private String exceptionText;

    private Long totalBalance;

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public Long getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Long totalBalance) {
        this.totalBalance = totalBalance;
    }
}
