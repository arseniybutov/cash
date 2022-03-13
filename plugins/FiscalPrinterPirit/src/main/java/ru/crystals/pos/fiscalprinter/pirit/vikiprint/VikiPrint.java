package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.check.DocumentNumber;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PingStatus;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgent;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritErrorMsg;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.AbstractVikiPrint;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.VikiPrintEnvdWithoutFP;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.VikiPrintF;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.VikiPrintFM15Envd;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.VikiPrintKNonFiscalized;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.utils.ByteUtils;
import ru.crystals.pos.utils.Timer;

import java.util.List;

/**
 * Плагин для работы со всеми типами VikiPrint (пока)
 */
@PrototypedComponent
public class VikiPrint extends AbstractPirit {
    protected static final Logger log = LoggerFactory.getLogger(VikiPrint.class);
    protected AbstractVikiPrint pirit;

    @Override
    public void start() throws FiscalPrinterException {
        pc.setParams(getPort(), config.getBaudRate());
        PiritErrorMsg.setFnDevice(isOFDDevice());
        startComProxy();
        if (firstStart) {
            startDevice(getDeviceForPort(getPort(), config.getBaudRate()));
            firstStart = false;
        } else {
            for (AbstractPirit ap : pcMap.values()) {
                try {
                    VikiPrint vp = (VikiPrint) ap;
                    vp.startDevice(vp.getDeviceForPort(vp.getPort(), vp.config.getBaudRate()));
                } catch (FiscalPrinterException ex) {
                    LOG.error("", ex);
                }
            }
        }
    }

    private void startDevice(PiritPrinterDevice piritPrinterDevice) throws FiscalPrinterException {
        if (piritPrinterDevice == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("NO_COMMUNICATION_WITH_PRINTER"));
        }
        try {
            setPort(StringUtils.trimToNull(piritPrinterDevice.getPort()));
            connectToPirit(getPort());
            PiritInfo piritInfo = getPiritInfo();
            restartPirit(piritInfo);
            pcMap.put(getFactoryNum(), this);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PiritPrinterDevice getDeviceForPort(String portName, String baudRate) {
        log.info("Search device on port {}", portName);
        try (PiritConnector pc = new PiritConnector(portName, baudRate)) {
            pc.reconnect();
            //
            PingStatus ps = pc.isPiritOnline();
            int c = 30;
            while (!ps.isOnline() && c > 0) {
                c--;
                ps = pc.isPiritOnline();
                Thread.sleep(1000);
            }
            if (!ps.isOnline()) {
                throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterPirit.getString("SERVICE_TIMEOUT"), CashErrorType.FATAL_ERROR);
            }
            if (pc.isPiritOnline().isOnline()) {
                log.info("Device on port {} is online, will try to get Pirit status", portName);
                PiritInfo piritInfo = getPiritInfo(pc);

                if (!isPrinterReady(pc)) {
                    log.warn("Printer is not ready. Will be restarted");
                    if (!restartKKM(pc)) {
                        return null;
                    }
                    piritInfo = getPiritInfo(pc);
                }

                return new PiritPrinterDevice(portName, piritInfo.getModel().getModelName(), piritInfo.isFiscalPrinter(), piritInfo.isFiscalMode());
            } else {
                log.info("Device on port {} is offline", portName);
            }
        } catch (Exception e) {
            log.warn("Unable to get status for port {}", portName, e);
        }
        return null;
    }

    private static boolean isPrinterReady(PiritConnector pc) {
        boolean isPrinterReady = true;
        try {
            DataPacket dp = pc.sendRequest(PiritCommand.GET_PRINTER_STATE);
            int status = dp.getIntegerSafe(0).orElse(0);

            if (ByteUtils.hasBit(status, 0)) {
                log.error(ResBundleFiscalPrinterPirit.getString("WARN_PRINTER_NOT_READY"));
                isPrinterReady = false;
            }
            if (ByteUtils.hasBit(status, 7)) {
                log.error(ResBundleFiscalPrinterPirit.getString("NO_COMMUNICATION_WITH_PRINTER"));
                isPrinterReady = false;
            }
        } catch (Exception e) {
            log.error("Printer not ready", e);
            isPrinterReady = false;
        }
        return isPrinterReady;
    }

    private static boolean restartKKM(PiritConnector pc) {
        try {
            pc.sendRequest(PiritCommand.RESTART);
        } catch (FiscalPrinterException e) {
            log.error("Unable to restart KKM", e);
            return false;
        }
        log.info("Waiting for restart");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer(5000);
        while (timer.isNotExpired()) {
            try {
                if (pc.isPiritOnline().isOnline()) {
                    log.info("Restarted");
                    return true;
                }
            } catch (Exception e) {
                log.error("Unable to restart KKM", e);
            }
        }
        return false;
    }

