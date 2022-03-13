package ru.crystals.pos.fiscalprinter.nfd.utils;

import org.junit.Test;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.currency.CurrencyHandlerParams;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.AddCommodity;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UtilsNFDTest {


    @Test
    public void convertGoodsToCommodityCase1Test() {
        final String mark = "00000046187703!&93WRp0000429d";
        initKZCurrencyHandler();

        Goods goods = new Goods();
        goods.setName("Товар");
        goods.setItem("12345");
        goods.setEndPositionPrice(19700L);
        goods.setEndPricePerUnit(24200L);
        goods.setQuant(815L);
        goods.setExcise(mark);

        final AddCommodity addCommodity = UtilsNFD.convertGoodsToCommodity(goods, true);

        final Modifier modifier = addCommodity.getModifier();
        assertEquals(new BigDecimal("242.00"), addCommodity.getPrice());
        assertEquals(new BigDecimal("0.815"), addCommodity.getQuantity());
        assertNotNull(modifier);
        assertEquals(new BigDecimal("0.23"), modifier.getSum());
        assertEquals(ModifierType.DISCOUNT, modifier.getType());
        assertEquals(mark, addCommodity.getExciseStamp());
    }

    @Test
    public void convertGoodsToCommodityCase2Test() {
        initKZCurrencyHandler();

        Goods goods = new Goods();
        goods.setName("Товар");
        goods.setItem("12345");
        goods.setEndPositionPrice(36800L);
        goods.setEndPricePerUnit(24200L);
        goods.setQuant(1523L);

        final AddCommodity addCommodity = UtilsNFD.convertGoodsToCommodity(goods, true);

        final Modifier modifier = addCommodity.getModifier();
        assertEquals(new BigDecimal("242.00"), addCommodity.getPrice());
        assertEquals(new BigDecimal("1.523"), addCommodity.getQuantity());
        assertNotNull(modifier);
        assertEquals(new BigDecimal("0.57"), modifier.getSum());
        assertEquals(ModifierType.DISCOUNT, modifier.getType());
    }

    private void initKZCurrencyHandler() {
        final CurrencyHandler kzt = new CurrencyHandlerFactory().getCurrencyHandler("KZT");
        final CurrencyHandlerParams params = new CurrencyHandlerParams();
        params.setMoneyGranularity(100L);
        params.setRoundingMode(RoundingMode.FLOOR);
        params.setRoundingModeForSale(RoundingMode.FLOOR);
        params.setRoundingModeForReturn(RoundingMode.FLOOR);

        kzt.setParams(params);
        CurrencyUtil.init(kzt);
    }

    @Test
    public void convertSinglePayment() {
        final List<Payment> input = Collections.singletonList(makePayment(0, 100_00));

        final Set<PaymentNFD> expected = Collections.singleton(new PaymentNFD(PaymentType.CASH, new BigDecimal("100.00")));
        final Set<PaymentNFD> actual = UtilsNFD.convertPayments(input);

        assertEquals(expected, actual);
    }

    @Test
    public void convertMultiplePayments() {
        final List<Payment> input = Arrays.asList(
                makePayment(0, 100_00),
                makePayment(1, 1000_00),
                makePayment(13, 10000_00),
                makePayment(0, 201_00),
                makePayment(1, 2001_00),
                makePayment(13, 20001_00)
        );

        final Set<PaymentNFD> expected = new HashSet<>(Arrays.asList(
                new PaymentNFD(PaymentType.CASH, new BigDecimal("301.00")),
                new PaymentNFD(PaymentType.CARD, new BigDecimal("3001.00")),
                new PaymentNFD(PaymentType.CREDIT, new BigDecimal("30001.00"))
        ));
        final Set<PaymentNFD> actual = UtilsNFD.convertPayments(input);

        assertEquals(expected, actual);
    }

    private Payment makePayment(long index, long sum) {
        final Payment payment = new Payment();
        payment.setSum(sum);
        payment.setIndexPaymentFDD100(index);
        return payment;
    }
}