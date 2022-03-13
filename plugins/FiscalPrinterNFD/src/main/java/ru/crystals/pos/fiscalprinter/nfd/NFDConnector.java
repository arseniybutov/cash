package ru.crystals.pos.fiscalprinter.nfd;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.checkdata.NfdReceipt;
import ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.AddCommodity;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.CancelDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.CloseDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.ConfirmInitialization;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.Deposit;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.GetShiftAccumulations;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.GetState;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.GetTradeOperationAccumulations;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.OpenDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.PerformInitialization;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.SetNDS;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.Withdrawal;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.XReport;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.ZReport;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains.CommonDomain;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains.ServiceDomain;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NFDMode;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ShiftAccumulationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationAccumulationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.Initialization;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.SetNDSResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.closedocument.CloseDocumentResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.confirminitialization.ConfirmInitializationResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getshiftaccumulations.GetShiftAccumulationsResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getstate.GetStateResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.gettradeoperationaccumulations.GetTradeOperationAccumulationsResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.performinitialization.PerformInitializationResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.xreport.XReportResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.zreport.ZReportResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport.NfdCommandHandler;
import ru.crystals.pos.fiscalprinter.nfd.utils.NfdFiscalStorageEmulator;
import ru.crystals.pos.fiscalprinter.nfd.utils.UtilsNFD;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.crystals.pos.fiscalprinter.nfd.utils.UtilsNFD.convertCheckType;
import static ru.crystals.pos.fiscalprinter.nfd.utils.UtilsNFD.convertGoodsToCommodity;

public class NFDConnector {

    private static final String NAMESPACE = "emul";
    private static final String NAMESPACE_URI = "http://emulator.nfd.neofiscal.neoservice.com/";

    private final NfdCommandHandler nfdCommandHandler;

    /**
     * Эмулятор счетчиков документов
     */
    protected NfdFiscalStorageEmulator storageEmulator = new NfdFiscalStorageEmulator();

    private boolean useRounding;

    public NFDConnector(String soapAction) {
        this.nfdCommandHandler = new NfdCommandHandler(soapAction, NAMESPACE, NAMESPACE_URI);
    }

