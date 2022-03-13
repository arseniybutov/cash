package ru.crystals.pos.fiscalprinter.nfd;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.ExtraFiscalDocType;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardInfoReport;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardReplaceReport;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.checkdata.NfdReceipt;
import ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber;
import ru.crystals.pos.fiscalprinter.nfd.techprocessdata.Taxes;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NFDMode;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.ShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.BarcodeDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.QrCodeDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.closedocument.CloseDocumentResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getstate.GetStateResponse;
import ru.crystals.pos.fiscalprinter.nfd.utils.UtilsNFD;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Используется в Казахстане
 */
@PrototypedComponent
public class NFDServiceImpl implements FiscalConnector, Configurable<NFDConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(NFDServiceImpl.class);

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .fiscalDocTypes(ExtraFiscalDocType.onlyMoney())
            .canReturnFullLastDocInfo(false)
            .periodForSendCashParams(Duration.ofMinutes(60))
            .build();

    private NFDConnector nfdConnector;

    private ValueAddedTaxCollection taxes;

    private List<CommonDocumentEntry> lastDocumentEntriesData;
    /**
     * Паттерн для получения значения ФП из ссылки ОФД
     */
    private final Pattern ofdLinkFPPattern = Pattern.compile("http.*i=([0-9]+)(?:&).*");

    private NFDConfig config;
    private int maxCharRow;

    @Override
    public Class<NFDConfig> getConfigClass() {
        return NFDConfig.class;
    }

    @Override
    public void setConfig(NFDConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        try {
            nfdConnector = new NFDConnector(config.getNfdUri());
            //Инициализируем NFD данными из ОФД
            initKKM();

            getTaxes();
            nfdConnector.setUseRounding(config.isUseRounding());
            configureTaxes();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            LOG.error("", e);
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Запрашивает статус NFD и инициализирует через Token если необходимо
     */
    private void initKKM() throws FiscalPrinterException {
        LOG.debug("NFD config. TOKEN: {}", config.getToken());
        nfdConnector.getStorageEmulator().loadState();
        GetStateResponse getStateResponse = nfdConnector.getState();
        final NFDMode mode = getStateResponse.getStateResultObject().getMode();
        if (mode.equals(NFDMode.INITIAL)) {
            firstInit();
        }
        nfdConnector.cancelDocOfOpened(mode);
        nfdConnector.validateFiscalData();
    }

    private void firstInit() throws FiscalPrinterException {
        //Первичная настройка
        if (StringUtils.isEmpty(config.getToken())) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("ERROR_INIT_DATA"), CashErrorType.FISCAL_ERROR);
        }
        nfdConnector.performInitialization(config.getToken());
        nfdConnector.confirmInitialization();
        GetStateResponse stateAfterInit = nfdConnector.getState();
        if (!stateAfterInit.getStateResultObject().getMode().equals(NFDMode.SHIFT_CLOSED)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("ERROR_INIT_DATA"), CashErrorType.FISCAL_ERROR);
        }
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterNFD.getString("DEVICE_NAME");
    }

    /**
     * Регистрационный номер ФН
     *
     * @return номер ФН
     */
    @Override
    public String getFNNum() {
        return nfdConnector.getStorageEmulator().getSerialNumber();
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return String.valueOf(nfdConnector.getStorageEmulator().getBIN());
    }

    /**
     * Заводской номер ФН
     *
     * @return номер ФН
     */
    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        return String.valueOf(nfdConnector.getStorageEmulator().getRNM());
    }

    @Override
    public void openShift(Cashier cashier) {

    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
        try {
            return nfdConnector.getState().getStateResultObject().getContinuousDocumentNumber();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Запрос регистрационного номера ФН
     *
     * @return регистрационный номер ПН
     */
    @Override
    public String getRegNum() throws FiscalPrinterException {
        return String.valueOf(nfdConnector.getStorageEmulator().getRNM());
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        try {
            NFDMode currentMode = nfdConnector.getState().getStateResultObject().getMode();
            return currentMode != NFDMode.SHIFT_CLOSED && currentMode != NFDMode.INITIAL;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            ShiftCounters shiftCounters = new ShiftCounters();

            ShiftAccumulation shiftAccumulations = nfdConnector.getShiftAccumulations().getShiftAccumulationData();

            /* Номер смены */
            shiftCounters.setShiftNum(nfdConnector.getState().getStateResultObject().getShiftNumber());

            /* Оплаты нал*/
            shiftCounters.setSumCashPurchase(shiftAccumulations.getCashSaleSum());

            /* Оплаты безнал*/
            shiftCounters.setSumCashlessPurchase(shiftAccumulations.getCashlessSaleSum());

            /* Возвраты нал */
            shiftCounters.setSumCashReturn(shiftAccumulations.getCashReturnSum());

            /* Возвраты безнал */
            shiftCounters.setSumCashlessReturn(shiftAccumulations.getCashlessReturnSum());

            /* Возвраты */
            shiftCounters.setSumReturn(shiftAccumulations.getAllReturnSum());

            /* Продажи*/
            shiftCounters.setSumSale(shiftAccumulations.getAllSaleSum());

            /* Сумма наличных в денежном ящике */
            Long sumCashEnd = shiftAccumulations.getCashSumShiftAccumulation();
            shiftCounters.setSumCashEnd(sumCashEnd);

            /* Количество чеков по типам операций */
            shiftCounters.setCountSale(shiftAccumulations.getSellCountShiftAccumulation());
            shiftCounters.setCountReturn(shiftAccumulations.getReturnCountShiftAccumulation());
            shiftCounters.setCountCashIn(shiftAccumulations.getDepositCountShiftAccumulation());
            shiftCounters.setCountCashOut(shiftAccumulations.getWithdrawalCountShiftAccumulation());

            Long sumCashIn = shiftAccumulations.getDepositSumShiftAccumulation();
            shiftCounters.setSumCashIn(sumCashIn);
            Long sumCashOut = shiftAccumulations.getWithdrawalSumShiftAccumulation();
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
    public void registerReport(Report report) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerReport(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            final BaseResponse response;
            if (report.isZReport()) {
                response = nfdConnector.fiscalizeZReport();
            } else {
                response = nfdConnector.xReport();
            }
            updateReportSectionList(sectionList, response);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        if (taxes != null) {
            return taxes;
        }

        taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(Taxes.NDS12.getId(), Taxes.NDS12.getValue(), Taxes.NDS12.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NDS8.getId(), Taxes.NDS8.getValue(), Taxes.NDS8.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NO_NDS.getId(), Taxes.NO_NDS.getValue(), Taxes.NO_NDS.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NDS0.getId(), Taxes.NDS0.getValue(), Taxes.NDS0.name()));
        return taxes;
    }

    private void configureTaxes() {
        try {
            nfdConnector.configureTaxGroup(TaxGroupNumber.NDS_8_GROUP);
        } catch (Exception e) {
            LOG.error("Unable to configure NDS 8", e);
        }
        try {
            nfdConnector.configureTaxGroup(TaxGroupNumber.NDS_0_GROUP);
        } catch (Exception e) {
            LOG.error("Unable to configure NDS 0", e);
        }
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        long shiftNumber = nfdConnector.getState().getStateResultObject().getShiftNumber();
        LOG.debug("shift number: {}", shiftNumber);
        return shiftNumber;
    }

    @Override
    public long getCashInCount() throws FiscalPrinterException {
        try {
            long result = nfdConnector.getMoneyPlacementShiftAccumulation()
                    .getShiftAccumulationData()
                    .getDepositCountShiftAccumulation();
            LOG.debug("CashIn = {}", result);
            return result;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashOutCount() throws FiscalPrinterException {
        try {
            long result = nfdConnector.getMoneyPlacementShiftAccumulation()
                    .getShiftAccumulationData()
                    .getWithdrawalCountShiftAccumulation();
            LOG.debug("CashOut = {}", result);
            return result;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        try {
            long result = nfdConnector.getShiftAccumulations()
                    .getShiftAccumulationData()
                    .getCashSumShiftAccumulation();
            LOG.debug("CashAmount = {}", result);
            return result;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void registerCheck(Check check) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerCheck(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        checkOpenShift();
        final NfdReceipt nfdReceipt = nfdConnector.openReceipt(check);
        for (Goods good : check.getGoods()) {
            nfdConnector.addPosition(nfdReceipt, good);
        }
        CloseDocumentResponse closeDocumentResponse = nfdConnector.closeReceipt(nfdReceipt, check);
        lastDocumentEntriesData = closeDocumentResponse.getCloseDocumentTextData();
        updateSectionList(sectionList, closeDocumentResponse.getCloseDocumentTextData());
    }

    @Override
    public void processNonFiscal(List<DocumentSection> sectionList, AbstractDocument document) throws FiscalPrinterException {
        if (isDocumentAfterCheck(document)) {
            updateHeaderSectionList(sectionList, lastDocumentEntriesData);
        }
    }

    private boolean isDocumentAfterCheck(AbstractDocument document) {
        return document instanceof DiscountsReport ||
                document instanceof PresentCardInfoReport ||
                document instanceof PresentCardReplaceReport;
    }

    /**
     * Добавление строк в sectionList для последующей печати на нефискальном принтере
     */
    private void updateSectionList(List<DocumentSection> sectionList, List<CommonDocumentEntry> commonDocumentEntries) throws FiscalPrinterException {
        updateHeaderSectionList(sectionList, commonDocumentEntries);
        updateFiscalSectionList(sectionList, commonDocumentEntries);
    }

    private void updateHeaderSectionList(List<DocumentSection> sectionList, List<CommonDocumentEntry> commonDocumentEntries) throws FiscalPrinterException {
        for (DocumentSection section : sectionList) {
            if (FiscalPrinterPlugin.SECTION_HEADER.equals(section.getName())) {
                List<FontLine> innerHeader = section.getContent();
                section.setContent(buildHeaderFromDocumentData(commonDocumentEntries));
                section.getContent().addAll(innerHeader);
            }
        }
    }

    private void updateFiscalSectionList(List<DocumentSection> sectionList, List<CommonDocumentEntry> commonDocumentEntries) {
        for (DocumentSection section : sectionList) {
            if (FiscalPrinterPlugin.SECTION_FISCAL.equals(section.getName())) {
                section.getContent().addAll(getOfdDocLinkFromDocumentEntries(commonDocumentEntries));
                section.getContent().addAll(getOfdReqFromDocumentEntries(commonDocumentEntries));
            }
        }
    }

    @Override
    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }

    private List<FontLine> buildHeaderFromDocumentData(List<CommonDocumentEntry> commonDocumentEntries) throws FiscalPrinterException {
        final String fiscalString = "ФИСКАЛЬНЫЙ ПРИЗНАК";
        final String docKPKPrefix = "#";
        final int rowSize = maxCharRow;

        try {
            String dataString = docKPKPrefix + getLastFiscalDocId();
            Optional<String> ofdLink = commonDocumentEntries.stream().
                    filter(c -> c instanceof BarcodeDocumentEntry || c instanceof QrCodeDocumentEntry).
                    map(CommonDocumentEntry::getData).findFirst();
            Matcher m = ofdLinkFPPattern.matcher(ofdLink.orElse(StringUtils.EMPTY));
            if (m.matches()) {
                dataString = m.group(1) + dataString;
            }

            String resultString = String.format("%1$-" + (rowSize - dataString.length()) + "s", fiscalString) + dataString;
            return Stream.of(new FontLine(resultString)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new FiscalPrinterException("Error on getting document data", e);
        }
    }

    private List<FontLine> getOfdDocLinkFromDocumentEntries(List<CommonDocumentEntry> commonDocumentEntries) {
        List<FontLine> result = new ArrayList<>();
        Optional<String> optOfdLink = commonDocumentEntries.stream().
                filter(c -> c instanceof BarcodeDocumentEntry || c instanceof QrCodeDocumentEntry).
                map(CommonDocumentEntry::getData).findFirst();
        if (optOfdLink.isPresent()) {
            BarCode qrCode = new BarCode(optOfdLink.get());
            qrCode.setType(BarCodeType.QR);
            qrCode.setTextPosition(TextPosition.NONE_TEXT);
            FontLine ofdQRLine = new FontLine(qrCode.getValue());
            ofdQRLine.setBarcode(qrCode);
            result.add(ofdQRLine);
        }
        return result;
    }

    private List<FontLine> getOfdReqFromDocumentEntries(List<CommonDocumentEntry> commonDocumentEntries) {
        List<FontLine> result = new ArrayList<>();
        String ofdString = "OФД";
        String dataString;
        String resultString;
        Optional<FontLine> optOfdReq = commonDocumentEntries.stream().filter(c -> c.getData().contains("ОФД -")).map(v -> new FontLine(v.getData())).findFirst();
        if (optOfdReq.isPresent()) {
            int rowSize = maxCharRow;
            dataString = optOfdReq.get().getContent().substring(optOfdReq.get().getContent().indexOf("") + ofdString.length() + 1);
            resultString = String.format("%1$-" + (rowSize - dataString.length()) + "s", ofdString) + dataString;
            FontLine ofdReqLine = new FontLine(resultString);
            result.add(ofdReqLine);
        }
        return result;
    }

    /**
     * Добавляем строки с ЗНМ, РНМ и временем на печать в конец X/Z отчета
     *
     * @param sectionList    список строк на печать
     * @param reportResponse данные X/Z отчета
     */
    private void updateReportSectionList(List<DocumentSection> sectionList, BaseResponse reportResponse) throws FiscalPrinterException {
        if (reportResponse != null) {
            List<FontLine> footerContent = new ArrayList<>();
            FontLine factoryLine = new FontLine(ResBundleFiscalPrinterNFD.getString("PRINT_FACTORY_NUM") + getFNNum());
            footerContent.add(factoryLine);
            FontLine regNumLine = new FontLine(ResBundleFiscalPrinterNFD.getString("PRINT_REG_NUM") + getRegNum());
            footerContent.add(regNumLine);
            String timeStr = UtilsNFD.getTimeString(new Date().toString());
            FontLine timeLine = new FontLine(ResBundleFiscalPrinterNFD.getString("PRINT_TIME") + timeStr);
            footerContent.add(timeLine);
            DocumentSection footerSection = new DocumentSection(FiscalPrinterPlugin.SECTION_FOOTER, footerContent);
            sectionList.add(footerSection);
        }
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        try {
            checkOpenShift();
            innerMakeMoneyOperation(money);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void innerMakeMoneyOperation(Money money) throws FiscalPrinterException {
        BigDecimal nfdSum = BigDecimalConverter.convertMoney(money.getValue());
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            nfdConnector.makeDeposit(nfdSum);
        } else if (money.getOperationType() == InventoryOperationType.CASH_OUT) {
            nfdConnector.makeWithdrawal(nfdSum);
        } else {
            throw new IllegalArgumentException("Invalid operation:" + money.getOperationType());
        }
    }

    private void checkOpenShift() throws FiscalPrinterException {
        try {
            NFDMode currentMode = nfdConnector.getState().getStateResultObject().getMode();
            if (currentMode.equals(NFDMode.INITIAL)) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("ERROR_STATE_SHIFT"), CashErrorType.SHIFT_OPERATION_NEED);
            }
            if (currentMode.equals(NFDMode.SHIFT_EXPIRED)) {
                nfdConnector.fiscalizeZReport();
                throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("WARN_CURRENT_SHIFT_MORE_24_H"), CashErrorType.SHIFT_OPERATION_NEED);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    @Override
    public boolean isRegistrationBeforeTemplateProcessing() {
        return false;
    }

}
