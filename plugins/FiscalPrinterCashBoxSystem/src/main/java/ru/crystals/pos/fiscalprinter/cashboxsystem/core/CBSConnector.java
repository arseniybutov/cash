package ru.crystals.pos.fiscalprinter.cashboxsystem.core;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.cashboxsystem.ResBundleFiscalPrinterCBS;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.DocHeader;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.Section;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.Tax;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.TicketItem;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint.Footer;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint.Header;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.CbsPaymentType;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Error;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Register;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Settings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.TaxType;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Taxes;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.HttpTransport;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.BaseRequest;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.CashOperation;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.CbsReport;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.GetRegisters;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.GetSettings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.PrintHeader;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.ReportX;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.ReportZ;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.ReportZCopy;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.SetSettings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.Ticket;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.HeaderResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.RegistersResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.ReportResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.SettingsResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.TicketResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.DataSerializer;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.utils.CbsKPKEmulator;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.utils.UtilsCBS;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.property.Properties;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Используется в Казахстане
 */
@PrototypedComponent
public class CBSConnector implements FiscalConnector, Configurable<CBSConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CBSConnector.class);

    private static final int NAME_MAX_LENGTH = 256;

    private static final long COUNT_ORDER = 1000;

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .periodForSendCashParams(Duration.ofMinutes(60))
            .build();

    /**
     * Эмулятор счетчиков документов, CBS своих счетчиков не имеет
     */
    private CbsKPKEmulator kpkEmulator = new CbsKPKEmulator();
    private Long cashNumber;
    private int maxCharRow = 44;

    /**
     * Реквизиты заголовка из CBS
     */
    private DocHeader headerRequisites;

    private ValueAddedTaxCollection taxes;
    private CBSConfig config;

    @Autowired
    private Properties properties;

    @Override
    public Class<CBSConfig> getConfigClass() {
        return CBSConfig.class;
    }

    @Override
    public void setConfig(CBSConfig config) {
        this.config = config;
    }

    /**
     * Инициализировать чек
     *
     * @param checkType - тип документа
     * @return
     */
    private Ticket openReceipt(CheckType checkType) {
        LOG.debug("open new Receipt");
        return new Ticket(checkType);
    }

    /**
     * Добавить товарную позицию в чек
     *  @param good        - товарная позиция
     * @return
     */
    private TicketItem makePosition(Goods good) {
        double taxPercent = good.getTax();
        LOG.debug("addPosition {} price: {} quant: {} tax: {}", good.getName(), good.getEndPricePerUnit(), good.getQuant(), taxPercent);
        TicketItem newPosition = new TicketItem();
        newPosition.setName(good.getName());
        CbsMoney money = CbsMoney.getMoneyFromLong(good.getEndPricePerUnit());
        newPosition.setPrice(money);
        newPosition.setRoundTotal(config.isUseRounding());
        if (config.isUseRounding()) {
            long dif = good.getEndPositionPrice() - Math.round(((double) (good.getEndPricePerUnit() * good.getQuant()) / 1000));
            if (dif != 0) {
                final CbsMoney difMoney = CbsMoney.getMoneyFromLong(Math.abs(dif));
                if (dif > 0) {
                    newPosition.setMarkupSum(difMoney);
                } else {
                    newPosition.setDiscountSum(difMoney);
                }
            }
        }
        newPosition.setCount((double) good.getQuant() / COUNT_ORDER);
        Section section = createDefaultSection();
        Tax tax = new Tax();
        if (taxPercent >= 0) {
            tax.setTaxType(TaxType.TAX_VAT.getCode());
            tax.setPercent(taxPercent);
        } else {
            tax.setTaxType(TaxType.TAX_WITHOUT_VAT.getCode());
            final Double nullPercent = 0.0;
            tax.setPercent(nullPercent);
        }
        section.setTax(tax);
        newPosition.setSection(section);

        return newPosition;
    }

    /**
     * Добавление оплат
     *
     * @param currReceipt
     * @param paymentId   - код типа оплаты
     * @param amount      - сумма оплаты в копейках
     */
    private void addPayment(Ticket currReceipt, int paymentId, long amount) throws FiscalPrinterException {
        CbsPaymentType paymentType = CbsPaymentType.typeFromCode(paymentId);
        switch (paymentType) {
            case PAYMENT_CASH:
                currReceipt.addPaymentCash(amount);
                break;
            case PAYMENT_CARD:
                currReceipt.addPaymentDebit(amount);
                break;
            case PAYMENT_CREDIT:
                currReceipt.addPaymentCredit(amount);
                break;
            case PAYMENT_TARE:
                currReceipt.addPaymentTare(amount);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported payment type: " + paymentType);
        }
    }

    @Override
    public void registerCheck(Check check) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerCheck(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            final Ticket currReceipt = openReceipt(check.getType());
            // add goods
            for (Goods good : check.getGoods()) {
                currReceipt.addItem(makePosition(good));
            }

            // add payments
            for (Payment payment : check.getPayments()) {
                LOG.debug("putPayment: {}, index({}), indexFFD100({})", payment.toString(), payment.getIndexPayment(), payment.getIndexPaymentFDD100());
                int codePayment = (int) payment.getIndexPaymentFDD100();
                addPayment(currReceipt, codePayment, payment.getSum());
            }

            TicketResponse fiscalDoc = closeReceipt(currReceipt, check);
            updateSectionList(sectionList, fiscalDoc);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private TicketResponse closeReceipt(Ticket currReceipt, Check check) throws FiscalPrinterException {
        LOG.debug("closeDocument()");

        currReceipt.setNotPrint(true);
        currReceipt.setOperatorName(check.getCashier().getNullSafeName().trim());
        currReceipt.setShiftNumber(check.getShiftNum().intValue());
        currReceipt.setKkmPos(cashNumber.intValue());

        return sendRequest(currReceipt);
    }

    /**
     * Добавление строк в sectionList для последующей печати на нефискальном
     * принтере
     *
     * @param sectionList строки для нефискальной печати
     * @param fiscalDoc   данные от CBS
     */
    private void updateSectionList(List<DocumentSection> sectionList, TicketResponse fiscalDoc) throws FiscalPrinterException {
        if (fiscalDoc != null) {
            Header docHeader = fiscalDoc.getDocTicketPrint().getHeader();
            Footer docFooter = fiscalDoc.getDocTicketPrint().getFooter();
            //Допоняем заголовок если чеки "офлайн"
            if (docHeader.isOffline()) {
                addHeaderLines(sectionList, docHeader);
            }
            //Формируем подвал для печати на чеке
            addFooterLines(sectionList, docFooter);
        }
    }

    /**
     * Добавление строк на печать в подвал чека
     *
     * @param sectionList список строк на печать
     * @param docFooter   подвал чека из ПФ
     */
    private void addFooterLines(List<DocumentSection> sectionList, Footer docFooter) {
        List<FontLine> footerContent = getFontLinesFromString(docFooter.getOfdInfo());
        FontLine factoryLine = new FontLine(ResBundleFiscalPrinterCBS.getString("PRINT_FACTORY_NUM") + getFactoryNum());
        footerContent.add(factoryLine);

        if (docFooter.getQrCode() != null) {
            BarCode ofdQR = new BarCode(docFooter.getQrCode());
            ofdQR.setType(BarCodeType.QR);
            ofdQR.setTextPosition(TextPosition.NONE_TEXT);
            FontLine ofdQRLine = new FontLine(ofdQR.getValue());
            ofdQRLine.setBarcode(ofdQR);
            ofdQR.setWidth(0);
            footerContent.add(ofdQRLine);
        }

        sectionList.add(new DocumentSection("footer", footerContent));
    }

    private void addHeaderLines(List<DocumentSection> sectionList, Header docHeader) {
        for (DocumentSection section : sectionList) {
            if (FiscalPrinterPlugin.SECTION_HEADER.equals(section.getName())) {
                List<FontLine> headerContent = section.getContent();

                List<FontLine> additionalContent = new ArrayList<>();
                if (docHeader != null && docHeader.isOffline()) {
                    additionalContent.add(getOfflineLine());
                }

                //Добовляем строки в начало секции
                headerContent.addAll(0, additionalContent);
                section.setContent(headerContent);
            }
        }
    }

    private FontLine getOfflineLine() {
        return new FontLine(StringUtils.center(ResBundleFiscalPrinterCBS.getString("PRINT_OFFLINE_STRING"), maxCharRow), Font.NORMAL);
    }

    private List<FontLine> getFontLinesFromString(String text) {
        List<FontLine> lines = new ArrayList<>();
        for (String line : text.split(System.getProperty("line.separator"))) {
            lines.add(new FontLine(line));
        }
        return lines;
    }

    private HeaderResponse printHeader() throws FiscalPrinterException {
        PrintHeader printHeader = new PrintHeader();
        printHeader.setNotPrint(true);

        return sendRequest(printHeader);
    }

    private SettingsResponse getSettings(List<Settings> settings) throws FiscalPrinterException {
        GetSettings getSettings = new GetSettings();
        getSettings.addSettings(settings);

        return sendRequest(getSettings);
    }

    private String getSetting(Settings setting) throws FiscalPrinterException {
        SettingsResponse response = getSettings(Collections.singletonList(setting));
        return response.getSettingValue(setting);
    }

    private void setSettings(Map<Settings, String> settings) throws FiscalPrinterException {
        SetSettings setSettings = new SetSettings();
        setSettings.addSettings(settings);

        sendRequest(setSettings);
    }

    @Override
    public void start() throws FiscalPrinterException {
        String cbsKkmId = getSetting(Settings.SETTINGS_KKMID);
        String kkmId = config.getKkmId();
        String token = config.getToken();
        LOG.debug("CBS config. KKMID: {} TOKEN: {}", kkmId, token);
        if ("0".equals(cbsKkmId)) {
            //Первичная настройка ID
            if (StringUtils.isEmpty(kkmId) || StringUtils.isEmpty(token)) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterCBS.getString("ERROR_INIT_DATA"), CashErrorType.FISCAL_ERROR);
            }

            Map<Settings, String> settings = new HashMap<>();
            settings.put(Settings.SETTINGS_KKMID, kkmId);
            settings.put(Settings.SETTINGS_TOKEN, token);

            setSettings(settings);
        }
        try {
            kpkEmulator.loadState();
            this.cashNumber = properties.getCashNumber();

            Map<Settings, String> settings = new HashMap<>();
            //Отключение автоинкассации на закрытии смены
            settings.put(Settings.SETTINGS_IS_AUTO_WITHDRAWAL, String.valueOf(false));
            //Включение псевдо-оффлайн режима
            settings.put(Settings.SETTINGS_IS_ENABLE_PSEUDO_OFFLINE, String.valueOf(config.isPseudoOffline()));
            setSettings(settings);

        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
        if (headerRequisites == null) {
            //"Печатая" заголовок мы запрашиваем реквизиты из CBS
            HeaderResponse response = printHeader();
            headerRequisites = response.getDocHeader();
        }
    }

    private RegistersResponse getRegisters(List<Register> registers) throws FiscalPrinterException {
        GetRegisters getRegisters = new GetRegisters();
        getRegisters.addRegisters(registers);

        return sendRequest(getRegisters);
    }

    private String getRegister(Register register) throws FiscalPrinterException {
        RegistersResponse response = getRegisters(Collections.singletonList(register));
        return response.getRegisterValue(register);
    }

    public void moneyOperation(Money moneyDoc, long totalSum, String operatorName) throws FiscalPrinterException {
        CashOperation cashOperation = new CashOperation(moneyDoc.getOperationType());
        cashOperation.setNotPrint(true);
        cashOperation.setOperatorName(operatorName);
        cashOperation.setShiftNumber(moneyDoc.getShiftNum().intValue());
        cashOperation.setKkmPos(cashNumber.intValue());

        CbsMoney sum = CbsMoney.getMoneyFromLong(totalSum);
        cashOperation.setSum(sum);

        sendRequest(cashOperation);
    }

    @Override
    public void registerReport(Report report) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerReport(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            if (report.isZReport()) {
                fiscalizeZReport(sectionList, report);
            } else {
                fiscalizeXReport(sectionList, report);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void fiscalizeZReport(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        ReportZ reportZ = new ReportZ();
        kpkEmulator.setZReportStarted(true);
        ReportResponse response = sendReport(reportZ, report);
        kpkEmulator.incShiftNum();
        kpkEmulator.setZReportStarted(false);
        updateReportSectionList(sectionList, response);
    }

    private void fiscalizeXReport(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        String isShiftOpen = getRegister(Register.REGISTER_OPEN_SHIFT);
        CbsReport reportX = Boolean.parseBoolean(isShiftOpen) ? new ReportX() : new ReportZCopy();
        ReportResponse response = sendReport(reportX, report);
        updateReportSectionList(sectionList, response);
    }

    private ReportResponse sendReport(CbsReport cbsReport, Report report) throws FiscalPrinterException {
        cbsReport.setNotPrint(true);
        cbsReport.setOperatorName(report.getCashier().getName());
        cbsReport.setShiftNumber((int) kpkEmulator.getShiftNum());
        cbsReport.setKkmPos(cashNumber.intValue());

        ReportResponse response = sendRequest(cbsReport);
        return response;
    }

    /**
     * //Добавляем строки с ЗНМ, РНМ и временем на печать в конец X/Z отчета
     *
     * @param sectionList    список строк на печать
     * @param reportResponse данные X/Z отчета
     */
    private void updateReportSectionList(List<DocumentSection> sectionList, ReportResponse reportResponse) {
        if (reportResponse != null) {
            List<FontLine> footerContent = new ArrayList<>();

            String nonNullableSaleStart = "=" + CurrencyUtil.convertMoneyToText(
                    reportResponse.getDocReportPrint().
                            getNoneNullableSumBegin().
                            getSumSale().
                            getLongFromMoney());
            FontLine nonNullableSaleLineStart = new FontLine(UtilsCBS.textSidesAlign(
                    ResBundleFiscalPrinterCBS.getString("NON_NULLABLE_SALE_START"), nonNullableSaleStart, maxCharRow
            ));
            footerContent.add(nonNullableSaleLineStart);
            String nonNullableReturnStart = "=" + CurrencyUtil.convertMoneyToText(
                    reportResponse.getDocReportPrint().
                            getNoneNullableSumBegin().
                            getSumSaleReturn().
                            getLongFromMoney());
            FontLine nonNullableReturnLineStart = new FontLine(UtilsCBS.textSidesAlign(
                    ResBundleFiscalPrinterCBS.getString("NON_NULLABLE_RETURN_START"), nonNullableReturnStart, maxCharRow
            ));
            footerContent.add(nonNullableReturnLineStart);

            String nonNullableSaleEnd = "=" + CurrencyUtil.convertMoneyToText(
                    reportResponse.getDocReportPrint().
                            getNoneNullableSum().
                            getSumSale().
                            getLongFromMoney());
            FontLine nonNullableSaleLineEnd = new FontLine(UtilsCBS.textSidesAlign(
                    ResBundleFiscalPrinterCBS.getString("NON_NULLABLE_SALE"), nonNullableSaleEnd, maxCharRow
            ));
            footerContent.add(nonNullableSaleLineEnd);
            String nonNullableReturnEnd = "=" + CurrencyUtil.convertMoneyToText(
                    reportResponse.getDocReportPrint().
                            getNoneNullableSum().
                            getSumSaleReturn().
                            getLongFromMoney());
            FontLine nonNullableReturnLineEnd = new FontLine(UtilsCBS.textSidesAlign(
                    ResBundleFiscalPrinterCBS.getString("NON_NULLABLE_RETURN"), nonNullableReturnEnd, maxCharRow
            ));
            footerContent.add(nonNullableReturnLineEnd);

            FontLine factoryLine = new FontLine(ResBundleFiscalPrinterCBS.getString("PRINT_FACTORY_NUM") + getFactoryNum());
            footerContent.add(factoryLine);
            FontLine regNumLine = new FontLine(ResBundleFiscalPrinterCBS.getString("PRINT_REG_NUM") + getRegNum());
            footerContent.add(regNumLine);

            String timeStr = UtilsCBS.getTimeString(reportResponse.getDocReportPrint().getDateTime());
            FontLine timeLine = new FontLine(ResBundleFiscalPrinterCBS.getString("PRINT_TIME") + timeStr);
            footerContent.add(timeLine);

            sectionList.add(new DocumentSection("footer", footerContent));
        }
    }

    /**
     * Возвращает секцию с имеием "Секция 1" и номером 1, без налога
     *
     * @return Объект секции с заполнеными полями имени и номера
     */
    private Section createDefaultSection() {
        Section section = new Section();
        final String sectionName = "Секция 1";
        section.setName(sectionName);
        final Long sectionCode = 1L;
        section.setCode(sectionCode);
        return section;
    }

    /**
     * Отправка запроса в CBS
     *
     * @param baseRequest запрос на отправку
     * @param <T>         Тип ответа на запрос
     * @return Объект с ответом от CBS соответствующего типа
     * @throws FiscalPrinterCommunicationException при ошибках в обмене данными
     */
    private <T> T sendRequest(BaseRequest baseRequest) throws FiscalPrinterException {
        try {
            LOG.debug("entering sendRequest({})", baseRequest.getTarget());
            String json = new HttpTransport(baseRequest).send();
            T response = DataSerializer.getInstance().deserialize(json, baseRequest.getResponseClass());
            checkForApiError(response);
            LOG.debug("leaving sendRequest({})", baseRequest.getTarget());
            return response;
        } catch (IOException e) {
            LOG.debug("Deserialization error: ", e);
            throw new FiscalPrinterException(ResBundleFiscalPrinterCBS.getString("ERROR_DESERIALIZE"), CashErrorType.FISCAL_ERROR);
        }
    }

    private <T> void checkForApiError(T response) throws FiscalPrinterException {
        //Есть "ответы" не относящиеся к BaseResponse, они ошибок не возвращают
        if (response instanceof BaseResponse) {
            Error error = ((BaseResponse) response).getError();
            if (error != Error.API_ERROR_NO) {
                throw new FiscalPrinterException(((BaseResponse) response).getErrorText(), CashErrorType.FISCAL_ERROR);
            }
        }
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        if (kpkEmulator.isZReportStarted()) {
            LOG.info("Interrupted Z report detected");
            if (!isShiftOpen()) {
                kpkEmulator.incShiftNum();
            }
            kpkEmulator.setZReportStarted(false);
        }
        return kpkEmulator.getShiftNum();
    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
        try {
            long result = Long.parseLong(getRegister(Register.REGISTER_SHIFT_TICKET_DOCUMENT_NUMBER));
            //При отсутствии документов в смене передаем -1, что бы сверка счетчиков его пропустила
            if (result == 0) {
                return -1L;
            }
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashInCount() throws FiscalPrinterException {
        try {
            long result = Long.parseLong(getRegister(Register.REGISTER_CASH_IN_COUNT));
            LOG.info("CashIn = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashOutCount() throws FiscalPrinterException {
        try {
            long result = Long.parseLong(getRegister(Register.REGISTER_CASH_OUT_COUNT));
            LOG.info("CashOut = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        try {
            BigDecimal result = new BigDecimal(getRegister(Register.REGISTER_CASH_SUM));
            LOG.info("CashAmount = {}", result);
            return CurrencyUtil.convertMoney(result);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        try {
            return Boolean.parseBoolean(getRegister(Register.REGISTER_OPEN_SHIFT));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterCBS.getString("DEVICE_NAME");
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            ShiftCounters shiftCounters = new ShiftCounters();

            // Запрашиваем данные из CBS
            List<Register> request = Arrays.asList(Register.REGISTER_PAYMENT_SALE_CASH, Register.REGISTER_PAYMENT_SALE_DEBIT,
                    Register.REGISTER_PAYMENT_SALE_CREDIT, Register.REGISTER_SALE_SUM, Register.REGISTER_PAYMENT_SALE_RETURN_CASH,
                    Register.REGISTER_PAYMENT_SALE_RETURN_DEBIT, Register.REGISTER_PAYMENT_SALE_RETURN_CREDIT, Register.REGISTER_RETURN_SALE_SUM,
                    Register.REGISTER_CASH_SUM, Register.REGISTER_SALE_COUNT, Register.REGISTER_RETURN_SALE_COUNT, Register.REGISTER_CASH_IN_COUNT,
                    Register.REGISTER_CASH_OUT_COUNT, Register.REGISTER_CASH_IN_SUM, Register.REGISTER_CASH_OUT_SUM);
            RegistersResponse registers = getRegisters(request);

            /* Номер смены */
            shiftCounters.setShiftNum(getShiftNum());

            /* Оплаты */
            Long sumCash = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_CASH));
            shiftCounters.setSumCashPurchase(sumCash);

            Long sumDebit = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_DEBIT));
            Long sumCredit = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_CREDIT));
            shiftCounters.setSumCashlessPurchase(sumDebit + sumCredit);

            Long sumSale = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_SALE_SUM));
            shiftCounters.setSumSale(sumSale);

            /* Возвраты */
            sumCash = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_RETURN_CASH));
            shiftCounters.setSumCashReturn(sumCash);

            sumDebit = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_RETURN_DEBIT));
            sumCredit = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_PAYMENT_SALE_RETURN_CREDIT));
            shiftCounters.setSumCashlessReturn(sumDebit + sumCredit);

            sumSale = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_RETURN_SALE_SUM));
            shiftCounters.setSumReturn(sumSale);

            /* Сумма наличных в денежном ящике */
            Long sumCashEnd = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_CASH_SUM));
            shiftCounters.setSumCashEnd(sumCashEnd);

            /* Количество чеков по типам операций */
            shiftCounters.setCountSale(registers.getRegisterLong(Register.REGISTER_SALE_COUNT));
            shiftCounters.setCountReturn(registers.getRegisterLong(Register.REGISTER_RETURN_SALE_COUNT));
            shiftCounters.setCountCashIn(registers.getRegisterLong(Register.REGISTER_CASH_IN_COUNT));
            shiftCounters.setCountCashOut(registers.getRegisterLong(Register.REGISTER_CASH_OUT_COUNT));

            Long sumCashIn = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_CASH_IN_SUM));
            shiftCounters.setSumCashIn(sumCashIn);
            Long sumCashOut = CurrencyUtil.convertMoney(registers.getRegisterAsBigDecimal(Register.REGISTER_CASH_OUT_SUM));
            shiftCounters.setSumCashOut(sumCashOut);

            /* Количество оплат по чекам. Нет данных */
            shiftCounters.setCountCashPurchase(0L);
            shiftCounters.setCountCashlessPurchase(0L);
            shiftCounters.setCountCashReturn(0L);
            shiftCounters.setCountCashlessReturn(0L);

            return shiftCounters;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        LOG.debug("makeMoneyOperation()");
        long totalSum = 0;
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            for (BankNote bankNote : money.getBankNotes()) {
                totalSum += bankNote.getValue();
            }
        } else {
            for (BankNote bankNote : money.getBankNotes()) {
                totalSum += bankNote.getValue() * bankNote.getCount();
            }
            if (money.getSumCoins() != null) {
                totalSum += money.getSumCoins();
            }
        }
        String cashierName = getCashierName(Optional.ofNullable(money.getCashier()).orElse(new Cashier("", "", "")));

        moneyOperation(money, totalSum, cashierName);
    }

    private String getCashierName(Cashier cashier) {
        return StringUtils.left(cashier.getNullSafeName(), NAME_MAX_LENGTH);
    }

    @Override
    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }

    @Override
    public String getINN() {
        return headerRequisites != null ? headerRequisites.getIinBin() : null;
    }

    @Override
    public String getRegNum() {
        return headerRequisites != null ? headerRequisites.getKkmRegisterNumber() : null;
    }

    @Override
    public String getFactoryNum() {
        return headerRequisites != null ? headerRequisites.getKkmSerialNumber() : null;
    }

    @Override
    public FnInfo getFnInfo() throws FiscalPrinterException {
        FnInfo fnInfo = new FnInfo();
        fnInfo.setLastFDNumber(String.valueOf(getLastFiscalDocId()));
        String offlineBeginDate = UtilsCBS.getDateTimeString(getRegister(Register.REGISTER_OFFLINE_TIME_BEGIN));
        fnInfo.setFirstNotSendedFDDate(offlineBeginDate);
        return fnInfo;
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        if (taxes != null) {
            return taxes;
        }

        taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(Taxes.NDS12.getId(), Taxes.NDS12.getValue(), Taxes.NDS12.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NO_NDS.getId(), Taxes.NO_NDS.getValue(), Taxes.NO_NDS.name()));

        LOG.debug("getTaxes(): {}", taxes);
        return taxes;
    }

    @Override
    public void openShift(Cashier cashier) {
        //
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    @Override
    public boolean isRegistrationBeforeTemplateProcessing() {
        return true;
    }
}
