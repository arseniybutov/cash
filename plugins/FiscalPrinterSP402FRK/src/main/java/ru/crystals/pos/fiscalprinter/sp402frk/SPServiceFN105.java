package ru.crystals.pos.fiscalprinter.sp402frk;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.AbstractFiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusCFTDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.KKTCommands;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.support.MaxLengthField;
import ru.crystals.pos.fiscalprinter.sp402frk.support.TaxType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.ResponseData;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.ResBundleFiscalPrinterSP;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@PrototypedComponent
public class SPServiceFN105 extends AbstractFiscalPrinterPlugin implements Configurable<SPConfig> {
    private final Logger log = LoggerFactory.getLogger(SPServiceFN105.class);

    private static final Charset CHARSET_CP866 = Charset.forName("cp866");
    private Connector connector;
    private boolean isItemFreeString = false;
    private ValueAddedTaxCollection taxes = null;
    private SPConfig config;

    @Override
    public Class<SPConfig> getConfigClass() {
        return SPConfig.class;
    }

    @Override
    public void setConfig(SPConfig config) {
        this.config = config;
    }

    @Override
    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public void start() throws FiscalPrinterException {
        log.debug("start...");

        if (StringUtils.isEmpty(config.getPort())) {
            throw new FiscalPrinterConfigException(ResBundleFiscalPrinterSP.getString("ERROR_CONFIG"));
        }

        connector = new Connector();
        connector.open(config.getPort(), config.getBaudRate(), config.isUseFlowControl());

        initStartParameters();

        log.debug("start Ok.");
    }

    @Override
    public void stop() {
        log.debug("stop()");
        connector.close();
        connector = null;
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        if (taxes != null) {
            return taxes;
        }

        taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(TaxType.NDS20.getId(), TaxType.NDS20.getValue(), TaxType.NDS20.name()));
        taxes.addTax(new ValueAddedTax(TaxType.NDS10.getId(), TaxType.NDS10.getValue(), TaxType.NDS10.name()));
        taxes.addTax(new ValueAddedTax(TaxType.NDS_20_120.getId(), TaxType.NDS_20_120.getValue(), TaxType.NDS_20_120.name()));
        taxes.addTax(new ValueAddedTax(TaxType.NDS_10_110.getId(), TaxType.NDS_10_110.getValue(), TaxType.NDS_10_110.name()));
        taxes.addTax(new ValueAddedTax(TaxType.NDS0.getId(), TaxType.NDS0.getValue(), TaxType.NDS0.name()));
        taxes.addTax(new ValueAddedTax(TaxType.NO_NDS.getId(), TaxType.NO_NDS.getValue(), TaxType.NO_NDS.name()));

