package ru.crystals.pos.fiscalprinter.de.fcc;

import org.junit.Ignore;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransaction;
import ru.crystals.set10dto.TaxVO;
import ru.crystals.set10dto.purchase.payments.PaymentTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 *
 * @author dalex
 */
public class FCCTest {

    class KPKCountersTest extends KPKCounters {

        private AtomicLong id = new AtomicLong(0);

        @Override
        synchronized String getTransactionID() {
            return String.valueOf(id.getAndIncrement());
        }
    }

    class FCCImplTest extends FCCImpl {
        public FCCImplTest() {
            super(new KPKCountersTest());
        }

        @Override
        public FCCTransaction getStartedTrasaction(String externalTransactionId) throws FCCException, IOException {
            return super.getStartedTrasaction(externalTransactionId);
        }

        @Override
        public String getFCCPaymentString(Check purchase) {
            return super.getFCCPaymentString(purchase);
        }

        @Override
        public String getFCCPurchaseTaxes(Check purchase) throws FCCException {
            return super.getFCCPurchaseTaxes(purchase);
        }

        @Override
        public BelegData getTrasactionById(String transactionId) throws FCCException, IOException {
            return super.getTrasactionById(transactionId); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private FCCImplTest sut = new FCCImplTest();

    @Test
    public void testCallTax() throws IOException, FCCException {
        float v = 10.7f;
        BigDecimal ff = BigDecimal.valueOf(v);
        System.out.println("V - " + ff);
    }

    @Ignore
    @Test
    public void testCall() throws IOException, FCCException {

        Logger.getLogger("ru.crystals.pos").setLevel(Level.ALL);

        // connect
        sut.setTaxes(getDeTaxes());
        FCConfig config = new FCConfig();
        config.setConnectorAddress("localhost:20001");
        sut.setConfig(config);
        sut.connect();

        Check purchase = CheckBuilder.newBuilder()
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "USD", 1010)
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "CHF", 2010)
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "CHF", 3010)
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "EUR", 4010)
                .addPayment(PaymentTypes.CASH_TYPE, "USD", 5000)
                .addPayment(PaymentTypes.CASH_TYPE, "CHF", 6000)
                .addPayment(PaymentTypes.CASH_TYPE, "USD", 7000)
                .addPayment(PaymentTypes.CASH_TYPE, "EUR", 8000)
                .appPosition("test1", 10000, 4000, 0, "0%")
                .appPosition("test1", 10000, 4000, 400, "5.5%")
                .appPosition("test1", 10000, 4000, 400, "5.5%")
                .appPosition("test1", 10000, 4000, 300, "10.7%")
                .appPosition("test1", 10000, 4000, 200, "7%")
                .appPosition("test1", 10000, 4000, 100, "19%")
                .addCashier("Petrov", "A","A")
                .build();

        sut.fiscalize(purchase);
    }

    @Ignore
    @Test
    public void testCall2() throws IOException, FCCException {

        Logger.getLogger("ru.crystals.pos").setLevel(Level.ALL);

        // connect
        FCConfig config = new FCConfig();
        config.setConnectorAddress("localhost:20001");
        sut.setConfig(config);
        sut.connect();
        FCCTransaction result = sut.getStartedTrasaction("");
        System.out.println("Result - " + result);
    }

    @Ignore
    @Test
    public void testCall3() throws IOException, FCCException {

        int belegNum = 251;
        Logger.getLogger("ru.crystals.pos").setLevel(Level.ALL);

        // connect
        FCConfig config = new FCConfig();
        config.setConnectorAddress("localhost:20001");
        sut.setConfig(config);
        sut.connect();
        BelegData result = sut.getTrasactionById(String.valueOf(belegNum));
        System.out.println("data - " + result);
//        try (FileOutputStream out = new FileOutputStream("/home/dalex/beleg-" + belegNum + ".tar")) {
//            out.write(result.getSrc());
//        }


//        System.out.println("Result - " + result);
    }

    @Test
    public void paymentStringTest() {

        // Order rules:
        // 1. first type cash, second cashless
        // 2. first currency EUR, second all other ordered by alphabet
        // String build rules:
        // 1. Skip empty payment
        // given
        Check purchase = CheckBuilder.newBuilder()
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "ZZZ", 1010) // 7
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "CCC", 2010) // 6
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "ZZZ", 3010) // 8
                .addPayment(PaymentTypes.BANK_CARD_TYPE, "EUR", 4010) // 5
                .addPayment(PaymentTypes.CASH_TYPE, "ZZZ", 5000) // 3
                .addPayment(PaymentTypes.CASH_TYPE, "ZZZ", 0) // skip
                .addPayment(PaymentTypes.CASH_TYPE, "CCC", 6000) // 2
                .addPayment(PaymentTypes.CASH_TYPE, "ZZZ", 7000) // 4
                .addPayment(PaymentTypes.CASH_TYPE, "EUR", 8000) // 1
                .addCashier("Petrov", "A","A")
                .build();

        // when
        String paymentString = sut.getFCCPaymentString(purchase);
        System.out.println("String: " + paymentString);

        // then       1         2             3             4             5           6               7               8
        assertEquals("80.00:Bar_60.00:Bar:CCC_50.00:Bar:ZZZ_70.00:Bar:ZZZ_40.10:Unbar_20.10:Unbar:CCC_10.10:Unbar:ZZZ_30.10:Unbar:ZZZ", paymentString);
    }

    @Test
    public void taxStringTest() throws FCCException {
        // Rules:
        // 1. ordered by index
        // 2. include all tax types (with zero)

        // given
        Check purchase = CheckBuilder.newBuilder()
                .appPosition("", 10000, 4000, 0, "0%")
                .appPosition("", 10000, 4000, 400, "5.5%")
                .appPosition("", 10000, 4000, 400, "5.5%")
                .appPosition("", 10000, 4000, 300, "10.7%")
                .appPosition("", 10000, 4000, 200, "7%")
                .appPosition("", 10000, 4000, 100, "19%")
                .addCashier("Petrov", "A","A")
                .build();

        sut.setTaxes(getDeTaxes());

        // when
        String result = sut.getFCCPurchaseTaxes(purchase);

        // then
        assertEquals("1.00_2.00_3.00_8.00_0.00", result);
    }

    private Collection<TaxVO> getDeTaxes() {
        Collection<TaxVO> taxes = new ArrayList<>();
        taxes.add(new TaxVO(1, 1900L, "19%"));
        taxes.add(new TaxVO(2, 700L, "7%"));
        taxes.add(new TaxVO(3, 1070L, "10.7%"));
        taxes.add(new TaxVO(4, 550L, "5.5%"));
        taxes.add(new TaxVO(5, 0L, "0%"));
        return taxes;
    }
}
