package ru.crystals.pos.fiscalprinter.atol3;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.AbstractFiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Value;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdditionalInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusCFTDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState.State;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PrototypedComponent
public class AtolServiceImpl extends AbstractFiscalPrinterPlugin implements Configurable<AtolConfig> {
    private static final Logger logger = LoggerFactory.getLogger(FiscalPrinter.class);


    private static final float NDS_18 = 18.0f;
    private static final float NDS_20 = 20.0f;
    private static final float NDS_18_118 = -18.0f;
    private static final float NDS_20_120 = -20.0f;

    protected FiscalDevice fiscalDevice = new FiscalDevice();
    protected boolean fiscalOperationComplete;

    private ValueAddedTaxCollection taxes = null;

    @Autowired
    private EthernetOverUsbService ethernetOverUsbService;

    private AtolConfig config;

    @Override
    public Class<AtolConfig> getConfigClass() {
        return AtolConfig.class;
    }

    @Override
    public void setConfig(AtolConfig config) {
        this.config = config;
    }

    @Override
    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public void start() throws FiscalPrinterException {
        logger.debug("start...");

        if (StringUtils.isEmpty(config.getPort())) {
            throw new FiscalPrinterConfigException(ResBundleFiscalPrinterAtol.getString("ERROR_CONFIG"));
        }

        startService();

        reconnect();

        logger.debug("start Ok.");
    }

    private String resolveNamedPort(String port) {
        Path path = Paths.get(port);
        if (Files.isSymbolicLink(path)) {
            try {
                return path.getParent().resolve(Files.readSymbolicLink(path)).toString();
            } catch (IOException e) {
                logger.error("Failed to resolve port: {}", path);
            }
        }

        return port;
    }

    private void startService() throws FiscalPrinterException {
        ethernetOverUsbService.start(Paths.get(config.getServiceExec()));
    }

    private void reconnect() throws FiscalPrinterException {
        try {
            fiscalDevice.open(resolveNamedPort(config.getPort()), config.getBaudRate(), config.isUseFlowControl());
        } catch (Exception ex) {
            logger.error("Error open port: ", ex);
            stop();
            startService();
            fiscalDevice.open(resolveNamedPort(config.getPort()), config.getBaudRate(), config.isUseFlowControl());
        }
        fiscalDevice.setQrCodeScaleFactor(config.getQrCodeScaleFactor());

        initStartParameters();
    }

    @Override
    public void stop() throws FiscalPrinterException {
        fiscalDevice.close();
        ethernetOverUsbService.stop();
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) {
        fiscalDevice.setRequisites(requisites);
    }

    @Override
    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        logger.debug("Getting taxes");

        int taxCount = fiscalDevice.getTaxCount();

        List<ValueAddedTax> taxesList = new ArrayList<>();
        for (byte taxIndex = 0; taxIndex < taxCount; taxIndex++) {
            String taxName = fiscalDevice.getTableValue(13, taxIndex + 1, 1).get(ValueDecoder.ATOL_STRING);
            float taxValue = fiscalDevice.getTableValue(13, taxIndex + 1, 2).get(ValueDecoder.LONG) / 100f;
            taxesList.add(new ValueAddedTax(taxIndex, taxValue, taxName));
        }

        ValueAddedTaxCollection taxCollection = new ValueAddedTaxCollection(taxesList);

