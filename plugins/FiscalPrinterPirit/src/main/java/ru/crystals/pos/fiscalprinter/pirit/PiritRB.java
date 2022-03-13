package ru.crystals.pos.fiscalprinter.pirit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.image.context.fiscal.FiscalDevice;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinterBase;
import ru.crystals.pos.fiscalprinter.FiscalPrinterImpl;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeEntity;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeException;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.PiritCountry;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgent;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgentRB;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritErrorMsg;
import ru.crystals.pos.fiscalprinter.pirit.core.font.FontConfiguration;
import ru.crystals.pos.fiscalprinter.pirit.core.rb.PiritRBKPKEmulator;
import ru.crystals.pos.fiscalprinter.pirit.core.rb.PiritReportData;
import ru.crystals.pos.fiscalprinter.templates.ControlTapeDocumentTemplate;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrototypedComponent
public class PiritRB extends AbstractPirit {
    private static final long MINIMAL_PRINTING_FIRMWARE_VERSION = 266;
    private static final long MINIMAL_PRINTING_FIRMWARE_TYPE = 21;

    protected FiscalPrinterBase fiscalPrinterModule;
    protected PiritRBKPKEmulator emulator = new PiritRBKPKEmulator();

    @Override
    public void start() throws FiscalPrinterException {
        startInner();
        //Ставим округление
        if (!isShiftOpen()) {
            getPiritConfig().setBelarusRoundToValue(0);
        }
        try {
            emulator.loadState();
        } catch (Exception e) {
            throwUnknownError(e);
        }
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return "3841149094";
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return pa.getINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер последней закрытой

     */
    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    @Override
    protected void checkStatusOfEklzAndFiscalMemory(StatusFP status, DataPacket statusDataPacket) throws Exception {
        int val = statusDataPacket.getIntegerSafe(1).orElse(0);
        if (ByteUtils.hasBit(val, 6)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_FREE_FISCAL_MEMORY"));
        } else if (ByteUtils.hasBit(val, 7)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_PASSWORD_FOR_ACCESS_TO_FISCAL_MEMORY"));
        }
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return emulator.getKPK();
    }

    @Override
    public boolean isControlTapeEmpty() {
        try {
            DataPacket dp = pc.sendRequest(PiritCommand.GET_STATUS);

            if ((dp.getLongValue(1) & 16) == 16) {
                LOG.debug("Control Tape is FULL");
                return false;
            }
        } catch (FiscalPrinterException e) {
            LOG.debug("Control Tape is EMPTY");
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOG.debug("Control Tape is EMPTY");
        return true;
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(report.getCashier()));
            pc.sendRequest(PiritCommand.PRINT_Z_REPORT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print Z-report", e);
        }
    }

    @Override
    public void printControlTape() throws FiscalPrinterException {
        try {
            pc.sendRequest(PiritCommand.PRINT_CONTROL_TAPE);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("Error print control tape", e);
        }
    }

    @Override
    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check.isCopy() || check instanceof FullCheckCopy) {
            printCheckCopy(check);
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
    public void putGoods(List<Goods> goods, boolean isAsyncMode) throws Exception {
        int posNum = 0;

        for (Goods good : goods) {
            DataPacket dp = new DataPacket();

            if (config.isPrintGoodsName()) {
                if (good.getName() == null) {
                    dp.putStringValue(defaultGoodsName);
                } else {
                    // ограничение на длину названия товара (см. док-цию команда 42)
                    dp.putStringValue(good.getName().length() > GOOD_NAME_MAX_LENGTH ? good.getName().substring(0, GOOD_NAME_MAX_LENGTH) : good.getName());
                }
            } else {
                dp.putStringValue("");
            }

            dp.putStringValue(makeItem(good));

            dp.putDoubleValue((double) good.getQuant() / COUNT_ORDER);
            dp.putDoubleValue((double) good.getStartPricePerUnit() / PRICE_ORDER);

            ValueAddedTax vat = taxes.lookupByValue(good.getTax());
            if (vat == null) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_TAX_VALUE"), PiritErrorMsg.getErrorType());
            }

            dp.putLongValue(vat.index);

            dp.putStringValue(config.isPrintPosNum() ? String.format("%3d ", ++posNum) : "");

            pc.sendRequest(PiritCommand.ADD_ITEM, dp, isAsyncMode);

            putDiscounts(good.getDiscs(), isAsyncMode);
            putMargin(good.getMargins(), isAsyncMode);
        }
    }

    @Override
    protected void configureFont2F(int designNumber) {
        fontManager.configure(piritConfig, config, piritConfig.isUseWidePaper() ? FontConfiguration.PIRIT_RB : FontConfiguration.PIRIT_RB_NARROW, designNumber);
    }

    protected String makeItem(Goods good) {
        if (!config.isPrintItem()) {
            return "";
        }
        return (good.getItem().length() > ITEM_MAX_LENGTH) ? good.getItem().substring(0, ITEM_MAX_LENGTH) : good.getItem();
    }

