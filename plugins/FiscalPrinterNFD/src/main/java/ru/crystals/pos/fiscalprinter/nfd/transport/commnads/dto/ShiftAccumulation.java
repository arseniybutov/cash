package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.MoneyPlacementType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NonNullableType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.CashSumShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.CommonShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.MoneyPlacementShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.RevenueShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationNonNullableShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationPaymentShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation.TradeOperationTotalShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class ShiftAccumulation extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "ShiftAccumulation";

    private static final EnumSet<PaymentType> CASH_PAYMENT_TYPES = EnumSet.of(PaymentType.CASH);
    private static final EnumSet<PaymentType> CASHLESS_PAYMENT_TYPES = EnumSet.of(PaymentType.CARD, PaymentType.CREDIT);

    @JacksonXmlProperty(localName = "accumulations")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CommonShiftAccumulation> accumulations = new ArrayList<>();

    public List<CommonShiftAccumulation> getAccumulations() {
        return accumulations;
    }

    public void setAccumulations(List<CommonShiftAccumulation> accumulations) {
        this.accumulations = accumulations;
    }

    public boolean add(CommonShiftAccumulation commonShiftAccumulation) {
        return accumulations.add(commonShiftAccumulation);
    }

    /**
     * Сумма оплат наличными
     */
    public long getCashSaleSum() {
        return getTradeOperationSum(TradeOperationType.SELL, CASH_PAYMENT_TYPES);
    }

    /**
     * Сумма оплат безнал
     */
    public long getCashlessSaleSum() {
        return getTradeOperationSum(TradeOperationType.SELL, CASHLESS_PAYMENT_TYPES);
    }

    /**
     * Сумма возвратов нал
     */
    public long getCashReturnSum() {
        return getTradeOperationSum(TradeOperationType.SELL_RETURN, CASH_PAYMENT_TYPES);
    }

    /**
     * Сумма возвратов безнал
     */
    public long getCashlessReturnSum() {
        return getTradeOperationSum(TradeOperationType.SELL_RETURN, CASHLESS_PAYMENT_TYPES);
    }

    public long getTradeOperationSum(TradeOperationType operationType, EnumSet<PaymentType> paymentTypes) {
        return accumulations.stream()
                .filter(operationType(operationType, paymentTypes))
                .map(CommonShiftAccumulation::getSumForSetX)
                .mapToLong(BigDecimal::longValue)
                .sum();
    }

    private Predicate<CommonShiftAccumulation> operationType(TradeOperationType operationType, EnumSet<PaymentType> paymentTypes) {
        return accumulation -> accumulation instanceof TradeOperationPaymentShiftAccumulation
                && Objects.equals(((TradeOperationPaymentShiftAccumulation) accumulation).getTradeOperationType(), operationType)
                && (paymentTypes.isEmpty() || paymentTypes.contains(((TradeOperationPaymentShiftAccumulation) accumulation).getType()));
    }

    /**
     * Наличные на кассе
     */
    public long getCashSumShiftAccumulation() {
        return accumulations.stream()
                .filter(CashSumShiftAccumulation.class::isInstance)
                .findFirst()
                .orElse(new CashSumShiftAccumulation()).getSumForSetX().longValue();
    }

    /**
     * Доход
     */
    public long getRevenueShiftAccumulation() {
        return accumulations.stream()
                .filter(RevenueShiftAccumulation.class::isInstance)
                .findFirst()
                .orElse(new RevenueShiftAccumulation()).getSumForSetX().longValue();
    }

    /**
     * Сумма продаж
     */
    public long getAllSaleSum() {
        TradeOperationNonNullableShiftAccumulation sumSellStart = (TradeOperationNonNullableShiftAccumulation) accumulations.stream()
                .filter(TradeOperationNonNullableShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL))
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getType().equals(NonNullableType.START))
                .findFirst()
                .orElse(new TradeOperationNonNullableShiftAccumulation());

        TradeOperationNonNullableShiftAccumulation sumSellEnd = (TradeOperationNonNullableShiftAccumulation) accumulations.stream()
                .filter(TradeOperationNonNullableShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL))
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getType().equals(NonNullableType.END))
                .findFirst()
                .orElse(new TradeOperationNonNullableShiftAccumulation());

        return sumSellEnd.getSumForSetX().longValue() - sumSellStart.getSumForSetX().longValue();
    }

    /**
     * Сумма возвратов
     */
    public long getAllReturnSum() {
        TradeOperationNonNullableShiftAccumulation sumReturnSellStart = (TradeOperationNonNullableShiftAccumulation) accumulations.stream()
                .filter(TradeOperationNonNullableShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL_RETURN))
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getType().equals(NonNullableType.START))
                .findFirst()
                .orElse(new TradeOperationNonNullableShiftAccumulation());

        TradeOperationNonNullableShiftAccumulation sumReturnSellEnd = (TradeOperationNonNullableShiftAccumulation) accumulations.stream()
                .filter(TradeOperationNonNullableShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL_RETURN))
                .filter(operation -> ((TradeOperationNonNullableShiftAccumulation) operation).getType().equals(NonNullableType.END))
                .findFirst()
                .orElse(new TradeOperationNonNullableShiftAccumulation());

        return sumReturnSellEnd.getSumForSetX().longValue() - sumReturnSellStart.getSumForSetX().longValue();
    }

    /**
     * Количество продаж в смене
     */
    public long getSellCountShiftAccumulation() {
        return accumulations.stream()
                .filter(TradeOperationTotalShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationTotalShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL))
                .map(obj -> (TradeOperationTotalShiftAccumulation) obj)
                .findFirst()
                .orElse(new TradeOperationTotalShiftAccumulation()).getCount();
    }

    /**
     * Количество возвратов в смене
     */
    public long getReturnCountShiftAccumulation() {
        return accumulations.stream()
                .filter(TradeOperationTotalShiftAccumulation.class::isInstance)
                .filter(operation -> ((TradeOperationTotalShiftAccumulation) operation).getTradeOperationType().equals(TradeOperationType.SELL_RETURN))
                .map(obj -> (TradeOperationTotalShiftAccumulation) obj)
                .findFirst()
                .orElse(new TradeOperationTotalShiftAccumulation()).getCount();
    }

    /**
     * Количество внесений в смене
     */
    public long getDepositCountShiftAccumulation() {
        return accumulations.stream()
                .filter(MoneyPlacementShiftAccumulation.class::isInstance)
                .filter(operation -> ((MoneyPlacementShiftAccumulation) operation).getType().equals(MoneyPlacementType.DEPOSIT))
                .map(obj -> (MoneyPlacementShiftAccumulation) obj)
                .findFirst()
                .orElse(new MoneyPlacementShiftAccumulation()).getCount();
    }


    /**
     * Количество изьятий в смене
     */
    public long getWithdrawalCountShiftAccumulation() {
        return accumulations.stream()
                .filter(MoneyPlacementShiftAccumulation.class::isInstance)
                .filter(operation -> ((MoneyPlacementShiftAccumulation) operation).getType().equals(MoneyPlacementType.WITHDRAWAL))
                .map(obj -> (MoneyPlacementShiftAccumulation) obj)
                .findFirst()
                .orElse(new MoneyPlacementShiftAccumulation()).getCount();
    }


    /**
     * Сумма внесений в смене
     */
    public long getDepositSumShiftAccumulation() {
        return accumulations.stream()
                .filter(MoneyPlacementShiftAccumulation.class::isInstance)
                .filter(operation -> ((MoneyPlacementShiftAccumulation) operation).getType().equals(MoneyPlacementType.DEPOSIT))
                .findFirst()
                .orElse(new MoneyPlacementShiftAccumulation()).getSumForSetX().longValue();
    }

    /**
     * Сумма изьятий в смене
     */
    public long getWithdrawalSumShiftAccumulation() {
        return accumulations.stream()
                .filter(MoneyPlacementShiftAccumulation.class::isInstance)
                .filter(operation -> ((MoneyPlacementShiftAccumulation) operation).getType().equals(MoneyPlacementType.WITHDRAWAL))
                .findFirst()
                .orElse(new MoneyPlacementShiftAccumulation()).getSumForSetX().longValue();
    }


    @Override
    public String toString() {
        return "ShiftAccumulation{" +
                "accumulations=" + accumulations +
                '}';
    }
}