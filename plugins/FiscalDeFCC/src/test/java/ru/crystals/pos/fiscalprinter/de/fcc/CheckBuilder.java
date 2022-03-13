package ru.crystals.pos.fiscalprinter.de.fcc;

import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dalex
 */
public class CheckBuilder {

    public static CheckBuilder newBuilder() {
        return new CheckBuilder();
    }

    private Check purchase = new Check();

    private CheckBuilder() {
        purchase.setCheckSumEnd(0L);
        purchase.setCheckSumStart(0L);
        purchase.setDiscountValue(0L);
        purchase.setDiscountValueTotal(0L);
    }

    private List<Payment> getPayments() {
        if (purchase.getPayments() == null) {
            purchase.setPayments(new ArrayList<>());
        }
        return purchase.getPayments();
    }

    private List<Goods> getPositions() {
        if (purchase.getGoods() == null) {
            purchase.setGoods(new ArrayList<>());
        }
        return purchase.getGoods();
    }

    public CheckBuilder addPayment(String type, String currency, long sum) {
        Payment p = new Payment();
        p.setCurrency(currency);
        p.setPaymentType(type);
        p.setSum(sum);
        getPayments().add(p);
        purchase.setCheckSumEnd(purchase.getCheckNumber() + sum);
        return this;
    }

    public Check build() {
        return purchase;
    }

    public CheckBuilder appPosition(String name, long price, long qnty, long tax, String taxClass) {
        Goods pos = new Goods();
        pos.setName(name);
        pos.setEndPositionPrice(price);
        pos.setQuant(qnty);
        pos.setTaxSum(tax);
        pos.setTaxName(taxClass);
        getPositions().add(pos);
        return this;
    }

    public CheckBuilder addCashier(String firstName, String middleName, String lastName) {
        Cashier cashier = new Cashier(firstName, middleName, lastName);
        purchase.setCashier(cashier);
        return this;
    }
}
