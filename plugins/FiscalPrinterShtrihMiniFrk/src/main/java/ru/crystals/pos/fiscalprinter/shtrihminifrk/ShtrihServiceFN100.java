package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFiscalizationResult;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihItemCode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihOperation;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalV2Ex;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.DocumentUtils;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Реализация для устройств с ФН через систему команд
 */
@PrototypedComponent
public class ShtrihServiceFN100 extends BaseShtrihServiceFN implements Configurable<BaseShtrihConfig> {

    @Override
    public Class<BaseShtrihConfig> getConfigClass() {
        return BaseShtrihConfig.class;
    }

    @Override
    public void setConfig(BaseShtrihConfig config) {
        super.setConfig(config);
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        LOG.debug("entering printCheck(Check)");

        // 1. как обычно записываем кассира и номер отдела в фискальник перед фискализацией документа:
        setCashierName(getCashierName(check.getCashier()));
        setDepartNumber(check.getDepart().intValue());

        // 2. печать заголовка
        printSubHeader(check.getCashNumber(), check.getCheckNumber(), check.getShiftId(), check.getCashier());
        printLine(new FontLine(getSeparator(), Font.NORMAL));

        // 3. Печать товаров со скидками и налогами, если они есть
        for (Goods product : check.getGoods()) {
            // 3.1. регистрация позиции
            String positionName = formatLeft(product.getItem(), ITEM_PLACE) + formatLeft(product.getName(), getMaxCharRow() - ITEM_PLACE);
            positionName = StringUtils.left(positionName, getMaxCharRow());
            ShtrihOperation operation = makeOperation(product);
            operation.setStringForPrinting(positionName);

            ShtrihDiscount shtrihDiscount = makeDiscount(product);
            if (shtrihDiscount.getSum() != 0) {
                // если округление не совпало точно, то укажем сумму
                operation.setSumm(product.getEndPositionPrice());
            }

            if (CheckType.SALE.equals(check.getType())) {
                // тип операции - приход
                operation.setCheckType((byte) 0x01);
            } else if (CheckType.RETURN.equals(check.getType())) {
                // тип операции - возврат прихода
                operation.setCheckType((byte) 0x02);
            }
            try {
                getConnector().regOperation(operation);
                if (StringUtils.isNotEmpty(product.getExcise())) {
                    putCodingMark(product);
                }
            } catch (IOException | PortAdapterException | ShtrihException e) {
                logExceptionAndThrowIt("Operation", e);
            }
        } // for product
        printLine(new FontLine(getSeparator(), Font.NORMAL));

        long checkDiscValueTotal;
        if (getDiscValue(check.getDiscs()) != 0) {
            checkDiscValueTotal = getDiscValue(check.getDiscs());
            if (!isOFDDevice()) {
                ShtrihDiscount discount = new ShtrihDiscount(checkDiscValueTotal, ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TITLE"));
                regDiscount(discount);
            }
        }

        checkDiscValueTotal = getDiscValue(check.getDiscs());
        if (check.getGoods() != null) {
            for (Goods goods : check.getGoods()) {
                checkDiscValueTotal += getDiscValue(goods.getDiscs());
            }
        }

        // Печать "ИТОГО СКИДКА НА ЧЕК СОСТАВИЛА"
        if ((checkDiscValueTotal == 0) && (check.getDiscountValueTotal() != null)) {
            checkDiscValueTotal = check.getDiscountValueTotal();
        }
        if (checkDiscValueTotal > 0) {
            String line;
            line = DocumentUtils.makeHeader(ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TEXT"), getMaxCharRow(), SPACE_SYMBOL);
            printLine(new FontLine(line, Font.NORMAL));

            line = ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT") +
                    formatRight(BigDecimal.valueOf(checkDiscValueTotal / 100.0).setScale(2, RoundingMode.HALF_UP).toString(),
                            getMaxCharRow() / 2 - ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT").length());
            printLine(new FontLine(line, Font.DOUBLEWIDTH));
        }

        // Пропуск строки
        printLine(new FontLine(SPACE, Font.NORMAL));

        if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
            printCheckBarcode(check);
        }

        if (check.getClientRequisites() != null) {
            try {
                getConnector().setClientData(check.getClientRequisites());
            } catch (IOException | PortAdapterException | ShtrihException e) {
                LOG.error("Error sent client data: {}", e.getMessage());
            }
        }

        if (check.isAnnul()) {
            annulDocument();
        } else {
            Map<Integer, Long> payments = getPayments(check);

            // закрыть/зарегистрировать чек
            ShtrihReceiptTotalV2Ex receiptTotal = makeReceiptTotalV2(check, payments);

            try {
                getConnector().closeReceiptV2Ex(receiptTotal);
            } catch (ShtrihException | IOException | PortAdapterException e) {
                logExceptionAndThrowIt("closeReceiptV2Ex(ShtrihReceiptTotal)", e);
            }

            // Осталось распечатать шапку следующего документа и отрезать чековую ленту
            closeNonFiscalDocument(check);
        }
        LOG.debug("leaving printCheck(Check)");
    }

    protected ShtrihOperation makeOperation(Goods goods) throws FiscalPrinterException {
        // добавлены слеши к наименованию позиции для отмены печати позиций внутренним шаблоном ФР, залипуха штриха.
        String namePos = "//" + goods.getName();
        int maxCharRow  = getMaxCharRow();
        ShtrihOperation shtrihOperation = new ShtrihOperation(namePos.length() < maxCharRow ? namePos : namePos.substring(0, maxCharRow),
                goods.getEndPricePerUnit(), goods.getQuant(), goods.getDepartNumber().byteValue());

        shtrihOperation.setQuantity(goods.getQuant() * 1000); //драйвер штриха принимает по 1 шт
        shtrihOperation.setTaxOne(getTaxId(goods.getTax()));
        // предмет расчета - товар
        shtrihOperation.setPaymentItemSing(goods.getCalculationSubject() != null ? goods.getCalculationSubject().byteValue() : (byte) 0x01);
        // признак способа расчета
        shtrihOperation.setPaymentTypeSing(goods.getCalculationMethod().byteValue());
        return shtrihOperation;
    }

    protected void putGoods(Check check) throws PortAdapterException, ShtrihException, IOException, FiscalPrinterException {
        for (Goods pos : check.getGoods()) {
            ShtrihOperation operation = makeOperation(pos);
            if (CheckType.SALE.equals(check.getType())) {
                // тип операции - приход
                operation.setCheckType((byte) 0x01);
            } else if (CheckType.RETURN.equals(check.getType())) {
                // тип операции - возврат прихода
                operation.setCheckType((byte) 0x02);
            }
            ShtrihDiscount shtrihDiscount = makeDiscount(pos);
            if (shtrihDiscount.getSum() != 0) {
                // если округление не совпало точно, то укажем сумму
                operation.setSumm(pos.getEndPositionPrice());
            }
            getConnector().regOperation(operation);

            if (StringUtils.isNotEmpty(pos.getExcise())) {
                putCodingMark(pos);
            }
        }
    }


    /**
     * КТН передается в виде xxxxYYYYserial
     * где x - код товара, y - GTIN переведенный в hex, SERIAL - серийный номер
     *
     * @param good товарная позиция
     * @return ShtrihItemCode с данными КТН
     */
    protected ShtrihItemCode getCodeMark(Goods good) {
        byte[] marking = ShtrihUtils.hexStringDataToByteArray(good.getMarkCode());

        String hexStrGtin = good.getMarkEanAsHex();
        byte[] hexGtin = ShtrihUtils.hexStringDataToByteArray(hexStrGtin);

        String serial = good.getSerialNumber();
        if (good.getMarkMrp() != null) {
            serial += good.getMarkMrp();
        }
        byte[] serialData = serial.getBytes(Charset.forName("cp866"));

        return new ShtrihItemCode(marking, hexGtin, serialData);
    }

    /* Возвращает правильный регистрационный номер
     * Изменение регистрационного номера при открытой смене приводит к ее аварийному закрытию
     * TODO: использовать метод когда клиенты будут подготовлены к такому обновлению.
     */
    public String getRegNumCorrect() throws FiscalPrinterException {
        LOG.debug("entering getRegNum()");
        try {
            if (regDataStorage.isRegistrationNumEmpty()) {
                ShtrihFiscalizationResult fiscalizationResult = getConnector().getLastFiscalizationResult();
                regDataStorage.setRegistrationNum(fiscalizationResult.getRegNum());
            }
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("getRegNum()", e);
        }
        LOG.debug("leaving getRegNum(). The result is: {}", regDataStorage.getRegistrationNum());
        return regDataStorage.getRegistrationNum();
    }

}
