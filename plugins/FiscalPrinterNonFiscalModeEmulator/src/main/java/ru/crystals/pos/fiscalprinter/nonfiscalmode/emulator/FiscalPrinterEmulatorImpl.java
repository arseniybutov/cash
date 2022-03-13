package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.CorrectionReceiptPaymentsEntity;
import ru.crystals.pos.check.CorrectionReceiptTaxesEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.correction.CorrectionReceiptType;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeWorker;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeWorkerMultiShift;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static ru.crystals.pos.templateengine.util.StringAlignFormatter.alignCenter;
import static ru.crystals.pos.templateengine.util.StringAlignFormatter.alignLeft;
import static ru.crystals.pos.templateengine.util.StringAlignFormatter.alignRight;

/**
 * Реализация эмулятора ФР для основного продукта.
 */
@PrototypedComponent
public class FiscalPrinterEmulatorImpl extends AbstractFiscalPrinterEmulator {

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return RegulatoryFeatures.defaultFeaturesTemplate()
                .canReturnFullLastDocInfo(config.isCanReturnFullLastDocInfo())
                .canMakeArbitraryRefund(config.isCanMakeArbitraryRefund())
                .build();
    }

    protected ControlTapeWorker getControlTapeWorker() throws FiscalPrinterException {
        return new ControlTapeWorkerMultiShift(fiscalData.getShiftNum(), innAddon);
    }

    protected ControlTapeDocumentTemplateEmulator getControlTapeTemplateEmulator() throws IOException {
        return ControlTapeDocumentTemplateEmulator.getInstance();
    }

    @Override
    public Date getEKLZActivizationDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return c.getTime();
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        FiscalDocumentData result = fiscalData.getLastFiscalDocumentData();
        if (result != null) {
            result.setOperationDate(getLastFiscalOperationDate());
            result.setFnNumber(getEklzNum());
            result.setQrCode(PluginUtils.buildQRCode(result, null));
        }
        return result;
    }

    /**
     * Печать чека коррекции
     *
     * @param correctionReceipt
     * @return номер ФД
     * @throws FiscalPrinterException
     */
    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws FiscalPrinterException {

        List<FontLine> doc = new ArrayList<>();
        int maxCharRow = 42;

        doc.add(new FontLine(alignCenter(getProperties().getShopName(), " ", maxCharRow), Font.NORMAL));
        doc.add(new FontLine(alignCenter(getProperties().getShopAddress(), " ", maxCharRow), Font.NORMAL));
        doc.add(new FontLine(alignCenter("ЧЕК КОРРЕКЦИИ (ПРИХОД)", " ", maxCharRow), Font.DOUBLEWIDTH));
        doc.add(new FontLine(alignLeft("КАССИР " + cashier.getLastnameFirstnameMiddleName(), " ", maxCharRow), Font.NORMAL));
        BigDecimal paymentSummary = new BigDecimal("0.0");

        for (CorrectionReceiptPaymentsEntity entity : correctionReceipt.getPayments()) {
            BigDecimal paymentSum = CurrencyUtil.convertMoney(entity.getPaymentSum());
            switch (entity.getCorrectionReceiptPaymentsEntityPK().getPaymentName()) {

                case CASH:
                    doc.add(new FontLine(alignRight("НАЛИЧНЫМИ              " + paymentSum, " ", maxCharRow), Font.NORMAL));
                    paymentSummary = paymentSummary.add(paymentSum);
                    break;
                case ELECTRON:
                    doc.add(new FontLine(alignRight("ЭЛЕКТРОННЫМ              " + paymentSum, " ", maxCharRow), Font.NORMAL));
                    paymentSummary = paymentSummary.add(paymentSum);
                    break;
                case PREPAYMENT:
                    doc.add(new FontLine(alignRight("ПРЕДВАРИТЕЛЬНАЯ ОПЛАТА (АВАНС)              " + paymentSum, " ", maxCharRow), Font.NORMAL));
                    paymentSummary = paymentSummary.add(paymentSum);
                    break;
                case POSTPAY:
                    doc.add(new FontLine(alignRight("ПОСЛЕДУЮЩАЯ ОПЛАТА (КРЕДИТ)              " + paymentSum, " ", maxCharRow), Font.NORMAL));
                    paymentSummary = paymentSummary.add(paymentSum);
                    break;
                case COUNTEROFFER:
                    doc.add(new FontLine(alignRight("ИНАЯ ФОРМА ОПЛАТЫ              " + paymentSum, " ", maxCharRow), Font.NORMAL));
                    paymentSummary = paymentSummary.add(paymentSum);
                    break;
                default:
                    break;
            }
        }

        doc.add(new FontLine(alignLeft("ИТОГО КОРРЕКЦИЯ " + paymentSummary, " ", maxCharRow), Font.NORMAL));
        CorrectionReceiptType correctionTypeField = correctionReceipt.getCorrectionType();
        String corrType = "";
        if (correctionTypeField.name().equalsIgnoreCase("INDEPENDENTLY")) {
            corrType = "самостоятельно";
        } else if (correctionTypeField.name().equalsIgnoreCase("ORDER")) {
            corrType = "по предписанию";
        }

        doc.add(new FontLine(alignLeft("ТИП КОРРЕКЦИИ " + corrType, " ", maxCharRow), Font.NORMAL));


        doc.add(new FontLine(alignLeft("ОСН. ДЛЯ КОРР. " + correctionReceipt.getReason(), " ", maxCharRow), Font.NORMAL));

        SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yy");
        String formattedDate1 = formatter1.format(correctionReceipt.getDateCreate());

        doc.add(new FontLine(alignLeft("" + formattedDate1 + "  " + correctionReceipt.getReasonDocNumber(), " ", maxCharRow), Font.NORMAL));

        for (CorrectionReceiptTaxesEntity entity : correctionReceipt.getTaxes()) {
            BigDecimal taxSum = CurrencyUtil.convertMoney(entity.getTaxSum());
            switch (entity.getCorrectionReceiptTaxesEntityPK().getTax()) {
                case TAX_20:
                    doc.add(new FontLine(alignLeft("СУММА НДС 20%                  " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                case TAX_10:
                    doc.add(new FontLine(alignLeft("СУММА НДС 10%                  " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                case TAX_0:
                    doc.add(new FontLine(alignLeft("СУММА НДС 0% " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                case TAX_NONDS:
                    doc.add(new FontLine(alignLeft("СУММА НДС нет НДС               " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                case TAX_20_120:
                    doc.add(new FontLine(alignLeft("СУММА НДС 20/120              " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                case TAX_10_110:
                    doc.add(new FontLine(alignLeft("СУММА НДС 10/110               " + taxSum, " ", maxCharRow), Font.NORMAL));
                    break;
                default:
                    //
            }
        }

        doc.add(new FontLine(alignLeft("ФН " + getFiscalPrinterData().getEklz(), " ", maxCharRow), Font.NORMAL));
        doc.add(new FontLine(alignLeft("РН ККТ " + getFiscalPrinterData().getRegNum().substring(0, 10) + " ИНН " + getFiscalPrinterData().getINN(), " ", maxCharRow),
                Font.NORMAL));
        doc.add(new FontLine(alignLeft("ЧЕК КОР. " + correctionReceipt.getNumber() + " ИТОГ " + paymentSummary + "   СНО ОСН", " ", maxCharRow), Font.NORMAL));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String formattedDate = formatter.format(correctionReceipt.getDateCreate());

        doc.add(new FontLine(alignLeft("" + formattedDate + " СМЕНА " + getFiscalPrinterData().getShiftNum(), " ", maxCharRow), Font.NORMAL));

        getFiscalPrinterData().incKPK();
        getFiscalPrinterData().incSPND();
        doc.add(new FontLine(alignLeft("ФД" + getFiscalPrinterData().getKPK() + " ФП 1156706415 ", " ", maxCharRow), Font.NORMAL));


        printLinesListInDoc(doc);
        closeDocument(null, null, cashier);

        return Optional.of(getLastKpk());
    }

    @Override
    public Optional<FiscalMarkValidationResult> validateMarkCode(PositionEntity position, MarkData markData, boolean isSale) {
        return Optional.of(fiscalData.getFiscalMarkValidationResult(markData.getRawMark()));
    }
}