    private void restartPirit(PiritInfo piritInfo) throws FiscalPrinterException {
        pa = new PiritAgent(pc);
        piritConfig.setConnector(pc);
        pirit = getPiritInstance(piritInfo);
        startWorkIfRequired(piritInfo);
        closeOrAnnulDocumentIfRequired(piritInfo);

        taxes = getTaxes();
        piritConfig.setCheckNumerationByCash(true);
        if (isOFDDevice()) {
            piritConfig.setRoundTaxesAfterAllPositionsAndDiscounts(false);
        } else {
            piritConfig.setRoundTaxesAfterAllPositionsAndDiscounts(true);
        }
        piritConfig.setAutoWithdrawal(false);

        setModelSpecificParameters();

        pirit.initFpCounters();
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            super.printMoneyDocument(money);
            return;
        }
        try {
            openDocument(money);
            fiscalMoneyDocument(money);
            closeDocument(true, null);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO посмотреть как используется
    @Override
    protected String tailorBarcodeValueToMaxLength(BarCode barCode) {
        if (barCode.getType() == BarCodeType.QR) {
            return StringUtils.left(barCode.getValue(), pirit.getPiritModel().getCapabilities().getMaxQRCodeLength());
        }

        return super.tailorBarcodeValueToMaxLength(barCode);
    }

    private PiritInfo getPiritInfo() throws FiscalPrinterException {
        return getPiritInfo(pc);
    }

    private static PiritInfo getPiritInfo(PiritConnector pc) throws FiscalPrinterException {
        return new PiritInfo(pc);
    }

    private AbstractVikiPrint getPiritInstance(PiritInfo piritInfo) {
        AbstractVikiPrint result;
        if (!piritInfo.isFiscalPrinter()) {

            if (piritInfo.isFM15()) {
                if (piritInfo.isFiscalMode()) {
                    result = new VikiPrintFM15Envd();
                } else {
                    result = new VikiPrintEnvdWithoutFP();
                }
            } else {
                result = new VikiPrintEnvdWithoutFP();
            }

        } else {
            if (piritInfo.isFiscalMode() && piritInfo.isIfOfdDevice()) {
                result = new VikiPrintF();
            } else {
                result = new VikiPrintKNonFiscalized();
            }
        }
        result.setPiritConfig(piritConfig);
        result.setPiritConnector(pc);
        result.setPiritAgent(pa);
        result.setModel(piritInfo.getModel());
        return result;
    }

    private void connectToPirit(String portName) throws FiscalPrinterConfigException, FiscalPrinterOpenPortException {
        // зачем??
        if (portName == null) {
            log.warn("Port not defined");
            throw new FiscalPrinterConfigException(ResBundleFiscalPrinterPirit.getString("ERROR_CONFIG"), CashErrorType.FATAL_ERROR);
        }
        // зачем??
        if (config.getBaudRate() == null) {
            log.warn("BaudRate not defined");
            throw new FiscalPrinterConfigException(ResBundleFiscalPrinterPirit.getString("ERROR_CONFIG"), CashErrorType.FATAL_ERROR);
        }

        try {
            pc.reconnect();
        } catch (Exception e) {
            log.warn("", e);
            throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterPirit.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
        }
    }

    private void closeOrAnnulDocumentIfRequired(PiritInfo piritInfo) throws FiscalPrinterException {
        try {
            if (piritInfo.isErrorCloseDoc()) {
                closeDocument(true, null);
            } else if (piritInfo.isOpenDoc()) {
                annulCheck();
            }
        } catch (FiscalPrinterException e) {
            throw e;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void startWorkIfRequired(PiritInfo piritInfo) throws FiscalPrinterException {
        if (piritInfo.isNeedStartWork()) {
            super.startWork();
        }
    }

    private void setModelSpecificParameters() {
        try {
            getPiritConfig().setUseCashDrawerMoneyControl(false);
            getPiritConfig().setOpenCashDrawerByCash(true);
            getPiritConfig().setTakeIntoAccountDocumentsCancelledOnRestart(false);
            getPiritConfig().setUseSmallerLineHeightPrintMode(true);
            getPiritConfig().setPrintVerticalBarsOnServiceDoc(false);
        } catch (Exception e) {
            log.error("Unable to set model specific parameters", e);
        }
    }

    @Override
    protected void printDocumentNumberBarcode(Check check, boolean isFiscalDocument, boolean isAsync) throws Exception {
        DocumentNumber number = PluginUtils.getDocumentNumberBarcode(check);
        BarCode documentBarcode = new BarCode(number.getBarcode());//number.getShortBarcode());
        documentBarcode.setTextPosition(TextPosition.NONE_TEXT);
        documentBarcode.setHeight(40);
        documentBarcode.setBarcodeLabel(number.getReadableBarcodeLabel());
        putBarCode(documentBarcode, isFiscalDocument, isAsync);
        FontLine barcodeLabel = new FontLine(StringUtils.center(documentBarcode.getBarcodeLabel(), getMaxCharRow(Font.SMALL, null)), Font.SMALL);
        printLine(barcodeLabel, isFiscalDocument, isAsync);
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return pirit.getEklzNum();
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return pirit.getINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return pirit.openShift(cashier);
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return pirit.getShiftNumber();
    }

    @Override
    protected void postProccesingPrintedDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        pirit.printDocumentAfter(sectionList, document);
    }

    @Override
    public String getDeviceName() {
        if (pirit != null) {
            return pirit.getDeviceName();
        }
        return "VikiPrint";
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return pirit.getLastKpk();
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        String result;
        if (pirit.hasRegNum()) {
            result = super.getRegNum();
        } else {
            result = super.getFactoryNum();
        }
        return result;
    }

    @Override
    public FiscalPrinterInfo getFiscalPrinterInfo() throws FiscalPrinterException {
        return super.getFiscalPrinterInfoInner();
    }

}
