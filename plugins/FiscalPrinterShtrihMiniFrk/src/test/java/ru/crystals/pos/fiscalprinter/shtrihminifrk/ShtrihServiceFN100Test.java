package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.fest.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;

@RunWith(BlockJUnit4ClassRunner.class)
public class ShtrihServiceFN100Test {

    private ShtrihServiceFN100 shtrihServiceFN100;

    /**
     * Проверка получения оплат, сгруппированных по типам.
     */
    @Test
    public void testGetPayments() {
        shtrihServiceFN100 = new ShtrihServiceFN100();
        Check check = new Check();
        check.setPayments(Collections.list(createPayment(0, "CashPaymentEntity", 1000),
                createPayment(1, "BankCardPaymentEntity", 2000),
                createPayment(1, "ExternalBankTerminalPaymentEntity", 3000)));
        Map<Integer, Long> result = shtrihServiceFN100.getPayments(check);
        long cash = result.get(0);
        assertEquals(cash, 1000L);
        long electron = result.get(1);
        assertEquals(electron, 5000L);

        check.getPayments().clear();
        check.setPayments(Collections.list(createPayment(0, "CashPaymentEntity", 300),
                createPayment(0, "CashPaymentEntity", 400),
                createPayment(0, "CashPaymentEntity", 500),
                createPayment(1, "BankCardPaymentEntity", 1200),
                createPayment(1, "BankCardPaymentEntity", 1800),
                createPayment(1, "ExternalBankTerminalPaymentEntity", 900),
                createPayment(1, "ExternalBankTerminalPaymentEntity", 800),
                createPayment(1, "ExternalBankTerminalPaymentEntity", 700),
                createPayment(1, "ExternalBankTerminalPaymentEntity", 600)));
        result = shtrihServiceFN100.getPayments(check);
        cash = result.get(0);
        assertEquals(cash, 1200L);
        electron = result.get(1);
        assertEquals(electron, 6000L);
    }

    private Payment createPayment(long fdd100, String type, long sum) {
        Payment payment = new Payment();
        payment.setIndexPaymentFDD100(fdd100);
        payment.setPaymentType(type);
        payment.setSum(sum);
        return payment;
    }

}