    protected void printBonusesServiceDocument(Check check, DocumentSection documentSection) throws FiscalPrinterException {
        List<DocumentSection> sections = new ArrayList<>();
        List<FontLine> newSectionContent = new ArrayList<>();
        //Сформируем шапку
        newSectionContent.add(new FontLine(" "));
        newSectionContent.add(new FontLine(StringUtils.leftPad("", getMaxCharRow(), "-"), Font.NORMAL));
        newSectionContent.add(new FontLine(StringUtils.center(ResBundleFiscalPrinterPirit.getString("BONUSES_SERVICE_DOC_TITTLE"), getMaxCharRow()), Font.NORMAL));
        newSectionContent.add(new FontLine(StringUtils.leftPad("", getMaxCharRow(), "-"), Font.NORMAL));
        //Добавим контент из шаблона
        newSectionContent.addAll(documentSection.getContent());
        newSectionContent.add(new FontLine(" "));
        //Подменим
        documentSection.setContent(newSectionContent);
        sections.add(new DocumentSection("header", Collections.<FontLine>emptyList()));
        sections.add(documentSection);
        sections.add(new DocumentSection("cut", Collections.<FontLine>emptyList()));
        printServiceByTemplate(sections, check);
    }

    @Override
    protected void closeCheck(Check check) throws Exception {
        try {
            pc.sendRequest(PiritCommand.CLOSE_DOCUMENT);
        } catch (FiscalPrinterException e) {
            if (e.getExceptionType() == FiscalPrinterExceptionType.TIMEOUT_EXPIRED) {
                emulator.incKPKAndGet();
            }
            throw e;
        }
    }

    private List<PaymentType> getPayments() throws FiscalPrinterException {
        try {
            return piritConfig.getPayments();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    public ControlTapeEntity getDocumentInfoFromControlTape(long numberDoc) throws FiscalPrinterException {
        ControlTapeEntity result;
        DataPacket dp = new DataPacket();
        dp.putLongValue(numberDoc);
        byte[] dataM = pc.sendRequestForData(PiritCommand.GET_DOCUMENT_FROM_CONTROL_TAPE, dp);
        result = PiritReportData.parseReport(dataM, getINN(), getRegNum(), getPayments(), isOFDDevice());
        return result;
    }

    @Override
    public void setFiscalPrinterModule(FiscalPrinterBase fiscalPrinterModule) {
        this.fiscalPrinterModule = fiscalPrinterModule;
    }

    @Override
    public List<FontLine> getDocumentInfoForPrintFromControlTape(long numberDoc) throws FiscalPrinterException {
        try {
            ControlTapeDocumentTemplate controlTypeTemplate = (ControlTapeDocumentTemplate) ((FiscalPrinterImpl) fiscalPrinterModule).getTemplate("control-tape");
            return controlTypeTemplate.getControlTapeEntityFormattedText(getDocumentInfoFromControlTape(numberDoc));
        } catch (Exception e) {
            LOG.error("", e);
            if (e instanceof ControlTapeException) {
                throw (ControlTapeException) e;
            } else {
                throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_READ_DATA"));
            }

        }
    }

    @Override
    protected long getTargetMinimalFirmwareVersion(long currentVersion) {
        return MINIMAL_BYN_VERSION;
    }

    @Override
    public PiritCountry getCountry() {
        return PiritCountry.BY;
    }

    @Override
    protected boolean useComProxy() {
        // Запуск comproxy не нужен.
        return false;
    }

    public PiritRBKPKEmulator getEmulator() {
        return emulator;
    }

    @Override
    public boolean isStartZ() {
        return emulator.getStartZ();
    }

    @Override
    public FiscalDevice getImagePrintingType() throws FiscalPrinterException {
        return getFirmwareLong() >= MINIMAL_PRINTING_FIRMWARE_VERSION
                && getFirmwareType() >= MINIMAL_PRINTING_FIRMWARE_TYPE ? FiscalDevice.PIRIT_2 : null;
    }

    @Override
    public void setPayments(List<PaymentType> payments) throws FiscalPrinterException {
        Map<Long, PaymentType> paymentsMap = new HashMap<>();
        try {
            payments.stream().filter(PaymentType::isSet10APIPaymentType).forEach(pt -> paymentsMap.put(pt.getIndexPayment(), pt));
            payments.stream().filter(pt -> !pt.isSet10APIPaymentType()).forEach(pt -> paymentsMap.putIfAbsent(pt.getIndexPayment(), pt));
            piritConfig.setPayments(paymentsMap.values());
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        } finally {
            paymentsMap.clear();
        }
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return RegulatoryFeatures.BY;
    }

    @Override
    protected PiritAgent createPiritAgent(PiritConnector piritConnector) {
        return new PiritAgentRB(piritConnector);
    }
}
