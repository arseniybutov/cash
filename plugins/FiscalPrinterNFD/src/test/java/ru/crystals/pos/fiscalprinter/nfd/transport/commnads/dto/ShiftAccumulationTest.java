package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import org.junit.Test;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.MoneyPlacementType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NonNullableType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.CashSumShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.MoneyPlacementShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.RevenueShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationModifierShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationNonNullableShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationPaymentShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationSectionShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationSectionTotalShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationTaxShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationTotalShiftAccumulation;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ShiftAccumulationTest {

    @Test
    public void SRTZ_155_report() {
        final ShiftAccumulation accumulation = new ShiftAccumulation();
        accumulation.add(makeRevenueShiftAcc("-108"));
        accumulation.add(makeMoneyPlacementShiftAcc("-108", 1L, MoneyPlacementType.DEPOSIT));
        accumulation.add(makeMoneyPlacementShiftAcc("0", 0L, MoneyPlacementType.WITHDRAWAL));

        accumulation.add(makeTradeOperationNonNullableShiftAcc("10890907.41", NonNullableType.START, TradeOperationType.SELL));
        accumulation.add(makeTradeOperationNonNullableShiftAcc("10890907.41", NonNullableType.END, TradeOperationType.SELL));

        accumulation.add(makeTradeOperationNonNullableShiftAcc("3957.48", NonNullableType.START, TradeOperationType.SELL_RETURN));
        accumulation.add(makeTradeOperationNonNullableShiftAcc("4065.48", NonNullableType.END, TradeOperationType.SELL_RETURN));

        accumulation.add(makeTradeOperationNonNullableShiftAcc("0", NonNullableType.START, TradeOperationType.BUY));
        accumulation.add(makeTradeOperationNonNullableShiftAcc("0", NonNullableType.END, TradeOperationType.BUY));

        accumulation.add(makeTradeOperationNonNullableShiftAcc("0", NonNullableType.START, TradeOperationType.BUY_RETURN));
        accumulation.add(makeTradeOperationNonNullableShiftAcc("0", NonNullableType.END, TradeOperationType.BUY_RETURN));

        accumulation.add(makeTradeOperationSectionTotalShiftAcc("0", TradeOperationType.SELL));
        accumulation.add(makeTradeOperationSectionTotalShiftAcc("0", TradeOperationType.BUY));
        accumulation.add(makeTradeOperationSectionTotalShiftAcc("108", TradeOperationType.SELL_RETURN));
        accumulation.add(makeTradeOperationSectionTotalShiftAcc("0", TradeOperationType.BUY_RETURN));

        accumulation.add(makeTradeOperationTaxShiftAcc("11.57", TradeOperationType.SELL_RETURN, 1, "108"));

        accumulation.add(makeCashSumShiftAccumulation("11.57"));

        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.DISCOUNT, TradeOperationType.SELL, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.DISCOUNT, TradeOperationType.BUY, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.DISCOUNT, TradeOperationType.SELL_RETURN, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.DISCOUNT, TradeOperationType.BUY_RETURN, 0));

        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.MARKUP, TradeOperationType.SELL, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.MARKUP, TradeOperationType.BUY, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.MARKUP, TradeOperationType.SELL_RETURN, 0));
        accumulation.add(makeTradeOperationModifierShiftAcc("0", ModifierType.MARKUP, TradeOperationType.BUY_RETURN, 0));

        accumulation.add(makeTradeOperationSectionShiftAcc("108", 1, TradeOperationType.SELL_RETURN, 2));

        accumulation.add(makeTradeOperationPaymentShiftAccumulation("58", PaymentType.CASH, TradeOperationType.SELL_RETURN));
        accumulation.add(makeTradeOperationPaymentShiftAccumulation("50", PaymentType.CARD, TradeOperationType.SELL_RETURN));

        accumulation.add(makeTradeOperationTotalShiftAcc("0", 0, TradeOperationType.SELL));
        accumulation.add(makeTradeOperationTotalShiftAcc("0", 0, TradeOperationType.BUY));
        accumulation.add(makeTradeOperationTotalShiftAcc("108", 2, TradeOperationType.SELL_RETURN));
        accumulation.add(makeTradeOperationTotalShiftAcc("0", 0, TradeOperationType.BUY_RETURN));


        /* Оплаты нал*/
        assertEquals(0L, accumulation.getCashSaleSum());

        /* Оплаты безнал*/
        assertEquals(0L, accumulation.getCashlessSaleSum());

        /* Возвраты нал */
        assertEquals(58_00L, accumulation.getCashReturnSum());

        /* Возвраты безнал */
        assertEquals(50_00L, accumulation.getCashlessReturnSum());

        /* Возвраты */
        assertEquals(108_00L, accumulation.getAllReturnSum());

        /* Продажи*/
        assertEquals(0L, accumulation.getAllSaleSum());
    }

    private RevenueShiftAccumulation makeRevenueShiftAcc(String sum) {
        final RevenueShiftAccumulation result = new RevenueShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        return result;
    }

    private MoneyPlacementShiftAccumulation makeMoneyPlacementShiftAcc(String sum, long count, MoneyPlacementType type) {
        final MoneyPlacementShiftAccumulation result = new MoneyPlacementShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setCount(count);
        result.setType(type);
        return result;
    }

    private TradeOperationNonNullableShiftAccumulation makeTradeOperationNonNullableShiftAcc(String sum, NonNullableType type,
                                                                                             TradeOperationType operationType) {
        final TradeOperationNonNullableShiftAccumulation result = new TradeOperationNonNullableShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setType(type);
        return result;
    }

    private TradeOperationModifierShiftAccumulation makeTradeOperationModifierShiftAcc(String sum, ModifierType type,
                                                                                       TradeOperationType operationType, long count) {
        final TradeOperationModifierShiftAccumulation result = new TradeOperationModifierShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setType(type);
        result.setCount(count);
        return result;
    }

    private TradeOperationSectionTotalShiftAccumulation makeTradeOperationSectionTotalShiftAcc(String sum, TradeOperationType operationType) {
        final TradeOperationSectionTotalShiftAccumulation result = new TradeOperationSectionTotalShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        return result;
    }

    private TradeOperationTaxShiftAccumulation makeTradeOperationTaxShiftAcc(String sum,TradeOperationType operationType,int groupNumber,
                                                                             String turnover) {
        final TradeOperationTaxShiftAccumulation result = new TradeOperationTaxShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setTaxGroupNumber(groupNumber);
        result.setTurnover(new BigDecimal(turnover));
        return result;
    }

    private CashSumShiftAccumulation makeCashSumShiftAccumulation(String sum) {
        final CashSumShiftAccumulation result = new CashSumShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        return result;
    }

    private TradeOperationPaymentShiftAccumulation makeTradeOperationPaymentShiftAccumulation(String sum, PaymentType type,
                                                                                              TradeOperationType operationType) {
        final TradeOperationPaymentShiftAccumulation result = new TradeOperationPaymentShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setType(type);
        return result;
    }

    private TradeOperationSectionShiftAccumulation makeTradeOperationSectionShiftAcc(String sum, int sectionNumber,
                                                                                     TradeOperationType operationType, long count) {
        final TradeOperationSectionShiftAccumulation result = new TradeOperationSectionShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setCount(count);
        result.setSectionNumber(sectionNumber);
        return result;
    }

    private TradeOperationTotalShiftAccumulation makeTradeOperationTotalShiftAcc(String sum, long count, TradeOperationType operationType) {
        final TradeOperationTotalShiftAccumulation result = new TradeOperationTotalShiftAccumulation();
        result.setSum(new BigDecimal(sum));
        result.setTradeOperationType(operationType);
        result.setCount(count);
        return result;
    }

}