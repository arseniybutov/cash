package ru.crystals.pos.fiscalprinter.de.fcc;

import java.util.ArrayList;
import java.util.List;
import ru.crystals.set10dto.purchase.PurchaseVO;
import ru.crystals.set10dto.purchase.payments.PaymentVO;
import ru.crystals.set10dto.purchase.positions.PositionVO;

/**
 *
 * @author dalex
 */
public class PurchaseBuilder {


    public static PurchaseBuilder newBuilder() {
        return new PurchaseBuilder();
    }

    private PurchaseBuilder(){}

    private PurchaseVO purchase = new PurchaseVO();


    private List<PaymentVO> getPayments() {
        if (purchase.getPayments() == null) {
            purchase.setPayments(new ArrayList<>());
        }
        return purchase.getPayments();
    }

    private List<PositionVO> getPositions() {
        if (purchase.getPositions() == null) {
            purchase.setPositions(new ArrayList<>());
        }
        return purchase.getPositions();
    }

    public PurchaseBuilder addPayment(String type, String currency, long sum) {
        PaymentVO p = new PaymentVO();
        p.setCurrency(currency);
        p.setPaymentType(type);
        p.setSumPay(sum);
        getPayments().add(p);
        return this;
    }

    public PurchaseVO build() {
        return purchase;
    }

    public PurchaseBuilder appPosition(String name, long price, long qnty, long tax, String taxClass) {
        PositionVO pos = new PositionVO();
        pos.setName(name);
        pos.setPriceStart(price);
        pos.setPriceEnd(price);
        pos.setQnty(qnty);
        pos.setNdsSum(tax);
        pos.setNdsClass(taxClass);
        getPositions().add(pos);
        return this;
    }
}
