package ru.crystals.pos.fiscalprinter.nfd.utils;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.AddCommodity;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber.NDS_0_GROUP;
import static ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber.NDS_12_GROUP;
import static ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber.NDS_8_GROUP;

public class UtilsNFD {

    private static final String CBS_TIME_DELIMITER = "T";

    /**
     * Возращает из строки формата "2000-01-01T20:15:00" подстроку с временем
     *
     * @param isoTime строка в формате "2000-01-01T20:15:00"
     * @return строка с временем или исходная строка если не найден разделитель
     */
    public static String getTimeString(String isoTime) {
        int timePos = isoTime.indexOf(CBS_TIME_DELIMITER);
        return timePos > 0 ? isoTime.substring(timePos + 1) : isoTime;
    }

    public static AddCommodity convertGoodsToCommodity(Goods good, boolean useRounding) {
        AddCommodity addCommodity = new AddCommodity();
        addCommodity.setName(good.getName());
        Set<Integer> taxGroupNumbers = new HashSet<>();
        final float positionNdsValue = good.getTax();
        if (Float.compare(positionNdsValue, 8.0f) == 0) {
            taxGroupNumbers.add(NDS_8_GROUP.getValue());
        } else if (Float.compare(positionNdsValue, 0.0f) == 0 || Float.compare(positionNdsValue, -1.0f) == 0) {
            taxGroupNumbers.add(NDS_0_GROUP.getValue());
        } else {
            taxGroupNumbers.add(NDS_12_GROUP.getValue());
        }
        setPositionSum(addCommodity, good, useRounding, taxGroupNumbers);

        addCommodity.setTaxGroupNumbers(taxGroupNumbers);
        // в кассе нет секций
        addCommodity.setSectionNumber(1);

        if (StringUtils.isNumeric(good.getItem())) {
            try {
                addCommodity.setCode(Long.parseLong(good.getItem()));
            } catch (NumberFormatException ignore) {
                // код вообще не обязателен, если есть наименование
            }
        }
        addCommodity.setExciseStamp(good.getExcise());
        return addCommodity;
    }

    private static void setPositionSum(AddCommodity addCommodity, Goods good, boolean useRounding, Set<Integer> taxGroupNumbers) {
        final BigDecimal quantity = BigDecimalConverter.convertQuantity(good.getQuant());
        addCommodity.setQuantity(quantity);
        addCommodity.setPrice(BigDecimalConverter.convertMoney(good.getEndPricePerUnit()));
        if (!useRounding) {
            return;
        }
        long dif = good.getEndPositionPrice() - Math.round(((double) (good.getEndPricePerUnit() * good.getQuant()) / 1000));
        if (dif != 0) {
            final Modifier modifier = new Modifier();
            modifier.setName("Коррекция округления");
            modifier.setSum(BigDecimalConverter.convertMoney(Math.abs(dif)));
            modifier.setTaxGroupNumbers(taxGroupNumbers);
            if (dif > 0) {
                modifier.setType(ModifierType.MARKUP);
            } else {
                modifier.setType(ModifierType.DISCOUNT);
            }
            addCommodity.setModifier(modifier);
        }
    }

    public static PaymentNFD convertPayment(Payment payment) {
        final BigDecimal sum = BigDecimalConverter.convertMoney(payment.getSum());
        switch ((int) payment.getIndexPaymentFDD100()) {
            case 0:
                return new PaymentNFD(PaymentType.CASH, sum);
            case 1:
                return new PaymentNFD(PaymentType.CARD, sum);
            case 13:
                return new PaymentNFD(PaymentType.CREDIT, sum);
            default:
                throw new IllegalArgumentException("Unsupported payment " + payment);
        }
    }

    public static TradeOperationType convertCheckType(CheckType checkType) throws FiscalPrinterException {
        switch (checkType) {
            case SALE:
                return TradeOperationType.SELL;
            case RETURN:
                return TradeOperationType.SELL_RETURN;
            default:
                throw new FiscalPrinterException("Unknown check type : " + checkType);
        }
    }

    public static Set<PaymentNFD> convertPayments(List<Payment> payments) {
        if (payments.size() == 1) {
            return Collections.singleton(UtilsNFD.convertPayment(payments.get(0)));
        }
        return payments.stream()
                .map(UtilsNFD::convertPayment)
                .collect(Collectors.groupingBy(PaymentNFD::getPaymentType,
                        Collectors.reducing(PaymentNFD::add)))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
