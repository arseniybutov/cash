package ru.crystals.pos.spi.ui.forms.payment;

import java.math.BigDecimal;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;

/**
 * Класс, расширяющий модель SumToPayFormParameters
 * для передачи необходимых данных форме ввода сумм к оплате/возврату
 *
 */
public class ExtSumToPayFormParameters {

    private SumToPayFormParameters params;

    private BigDecimal sum;

    private BigDecimal paid;
    
    /**
     * true - продажа, false - возврат
     */
    private boolean sale = true;

    /**
     * true - продажа, false - возврат
     */
    public boolean isSale() {
        return sale;
    }
    
    /**
     * true - продажа, false - возврат
     */
    public void setSale(boolean sale) {
        this.sale = sale;
    }

    public ExtSumToPayFormParameters(SumToPayFormParameters params) {
        this.params = params;
    }
    
    public SumToPayFormParameters getParams() {
        return params;
    }

    public void setParams(SumToPayFormParameters params) {
        this.params = params;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

}
