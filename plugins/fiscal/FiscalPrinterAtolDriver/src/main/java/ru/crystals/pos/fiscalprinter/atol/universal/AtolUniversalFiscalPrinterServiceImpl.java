package ru.crystals.pos.fiscalprinter.atol.universal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import ru.crystals.pos.CashException;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.AbstractFiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FfdVersion;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.atol.universal.connector.AtolUniversalConnector;
import ru.crystals.pos.fiscalprinter.atol.universal.json.OverallTotalsDTO;
import ru.crystals.pos.fiscalprinter.atol.universal.json.Receipts;
import ru.crystals.pos.fiscalprinter.atol.universal.json.ShiftTotalsDTO;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@PrototypedComponent
public class AtolUniversalFiscalPrinterServiceImpl extends AbstractFiscalPrinterPlugin implements Configurable<AtolUniversalConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(AtolUniversalFiscalPrinterServiceImpl.class);
    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .skipXReportOnExpiredShift(true)
            .skipMoneyDocumentOnExpiredShift(true)
            .build();

    private AtolUniversalConnector connector;
    private AtolUniversalConfig config;
    private Integer cachedMaxChar;
    private String cachedInn;
    private String cachedRegNum;

    @NonNull
    @Override
    public Class<AtolUniversalConfig> getConfigClass() {
        return AtolUniversalConfig.class;
    }

    @Override
    public void setConfig(@NonNull AtolUniversalConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws CashException {
        connector = new AtolUniversalConnector(config);
        connector.start();
    }

    @Override
    public void stop() {
        connector.stop();
    }

    @Override
    public String getRegNum() {
        if (cachedRegNum == null) {
            cachedRegNum = connector.getRegNum();
        }
        return cachedRegNum;
    }

    @Override
    public String getINN() {
        if (cachedInn == null) {
            cachedInn = connector.getINN();
        }
        return cachedInn;
    }

    @Override
    public String getEklzNum() {
        return connector.getEklzNum();
    }

    @Override
    public long getShiftNumber() {
        long shiftNum = connector.getShiftNumber();
        // для корректной работы сравнения смен: считается, что ФР возвращает номер смены следующего Z-отчета
        // см. ShiftSyncImpl
        return isShiftOpen() ? shiftNum : shiftNum + 1;
    }

    @Override
    public boolean isShiftOpen() {
        return connector.isShiftOpen();
    }

    @Override
    public long getLastKpk() {
        return connector.getLastDocNum();
    }

    @Override
    public long getCountCashIn() {
        return connector.getCountCashIn();
    }

    @Override
    public long getCountCashOut() {
        return connector.getCountCashOut();
    }

    @Override
    public long getCountAnnul() {
        return connector.getCountAnnul();
    }

    @Override
    public long getCashAmount() {
        return connector.getCashAmount();
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() {
        long lastKpk = getLastKpk();
        return connector.getLastFiscalDocumentData(lastKpk);
    }

    @Override
    public Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        OverallTotalsDTO.OverallTotals overallTotals = connector.getOverallTotals();
        return Optional.of(new IncrescentTotal(
                BigDecimalConverter.convertMoneyToLong(overallTotals.getReceipts().getSell().getSum()),
                BigDecimalConverter.convertMoneyToLong(overallTotals.getReceipts().getSellReturn().getSum()),
                BigDecimalConverter.convertMoneyToLong(overallTotals.getReceipts().getBuy().getSum()),
                BigDecimalConverter.convertMoneyToLong(overallTotals.getReceipts().getBuyReturn().getSum())));
    }

    /**
     * Атол также возвращает информацию по возврату расхода:
     * количество чеков возврата расхода - shiftTotals.getReceipts().getBuyReturn().getCount()
     * сумма возврата расхода - shiftTotals.getReceipts().getBuyReturn().getSum()
     * сумма возврата расхода по аналогии: shiftTotals.getReceipts().getBuyReturn().getPayments()
     */
    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        ShiftTotalsDTO.ShiftTotals shiftTotals = connector.getShiftTotals();

        ShiftCounters counters = new ShiftCounters();
        counters.setShiftNum(shiftTotals.getShiftNumber());
        counters.setSumCashEnd(BigDecimalConverter.convertMoneyToLong(shiftTotals.getCashDrawer().getSum()));

        counters.setCountSale(shiftTotals.getReceipts().getSell().getCount());
        counters.setCountReturn(shiftTotals.getReceipts().getSellReturn().getCount());
        counters.setCountExpenseReceipt(shiftTotals.getReceipts().getBuy().getCount());
        counters.setCountReturnExpenseReceipt(shiftTotals.getReceipts().getBuyReturn().getCount());

        counters.setSumSale(getSum(shiftTotals.getReceipts().getSell().getPayments()));
        counters.setSumReturn(getSum(shiftTotals.getReceipts().getSellReturn().getPayments()));
        counters.setSumExpenseReceipt(getSum(shiftTotals.getReceipts().getBuy().getPayments()));
        counters.setSumReturnExpenseReceipt(getSum(shiftTotals.getReceipts().getBuyReturn().getPayments()));

        counters.setCountCashIn(shiftTotals.getIncome().getCount());
        counters.setCountCashOut(shiftTotals.getOutcome().getCount());

        counters.setSumCashIn(BigDecimalConverter.convertMoneyToLong(shiftTotals.getIncome().getSum()));
        counters.setSumCashOut(BigDecimalConverter.convertMoneyToLong(shiftTotals.getOutcome().getSum()));

        // в атоле нельзя посчитать количество чеков по определенному типу оплаты, есть только сумма
        counters.setCountCashPurchase(0L);
        counters.setCountCashlessPurchase(0L);
        counters.setCountCashExpenseReceipt(0L);
        counters.setCountCashlessExpenseReceipt(0L);

        Receipts.ReceiptType.Payments sell = shiftTotals.getReceipts().getSell().getPayments();
        counters.setSumCashPurchase(BigDecimalConverter.convertMoneyToLong(sell.getCash()));
        counters.setSumCashlessPurchase(BigDecimalConverter.convertMoneyToLong(sell.getElectronically())
                + BigDecimalConverter.convertMoneyToLong(sell.getCredit())
                + BigDecimalConverter.convertMoneyToLong(sell.getPrepaid())
                + BigDecimalConverter.convertMoneyToLong(sell.getOther()));

        Receipts.ReceiptType.Payments sellReturn = shiftTotals.getReceipts().getSellReturn().getPayments();
        counters.setSumCashReturn(BigDecimalConverter.convertMoneyToLong(sellReturn.getCash()));
        counters.setSumCashlessReturn(BigDecimalConverter.convertMoneyToLong(sellReturn.getElectronically())
                + BigDecimalConverter.convertMoneyToLong(sellReturn.getCredit())
                + BigDecimalConverter.convertMoneyToLong(sellReturn.getPrepaid())
                + BigDecimalConverter.convertMoneyToLong(sellReturn.getOther()));

        Receipts.ReceiptType.Payments buy = shiftTotals.getReceipts().getBuy().getPayments();
        counters.setSumCashExpenseReceipt(BigDecimalConverter.convertMoneyToLong(buy.getCash()));
        counters.setSumCashlessExpenseReceipt(BigDecimalConverter.convertMoneyToLong(buy.getElectronically())
                + BigDecimalConverter.convertMoneyToLong(buy.getCredit())
                + BigDecimalConverter.convertMoneyToLong(buy.getPrepaid())
                + BigDecimalConverter.convertMoneyToLong(buy.getOther()));

        Receipts.ReceiptType.Payments buyReturn = shiftTotals.getReceipts().getBuyReturn().getPayments();
        counters.setSumCashExpenseReceipt(BigDecimalConverter.convertMoneyToLong(buyReturn.getCash()));
        counters.setSumCashlessExpenseReceipt(BigDecimalConverter.convertMoneyToLong(buyReturn.getElectronically())
                + BigDecimalConverter.convertMoneyToLong(buyReturn.getCredit())
                + BigDecimalConverter.convertMoneyToLong(buyReturn.getPrepaid())
                + BigDecimalConverter.convertMoneyToLong(buyReturn.getOther()));

        return counters;
    }

    // считаем сумму сами из-за расхождения сумм в ответах атола
    private static long getSum(Receipts.ReceiptType.Payments payments) {
        double sum = payments.getCash()
                + payments.getCredit()
                + payments.getElectronically()
                + payments.getOther()
                + payments.getPrepaid()
                + payments.getUserPaymentType5()
                + payments.getUserPaymentType6()
                + payments.getUserPaymentType7()
                + payments.getUserPaymentType8()
                + payments.getUserPaymentType9();
        return BigDecimalConverter.convertMoneyToLong(sum);
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        if (cachedMaxChar == null) {
            cachedMaxChar = connector.getMaxCharRow();
        }
        return cachedMaxChar;
    }

    @Override
    public String getFactoryNum() {
        return connector.getFactoryNum();
    }

    @Override
    public void setDate(Date date) {
        connector.setDate(date);
    }

    @Override
    public Date getDate() {
        return connector.getDate();
    }

    @Override
    public String getVerBios() {
        return connector.getVerBios();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        connector.openShift(cashier);
        return getShiftNumber();
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("INCOMPATIBLE_PRINT_DOCUMENT_METHOD_FOR_OFD"));
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            for (BankNote bankNote : money.getBankNotes()) {
                StringBuilder row = new StringBuilder();
                String str = bankNote.getValue() / 100 + "." + String.format("%02d", bankNote.getValue() % 100);
                row.append(String.format("%15.15s", str));
                connector.putText(new Text(row.toString()));
            }
        } else {
            for (BankNote bankNote : money.getBankNotes()) {
                long value = bankNote.getValue() * bankNote.getCount();
                StringBuilder row = new StringBuilder();
                String str = bankNote.getValue() / 100 + "." + String.format("%02d", bankNote.getValue() % 100) + "x" + bankNote.getCount();
                row.append(String.format("%25.25s", str));
                str = " =" + value / 100 + "." + String.format("%02d", value % 100);
                row.append(String.format("%15.15s", str));
                connector.putText(new Text(row.toString()));
            }

            if (money.getSumCoins() != null) {
                StringBuilder row = new StringBuilder(String.format("%25.25s",
                        ResBundleFiscalPrinterAtolUniversal.getString("PD_CASH_OUT_COINS")));
                String str = " =" + money.getSumCoins() / 100 + "." + String.format("%02d", money.getSumCoins() % 100);
                row.append(String.format("%15.15s", str));

                connector.putText(new Text(row.toString()));
            }
        }

        connector.fiscalMoneyDocument(money);
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        connector.openNonfiscalDocument();
        for (Row row : serviceDocument.getRows()) {
            if (row instanceof Text) {
                connector.putText((Text) row);
            } else if (row instanceof BarCode) {
                connector.putBarCode((BarCode) row);
                connector.putText(new Text(""));
            }
        }
        connector.closeNonfiscalDocument();
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        connector.printXReport(report.getCashier());
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        connector.printZReport(report.getCashier());
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        connector.openMoneyDrawer();
    }

    @Override
    public boolean isMoneyDrawerOpen() {
        return connector.isMoneyDrawerOpen();
    }

    @Override
    public StatusFP getStatus() {
        return connector.getStatus();
    }

    @Override
    public void printDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        printDocumentByTemplate(sectionList, document);
    }

    private void printDocumentByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        if (document instanceof Check) {
            Check check = (Check) document;
            if (check.isAnnul()) {
                printAnnulCheckByTemplate(sectionList, check);
            } else if (check instanceof FullCheckCopy || check.isCopy()) {
                printServiceByTemplate(sectionList);
            } else {
                printCheckByTemplate(sectionList, check);
            }
        } else if (document instanceof Report) {
            printReportByTemplate(sectionList, (Report) document);
        } else if (document instanceof Money) {
            printMoneyByTemplate(sectionList, (Money) document);
        } else {
            printServiceByTemplate(sectionList);
        }
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals(SECTION_HEADER)) {
                    connector.openDocument(check);
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals(SECTION_LOGO)) {
                    connector.printLogo();
                } else if (sectionName.equals(SECTION_POSITION) || SECTION_POSITION_WITH_GOOD_SETS.equals(sectionName)) {
                    try {
                        connector.putGoodsOnAnnulationCancel(check.getGoods());
                    } catch (Exception e) {
                        LOG.warn("Error on put positions (annulation)", e);
                    }
                } else if (sectionName.equals(SECTION_PAYMENT)) {
                    printLinesListInDoc(section.getContent());
                    try {
                        connector.putPayments(check.getPayments());
                    } catch (Exception e) {
                        LOG.warn("Error on put payments (annulation)", e);
                    }
                } else if (sectionName.equals(SECTION_FISCAL)) {
                    connector.annulCheckIfNotClosed();
                } else if (!(sectionName.equals(SECTION_TOTAL) || sectionName.equals(SECTION_FOOTER))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printServiceByTemplate(List<DocumentSection> sectionList) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals(SECTION_HEADER)) {
                    connector.openServiceDocument();
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals(SECTION_CUT)) {
                    connector.closeNonfiscalDocument();
                } else if (sectionName.equals(SECTION_LOGO)) {
                    connector.printLogo();
                } else if (!sectionName.equals(SECTION_FOOTER)) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printLinesListInDoc(List<FontLine> content) throws FiscalPrinterException {
        for (FontLine str : content) {
            LOG.debug("printLinesListInDoc: {}", str);
            connector.putText(new Text(str));
        }
    }

    private void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals(SECTION_HEADER)) {
                    connector.openDocument(check);
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals(SECTION_POSITION) || SECTION_POSITION_WITH_GOOD_SETS.equals(sectionName)) {
                    connector.putGoods(check.getGoods(), check);
                    connector.putCheckAgentInfo(check);
                } else if (sectionName.equals(SECTION_PAYMENT)) {
                    printLinesListInDoc(section.getContent());
                    connector.putPayments(check.getPayments());
                } else if (sectionName.equals(SECTION_FISCAL)) {
                    if (check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                        connector.putBarCode(PluginUtils.getDocumentBarcode(check));
                    }
                    connector.closeDocument(check);
                } else if (sectionName.equals(SECTION_LOGO)) {
                    connector.printLogo();
                } else if (!(sectionName.equals(SECTION_FOOTER))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals(SECTION_HEADER)) {
                    connector.openServiceDocument();
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals(SECTION_FISCAL) && !report.isCopy()) {
                    // отрезаем нефискальную часть отчета
                    connector.closeNonfiscalDocument();
                    if (report.isZReport()) {
                        connector.printZReport(report.getCashier());
                    } else if (report.isXReport()) {
                        connector.printXReport(report.getCashier());
                    }
                } else if (sectionName.equals(SECTION_CUT)) {
                    if (report.isCopy()) {
                        // для копии нет фискальной части - нужно отдельно вызвать отрезку
                        connector.closeNonfiscalDocument();
                    }
                } else if (sectionName.equals(SECTION_LOGO)) {
                    connector.printLogo();
                } else if (!sectionName.equals(SECTION_FOOTER)) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals(SECTION_HEADER)) {
                    connector.openDocument(money);
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals(SECTION_FISCAL)) {
                    connector.fiscalMoneyDocument(money);
                } else if (sectionName.equals(SECTION_LOGO)) {
                    connector.printLogo();
                } else if (!(sectionName.equals(SECTION_CUT) || sectionName.equals(SECTION_FOOTER))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    @Override
    public boolean isOFDDevice() {
        return true;
    }

    @Override
    public Optional<FiscalMarkValidationResult> validateMarkCode(PositionEntity position,
                                                                 MarkData markData,
                                                                 boolean isSale) throws FiscalPrinterException {
        return connector.validateMarkCode(position, markData, isSale);
    }

    @Override
    public void clearBeforeMarkRevalidation() throws FiscalPrinterException {
        connector.clearBeforeMarkRevalidation();
    }

    @Override
    public FfdVersion getFfdVersion() {
        return connector.getFfdVersion();
    }
}
