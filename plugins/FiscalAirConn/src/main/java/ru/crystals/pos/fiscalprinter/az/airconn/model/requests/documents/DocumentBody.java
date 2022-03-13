package ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.fiscalprinter.az.airconn.model.Item;
import ru.crystals.pos.fiscalprinter.az.airconn.model.VatAmount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DocumentBody {
    private String cashier;
    private String currency = "AZN";
    private List<Item> items;
    private BigDecimal sum;
    private BigDecimal cashSum;
    private BigDecimal cashlessSum;
    private BigDecimal prepaymentSum;
    private BigDecimal creditSum;
    private BigDecimal bonusSum;
    private List<VatAmount> vatAmounts;
    /**
     * Используется в документах money_back и rollback
     */
    private String parentDocument;
    /**
     * Исользуется в документе correction
     */
    private String firstOperationAtUtc;
    private String lastOperationAtUtc;

    /**
     * Инициализирует все типы оплат нулем, нулевые оплаты нужны в документах продажы и возврата
     */
    public void initPayments() {
        //Используется convertMoney для установки денежного scale,
        //без него AirConn воспримет данные как integer и выдаст ошибку
        cashSum = cashlessSum = prepaymentSum = creditSum = bonusSum = BigDecimalConverter.convertMoney(0);
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        //По документации имя кассира "cashier" имеет максимальную длину 64, на практике принимает не более 32.
        final int NAME_MAX_LENGTH = 32;
        this.cashier = StringUtils.left(cashier, NAME_MAX_LENGTH);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getCashSum() {
        return cashSum;
    }

    public void setCashSum(BigDecimal cashSum) {
        this.cashSum = cashSum;
    }

    public void addCashSum(BigDecimal value) {
        cashSum = cashSum.add(value);
    }

    public BigDecimal getCashlessSum() {
        return cashlessSum;
    }

    public void setCashlessSum(BigDecimal cashlessSum) {
        this.cashlessSum = cashlessSum;
    }

    public void addCashlessSum(BigDecimal value) {
        cashlessSum = cashlessSum.add(value);
    }

    public BigDecimal getPrepaymentSum() {
        return prepaymentSum;
    }

    public void setPrepaymentSum(BigDecimal prepaymentSum) {
        this.prepaymentSum = prepaymentSum;
    }

    public void addPrepaymentSum(BigDecimal value) {
        prepaymentSum = prepaymentSum.add(value);
    }

    public BigDecimal getCreditSum() {
        return creditSum;
    }

    public void setCreditSum(BigDecimal creditSum) {
        this.creditSum = creditSum;
    }

    public void addCreditSum(BigDecimal value) {
        creditSum = creditSum.add(value);
    }

    public BigDecimal getBonusSum() {
        return bonusSum;
    }

    public void setBonusSum(BigDecimal bonusSum) {
        this.bonusSum = bonusSum;
    }

    public void addBonusSum(BigDecimal value) {
        bonusSum = bonusSum.add(value);
    }

    /**
     * Уберает сдачу из суммы оплаты наличными
     */
    public void removerChangeFromPayment() {
        BigDecimal fullPayment = cashSum.add(cashlessSum).add(prepaymentSum).add(creditSum).add(bonusSum);
        BigDecimal change = fullPayment.subtract(sum);
        cashSum = cashSum.subtract(change);
    }

    public List<VatAmount> getVatAmounts() {
        return vatAmounts;
    }

    public void setVatAmounts(List<VatAmount> vatAmounts) {
        this.vatAmounts = vatAmounts;
    }

    public void addVatAmount(VatAmount newVatAmount) {
        if (vatAmounts == null) {
            vatAmounts = new ArrayList<>();
        }
        //Аккамулируем суммы товаров в vatAmounts по процентам налогов
        for (VatAmount vat : vatAmounts) {
            if (vat.getVatPercent() == null) {
                if (newVatAmount.getVatPercent() == null) {
                    vat.setVatSum(vat.getVatSum().add(newVatAmount.getVatSum()));
                    return;
                }
                continue;
            }
            if (vat.getVatPercent().equals(newVatAmount.getVatPercent())) {
                vat.setVatSum(vat.getVatSum().add(newVatAmount.getVatSum()));
                return;
            }
        }
        vatAmounts.add(newVatAmount);
    }

    public String getParentDocument() {
        return parentDocument;
    }

    public void setParentDocument(String parentDocument) {
        this.parentDocument = parentDocument;
    }

    public String getFirstOperationAtUtc() {
        return firstOperationAtUtc;
    }

    public void setFirstOperationAtUtc(String firstOperationAtUtc) {
        this.firstOperationAtUtc = firstOperationAtUtc;
    }

    public String getLastOperationAtUtc() {
        return lastOperationAtUtc;
    }

    public void setLastOperationAtUtc(String lastOperationAtUtc) {
        this.lastOperationAtUtc = lastOperationAtUtc;
    }
}