        log.debug("getTaxes(): {}", taxes);
        return taxes;
    }

    @Override
    public void setCashNumber(long cashNumber) {
        connector.setCashNumber(cashNumber);
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        log.debug("getINN()");
        return connector.getINN();
    }

    @Override
    public int getPaymentLength() {
        log.debug("getPaymentLength()");
        return connector.getMaxLengthField(MaxLengthField.PAYMENTNAME);
    }

    @Override
    public boolean isFFDDevice() {
        return true;
    }

    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        log.debug("getCountCashIn()");
        ResponseData cashInData = connector.getSpecificData(KKTCommands.GET_COUNTERS, "MOIncomeCount");
        return cashInData.getIntValue();
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        log.debug("getCountCashOut()");
        ResponseData cashOutData = connector.getSpecificData(KKTCommands.GET_COUNTERS, "MOOutcomeCount");
        return cashOutData.getIntValue();
    }

    @Override
    public long getCountAnnul() {
        //no hardware annul in SP402FR-k
        return -1L;
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        log.debug("getCashAmount()");
        ResponseData cashData = connector.getSpecificData(KKTCommands.GET_COUNTERS, "CashSumm");
        return UtilsSP.bigDecimalPriceToLong(cashData.getFloatValue());
    }

    /**
     * Это возможно не накопительные счечтчики (передаются в теге 1157), а итоги смены (1194) - тут явно не задано.
     * Накопительные передаются в теге 1157 если включено:
     * GetCumulativeCounters STRING Возвращать накопительные итоги в ответах на запросы счётчиков (true/false)
     * UseCumulativeCounters STRING Использовать накопительные итоги в отчётах (true/false)
     */
    @Override
    public Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        log.debug("getIncrescentTotal()");
        ResponseData saleData = connector.getSpecificData(KKTCommands.GET_COUNTERS, "1129");
        ResponseData saleSumm = connector.searchElementByName(saleData, "1201");
        IncrescentTotal result = new IncrescentTotal();
        result.addSale(UtilsSP.bigDecimalPriceToLong(saleSumm.getFloatValue()));
        return Optional.of(result);
    }

    @Override
    public String getDeviceName() {
        return connector.getNameDevice();
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) throws FiscalPrinterException {
        log.debug("postProcessNegativeScript()");
        stop();
        start();
    }

    @Override
    public void setDate(Date date) throws FiscalPrinterException {
        log.debug("setDate()");
        connector.setDateTime(date);
    }

    @Override
    public Date getDate() throws FiscalPrinterException {
        log.debug("getDate()");
        return connector.getDateTime();
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        log.debug("isShiftOpen()");
        ResponseData shiftData = connector.getSpecificData(KKTCommands.GET_FN_SHORT_STATUS, "ShiftState");
        boolean isShiftOpen = shiftData.getIntValue() > 0;
        log.debug("isShiftOpen() shiftState: {}", isShiftOpen);
        return isShiftOpen;
    }

    public Boolean isShiftOvertime() throws FiscalPrinterException {
        log.debug("isShiftOvertime()");
        ResponseData shiftData = connector.getSpecificData(KKTCommands.GET_FN_SHORT_STATUS, "ShiftOvertime");
        return shiftData.getIntValue() == 1;
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        log.debug("getShiftNumber()");
        ResponseData shiftData = connector.requestCommandData(KKTCommands.GET_FN_SHORT_STATUS, "");
        long shiftNumber = connector.searchElementByName(shiftData, "1038").getIntValue();
        boolean isShiftOpen = connector.searchElementByName(shiftData, "ShiftState").getIntValue() > 0;
        if (!isShiftOpen) {
            shiftNumber++;
        }
        log.debug("getShiftNumber() result: {}", shiftNumber);

        return shiftNumber;
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        log.debug("getRegNum()");
        return connector.getRegNum();
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        log.debug("getFactoryNum()");
        return connector.getFactoryNum();
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        log.debug("getEklzNum()");
        return connector.getFNNum();
    }


    @Override
    public long getLastKpk() throws FiscalPrinterException {
        log.debug("getLastKpk()");
        ResponseData fnData = connector.getSpecificData(KKTCommands.GET_STATUS, "lastDocNumber");
        return fnData.getIntValue();
    }

    @Override
    public Date getEKLZActivizationDate() throws FiscalPrinterException {
        log.debug("getEKLZActivizationDate()");
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
            return dateTimeFormat.parse(connector.getRegDate());
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        log.debug("getShiftCounters()");

        ResponseData countersData = connector.requestCommandData(KKTCommands.GET_COUNTERS, "");
        ShiftCounters counters = new ShiftCounters();
        counters.setShiftNum(getShiftNumber());

        BigDecimal cashSumm = connector.searchElementByName(countersData, "CashSumm").getFloatValue();
        counters.setSumCashEnd(UtilsSP.bigDecimalPriceToLong(cashSumm));

        ResponseData saleData = connector.searchElementByName(countersData, "1129");
        ResponseData refundData = connector.searchElementByName(countersData, "1130");

        BigDecimal saleCash = connector.searchElementByName(saleData, "1136").getFloatValue();
        BigDecimal saleCashless = connector.searchElementByName(saleData, "1138").getFloatValue();
        BigDecimal saleAll = connector.searchElementByName(saleData, "1201").getFloatValue();
        long countSale = connector.searchElementByName(saleData, "1135").getIntValue();

        BigDecimal refundCash = connector.searchElementByName(refundData, "1136").getFloatValue();
        BigDecimal refundCashless = connector.searchElementByName(refundData, "1138").getFloatValue();
        BigDecimal refundAll = connector.searchElementByName(refundData, "1201").getFloatValue();
        long countRefund = connector.searchElementByName(refundData, "1135").getIntValue();

        counters.setSumCashPurchase(UtilsSP.bigDecimalPriceToLong(saleCash));
        counters.setSumCashlessPurchase(UtilsSP.bigDecimalPriceToLong(saleCashless));
        counters.setSumCashReturn(UtilsSP.bigDecimalPriceToLong(refundCash));
        counters.setSumCashlessReturn(UtilsSP.bigDecimalPriceToLong(refundCashless));

        counters.setSumSale(UtilsSP.bigDecimalPriceToLong(saleAll));
        counters.setSumReturn(UtilsSP.bigDecimalPriceToLong(refundAll));

        counters.setCountSale(countSale);
        counters.setCountReturn(countRefund);

        counters.setCountCashPurchase(0L);
        counters.setCountCashlessPurchase(0L);
        counters.setCountCashReturn(0L);
        counters.setCountCashlessReturn(0L);

        return counters;
    }

    @Override
    public String getVerBios() throws FiscalPrinterException {
        log.debug("getVerBios()");
        return connector.getFWVersion();
    }

    @Override
    public StatusFP getStatus() {
        log.debug("getStatus()");
        return new StatusFP();
    }

    @Override
    public PrinterState getPrinterState() {
        log.debug("getPrinterState()");
        return new PrinterState();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        log.debug("openShift()");
        connector.openShift(cashier);
        long result = getShiftNumber();
        log.debug("openShift(Cashier cashier): {}", result);
        return result;
    }

    @Override
    public void printFNReport(Cashier cashier) throws FiscalPrinterException {
        log.debug("printFNReport({})", cashier);
        connector.printFNReport(cashier);
    }

    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws FiscalPrinterException {
        log.debug("printCorrectionReceipt({})", cashier);
        ResponseData correctionData = connector.printCorrectionReceipt(correctionReceipt, cashier);
        ResponseData lastKpk = connector.searchElementByName(correctionData, "1041");
        try {
            if (lastKpk != null) {
                return Optional.of(Long.parseLong(lastKpk.getStrValue()));
            }
        } catch (Exception e) {
            log.error("");
        }
        return Optional.of(getLastKpk());
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        log.debug("printCheck(Check check)");
        try {
            openDocument(check);

            putGoods(check.getGoods());
            putPayments(check);

            if (check.getDiscountValueTotal() != null) {
                putText(new Text(ResBundleFiscalPrinterSP.getString("PD_DISCOUNT_SUM") +
                        String.format("%.2f", (double) check.getDiscountValueTotal() / 100).replace(',', '.')));
            }

            // здесь раньше была печать чекового ШК, но по факту она никогда не вызывалась (а у СП может печатать только EAN13 и QR)

            if (check.isAnnul()) {
                annulCheck();
            } else {
                if (isItemFreeString) {
                    setItemFreeString(false);
                }
                closeDocument();
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void closeDocument() throws FiscalPrinterException {
        closeDocument(null);
    }

    private void closeDocument(BarCode barCode) throws FiscalPrinterException {
        log.debug("closeDocument()");
        if (connector.isDocOpen()) {
            if (barCode != null) {
                putBarCode(barCode);
            }
            connector.closeDocument();
        }
    }

    private void annulCheck() throws FiscalPrinterException {
        connector.cancelDocument();
    }

    private void putBarCode(BarCode barcode) {
        connector.printBarCode(barcode);
    }

    private void putText(Text text) {
        connector.addPrintString(text.getValue());
    }

    private void putPayments(Check check) throws FiscalPrinterException {
        long checkSum = check.getCheckSumEnd();
        connector.setCheckSumm(checkSum);
        for (Payment pay : check.getPayments()) {
            log.debug("putPayment: {}, index({}), indexFFD100({})", pay.toString(), pay.getIndexPayment(), pay.getIndexPaymentFDD100());

            int codePayment = (int) pay.getIndexPaymentFDD100();
            BigDecimal paymentValue = UtilsSP.longPriceToBigDecimal(pay.getSum());
            connector.addPayment(codePayment, paymentValue);
        }
    }

    private void putGoods(List<Goods> positions) throws FiscalPrinterException {
        for (Goods position : positions) {
            String nameItem = "";
            if (config.isPrintGoodsName()) {
                nameItem = position.getName();
                if (position.getName() == null) {
                    nameItem = config.getDefaultGoodsName();
                }
            }

            long price = position.getEndPricePerUnit();
            float quantity = (float) position.getQuant() / 1000;
            ValueAddedTax tax = taxes.lookupByValue(position.getTax());
            if (tax == null) {
                throw new FiscalPrinterException("Tax was not found");
            }
            String freeText = "";
            connector.addPosition(nameItem, price, quantity, (int) tax.index + 1, freeText, getCodeMark(position), position.getAdditionalInfo());
        }
    }

    private String getCodeMark(Goods good) {
        if (good.getExcise() == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(good.getMarkCode())
                .append(good.getMarkEanAsHex())
                .append(convertToHexString(good.getSerialNumber()));

        if (good.getMarkMrp() != null) {
            builder.append(convertToHexString(good.getMarkMrp()));
        }

        return builder.toString();
    }

    private String convertToHexString(String text) {
        byte[] bytes = text.getBytes(CHARSET_CP866);
        return bytesToHex(bytes, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int read) {
        StringBuilder result = new StringBuilder();
        String hex;
        if (bytes != null) {
            for (int j = 0; j < read; j++) {
                int v = bytes[j] & 0xFF;
                hex = Integer.toHexString(v);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                result.append(hex);
            }
        }
        return result.toString();
    }

    private void openDocument(FiscalDocument document) throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }

        if (document instanceof Check) {
            Check check = (Check) document;
            connector.openReceipt(check.getType(), check.getCashier());
        }
    }

    @Override
    public boolean printCheckCopy(Check checkCopy) throws FiscalPrinterException {
        log.debug("printCheckCopy(checkCopy, content)");
        checkCopy.setAnnul(true);
        printCheck(checkCopy);
        return true;
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        log.debug("printMoneyDocument(Money money, Long lastKpk)");

        connector.moneyInOut(money.getOperationType(), money.getValue());

    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        log.debug("printServiceDocument()");

        if (isDocOpen()) {
            annulCheck();
        }

        connector.openNonFiscalDoc("");
        for (Row row : serviceDocument.getRows()) {
            if (row instanceof Text) {
                putText((Text) row);
            } else if (row instanceof BarCode) {
                connector.printBarCode((BarCode) row);
                putText(new Text(""));
            }
        }
        connector.printNonFiscalDoc("");
    }

    private boolean isDocOpen() {
        log.debug("isDocOpen()");
        return connector.isDocOpen();
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        log.debug("printXReport()");
        connector.printXReport(report.getCashier());
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        log.debug("printZReport()");
        connector.printZReport(report.getCashier());
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        log.debug("openMoneyDrawer()");
        connector.executeCommand(KKTCommands.OPEN_CASH_DRAWER, "");
    }

    @Override
    public boolean isMoneyDrawerOpen() throws FiscalPrinterException {
        log.debug("isMoneyDrawerOpen()");
        ResponseData factoryData = connector.getSpecificData(KKTCommands.GET_DRAWER_STATUS, "IsOpen");
        return factoryData.getIntValue() == 1;
    }

    private void printLogo() {
        // печать логотипа
    }

    private void printLinesListInDoc(List<FontLine> content) throws FiscalPrinterException {
        log.debug("start printLinesListInDoc");

        for (FontLine fontLine : content) {
            if ((fontLine != null && !fontLine.equals(""))) {
                TextSize size;
                switch (fontLine.getFont()) {
                    case SMALL:
                        size = TextSize.SMALL;
                        break;
                    case DOUBLEHEIGHT:
                        size = TextSize.DOUBLE_HEIGHT;
                        break;
                    case DOUBLEWIDTH:
                        size = TextSize.DOUBLE_WIDTH;
                        break;
                    default:
                        size = TextSize.NORMAL;
                        break;
                }
                Text text = new Text(fontLine.getContent(), size);
                connector.addPrintString(text.getValue());
            }
        }
        log.debug("finish printLinesListInDoc");
    }

    private void printItemLinesList(List<FontLine> content) {
        log.debug("start printItemLinesList");
        for (FontLine str : content) {
            connector.addItemLine(str.getContent());
        }
        log.debug("finish printItemLinesList");
    }

    private void openServiceDocument() throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }
        connector.openNonFiscalDoc("");
    }

    private void fiscalMoneyDocument(Money money) throws FiscalPrinterException {
        log.debug("fiscalMoneyDocument(Money money)");
        connector.moneyInOut(money.getOperationType(), money.getValue());
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        return connector.getSymbolsPerLine();
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
            } else if (check.isCopy() || check instanceof FullCheckCopy) {
                printCopyCheckByTemplate(sectionList, check);
            } else {
                printCheckByTemplate(sectionList, check);
            }
        } else if (document instanceof Report) {
            printReportByTemplate(sectionList, (Report) document);
        } else if (document instanceof Money) {
            printMoneyByTemplate(sectionList, (Money) document);
        } else if (document instanceof DiscountsReport) {
            printServiceByTemplate(sectionList);
        } else if (document instanceof BonusCFTDocument) {
            printBonusCFTReportByTemplate(sectionList);
        } else if (document instanceof DailyLogData) {
            printBankDailyReportByTemplate(sectionList);
        }
    }

    private void printBankDailyReportByTemplate(List<DocumentSection> sectionList) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("slip")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("cut")) {
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printBonusCFTReportByTemplate(List<DocumentSection> sectionList) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case "logo":
                        printLogo();
                        break;
                    case "operationList":
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case "cut":
                        closeDocument();
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printServiceByTemplate(List<DocumentSection> sectionList) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("cut")) {
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("fiscal")) {
                    if (money.getOperationType() != InventoryOperationType.DECLARATION) {
                        fiscalMoneyDocument(money);
                    }
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else if (!(sectionName.equals("cut") || sectionName.equals("footer"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("fiscal") && !report.isCopy()) {
                    if (report.isZReport()) {
                        printZReport(report);
                    } else if (report.isXReport()) {
                        printXReport(report);
                    }
                } else if (sectionName.equals("cut")) {
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            openDocument(check);
            if (!isItemFreeString) {
                setItemFreeString(true);
            }
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    for (FontLine headerStr : section.getContent()) {
                        connector.addHeaderLine(headerStr.getContent());
                    }
                } else if (sectionName.equals("position") || sectionName.equals("positionSectionWithGoodSets")) {
                    putGoods(check.getGoods());
                    printItemLinesList(section.getContent());
                } else if (sectionName.equals("payment")) {
                    putPayments(check);
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else if (!sectionName.equals("cut")) {
                    printLinesListInDoc(section.getContent());
                }
            }
            BarCode barCode = check.getPrintDocumentSettings().isNeedPrintBarcode() ? PluginUtils.getDocumentBarcode(check) : null;
            closeDocument(barCode);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("fiscal")) {
                    putBarCode(PluginUtils.getDocumentBarcode(check));
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else if (!sectionName.equals("cut")) {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            openServiceDocument();
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("position")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("payment")) {
                    printLinesListInDoc(section.getContent());
                    putPayments(check);
                } else if (sectionName.equals("fiscal")) {
                    connector.addPrintString(ResBundleFiscalPrinterSP.getString("ANNUL_CHECK"));
                } else if (sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                } else if (!sectionName.equals("cut")) {
                    printLinesListInDoc(section.getContent());
                }
            }
            closeDocument();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void setItemFreeString(boolean isItemFreeString) throws FiscalPrinterException {
        connector.setItemFreeString(isItemFreeString);
        this.isItemFreeString = isItemFreeString;
    }

    private void initStartParameters() throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }
        getTaxes();
        setItemFreeString(true);
    }
}
