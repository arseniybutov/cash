package ru.crystals.pos.fiscalprinter.sp402frk;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.CorrectionReceiptPaymentsEntity;
import ru.crystals.pos.check.CorrectionReceiptTaxesEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdditionalInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ArmCorrection;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ArmCorrectionReason;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ArmMoneyOperation;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ArmNonFiscalDoc;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.KKTCommands;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.PrintFNReport;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.PrintXReport;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.SetDateTime;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ShiftCloseZ;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.ShiftOpen;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt.ArmItemProvider;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt.ArmReceipt;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt.ArmReceiptItem;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.settings.FDDesign;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.settings.MacroSettings;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.settings.PRNSettings;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.settings.PrinterParams;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.settings.Settings;
import ru.crystals.pos.fiscalprinter.sp402frk.device.Device;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataStorage;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTPaymentType;
import ru.crystals.pos.fiscalprinter.sp402frk.support.MaxLengthField;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.ArmResponse;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.ResponseData;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.Transport;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.ResBundleFiscalPrinterSP;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Connector {

    private final Logger logger = LoggerFactory.getLogger(Connector.class);

    private Transport trans;

    private Device device;
    private ArmReceipt currReceipt;
    private ArmNonFiscalDoc currServiceDoc;
    private KKTDataStorage regDataStorage;
    //Заголовок, указывающий, что следующие данные в строке представляют собой штрих-код
    private static final String BARCODE_HEADER = "‼?b";
    //Заголовок, указывающий, что следующие данные в строке представляют QR код
    private static final String QRCODE_HEADER = "‼?q";
    private String cashNumber = "1";

    public Connector() {
        trans = new Transport();
    }

    public void open(String port, int baudRate, boolean useFlowControl) throws FiscalPrinterException {
        logger.debug("open port: " + port + " baudRate: " + baudRate + " useFlowControl: " + useFlowControl);
        trans.open(port, baudRate, useFlowControl);
        device = new Device();
        regDataStorage = new KKTDataStorage();
        String firmwareVer = getFWVersion();
        logger.info("Connected to SP402FR-K, firmware version: {}", firmwareVer);
        device.setMaxTextLength(getFRTextLength());
    }

    public void close() {
        trans.close();
    }

    public String getFWVersion() throws FiscalPrinterException {
        if (regDataStorage.isFirmwareVerEmpty()) {
            updateKKTInfo();
        }
        return regDataStorage.getFirmwareVer();
    }

    public String getINN() throws FiscalPrinterException {
        if (regDataStorage.isDeviceINNEmpty()) {
            updateRegistrationParams();
        }
        return regDataStorage.getDeviceINN();
    }

    public String getRegDate() throws FiscalPrinterException {
        if (regDataStorage.isRegistrationDateEmpty()) {
            updateRegistrationParams();
        }
        return regDataStorage.getRegistrationDate();
    }

    public String getRegNum() throws FiscalPrinterException {
        if (regDataStorage.isRegistrationNumEmpty()) {
            updateKKTInfo();
        }
        return regDataStorage.getRegistrationNum();
    }

    private void updateKKTInfo() throws FiscalPrinterException {
        ResponseData responseData = requestCommandData(KKTCommands.GET_KKT_INFO, "");
        if (responseData != null) {
            ResponseData regNumData = searchElementByName(responseData, "1037");
            if (regNumData != null) {
                regDataStorage.setRegistrationNum(regNumData.getStrValue());
            }
            ResponseData factoryData = searchElementByName(responseData, "1013");
            if (factoryData != null) {
                regDataStorage.setFactoryNum(factoryData.getStrValue());
            }
            ResponseData firmwareVer = searchElementByName(responseData, "KKTFWVersion");
            if (firmwareVer != null) {
                regDataStorage.setFirmwareVer(firmwareVer.getStrValue());
            }
        }
    }

    private void updateRegistrationParams() throws FiscalPrinterException {
        ResponseData responseData = requestCommandData(KKTCommands.GET_REGISTRATION_PARAMS, "");
        if (responseData != null) {
            ResponseData innData = searchElementByName(responseData, "1018");
            if (innData != null) {
                regDataStorage.setDeviceINN(innData.getStrValue());
            }
            ResponseData registrationDate = searchElementByName(responseData, "1012");
            if (registrationDate != null) {
                regDataStorage.setRegistrationDate(registrationDate.getStrValue());
            }
        }
    }

    public String getFNNum() throws FiscalPrinterException {
        if (regDataStorage.isFnNumberEmpty()) {
            ResponseData fnData = getSpecificData(KKTCommands.GET_STATUS, "1041");
            regDataStorage.setFnNumber(fnData.getStrValue());
        }
        return regDataStorage.getFnNumber();
    }

    public String getFactoryNum() throws FiscalPrinterException {
        if (regDataStorage.isFactoryNumEmpty()) {
            updateKKTInfo();
        }
        return regDataStorage.getFactoryNum();
    }

    public ResponseData getSpecificData(int cmd, String dataName) throws FiscalPrinterException {
        ResponseData responseData = requestCommandData(cmd, "");
        if (responseData != null) {
            logger.debug("searching {} in response data", dataName);
            ResponseData responseElement = searchElementByName(responseData, dataName);
            if (responseElement != null) {
                return responseElement;
            }
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_PARAM_NOT_FOUND") + " Command: " + cmd + " Data: " + dataName,
                    CashErrorType.NEED_RESTART);
        }
        throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DATA_PACKET"), CashErrorType.NEED_RESTART);
    }

    /**
     * Выполняет команду ККТ, парсинг данных ответа не производится
     */
    public void executeCommand(int cmd, String requestData) throws FiscalPrinterException {
        trans.sendRequest(cmd, requestData);

        ArmResponse response = trans.receiveResponse(cmd);
        checkResponseError(response);
    }

    /**
     * Выполняет команду ККТ
     *
     * @return данные из ответа ККТ
     */
    public ResponseData requestCommandData(int cmd, String requestData) throws FiscalPrinterException {
        trans.sendRequest(cmd, requestData);

        ArmResponse response = trans.receiveResponse(cmd);
        checkResponseError(response);
        return response.getDeserializedData();
    }

    /**
     * Печать отчёта о текущем состоянии расчётов
     *
     * @param cashier - кассир
     */
    public void printFNReport(Cashier cashier) throws FiscalPrinterException {
        PrintFNReport fnReport = new PrintFNReport();

        fnReport.setUserLoginName(cashier.getNullSafeName().trim());
        String dataStr = serializeRequestData(fnReport, PrintFNReport.class);

        executeCommand(KKTCommands.PRINT_FN_REPORT, dataStr);
    }

    /**
     * Печать чека коррекции
     *
     * @param correctionReceipt - сущность чека из базы с параметрами
     * @param cashier           - кассир
     * @return ответные данные из ККТ
     */
    public ResponseData printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws FiscalPrinterException {
        ArmCorrection correction = new ArmCorrection();

        correction.setUserLoginName(cashier.getNullSafeName().trim());
        correction.setUserINN(cashier.getInn());

        switch (correctionReceipt.getAccountSign()) {
            case RECEIPT:
                correction.setReceiptType(1);
                break;
            case SPENDING:
                correction.setReceiptType(3);
                break;
            default:
                break;
        }

        int taxSystem = 1 << correctionReceipt.getTaxSystem().ordinal();
        correction.setTaxSystem(taxSystem);
        correction.setCorrectionType(correctionReceipt.getCorrectionType().ordinal());

        ArmCorrectionReason correctionReason = new ArmCorrectionReason();
        correctionReason.setReasonStr(correctionReceipt.getReason());
        SimpleDateFormat reasonDate = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
        correctionReason.setDocDate(reasonDate.format(correctionReceipt.getReasonDocDate()));
        correctionReason.setDocNumber(correctionReceipt.getReasonDocNumber());
        correction.setCorrectionReason(correctionReason);

        correction = setCorrectionPayments(correction, correctionReceipt.getPayments());
        correction = setCorrectionTaxes(correction, correctionReceipt.getTaxes());

        String dataStr = serializeRequestData(correction, ArmCorrection.class);

        ResponseData receiptData = requestCommandData(KKTCommands.CORRECTION, dataStr);
        return receiptData;
    }

    private ArmCorrection setCorrectionPayments(ArmCorrection correction, Set<CorrectionReceiptPaymentsEntity> payments) {
        for (CorrectionReceiptPaymentsEntity entity : payments) {
            BigDecimal paymentSum = UtilsSP.longPriceToBigDecimal(entity.getPaymentSum());
            BigDecimal currSumm;
            switch (entity.getCorrectionReceiptPaymentsEntityPK().getPaymentName()) {
                case CASH:
                    currSumm = correction.getCashSumm();
                    correction.setCashSumm(currSumm.add(paymentSum));
                    break;
                case ELECTRON:
                    currSumm = correction.getNonCashSumm();
                    correction.setNonCashSumm(currSumm.add(paymentSum));
                    break;
                case PREPAYMENT:
                    currSumm = correction.getPrepaymentSumm();
                    correction.setPrepaymentSumm(currSumm.add(paymentSum));
                    break;
                case POSTPAY:
                    currSumm = correction.getPostpaymentSumm();
                    correction.setPostpaymentSumm(currSumm.add(paymentSum));
                    break;
                case COUNTEROFFER:
                    currSumm = correction.getOncomingSumm();
                    correction.setOncomingSumm(currSumm.add(paymentSum));
                    break;
                default:
                    break;
            }
        }
        return correction;
    }

    private ArmCorrection setCorrectionTaxes(ArmCorrection correction, Set<CorrectionReceiptTaxesEntity> taxes) {
        for (CorrectionReceiptTaxesEntity entity : taxes) {
            BigDecimal taxSum = UtilsSP.longPriceToBigDecimal(entity.getTaxSum());
            BigDecimal currTaxSumm;
            String taxName = entity.getCorrectionReceiptTaxesEntityPK().getTax().getName();
            if (taxName.equalsIgnoreCase("18%")) {
                currTaxSumm = correction.getTax18();
                correction.setTax18(currTaxSumm.add(taxSum));
            } else if (taxName.equalsIgnoreCase("10%")) {
                currTaxSumm = correction.getTax10();
                correction.setTax10(currTaxSumm.add(taxSum));
            } else if (taxName.equalsIgnoreCase("0%")) {
                currTaxSumm = correction.getTax0();
                correction.setTax0(currTaxSumm.add(taxSum));
            } else if (taxName.equalsIgnoreCase("NO_NDS")) {
                currTaxSumm = correction.getTaxNon();
                correction.setTaxNon(currTaxSumm.add(taxSum));
            } else if (taxName.equalsIgnoreCase("18/118")) {
                currTaxSumm = correction.getTax118();
                correction.setTax118(currTaxSumm.add(taxSum));
            } else if (taxName.equalsIgnoreCase("10/110")) {
                currTaxSumm = correction.getTax10();
                correction.setTax10(currTaxSumm.add(taxSum));
            }
        }
        return correction;
    }

    /**
     * Печать X отчёта
     *
     * @param cashier - кассир
     */
    public void printXReport(Cashier cashier) throws FiscalPrinterException {
        PrintXReport xReportData = new PrintXReport();

        xReportData.setUserLoginName(cashier.getNullSafeName().trim());
        String dataStr = serializeRequestData(xReportData, PrintXReport.class);

        executeCommand(KKTCommands.PRINT_X_REPORT, dataStr);
    }

    /**
     * Печать Z отчёта
     *
     * @param cashier - кассир
     */
    public void printZReport(Cashier cashier) throws FiscalPrinterException {
        ShiftCloseZ zReportData = new ShiftCloseZ();

        zReportData.setUserLoginName(cashier.getNullSafeName().trim());
        if (cashier.getInn() != null) {
            zReportData.setUserINN(cashier.getInn());
        }
        String dataStr = serializeRequestData(zReportData, ShiftCloseZ.class);

        executeCommand(KKTCommands.PRINT_Z_REPORT, dataStr);
    }

    public void openShift(Cashier cashier) throws FiscalPrinterException {
        logger.debug("openShift start");
        ShiftOpen shiftData = new ShiftOpen();

        shiftData.setUserLoginName(cashier.getNullSafeName().trim());
        if (cashier.getInn() != null) {
            shiftData.setUserINN(cashier.getInn());
        }
        String dataStr = serializeRequestData(shiftData, ShiftOpen.class);

        executeCommand(KKTCommands.SHIFT_OPEN, dataStr);
        logger.debug("openShift end");
    }

    public Date getDateTime() throws FiscalPrinterException {
        ResponseData dateData = getSpecificData(KKTCommands.GET_DATE, "DateTime");
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
            return dateTimeFormat.parse(dateData.getStrValue());
        } catch (Exception e) {
            logger.debug("getDateTime error: ", e);
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    public void setDateTime(Date date) throws FiscalPrinterException {
        SetDateTime dateData = new SetDateTime();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
        dateData.setDateTime(dateTimeFormat.format(date));
        String dataStr = serializeRequestData(dateData, SetDateTime.class);

        executeCommand(KKTCommands.SET_DATE, dataStr);
    }

    public int getFRTextLength() throws FiscalPrinterException {
        PRNSettings prnSettings = new PRNSettings();
        PrinterParams printerParams = new PrinterParams();
        prnSettings.setPrinterParams(printerParams);

        Settings settings = new Settings();
        settings.setPrnSettings(prnSettings);
        String dataStr = serializeRequestData(settings, Settings.class);

        ResponseData responseData = requestCommandData(KKTCommands.GET_SETTINGS, dataStr);
        ResponseData responseElement = searchElementByName(responseData, "PrintWidthChars");
        if (responseElement != null) {
            String textLength = responseElement.getStrValue();
            logger.debug("KKT text length: {}", textLength);
            return Integer.parseInt(textLength);
        }
        return device.getMaxTextLength();
    }

    /**
     * Инициализировать чек
     *
     * @param checkType - тип документа
     */
    public void openReceipt(CheckType checkType, Cashier cashier) {
        logger.debug("open new Document");

        currReceipt = new ArmReceipt();
        if (checkType == CheckType.SALE) {
            currReceipt.setReceiptType(1);
        } else {
            currReceipt.setReceiptType(2);
        }
        currReceipt.setTaxSystem(1);
        currReceipt.setUserLoginName(cashier.getNullSafeName().trim());
        if (cashier.getInn() != null) {
            currReceipt.setUserINN(cashier.getInn());
        }
    }

    /**
     * Добавить товарную позицию в чек
     *
     * @param name     - название позиции
     * @param quant    - количество товара в килограммах, если 1 шт - то 1.000f
     * @param price    - цена за единицу товара
     * @param taxType  - id НДС позиции из класса TaxType
     * @param itemText - произвольный текст
     */
    public void addPosition(String name, long price, float quant, int taxType, String itemText, String excise, AdditionalInfo addInfo) throws FiscalPrinterException {
        logger.debug("addPosition {} price: {} quant: {}", name, price, quant);

        if (currReceipt == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DOC_STATE"), CashErrorType.NEED_RESTART);
        }
        ArmReceiptItem newPosition = new ArmReceiptItem();
        newPosition.setItemName(name);
        newPosition.setPrice(UtilsSP.longPriceToBigDecimal(price));
        newPosition.setQuantity(BigDecimal.valueOf(quant));
        newPosition.setTaxType(taxType);
        newPosition.setItemText(itemText);
        newPosition.setNomenclatureCode(excise);
        //Поле обязателдьно для печати свободной строкой
        newPosition.setPriceWithDiscount(UtilsSP.longPriceToBigDecimal(price));

        addProviderData(newPosition, addInfo);

        currReceipt.addItem(newPosition);
        //Решинеие проблемы таймаута при большом количестве позиций в чеке.
        trans.addTimeOut(Transport.TIME_OUT1);
    }

    /**
     * Добавить информацию о поставщике в позицию
     * @param position позиция
     * @param addInfo дополнительная информация
     */
    private void addProviderData(ArmReceiptItem position, AdditionalInfo addInfo) {
        if (addInfo == null) {
           return;
        }
        logger.debug("addProviderData inn: {} name: {}", addInfo.getDebitorINN(), addInfo.getDebitorName());
        ArmItemProvider providerData = new ArmItemProvider();
        providerData.setProviderName(addInfo.getDebitorName() == null ? StringUtils.EMPTY : addInfo.getDebitorName());
        providerData.setProviderPhone(addInfo.getDebitorPhone() == null ? StringUtils.EMPTY : addInfo.getDebitorPhone());
        position.setArmItemProvider(providerData);
        position.setProviderINN(addInfo.getDebitorINN() == null ? StringUtils.EMPTY : addInfo.getDebitorINN());
        position.setAgentCalcSign(addInfo.getAgentType().getBitMask());
    }

    /**
     * Добавить признак агента в чек
     * @param agentSign признак агента
     */
    public void addCheckAgentData(int agentSign, String agentPhone) throws FiscalPrinterException {
        logger.debug("addCheckAgentData: {}", agentSign);

        if (currReceipt == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DOC_STATE"), CashErrorType.NEED_RESTART);
        }
        currReceipt.setAgentSign(agentSign);
        currReceipt.setProviderPhone(agentPhone);
    }

    public void addPrintString(String text) {
        logger.debug("addPrintString: \"{}\"", text);
        if (text == null || text.isEmpty()) {
            return;
        }
        if (currReceipt != null) {
            currReceipt.addFooterText(text);
        }
        if (currServiceDoc != null) {
            currServiceDoc.addLine(text);
        }
    }

    public void addItemLine(String text) {
        logger.debug("addItemLine: \"{}\"", text);
        if (text == null || text.isEmpty()) {
            return;
        }
        if (currReceipt != null && !currReceipt.getItems().isEmpty()) {
            currReceipt.getItems().get(0).addItemText(text);
        }
    }

    public void addHeaderLine(String text) {
        logger.debug("addHeaderLine: \"{}\"", text);
        if (text == null || text.isEmpty()) {
            return;
        }
        if (currReceipt != null) {
            currReceipt.addHeaderText(text);
        }
    }

    /**
     * Добавление оплат
     *
     * @param paymentType - код типа оплаты
     * @param amount      - сумма оплаты в копейках
     */
    public void addPayment(int paymentType, BigDecimal amount) throws FiscalPrinterException {
        logger.debug("addPayment, type: {}, amount: {}", paymentType, amount);

        if (currReceipt == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DOC_STATE"), CashErrorType.NEED_RESTART);
        }
        BigDecimal currSumm;
        switch (paymentType) {
            case KKTPaymentType.CASH:
                currSumm = currReceipt.getCashSumm();
                currReceipt.setCashSumm(currSumm.add(amount));
                break;
            case KKTPaymentType.PREPAYMENT:
                currSumm = currReceipt.getPrepaymentSumm();
                currReceipt.setPrepaymentSumm(currSumm.add(amount));
                break;
            case KKTPaymentType.POSTPAYMENT:
                currSumm = currReceipt.getPostpaymentSumm();
                currReceipt.setPostpaymentSumm(currSumm.add(amount));
                break;
            case KKTPaymentType.ONCOMING:
                currSumm = currReceipt.getOncomingSumm();
                currReceipt.setOncomingSumm(currSumm.add(amount));
                break;
            default:
                // Проверяем относится ли тип оплаты к ЭЛЕКТРОННЫМ
                if (paymentType >= KKTPaymentType.NON_CASH && paymentType < KKTPaymentType.PREPAYMENT) {
                    currSumm = currReceipt.getNonCashSumm();
                    currReceipt.setNonCashSumm(currSumm.add(amount));
                } else {
                    throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_UNSUPPORTED_PAYMENT"), CashErrorType.NEED_RESTART);
                }
        }
    }

    /**
     * Сохраняем общую сумму чека, для последующего расчета скидки
     */
    public void setCheckSumm(long amount) throws FiscalPrinterException {
        if (currReceipt == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DOC_STATE"), CashErrorType.NEED_RESTART);
        }
        currReceipt.setCheckSumm(UtilsSP.longPriceToBigDecimal(amount));
    }

    private void closeReceipt() throws FiscalPrinterException {
        logger.debug("closeDocument()");

        if (currReceipt == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_DOC_STATE"), CashErrorType.NEED_RESTART);
        }

        BigDecimal paymentSum = currReceipt.getCashSumm().add(currReceipt.getNonCashSumm())
                .add(currReceipt.getPrepaymentSumm())
                .add(currReceipt.getPostpaymentSumm())
                .add(currReceipt.getOncomingSumm());

        float change = paymentSum.subtract(currReceipt.getCheckSumm()).floatValue();
        logger.debug("receipt change = {}", change);
        if (change > 0.0f && change < paymentSum.floatValue()) {
            currReceipt.setCashSumm(currReceipt.getCashSumm().subtract(BigDecimal.valueOf(change)));
        }

        currReceipt.setPosNum(cashNumber);
        String dataStr = serializeRequestData(currReceipt, ArmReceipt.class);
        currReceipt = null;
        requestCommandData(KKTCommands.PRINT_RECEIPT, dataStr);
    }

    /**
     * Добавление штрих-кода в документ, для печати доступны штрих коды EAN13 и QR
     *
     * @param barcode - штрих-код для печати
     */
    public void printBarCode(BarCode barcode) {
        logger.trace("entering printBarCode(BarCode). The argument is: barcode [{}]", barcode);
        if (barcode == null) {
            logger.error("leaving printBarCode(BarCode): The argument is NULL!");
            return;
        }

        if (currReceipt != null) {
            printReceiptBarCode(barcode);
        }

        if (currServiceDoc != null) {
            printSlipBarCode(barcode);
        }
    }

    /**
     * Добавление штрих-кода в документ, для печати доступны штрих коды EAN13 и QR
     *
     * @param barcode - штрих-код для печати
     */
    private void printReceiptBarCode(BarCode barcode) {
        boolean printTextAtTop = TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.TOP_TEXT.equals(barcode.getTextPosition());
        boolean printTextAtBottom = (TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.BOTTOM_TEXT.equals(barcode.getTextPosition()));
        printTextAtBottom &= !BarCodeType.QR.equals(barcode.getType());

        if (printTextAtTop) {
            currReceipt.addFooterText(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }
        if (BarCodeType.QR.equals(barcode.getType())) {
            String qrcodeStr = QRCODE_HEADER + String.format("%04d", barcode.getValue().length()) + barcode.getValue();
            currReceipt.addFooterText(qrcodeStr);
        }
        if (BarCodeType.EAN13.equals(barcode.getType())) {
            String barcodeStr = BARCODE_HEADER + String.format("%02d", barcode.getValue().length()) + barcode.getValue();
            currReceipt.addFooterText(barcodeStr);
        }
        if (printTextAtBottom) {
            currReceipt.addFooterText(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }
    }

    /**
     * Добавление штрих-кода в документ, для печати доступны штрих коды EAN13 и QR
     *
     * @param barcode - штрих-код для печати
     */
    private void printSlipBarCode(BarCode barcode) {
        boolean printTextAtTop = TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.TOP_TEXT.equals(barcode.getTextPosition());
        boolean printTextAtBottom = (TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.BOTTOM_TEXT.equals(barcode.getTextPosition()));
        printTextAtBottom &= !BarCodeType.QR.equals(barcode.getType());

        if (printTextAtTop) {
            currServiceDoc.addLine(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }
        if (BarCodeType.QR.equals(barcode.getType())) {
            String qrcodeStr = QRCODE_HEADER + String.format("%04d", barcode.getValue().length()) + barcode.getValue();
            currServiceDoc.addLine(qrcodeStr);
        }
        if (BarCodeType.EAN13.equals(barcode.getType())) {
            String barcodeStr = BARCODE_HEADER + String.format("%02d", barcode.getValue().length()) + barcode.getValue();
            currServiceDoc.addLine(barcodeStr);
        }
        if (printTextAtBottom) {
            currServiceDoc.addLine(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }
    }

    /**
     * Инициализировать нефискальный документ
     *
     * @param text - содержимое нефискального документа
     */
    public void openNonFiscalDoc(String text) {
        logger.debug("open new NonFiscal Document");

        currServiceDoc = new ArmNonFiscalDoc();
        currServiceDoc.setNonFiscalText(text);
    }

    /**
     * Печать нефискального документа
     *
     * @param text - текст нефискального документа
     */
    public void printNonFiscalDoc(String text) throws FiscalPrinterException {
        logger.debug("print NonFiscal Document");

        if (currServiceDoc == null) {
            currServiceDoc = new ArmNonFiscalDoc();
        }
        currServiceDoc.addLine(text);
        String dataStr = serializeRequestData(currServiceDoc, ArmNonFiscalDoc.class);
        currServiceDoc = null;
        executeCommand(KKTCommands.PRINT_NONFISCAL, dataStr);
    }

    public void closeDocument() throws FiscalPrinterException {
        if (currReceipt != null) {
            closeReceipt();
        }
        if (currServiceDoc != null) {
            printNonFiscalDoc("");
        }
    }

    /**
     * Аннулировать документ
     */
    public void cancelDocument() throws FiscalPrinterException {
        logger.debug("cancel Document");
        currReceipt = null;
        currServiceDoc = null;
        printNonFiscalDoc(ResBundleFiscalPrinterSP.getString("ANNUL_CHECK"));
    }

    public boolean isDocOpen() {
        return currReceipt != null || currServiceDoc != null;
    }

    public void setCashNumber(long cashNumber) {
        this.cashNumber = String.valueOf(cashNumber);
    }

    /**
     * Внесение/Изъятие
     *
     * @param type   - тип опирации, внесение или изъятие
     * @param amount - сумма
     */
    public void moneyInOut(InventoryOperationType type, long amount) throws FiscalPrinterException {
        logger.debug("money InOut");
        ArmMoneyOperation moneyOperation = new ArmMoneyOperation();

        BigDecimal cashSumm = UtilsSP.longPriceToBigDecimal(amount);
        moneyOperation.setCashSumm(cashSumm);
        switch (type) {
            case CASH_IN:
                moneyOperation.setMoneyOperationType(ArmMoneyOperation.CASH_IN);
                break;
            case CASH_OUT:
                moneyOperation.setMoneyOperationType(ArmMoneyOperation.CASH_OUT);
                break;
            default:
                return;
        }
        String dataStr = serializeRequestData(moneyOperation, ArmMoneyOperation.class);

        executeCommand(KKTCommands.CASH_IN_OUT, dataStr);
    }

    /**
     * Установить режим свободной печати товарной позиции
     *
     * @param useFreeString - true - товарная позиция не печатается, только свободная строка
     */
    public void setItemFreeString(boolean useFreeString) throws FiscalPrinterException {
        logger.debug("set ItemFreeString");

        FDDesign design = new FDDesign();
        design.setUseItemFreeString(Boolean.toString(useFreeString));

        MacroSettings macroSettings = new MacroSettings();
        macroSettings.setFdDesign(design);

        Settings settings = new Settings();
        settings.setMacroSettings(macroSettings);

        String dataStr = serializeRequestData(settings, Settings.class);
        executeCommand(KKTCommands.SET_SETTINGS, dataStr);
    }

    //------------Util methods----------//

    private String serializeRequestData(Object data, Class... classesToBeBound) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(data, sw);
            return sw.toString();
        } catch (JAXBException e) {
            logger.debug("RequestData serialization error: ", e);
        }
        return "";
    }

    public ResponseData searchElementByName(ResponseData data, String name) {
        for (Object element : data.getValue()) {
            if (element instanceof ResponseData) {
                ResponseData paElement = (ResponseData) element;
                if (paElement.getName().equals(name)) {
                    logger.debug("element found in searchElementByName({})", name);
                    return paElement;
                }
                if (paElement.getType().equals(KKTDataType.STRUCT)) {
                    ResponseData childElement = searchElementByName(paElement, name);
                    if (childElement != null) {
                        return childElement;
                    }
                }
            }
        }
        return null;
    }

    private void checkResponseError(ArmResponse response) throws FiscalPrinterException {
        int result = response.getResponseBody().getResult();
        if (result != 0) {
            long errCode = response.getResponseBody().getErrorCode();
            String errDescription = response.getResponseBody().getErrorDescription();
            throw new FiscalPrinterException(errDescription, CashErrorType.NOT_CRITICAL_ERROR, errCode);
        }
    }

    public int getMaxLengthField(MaxLengthField maxLen) {
        if (maxLen == MaxLengthField.DEFAULTTEXT || maxLen == MaxLengthField.PAYMENTNAME) {
            return device.getMaxTextLength();
        } else {
            return device.getMaxSettingsLength();
        }
    }

    public String getNameDevice() {
        if (device == null) {
            return ResBundleFiscalPrinterSP.getString("DEVICE_NAME_FR_SP402FR_K");
        }
        return device.getName();
    }

    /**
     * Вернет количество символов в строке
     *
     * @return не отрицательное число
     */
    public int getSymbolsPerLine() {
        return device.getMaxTextLength();
    }

    public Device getDevice() {
        return device;
    }

}