        logger.debug("getTaxes(): {}", taxesList);
        return taxCollection;
    }

    @Override
    public void setPayments(List<PaymentType> payments) throws FiscalPrinterException {
        boolean[] present = new boolean[fiscalDevice.getPaymentsCount()];
        // список присутствующих питов оплат
        present[0] = true; // наличные присутствуют априоре
        for (PaymentType payment : payments) {
            if (payment.getIndexPayment() > PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex() && payment.getIndexPayment() < fiscalDevice.getPaymentsCount()) {
                String s = String.format("%-" + fiscalDevice.getMaxLengthField(MaxLengthField.PAYMENTNAME) + "s", payment.getName());
                fiscalDevice.setTableValue(12, (int) payment.getIndexPayment(), 1, s);
                present[(int) payment.getIndexPayment()] = true; // помечаем как присутствующий
            }
        }
        // пробегаемся по всем типам оплат и посылаем пустую строку наименования
        // для отсутствующих типов платежей
        for (int i = 0; i < present.length; i++) {
            if (!present[i]) {
                String s = String.format("%-" + fiscalDevice.getMaxLengthField(MaxLengthField.PAYMENTNAME) + "s", " ");
                fiscalDevice.setTableValue(12, i, 1, s);
            }
        }
    }

    @Override
    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        fiscalDevice.setCashNumber(cashNumber);
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return fiscalDevice.getINN();
    }

    @Override
    public int getPaymentLength() {
        return fiscalDevice.getMaxLengthField(MaxLengthField.PAYMENTNAME);
    }

    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        return fiscalDevice.getParameter(Registers.CASHIN_COUNT.getRegisterNumber()).get(ValueDecoder.LONG);
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        return fiscalDevice.getParameter(Registers.CASHOUT_COUNT.getRegisterNumber()).get(ValueDecoder.LONG);
    }

    @Override
    public long getCountAnnul() throws FiscalPrinterException {
        return fiscalDevice.getCountAnnul();
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        return fiscalDevice.getParameter(Registers.CASH_AMOUNT.getRegisterNumber()).get(ValueDecoder.LONG);
    }

    @Override
    public Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        // Это не нарастающие итоги (13, 14 регистры), а сменные итоги (12 регистр) - но плагин под выпил, после перехода на универсальный драйвер
        return Optional.of(new IncrescentTotal(
                fiscalDevice.getParameter(Registers.SHIFT_TOTAL.getRegisterNumber()).get(ValueDecoder.LONG),
                fiscalDevice.getParameter(Registers.SHIFT_TOTAL.getRegisterNumber(), 2).get(ValueDecoder.LONG)));
    }

    @Override
    public String getDeviceName() {
        return fiscalDevice.getName();
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) throws FiscalPrinterException {
        reconnect();
    }

    @Override
    public void setDate(Date date) throws FiscalPrinterException {
        fiscalDevice.setDateTime(date);
    }

    @Override
    public Date getDate() throws FiscalPrinterException {
        return fiscalDevice.getDateTime();
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        return fiscalDevice.isShiftOpen();
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return fiscalDevice.getShiftNumber();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Внимание! Реализация данного метода возвращает заводской номер, а не регистрационный!
     *
     * @return заводской номер фискального регистратора
     * @throws FiscalPrinterException если при получении заводского номера фискального регистратора возникли ошибки
     */
    @Override
    public String getRegNum() throws FiscalPrinterException {
        return fiscalDevice.getSerialNumber();
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        logger.trace("entering getFactoryNum()");
        String result = fiscalDevice.getSerialNumber();
        logger.trace("leaving getFactoryNum(). The result is: \"{}\"", result);

        return result;
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return fiscalDevice.getEKLZNumber();
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        try {
            long result = fiscalDevice.getLastFiscalDocumentNum();
            logger.debug("getLastKpk(): " + result);
            return result;
        } catch (Exception ex) {
            postProcessNegativeScript(null);
            throw ex;
        }
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        ShiftCounters counters = new ShiftCounters();
        counters.setShiftNum(getShiftNumber());
        counters.setSumCashEnd(getCashAmount());

        long saleCash = getPaymentSumm(CheckType.SALE, CodePayment.CASH.getFiscalCode().intValue());
        long refundCash = getPaymentSumm(CheckType.RETURN, CodePayment.CASH.getFiscalCode().intValue());
        long saleCashless = getCashlessSumm(CheckType.SALE);
        long refundCashless = getCashlessSumm(CheckType.RETURN);

        counters.setCountSale(fiscalDevice.getParameter(
                Registers.PURCHASE_COUNT.getRegisterNumber(), CheckType.SALE.ordinal() + 1).get(ValueDecoder.LONG, 0, 5));

        counters.setCountReturn(fiscalDevice.getParameter(
                Registers.PURCHASE_COUNT.getRegisterNumber(), CheckType.RETURN.ordinal() + 1).get(ValueDecoder.LONG, 0, 5));

        counters.setSumSale(saleCash + saleCashless);
        counters.setSumReturn(refundCash + refundCashless);

        // оплат (нал/безнал)
        counters.setCountCashPurchase(0L);
        counters.setCountCashlessPurchase(0L);
        counters.setCountCashReturn(0L);
        counters.setCountCashlessReturn(0L);

        counters.setSumCashPurchase(saleCash);
        counters.setSumCashlessPurchase(saleCashless);
        counters.setSumCashReturn(refundCash);
        counters.setSumCashlessReturn(refundCashless);

        return counters;
    }

    /**
     * Получить сумму по типу чека и коду оплаты из ФР
     *
     * @param checkType   тип чека (продажа/возврат)
     * @param paymentCode код типа оплаты в ФР
     * @return сумма
     * @throws FiscalPrinterException
     */
    private long getPaymentSumm(CheckType checkType, int paymentCode) throws FiscalPrinterException {
        return fiscalDevice.getParameter(Registers.PAYMENT_SUMS.getRegisterNumber(), checkType.ordinal() + 1, paymentCode).get(ValueDecoder.LONG);
    }

    /**
     * Получить сумму безналичных оплат по типу чека из ФР
     *
     * @param checkType тип чека (продажа/возврат)
     * @return сумма
     * @throws FiscalPrinterException
     */
    private long getCashlessSumm(CheckType checkType) throws FiscalPrinterException {
        long result = 0;
        for (CodePayment code : CodePayment.getCashlessCodes()) {
            result += getPaymentSumm(checkType, code.getFiscalCode().intValue());
        }
        return result;
    }

    @Override
    public String getVerBios() throws FiscalPrinterException {
        return fiscalDevice.getVersion();
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        StatusFP result = new StatusFP();
        if (fiscalDevice.getSpecificData().isCapOpen()) {
            result.setStatus(ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP.Status.OPEN_COVER);
            result.getDescriptions().add(ResBundleFiscalPrinterAtol.getString("OPEN_PRINTER_COVER"));
        }
        return result;
    }

    @Override
    public PrinterState getPrinterState() throws FiscalPrinterException {
        PrinterState result = new PrinterState();
        if (fiscalDevice.getSpecificData().isCapOpen()) {
            result.setState(State.OPEN_COVER);
            result.getDescriptions().add(ResBundleFiscalPrinterAtol.getString("OPEN_PRINTER_COVER"));
        }
        return result;
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        fiscalDevice.addCashierName(cashier.getCashierStringForOFDTag1021().trim());
        fiscalDevice.openShift();
        taxes = getTaxes();
        long result = getShiftNumber();
        logger.debug("openShift(" + UtilsAtol.cahiers2String(cashier) + "): " + result);
        return result;
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("INCOMPATIBLE_PRINT_DOCUMENT_METHOD_FOR_OFD"));
    }

    private void closeDocument() throws FiscalPrinterException {
        logger.debug("closeDocument");
        if (fiscalDevice.isDocOpen()) {
            fiscalDevice.closeDocument(true);
        } else {
            fiscalDevice.cutPaper();
        }
    }

    protected void annulCheck() throws FiscalPrinterException {
        fiscalDevice.cancelDocument();
    }

    private void putBarCode(BarCode barcode) throws FiscalPrinterException {
        fiscalDevice.printBarCode(barcode);
    }

    private void putText(Text text) throws FiscalPrinterException {
        fiscalDevice.addPrintString(text);
    }

    private void putPayments(List<Payment> payments) throws FiscalPrinterException {
        for (Payment pay : payments) {
            byte codePayment = CodePayment.getFiscalCodeByFfdCode(pay.getIndexPaymentFDD100()).byteValue();
            fiscalDevice.addPayment(codePayment, pay.getSum(), null);
        }
    }

    protected void openDocument(FiscalDocument document) throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }

        if (document instanceof Check) {
            fiscalOperationComplete = false;
            Check check = (Check) document;
            fiscalDevice.openDocument(check.getType());
            fiscalDevice.addClientRequisite(check.getClientRequisites());
            fiscalDevice.addCashierName(check.getCashier().getCashierStringForOFDTag1021().trim());
        }
    }

    @Override
    public boolean printCheckCopy(Check checkCopy) throws FiscalPrinterException {
        logger.debug("printCheckCopy(" + UtilsAtol.fiscalDocument2String(checkCopy) + "\")");
        checkCopy.setAnnul(true);
        printCheck(checkCopy);
        return true;
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("INCOMPATIBLE_PRINT_DOCUMENT_METHOD_FOR_OFD"));
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }

        for (Row row : serviceDocument.getRows()) {
            if (row instanceof Text) {
                putText((Text) row);
            } else if (row instanceof BarCode) {
                putBarCode((BarCode) row);
                putText(new Text(""));
            }
        }
        fiscalDevice.cutPaper();
    }

    public boolean isDocOpen() throws FiscalPrinterException {
        return fiscalDevice.isDocOpen();
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        fiscalDevice.addCashierName(report.getCashier().getCashierStringForOFDTag1021().trim());
        fiscalDevice.printXReport();
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        fiscalDevice.addCashierName(report.getCashier().getCashierStringForOFDTag1021().trim());
        fiscalDevice.printZReport();
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        fiscalDevice.openDrawer();
    }

    @Override
    public boolean isMoneyDrawerOpen() throws FiscalPrinterException {
        return fiscalDevice.isDrawerOpen(config.isInvertDrawerState());
    }

    @Override
    public void printReportFiscalMemoryByDate(Date startDate, Date endDate, String password, boolean isFullReport) throws FiscalPrinterException {
        logger.debug(
                "printReportFiscalMemoryByDate(" + UtilsAtol.date2String(startDate) + ", " + UtilsAtol.date2String(endDate) + ", \"" + password + "\", " +
                        isFullReport + ")");
        fiscalDevice.printReport(startDate, endDate, password, isFullReport);
    }

    @Override
    public void printReportFiscalMemoryByShiftID(long startShiftID, long endShiftID, String password, boolean isFullReport)
            throws FiscalPrinterException {
        logger.debug("printReportFiscalMemoryByShiftID(" + startShiftID + ", " + endShiftID + ", \"" + password + "\", " + isFullReport + ")");
        fiscalDevice.printReport(startShiftID, endShiftID, password, isFullReport);
    }

    public void printLogo() throws FiscalPrinterException {
        logger.debug("printLogo");
        int imageIndex = 1;
        fiscalDevice.printLogo(imageIndex);
    }

    private void printLinesListInDoc(List<FontLine> content) throws FiscalPrinterException {
        logger.debug("start printLinesListInDoc");

        for (FontLine str : content) {
            if (str != null && !"".equals(str.toString())) {
                TextSize size;
                switch (str.getFont()) {
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
                Text text = new Text(str.getContent(), size);

                fiscalDevice.addPrintString(text);
            }
        }
        logger.debug("finish printLinesListInDoc");
    }

    private void openServiceDocument() throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }
    }

    protected void fiscalizeSum(List<Goods> goods) throws FiscalPrinterException {
        for (Goods position : goods) {
            // При отличии Цены х Количество от EndPositionPrice, ККТ считает разницу скидкой и печатает ее на чеке
            long price = position.getStartPricePerUnit();
            long amount = position.getQuant();

            ValueAddedTax tax = getTax(position.getTax());

            fiscalDevice.beginPosition();

            putCodingMark(position);
            if (position.getAdditionalInfo() != null) {
                putAdditionalInfo(position.getAdditionalInfo());
            }

            fiscalDevice.addPosition(price, amount, position.getEndPositionPrice(), (int) tax.index + 1, 0, 0, 0, position.getName());
        }
    }

    /**
     * Отправляет в ФР реквизиты для передачи в ОФД тега 1162 в случае если:
     * 1) либо товар маркированный
     * 2) либо немаркированный товар содержит обязательный для передачи в ОФД КТН
     */
    protected void putCodingMark(Goods good) throws FiscalPrinterException {
        String itemCode = null;

        if (StringUtils.isNotEmpty(good.getExcise())) {
            itemCode = getCodeMark(good);
        } else if (isOfdCodeAvailable(good)) {
            itemCode = getRawItemCode(good);
        }

        if (itemCode != null) {
            fiscalDevice.addCodingMark(itemCode);
        }
    }

    protected String getRawItemCode(Goods good) {
        Charset charsetCP866 = Charset.forName("cp866");
        String codeMark = new String(UtilsAtol.hexStringDataToByteArray(Goods.RAW_OFD_CODE_PREFIX), charsetCP866);
        String hexStrRawEAN13 = good.getRawOfdCodeAsHex();
        codeMark += new String(UtilsAtol.hexStringDataToByteArray(hexStrRawEAN13), charsetCP866);
        return codeMark;
    }

    protected String getCodeMark(Goods good) {
        Charset charsetCP866 = Charset.forName("cp866");

        String codeMark = new String(UtilsAtol.hexStringDataToByteArray(good.getMarkCode()), charsetCP866);

        String hexStrGtin = good.getMarkEanAsHex();
        codeMark += new String(UtilsAtol.hexStringDataToByteArray(hexStrGtin), charsetCP866);

        codeMark += good.getSerialNumber();
        if (good.getMarkMrp() != null) {
            codeMark += good.getMarkMrp();
        }

        return codeMark;
    }

    private boolean isOfdCodeAvailable(Goods good) {
        return good.isOfdCodeMandatory() && StringUtils.isNotBlank(good.getOfdCode());
    }

    /**
     * Отправка дополнительных реквизитов в ОФД
     *
     * @param additionalInfo дополнительная информация товарной позиции
     * @throws FiscalPrinterException
     */
    private void putAdditionalInfo(AdditionalInfo additionalInfo) throws FiscalPrinterException {
        // ИНН поставщика (тег 1226)
        fiscalDevice.addDebitorINN(additionalInfo.getDebitorINN());
        // Признак агента по предмету расчета (тег 1222)
        fiscalDevice.addAgentSign(additionalInfo.getAgentType().getBitMask());
        // Данные поставщика (теги 1171, 1225)
        fiscalDevice.addDebitorData(additionalInfo.getDebitorPhone(), additionalInfo.getDebitorName());
    }

    protected ValueAddedTax getTax(float taxValue) throws FiscalPrinterException {
        ValueAddedTax tax = taxes.lookupByValue(taxValue);
        // Сделано на переходный период (НДС 20%), когда возможно ФР не перепрошит и в нем старые налоги, пробуем найти индекс по ним.
        if (tax == null) {
            if (taxValue == NDS_20) {
                tax = taxes.lookupByValue(NDS_18);
            } else if (taxValue == NDS_20_120) {
                tax = taxes.lookupByValue(NDS_18_118);
            }
            // Если все таки ничего не нашли кидаем ошибку
            if (tax == null) {
                throw new FiscalPrinterException("Tax was not found");
            }
        }
        return tax;
    }

    private void fiscalMoneyDocument(Money money) throws FiscalPrinterException {
        if (isShiftMoreThan24h()) {
            logger.warn("do not perform cash in/out because shift more 24h");
            return;
        }

        logger.debug("fiscalMoneyDocument(" + UtilsAtol.fiscalDocument2String(money) + ")");
        if (money.getValue() != 0) {
            fiscalDevice.addMoneyInOut(money.getOperationType() == InventoryOperationType.CASH_IN ? money.getValue() : -money.getValue());
        } else {
            fiscalDevice.cutPaper();
        }
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        TextSize size;
        switch (font) {
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
        return fiscalDevice.getMaxLenthForText(size);
    }

    @Override
    public void printDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        logger.debug("call printDocument(" + UtilsAtol.list2String(sectionList) + ", " + UtilsAtol.fiscalDocument2String(document) + ")");
        printDocumentByTemplate(sectionList, document);
    }

    public void printDocumentByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
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
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        closeDocument();
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case "slip":
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printBonusCFTReportByTemplate(List<DocumentSection> sectionList) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_OPERATION_LIST:
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        closeDocument();
                        break;
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
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        closeDocument();
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
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
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                            openServiceDocument();
                        } else {
                            openDocument(money);
                        }
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                            closeDocument();
                        } else {
                            fiscalMoneyDocument(money);
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
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
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (report.isCopy()) {
                            break;
                        }
                        if (report.isZReport()) {
                            printZReport(report);
                        } else if (report.isXReport()) {
                            printXReport(report);
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        if (report.isCopy()) {
                            closeDocument();
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openDocument(check);
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_POSITION:
                    case "positionSectionWithGoodSets":
                        // Мы не печатаем информацию по позициям,
                        // АТОЛ это делает сам при регистрации в соответствии со своим внутренним шаблоном
                        fiscalizeSum(check.getGoods());
                        break;
                    case FiscalPrinterPlugin.SECTION_PAYMENT:
                        putPayments(check.getPayments());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                            printDocumentNumberBarcode(check);
                        }
                        closeDocument();
                        if (check.getType().equals(CheckType.SALE)) {
                            fiscalDevice.closeFiscalDocument();
                        }
                        fiscalOperationComplete = true;
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printDocumentNumberBarcode(Check check) throws Exception {
        BarCode documentBarcode = PluginUtils.getDocumentBarcode(check);
        documentBarcode.setTextPosition(TextPosition.NONE_TEXT);
        putBarCode(documentBarcode);
        FontLine barcodeLabel = new FontLine(StringUtils.center(documentBarcode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
        fiscalDevice.addPrintString(barcodeLabel.convertToText());
    }

    private void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openServiceDocument();
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        putBarCode(PluginUtils.getDocumentBarcode(check));
                        closeDocument();
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        openDocument(check);
                        printLinesListInDoc(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_POSITION:
                    case "positionSectionWithGoodSets":
                        // Мы не печатаем информацию по позициям,
                        // АТОЛ это делает сам при регистрации в соответствии со своим внутренним шаблоном
                        fiscalizeSum(check.getGoods());
                        break;
                    case FiscalPrinterPlugin.SECTION_PAYMENT:
                        putPayments(check.getPayments());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        annulCheck();
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:

                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesListInDoc(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setInitialTableValue(int numColumn, byte value) throws FiscalPrinterException {
        fiscalDevice.setTableValue(2, 1, numColumn, value);
    }

    private void initStartParameters() throws FiscalPrinterException {
        try {
            fiscalDevice.abort();

            if (isDocOpen()) {
                annulCheck();
            }
        } catch (FiscalPrinterException e) {
            if (e.getErrorCode() == 102) {
                fiscalDevice.cancelDocument();
            } else {
                throw e;
            }
        }

        //@formatter:off
        fiscalDevice.setMode(StateMode.PROGRAMMING);
        taxes = getTaxes();

        int maxTextLength = fiscalDevice.getTableValue(2, 1, 55).get(ValueDecoder.LONG).intValue();
        fiscalDevice.setMaxTextLength(maxTextLength);

        // Тип оплаты 2 разрешен, контроль наличности можно отключать
        setInitialTableValue(3, (byte) 2);

        // Тип оплаты 3 разрешен, контроль наличности можно отключать
        setInitialTableValue(4, (byte) 2);

        // Тип оплаты 4 разрешен, контроль наличности можно отключать
        setInitialTableValue(5, (byte) 2);

        // Режим работы ККТ - для торговли
        setInitialTableValue(8, (byte) 0);

        /*
         Тип налога (
                0 - запрещено,
                1 - налог на весь чек,
                2 - налог на каждую продажу)
        */
        setInitialTableValue(11, (byte) 0);

        // Печать остатка ФП в отчете (
        setInitialTableValue(12, (byte) 1);

        // Печать QR кода (0 - запрещено, 1 - разрешено)
        setInitialTableValue(14, (byte) 1);

        // Печать названия секции (0 - запрещено, 1 - разрешено)
        setInitialTableValue(15, (byte) 1);

        /*
         Параметры печати суточных отчетов (
         Бит0: печать необнуляемой суммы (0 - запрещено, 1 - разрешено)
         Бит1: (имеет смысл только если нулевой бит = 1):
                - печать всей необнуляемой суммы (
                        0 - печатать всю сумму,
                        1 - печатать значение необнуляемой суммы с момента последней перерегистрации)
         Бит2: производить инкассацию (0 - запрещено, 1 - разрешено) )
        */
        byte prop = 1 | 2;
        setInitialTableValue(18, prop);

        /*
         Работа с денежным ящиком
                0 – при закрытии чека денежный ящик открываться не будет
                1 - при закрытии чека денежный ящик будет открываться
        */
        setInitialTableValue(20, (byte) 0);

        /*
         Отрезать чек после завершения документа (
                 0 - не отрезать,
                 1 - отрезать не полностью,
                 2 - отрезать полностью)
        */
        setInitialTableValue(24, (byte) (1));
        // Печатать имена кассиров
        setInitialTableValue(26, (byte) 0);

        /*
         Печатать название чека продажи
         0 – запрещена
         1 – разрешена на ЧЛ
         2 – разрешенана КЛ
         3 – разрешена на ЧЛ и  КЛ
        */
        setInitialTableValue(29, (byte) 0);

        long linesBeforeCut = fiscalDevice.getTableValue(2, 1, 36).get(ValueDecoder.LONG);
        fiscalDevice.setLinesBeforeCut(linesBeforeCut);

        // Печатать номер секции
        setInitialTableValue(42, (byte) 0);

        if (fiscalDevice.isPrintDiscountsInReports()) {
            /*
            Печать скидок/надбавок в Z- и X- отчетах и при регистрации
                0 – запрещено,
                1 – печатать на ЧЛ
                2 – печатать на КЛ.
                3 – печатать на ЧЛ и КЛ
            */
            setInitialTableValue(75, (byte) 1);
        }

        setInitialTableValue(78, (byte) 1);

        /*
         Печать служебных сообщений
         0-й бит: печатать на ЧЛ документ готовности к работе,
         0 – нет,
         1 – да
         1-й бит не используется
         2-й бит: печатать на ЧЛ сообщение о вводе даты-времени, 0 – нет, 1 – да
         3-й бит не используется.
        */
        setInitialTableValue(83, (byte) 1);

        // Режим совместимости включен
        setInitialTableValue(109, (byte) 1);
    }

    private boolean isShiftMoreThan24h() throws FiscalPrinterException {
        final long SHIFT_MORE_THAN_24H = 2;
        return fiscalDevice.getParameter(18, 0, 1).get(ValueDecoder.BIN, 0, 1) == SHIFT_MORE_THAN_24H;
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        try {
            Value lastPurchaseData = fiscalDevice.getParameter(51);

            FiscalDocumentData result = new FiscalDocumentData();
            result.setType(lastPurchaseData.get(ValueDecoder.LONG, 5, 1) == 1 ? FiscalDocumentType.SALE : FiscalDocumentType.REFUND);
            result.setSum(lastPurchaseData.get(ValueDecoder.LONG, 6, 7));
            result.setNumFD(lastPurchaseData.get(ValueDecoder.LONG, 0, 5));
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }
}