    public GetStateResponse getState() throws FiscalPrinterException {
        GetStateResponse getStateResponse = (GetStateResponse) nfdCommandHandler.invokeCommand(new GetState());
        if (getStateResponse.getStateResultObject().getMode().equals(NFDMode.OFFLINE_PERIOD_EXPIRED)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("ERROR_OFFLINE_MODE"), CashErrorType.FISCAL_ERROR);
        }
        return getStateResponse;
    }

    public PerformInitializationResponse performInitialization(String token) throws FiscalPrinterException {
        PerformInitializationResponse response = (PerformInitializationResponse) nfdCommandHandler.invokeCommand(new PerformInitialization(token));
        fillFiscalData(response.getInitializationResultObject());
        return response;
    }

    public ConfirmInitializationResponse confirmInitialization() throws FiscalPrinterException {
        return (ConfirmInitializationResponse) nfdCommandHandler.invokeCommand(new ConfirmInitialization());
    }

    private NfdReceipt openDocument(TradeOperationType tradeOperationType, CommonDomain domain) throws FiscalPrinterException {
        nfdCommandHandler.invokeCommand(new OpenDocument(tradeOperationType, domain));
        return new NfdReceipt(tradeOperationType, domain);
    }

    private void addCommodity(NfdReceipt currReceipt, AddCommodity commodity) throws FiscalPrinterException {
        nfdCommandHandler.invokeCommand(commodity);
        if (currReceipt == null) {
            currReceipt = new NfdReceipt(TradeOperationType.SELL, new ServiceDomain("1"));
        }
        currReceipt.addCommodities(commodity);
    }

    public void cancelDocument() throws FiscalPrinterException {
        nfdCommandHandler.invokeCommand(new CancelDocument());
    }

    public GetShiftAccumulationsResponse getShiftAccumulations() throws FiscalPrinterException {
        Set<ShiftAccumulationType> shiftAccumulationTypes = new HashSet<>(Arrays.asList(ShiftAccumulationType.values()));
        return (GetShiftAccumulationsResponse) nfdCommandHandler.invokeCommand(new GetShiftAccumulations(shiftAccumulationTypes));
    }

    public GetShiftAccumulationsResponse getMoneyPlacementShiftAccumulation() throws FiscalPrinterException {
        Set<ShiftAccumulationType> shiftAccumulationTypes = new HashSet<>(Arrays.asList(ShiftAccumulationType.MONEY_PLACEMENT));
        return (GetShiftAccumulationsResponse) nfdCommandHandler.invokeCommand(new GetShiftAccumulations(shiftAccumulationTypes));
    }

    public GetTradeOperationAccumulationsResponse getTradeOperationAccumulations() throws FiscalPrinterException {
        Set<TradeOperationAccumulationType> tradeOperationAccumulationTypes = new HashSet<>(Arrays.asList(TradeOperationAccumulationType.values()));
        return (GetTradeOperationAccumulationsResponse) nfdCommandHandler.invokeCommand(new GetTradeOperationAccumulations(tradeOperationAccumulationTypes));
    }

    private CloseDocumentResponse closeDocument(NfdReceipt nfdReceipt, CloseDocument closeDocument) throws FiscalPrinterException {
        final String text = closeDocument.getText();
        final Set<PaymentNFD> payments = closeDocument.getPayments();
        CloseDocumentResponse response = (CloseDocumentResponse) nfdCommandHandler.invokeCommand(new CloseDocument(text, payments));
        nfdReceipt.addCloseDocumentText(text);
        nfdReceipt.setPayments(payments);
        nfdReceipt.setTaxGroupNumbers(closeDocument.getTaxGroupNumbers());
        return response;
    }

    public void makeDeposit(BigDecimal sum) throws FiscalPrinterException {
        nfdCommandHandler.invokeCommand(new Deposit(sum));
    }

    public void makeWithdrawal(BigDecimal sum) throws FiscalPrinterException {
        nfdCommandHandler.invokeCommand(new Withdrawal(sum));
    }

    public SetNDSResponse configureTaxGroup(TaxGroupNumber taxGroup) throws FiscalPrinterException {
        return (SetNDSResponse) nfdCommandHandler.invokeCommand(new SetNDS(taxGroup));
    }

    public XReportResponse xReport() throws FiscalPrinterException {
        return (XReportResponse) nfdCommandHandler.invokeCommand(new XReport());
    }

    private ZReportResponse zReport() throws FiscalPrinterException {
        return (ZReportResponse) nfdCommandHandler.invokeCommand(new ZReport());
    }

    public ZReportResponse fiscalizeZReport() throws FiscalPrinterException {
        ZReportResponse response = zReport();
        storageEmulator.incKPK();
        return response;
    }

    public NfdReceipt openReceipt(Check check) throws FiscalPrinterException {
        NFDMode mode = getState().getStateResultObject().getMode();
        cancelDocOfOpened(mode);
        if (mode.equals(NFDMode.OFFLINE_PERIOD_EXPIRED)) {
            throw new FiscalPrinterException("OFFLINE_PERIOD_EXPIRED");
        }
        if (mode.equals(NFDMode.SHIFT_EXPIRED)) {
            throw new FiscalPrinterException("SHIFT_EXPIRED");
        }
        if (mode.equals(NFDMode.SHIFT_OPEN) || mode.equals(NFDMode.SHIFT_CLOSED)) {
            TradeOperationType tradeOperationType = convertCheckType(check.getType());
            return openDocument(tradeOperationType, new ServiceDomain("0"));
        }
        throw new FiscalPrinterException("Invalid mode: " + mode);
    }

    public void cancelDocOfOpened(NFDMode mode) throws FiscalPrinterException {
        if (mode.equals(NFDMode.OPEN_SELL) ||
                mode.equals(NFDMode.OPEN_SELL_RETURN) ||
                mode.equals(NFDMode.OPEN_BUY) ||
                mode.equals(NFDMode.OPEN_BUY_RETURN)) {
            cancelDocument();
        }
    }

    public CloseDocumentResponse closeReceipt(NfdReceipt nfdReceipt, Check check) throws FiscalPrinterException {
        try {
            CloseDocument closeDocument = new CloseDocument();
            final Set<PaymentNFD> payments = UtilsNFD.convertPayments(check.getPayments());
            nfdReceipt.setPayments(payments);
            closeDocument.setPayments(payments);
            closeDocument.setText(check.getClientRequisites());
            closeDocument.setTaxGroupNumbers(nfdReceipt.getTaxGroupNumbers());
            CloseDocumentResponse response = closeDocument(nfdReceipt, closeDocument);
            storageEmulator.incKPK();
            return response;
        } catch (IllegalArgumentException e) {
            throw new FiscalPrinterException("Unknown check type : " + check.getType(), e);
        }
    }

    public void addPosition(NfdReceipt nfdReceipt, Goods good) throws FiscalPrinterException {
        addCommodity(nfdReceipt, convertGoodsToCommodity(good, isUseRounding()));
    }


    public NfdFiscalStorageEmulator getStorageEmulator() {
        return storageEmulator;
    }

    private void fillFiscalData(Initialization initialization) throws FiscalPrinterException {
        try {
            storageEmulator.setRNM(Long.parseLong(initialization.getRnm()));
            storageEmulator.setSerialNumber(initialization.getSerialNumber());
            storageEmulator.setTaxPayer(initialization.getTaxPayer());
            storageEmulator.setBIN(Long.parseLong(initialization.getBin()));
            storageEmulator.setTaxation(initialization.getTaxation());
            storageEmulator.setDepartment(initialization.getDepartment());
            storageEmulator.setCashDeskCode(initialization.getCashDeskCode());
            storageEmulator.setAddress(initialization.getAddress());
        } catch (NumberFormatException e) {
            throw new FiscalPrinterException("Invalid initialization data format", e);
        }
        validateFiscalData();
    }

    public void validateFiscalData() throws FiscalPrinterException {
        if (storageEmulator.getRNM() == 0
                || "".equals(storageEmulator.getSerialNumber())
                || "".equals(storageEmulator.getTaxPayer())
                || storageEmulator.getBIN() == 0) {
            throw new FiscalPrinterException("Invalid state of fiscal data : " + storageEmulator);
        }
    }

    public void setUseRounding(boolean useRounding) {
        this.useRounding = useRounding;
    }

    public boolean isUseRounding() {
        return useRounding;
    }
}

