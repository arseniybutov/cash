package ru.crystals.pos.fiscalprinter.pirit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeEntity;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeException;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.PiritStatus;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritErrorMsg;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.utils.time.DateConverters;

import java.util.Date;
import java.util.List;

/**
 * Created by Tatarinov Eduard on 10.01.17.
 */
@PrototypedComponent
public class PiritRBSKNO extends PiritRB {

    private static final Logger log = LoggerFactory.getLogger(PiritRBSKNO.class);

    private Long fwType;

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            ShiftCounters sc = super.getShiftCounters();
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_SUMS_BY_TYPE);
            sc.setSumReturn(sc.getSumReturn() + dp.getDoubleMoneyToLongValue(1));//добавляем сумму аннулирования

            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);
            sc.setCountReturn(sc.getCountReturn() + dp.getLongValue(3));//добавляем счетчик аннулирования
            return sc;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        try {
            //сбрасываем признак печати аппаратного Z отчета если предыдущая смена была закрыта при сверке во время перезагрузки
            emulator.setStartZ(false);
            pc.sendRequest(PiritCommand.OPEN_SHIFT_IN_FN);
            return pa.getShiftNumber();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("Open shift error!", e);
        }
    }

    @Override
    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check.isFiscalCopy()) {
            printCheckCopy(check);
        } else if (check.isCopy()) {
            printCopyCheckByTemplate(sectionList, check);
        } else {
            printCheck(check);
            if (check.getType() == CheckType.SALE && !check.isAnnul()) {
                emulator.incKPKAndGet();
            }
        }
        for (DocumentSection documentSection : sectionList) {
            if (documentSection.getName().equalsIgnoreCase("bonuses") && CollectionUtils.isNotEmpty(documentSection.getContent())) {
                printBonusesServiceDocument(check, documentSection);
            }
        }
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        try {
            final PiritStatus piritStatus = getPiritStatus();
            if (piritStatus.needToStartWork()) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("WARN_NEED_RESTART"), PiritErrorMsg.getErrorType());
            }
            if (piritStatus.isDocOpened()) {
                annulCheck();
            }

            DataPacket dp = new DataPacket();

            if (check.getType() == CheckType.SALE) {
                dp.putLongValue(PIRIT_CHECK_SALE);
            } else if (check.getType() == CheckType.RETURN) {
                dp.putLongValue(PIRIT_CHECK_RETURN);
            }

            dp.putLongValue(getFiscalDocDepart(check));

            dp.putStringValue(getCashierName(check.getCashier()));
            dp.putLongValue(check.getCheckNumber());

            pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);

            this.putGoods(check.getGoods(), false);

            pc.sendRequest(PiritCommand.SUBTOTAL, false);

            this.putDiscounts(check.getDiscs(), false);
            this.putMargin(check.getMargins(), false);

            this.putPayments(check.getPayments(), false);

            if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                printDocumentNumberBarcode(check, true, false);
            }

            if (check.isAnnul()) {
                annulCheck();
            } else {
                closeCheck(check);
            }
        } catch (FiscalPrinterException fex) {
            throw fex;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print check", e);
        }
    }

    @Override
    public boolean printCheckCopy(Check check) throws FiscalPrinterException {
        printCopyDocumentInner(check.getFiscalDocId());
        return true;
    }

    @Override
    public void printMoneyDocumentCopy(Money money) throws FiscalPrinterException {
        printCopyDocumentInner(money.getFiscalDocId());
    }

    private void printCopyDocumentInner(long documentNumber) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(documentNumber);
            pc.sendRequest(PiritCommand.PRINT_COPY_DOCUMENT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print document copy", e);
        }
    }

    @Override
    protected String makeItem(Goods goods) {
        String item = BarcodeType.getByGoods(goods).name() + goods.getBarcode();
        item = (item.length() > ITEM_MAX_LENGTH) ? item.substring(0, ITEM_MAX_LENGTH) : item;
        return item;
    }

    @Override
    public boolean isOFDDevice() {
        return true;
    }  // СКНО устройство

    @Override
    public void printSKNOStatus() throws FiscalPrinterException {
        pc.sendRequest(PiritCommand.PRINT_SKNO, false);
        long timer = System.currentTimeMillis();

        StatusFP status = new StatusFP();
        while (timer + 3000 > System.currentTimeMillis()) {
            try {
                DataPacket dp = pc.sendRequest(PiritCommand.GET_PRINTER_STATE);
                status.setLongStatus(dp.getLongValue(0));
                Thread.sleep(500);
                break;
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        log.info("");
    }

    @Override
    public void printLastZCopy() throws FiscalPrinterException {
        pc.sendRequest(PiritCommand.PRINT_LAST_Z_REPORT);
    }

    @Override
    public synchronized void printSKNOAnnulCheck(Check check) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(check.getSuperCheck().getFiscalDocId());
            double[] payments = new double[16];
            for (Payment pay : check.getPayments()) {
                payments[(int) pay.getIndexPayment()] += CurrencyUtil.convertMoney(pay.getSum()).doubleValue();
            }
            for (double p : payments) {
                dp.putDoubleValue(p);
            }
            dp.putStringValue(getCashierName(check.getCashier()));

            if (shouldPassDocNumberOnCancel()) {
                // (Дробное число) сумма скидки по документу
                dp.putDoubleValue(0.0);
                // (Дробное число) сумма надбавки,
                dp.putDoubleValue(0.0);
                // (Целое число) Номер документа.
                dp.putLongValue(check.getCheckNumber());
            }
            pc.sendRequest(PiritCommand.ANNUL_DOCUMENT, dp, false);
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print annul check", e);
        }
    }

    /**
     * Начиная с определенной версии/типа прошивки Пирит РБ СКНО требует передачи номера документа
     * при включенной нумерации документов внешней системой (у нас это включено)
     */
    private boolean shouldPassDocNumberOnCancel() throws FiscalPrinterException {
        return getFirmwareLong() >= 266 && getFirmwareTypeRB() >= 35;
    }

    /**
     * Возвращает тип прошивки фискального регистратора (дополнительная информация Пирит РБ)
     */
    private long getFirmwareTypeRB() throws FiscalPrinterException {
        if (fwType == null) {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_FW_TYPE);
            try {
                fwType = dp.getLongValue(1);
                log.debug("FW type {}", fwType);
            } catch (Exception e) {
                throw new FiscalPrinterException("Failed to get FW type", e);
            }
        }
        return fwType;
    }

    @Override
    public void printFiscalReportByDate(Date startDate, Date endDate, String password, boolean isFullReport) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(isFullReport ? 1L : 0L);
            dp.putDateValue(DateConverters.toLocalDate(startDate));
            dp.putDateValue(DateConverters.toLocalDate(endDate));
            dp.putStringValue(password);
            pc.sendRequest(PiritCommand.PRINT_FISCAL_REPORT_BY_DATE, dp, true);
        } catch (FiscalPrinterException fex) {
            throw fex;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print report BEP", e);
        }
    }

    @Override
    public String getDocUidFromControlTape(FiscalDocument document) throws FiscalPrinterException {
        ControlTapeEntity controlTapeEntity = getDocumentInfoFromControlTape(document.getFiscalDocId());
        long documentNumber;
        if (document instanceof Check) {
            documentNumber = ((Check) document).getCheckNumber();
        } else if (document instanceof Money) {
            documentNumber = ((Money) document).getCheckNumber();
        } else {
            throw new ControlTapeException(ResBundleFiscalPrinter.getString("DOCUMENT_NOT_FOUND"), CashErrorType.NOT_CRITICAL_ERROR);
        }
        if (controlTapeEntity.getNumberDocument() != null && documentNumber != controlTapeEntity.getNumberDocument().longValue()) {
            throw new ControlTapeException(ResBundleFiscalPrinter.getString("DOCUMENT_NOT_FOUND"), CashErrorType.NOT_CRITICAL_ERROR);
        }
        return controlTapeEntity.getSknoUi();
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_RECEIPT_DATA_LAST);
            FiscalDocumentData result = new FiscalDocumentData();
            result.setType(FiscalDocumentType.getTypeByCode(dp.getLongValue(1)));
            // индекс 4 - это именно тот номер документа, который мы используем в качестве ФД для чеков
            result.setNumFD(dp.getLongValue(4));
            result.setSum(dp.getDoubleMoneyToLongValue(5));
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Тип штрихкода
     */
    private enum BarcodeType {
        /**
         * не GTIN
         */
        A,
        /**
         * GTIN
         */
        B,
        /**
         * услуга
         */
        C;

        public static BarcodeType getByGoods(Goods goods) {
            BarcodeType result = A;
            if (goods.getGoodsFeature() != null && !goods.getGoodsFeature().trim().isEmpty()
                    && goods.getGoodsFeature().trim().equalsIgnoreCase("service")) {
                result = C;
            } else if (goods.getBarcodeType() != null && !goods.getBarcodeType().trim().isEmpty()
                    && goods.getBarcodeType().trim().equalsIgnoreCase("GTIN")) {
                result = B;
            }
            return result;
        }

    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        long numberFiscalDoc = super.getLastDocNum();
        long numberShiftReport = getLastShiftReportNum();
        return Math.max(numberFiscalDoc, numberShiftReport);
    }

    private long getLastShiftReportNum() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_LAST_REPORT);
            return dp.getLongValue(2);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(report.getCashier()));
            emulator.setStartZ(true);
            pc.sendRequest(PiritCommand.PRINT_Z_REPORT, dp);
            emulator.setStartZ(false);
            emulator.incKPKAndGet();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print Z-report", e);
        }
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        final int maxCharRow = super.getMaxCharRow(font, extendedFont);
        // Пирит РБ печатает решетки в начале и конце произвольных строк на фискальных документах, поэтому уменьшаем максимальную длину на два символа
        return maxCharRow - 2;
    }

    @Override
    protected int getMaxCharRow(Text text) {
        final int maxCharRow = super.getMaxCharRow(text);
        // Пирит РБ печатает решетки в начале и конце произвольных строк на фискальных документах, поэтому уменьшаем максимальную длину на два символа
        return maxCharRow - 2;
    }
}
