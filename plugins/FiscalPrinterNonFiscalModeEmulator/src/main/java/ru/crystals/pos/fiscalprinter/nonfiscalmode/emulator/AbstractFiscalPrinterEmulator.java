package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.controltape.ControlTape;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeEntity;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeIsEmptyException;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeWorker;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusCFTDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Disc;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnStatus;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Margin;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.info.FnDocInfo;
import ru.crystals.pos.fiscalprinter.datastruct.info.ProxySoftwareInfo;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.CheckUtils;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ManualExceptionAppender;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.FRError;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.PrinterFrame;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.templateengine.functions.FiscalLengthSupplier;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Эмулятор ФР Основной инструмент тестирования без ФР
 */
public abstract class AbstractFiscalPrinterEmulator implements FiscalPrinterPlugin, Configurable<FiscalPrinterEmulatorConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFiscalPrinterEmulator.class);
    private PrinterFrame printerView;
    protected FilePrinterEmulator filePrinterEmulator;
    private OFDInfoWriter ofdInfoWriter;
    protected FiscalPrinterEmulatorConfig config;

    private static final AtomicLong indexAddon = new AtomicLong(0);
    protected final long innAddon;
    protected FiscalPrinterData fiscalData;
    protected ReceiptPrinter printer;

    @Autowired
    private Properties properties;

    @Autowired
    private Loyal loyal;

    public AbstractFiscalPrinterEmulator() {
        innAddon = indexAddon.getAndIncrement();
        filePrinterEmulator = new FilePrinterEmulator(innAddon);
        printer = new Emulator(filePrinterEmulator);
        fiscalData = new FiscalPrinterEmulatorData(filePrinterEmulator, innAddon);
        ofdInfoWriter = new OFDInfoWriter(innAddon);
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public Class<FiscalPrinterEmulatorConfig> getConfigClass() {
        return FiscalPrinterEmulatorConfig.class;
    }

    @Override
    public void setConfig(FiscalPrinterEmulatorConfig config) {
        this.config = config;
        ((Emulator) printer).setMaxCharRow(config.getMaxCharRow());
    }

    @Override
    public void start() throws FiscalPrinterException {
        try {
            fiscalData.loadState();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        int printPreviewPort = config.getPrintPreviewPort() + (int) innAddon * 2;
        int manualExceptionPort = config.getManualExceptionPort() + (int) innAddon * 2;

        fiscalData.setZOnClosedShift(config.iszOnClosedShift());
        fiscalData.setMaxCharRow(config.getMaxCharRow());
        Timeouts.PRINT_STRING_INTERVAL = config.getPrintLineInterval();
        Timeouts.CLOSE_CHECK_INTERVAL = config.getCloseCheckInterval();
        if (config.isCreatePrinterFrame()) {
            printerView = new PrinterFrame(false, innAddon);
            printerView.setOFDMode(isOfdMode());
            printerView.setVisible(true);

            ((FiscalPrinterEmulatorData) fiscalData).setIPrinterView(printerView);
        }
        if (config.isShowPrinterFrame()) {
            try {
                Registry registry = LocateRegistry.getRegistry(config.getPrintPreviewHost(), printPreviewPort);
                fiscalData.setRegistry(registry);
                LOG.debug("RMI Connect to PrinterFrame " + config.getPrintPreviewHost() + ":" + printPreviewPort);
            } catch (RemoteException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    if (netint.getDisplayName() != null && !netint.getDisplayName().toUpperCase().contains("VMWARE")) {
                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                            if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                                LOG.debug(netint.getName());
                                LOG.debug("InetAddress: " + inetAddress.getHostAddress());
                                System.setProperty("java.rmi.server.hostname", inetAddress.getHostAddress());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                LOG.error("Socket start fail", e);
            }

            LOG.debug("java.rmi.server.hostname: " + System.getProperty("java.rmi.server.hostname"));
            FiscalPrinterEmulatorData fd = (FiscalPrinterEmulatorData) fiscalData;
            ManualExceptionAppender stub = (ManualExceptionAppender) UnicastRemoteObject.exportObject(fd, manualExceptionPort);
            Registry registry = LocateRegistry.createRegistry(manualExceptionPort);
            registry.rebind("ManualExceptionAppender", stub);
            LOG.info("RMI Listening port: " + manualExceptionPort);
        } catch (RemoteException e) {
            LOG.error("RMI start fail", e);
        }
    }

    protected boolean isOfdMode() {
        return config.isOfdMode();
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        String result = null;
        try {
            result = fiscalData.getEklz();
        } catch (Exception e) {
            throwException(e);
        }
        return result;
    }

    @Override
    public String getVerBios() {
        return "27";
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterEmulator.getString("DEVICE_NAME");
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        try {
            check = validate(check);
            check.setPayments(ru.crystals.pos.utils.CheckUtils.reduceAndSortPaymentsByIndexPaymentFDD(check.getPayments()));
            getControlTapeWorker().saveEntity(
                    new ControlTapeEntity(check, getTestInn(), fiscalData.getRegNum(), getPayments()));

            openDocument(check);

            putGoods(check.getGoods());
            putDics(check.getDiscs(), false);
            putMargin(check.getMargins(), false);
            putPayments(check);

            if (check.getDiscountValueTotal() != null) {
                putText(new Text(ResBundleFiscalPrinter.getString("PD_DISCOUNT_SUM") + " "
                        + String.format("%.2f", (double) check.getDiscountValueTotal() / 100).replace(',', '.')));
            }

            if (check.isAnnul()) {
                annulCheck(check);
            } else {
                closeDocument(check, PluginUtils.getBarcode(check));
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void putText(Text text) throws FiscalPrinterException {
        checkErrorBeforeFiscalize();
        fiscalData.printLine(text.getValue());
    }

    private void putMargin(List<Margin> margins, boolean positional) throws FiscalPrinterException {
        for (Margin margin : margins) {
            fiscalData.putMargin(positional, margin.getName(), margin.getType(), margin.getValue());
        }
    }

    private void putDics(List<Disc> discs, boolean positional) throws FiscalPrinterException {
        for (Disc disc : discs) {
            fiscalData.putDiscount(positional, disc.getName(), disc.getType(), disc.getValue());
        }
    }

    private void putGoods(List<Goods> goods) throws FiscalPrinterException {
        for (Goods position : goods) {
            fiscalData.putCheckPosition(position.getName(), position.getQuant(), position.getStartPricePerUnit(), position.getDepartNumber());
            putDics(position.getDiscs(), true);
            putMargin(position.getMargins(), true);
        }
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        try {
            getControlTapeWorker().saveEntity(new ControlTapeEntity(money, getTestInn(), fiscalData.getRegNum()));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            putText(new Text(ResBundleFiscalPrinter.getString("PD_CURRENCY_NAME") + money.getCurrency()));

            for (BankNote bankNote : money.getBankNotes()) {
                StringBuilder row = new StringBuilder();

                String str = money.getCurrency();
                row.append(String.format("%10.10s", str));

                str = bankNote.getValue() / 100 + "." + String.format("%02d", bankNote.getValue() % 100);

                row.append(String.format("%15.15s", str));

                putText(new Text(row.toString()));
            }
        } else {
            putText(new Text(ResBundleFiscalPrinter.getString("PD_CURRENCY_NAME") + money.getCurrency()));

            for (BankNote bankNote : money.getBankNotes()) {
                long value = bankNote.getValue() * bankNote.getCount();

                StringBuilder row = new StringBuilder();

                String str = bankNote.getValue() / 100 + "." + String.format("%02d", bankNote.getValue() % 100) + "x" + bankNote.getCount();

                row.append(String.format("%25.25s", str));

                str = " =" + value / 100 + "." + String.format("%02d", value % 100);
                row.append(String.format("%15.15s", str));

                putText(new Text(row.toString()));
            }

            if (money.getSumCoins() != null) {
                StringBuilder row = new StringBuilder(String.format("%25.25s", ResBundleFiscalPrinter.getString("PD_CASH_OUT_COINS")));
                String str = " =" + money.getSumCoins() / 100 + "." + String.format("%02d", money.getSumCoins() % 100);
                row.append(String.format("%15.15s", str));

                putText(new Text(row.toString()));
            }
        }

        fiscalData.doCashOperation(money.getOperationType(), money.getValue(), money.getCashier());
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        fiscalData.xReport(report.getCashier());
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        fiscalData.zReport(report.getCashier());
    }

    protected abstract ControlTapeWorker getControlTapeWorker() throws ClassNotFoundException, FiscalPrinterException, IOException;

    @Override
    public void printControlTape() throws FiscalPrinterException {
        LOG.info("===================Print control tape===============================");
        try {
            ControlTape controlTape = getControlTapeWorker().getControlTape();
            if (controlTape == null) {
                throw new ControlTapeIsEmptyException();
            }
            String IHH = getTestInn();

            ControlTapeDocumentTemplateEmulator template = getControlTapeTemplateEmulator();
            template.setPlugin(this);
            template.setLengthSupplier(new FiscalLengthSupplier(this));
            controlTape.setShopName(controlTape.getShopName());
            controlTape.setShopAddress(controlTape.getShopAddress());
            controlTape.setFormattedDateTime(new Date().toString());
            controlTape.setControlTapeNumber(1L);
            controlTape.setINN(IHH);
            controlTape.setShiftNum(controlTape.getShiftNum());
            controlTape.setNumberField(fiscalData.getSPND());
            controlTape.setRegNumber(fiscalData.getRegNum());
            controlTape.setCashierName("cashierName");

            template.printDocument(controlTape);
        } catch (Exception e) {
            throwException(e);
        }
    }

    protected abstract ControlTapeDocumentTemplateEmulator getControlTapeTemplateEmulator() throws IOException;

    @Override
    public boolean isControlTapeEmpty() {
        try {
            getControlTapeWorker();
            return getControlTapeWorker().isControlTapeEmpty();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return true;
        }
    }

    @Override
    public List<FontLine> getDocumentInfoForPrintFromControlTape(long documentNumber) throws FiscalPrinterException {
        try {
            getControlTapeWorker();
            return getControlTapeWorker().getControlTapeEntityForPrint(documentNumber);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            if (e instanceof FiscalPrinterException) {
                throw (FiscalPrinterException) e;
            }
            return null;
        }
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        LOG.info("printServiceDocument");
        checkErrorBeforeFiscalize();
        try {
            for (Row row : serviceDocument.getRows()) {
                if (row instanceof Text) {
                    fiscalData.printLine(row.getValue());
                } else if (row instanceof BarCode) {
                    fiscalData.printBarcode((BarCode) row);
                }
            }
            fiscalData.skipAndCut();
            fiscalData.incSPND();
            fiscalData.updateState();
        } catch (Exception e) {
            throwException(e);
        }
    }

    private String testInn = null;

    // В эмуляторе пока ИНН будет различаться на 1
    private synchronized String getTestInn() {
        if (testInn == null) {
            String stringInn = properties.getShopINN();
            if (stringInn != null && stringInn.length() > 0) {
                Long inn = Long.valueOf(stringInn);
                inn = inn + innAddon;
                testInn = String.valueOf(inn);
                if (printerView != null) {
                    printerView.setInn("ИНН: " + testInn);
                }
            }
            return testInn;
        }
        return testInn;
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        String inn = getTestInn();
        fiscalData.setINN(inn);
        return inn;
    }

    @Override
    public Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        return fiscalData.getIncTotal();
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        LOG.info(" getStatus ");
        StatusFP status = null;
        try {
            status = fiscalData.getStatus();
        } catch (Exception e) {
            throwException(e);
        }
        return status;
    }

    @Override
    public void printDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        if (document instanceof ControlTape) {
            // печатаем контрольную ленту
            List<FontLine> lines = new ArrayList<FontLine>();
            for (DocumentSection section : sectionList) {
                lines.addAll(section.getContent());
            }
            try {
                printLinesListInDoc(lines);
                FiscalPrinterEmulatorData tempFData = (FiscalPrinterEmulatorData) fiscalData;
                tempFData.setControlTapeCount(tempFData.getControlTapeCount() + 1);
                getControlTapeWorker().createNewControlTape(tempFData.getControlTapeCount());
                fiscalData.skipAndCut();
            } catch (Exception e) {
                throwException(e);
            }
        } else {
            try {
                if (document instanceof Check) {
                    getControlTapeWorker().saveEntity(
                            new ControlTapeEntity((Check) document, getTestInn(), fiscalData.getRegNum(), getPayments()));
                } else if (document instanceof Money) {
                    getControlTapeWorker().saveEntity(
                            new ControlTapeEntity((Money) document, getTestInn(), fiscalData.getRegNum()));
                }
            } catch (FiscalPrinterException fpe) {
                throw fpe;
            } catch (Exception ex) {
                throw new FiscalPrinterException("Can't save controlTape entity!", ex);
            }
            try {
                printDocumentByTemplate(sectionList, document);
            } catch (Exception e) {
                try {
                    if (document instanceof Check) {
                        getControlTapeWorker().removeEntity(((Check) document).getCheckNumber());
                    } else if (document instanceof Money) {
                        getControlTapeWorker().removeEntity(((Money) document).getCheckNumber());
                    }
                } catch (Exception ex) {
                    LOG.info("Can't remove entity from control tape");
                }
                throwException(e);
            }
        }
    }

    protected void checkErrorBeforeFiscalize() throws FiscalPrinterException {
        if (printerView != null) {
            FRError fpError = printerView.getPrinterBroken();
            switch (fpError) {
                case NONE:
                    break;
                case BEFORE_FISCALIZE:
                    throw makeGeneralError();
                case PAPER_EMPTY:
                    throw new FiscalPrinterException(ResBundleFiscalPrinterEmulator.getString("NO_PAPER_ERROR"));
            }
        }
    }

    private void checkErrorAfterFiscalize() throws FiscalPrinterException {
        if (printerView != null) {
            FRError fpError = printerView.getPrinterBroken();
            switch (fpError) {
                case AFTER_FISCALIZE:
                    final FiscalPrinterCommunicationException fpe = makeGeneralError();
                    fpe.setRegistrationComplete(true);
                    throw fpe;

            }
        }
    }

    private FiscalPrinterCommunicationException makeGeneralError() {
        return new FiscalPrinterCommunicationException(ResBundleFiscalPrinterEmulator.getString("GENERAL_FP_ERROR"), CashErrorType.FISCAL_ERROR);
    }

    protected void printDocumentByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws Exception {
        checkErrorBeforeFiscalize();

        if (document instanceof Check) {
            Check check = validate((Check) document);
            check.setPayments(ru.crystals.pos.utils.CheckUtils.reduceAndSortPaymentsByIndexPaymentFDD(check.getPayments()));
            if (check.isAnnul()) {
                printAnnulCheckByTemplate(sectionList, check);
            } else if (check.isCopy() || check instanceof FullCheckCopy) {
                printCopyCheckByTemplate(sectionList, check);
            } else {
                printCheckByTemplate(sectionList, check);
            }
        } else if (document instanceof Report) {
            Report report = (Report) document;
            if (!report.isCopy()) {
                printReportByTemplate(sectionList, report);
            } else {
                printReportCopyByTemplate(sectionList, report);
            }
        } else if (document instanceof Money) {
            printMoneyByTemplate(sectionList, (Money) document);
        } else if (document instanceof DiscountsReport) {
            printServiceByTemplate(sectionList, document);
        } else if (document instanceof BonusCFTDocument) {
            printBonusCFTReportByTemplate(sectionList, document);
        } else if (document instanceof DailyLogData) {
            printBankDailyReportByTemplate(sectionList, document);
        } else {
            printServiceByTemplate(sectionList, document);
        }

        checkErrorAfterFiscalize();
    }

    private void printReportCopyByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        checkErrorBeforeFiscalize();
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openServiceDocument(report.getCashier());
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("fiscal")) {
                    closeDocument(null, null);
                } else if (sectionName.equals("cut")) {
                    if (!report.isZReport() && !report.isXReport()) {
                        closeDocument(null, null);
                    }
                } else if (!sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void printBankDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openServiceDocument(document.getCashier());
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("slip")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("cut")) {
                    closeDocument(null, null);
                } else if (!(sectionName.equals("footer"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void printBonusCFTReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case "logo":
                        printLogo();
                        break;
                    case "operationList":
                        openServiceDocument(document.getCashier());
                        printLinesListInDoc(section.getContent());
                        break;
                    case "cut":
                        closeDocument(null, null);
                        break;
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void printServiceByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        checkErrorBeforeFiscalize();
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openServiceDocument(document.getCashier());
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("cut")) {
                    closeDocument(null, null);
                } else if (!(sectionName.equals("footer"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                        openServiceDocument(money.getCashier());
                    } else {
                        openDocument(money);
                    }
                    printLinesListInDoc(section.getContent());
                    ofdInfoWriter.writeOFDInfo(money.getCashier());
                } else if (sectionName.equals("fiscal")) {
                    if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                        closeDocument(null, null);
                    } else {
                        fiscalMoneyDocument(money);
                    }
                } else if (!(sectionName.equals("footer") || sectionName.equals("cut"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void fiscalMoneyDocument(Money money) throws FiscalPrinterException {
        fiscalData.doCashOperation(money.getOperationType(), money.getValue(), money.getCashier());
    }

    protected void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        checkErrorBeforeFiscalize();

        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openServiceDocument(report.getCashier());
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("fiscal")) {
                    closeDocument(null, null, report.getCashier());
                    if (report.isZReport()) {
                        printZReport(report);
                    } else if (report.isXReport()) {
                        printXReport(report);
                    }
                } else if (sectionName.equals("cut")) {
                    if (!report.isZReport() && !report.isXReport()) {
                        closeDocument(null, null);
                    }
                } else if (!sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openDocument(check);
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("position") || sectionName.equals("positionSectionWithGoodSets")) {
                    printLinesListInDoc(section.getContent());
                    fiscalizeSum(check.getType(), check.getDepart(), check.getCheckSumEnd());
                } else if (sectionName.equals("payment")) {
                    putPayments(check);
                } else if (sectionName.equals("fiscal")) {
                    if (check.getUid() != null) {
                        printLine(new FontLine(ResBundleFiscalPrinter.getString("DOC_UID") + check.getUid()));
                    }
                    BarCode barCode = check.getPrintDocumentSettings().isNeedPrintBarcode() ? PluginUtils.getDocumentBarcode(check) : null;
                    closeDocument(check, barCode, check.getCashier());
                } else if (!(sectionName.equals("footer") || sectionName.equals("cut"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void fiscalizeSum(CheckType type, Long depart, Long checkSumEnd) throws FiscalPrinterException {
        fiscalData.putCheckPosition("ИТОГ", 1000L, checkSumEnd, depart);
    }

    private void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openServiceDocument(check.getCashier());
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("cut")) {
                    if (check.getUid() != null) {
                        printLine(new FontLine(ResBundleFiscalPrinter.getString("DOC_UID") + check.getUid()));
                    }
                    closeDocument(check, PluginUtils.getDocumentBarcode(check));
                } else if (!sectionName.equals("footer")) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    protected void closeDocument(Check purchase, BarCode barcode) throws FiscalPrinterException {
        closeDocument(purchase, barcode, null);
    }

    void closeDocument(Check purchase, BarCode barcode, Cashier cashier) throws FiscalPrinterException {
        if (fiscalData.isCheckOpen()) {
            fiscalData.closeCheck(purchase, barcode);
        } else if ((purchase == null || purchase.isCopy() || purchase.getPrintDocumentSettings().isNeedPrintBarcode())) {
            fiscalData.printBarcode(barcode);
        }
        ofdInfoWriter.writeOFDInfo(purchase);
        ofdInfoWriter.writeOFDInfo(cashier);
        fiscalData.skipAndCut();
    }

    protected void openServiceDocument(Cashier cashier) throws FiscalPrinterException {
        if (fiscalData.isCheckOpen()) {
            fiscalData.cancelCheque();
            fiscalData.skipAndCut();
        }
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if (sectionName.equals("logo")) {
                    printLogo();
                } else if (sectionName.equals("header")) {
                    openDocument(check);
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("position")) {
                    printLinesListInDoc(section.getContent());
                } else if (sectionName.equals("payment")) {
                    putPayments(check);
                } else if (sectionName.equals("fiscal")) {
                    annulCheck(check);
                } else if (!(sectionName.equals("footer") || sectionName.equals("cut"))) {
                    printLinesListInDoc(section.getContent());
                }
            }
        } catch (Exception e) {
            throwException(e);
        }
    }

    private void annulCheck(Check check) throws FiscalPrinterException {
        if (fiscalData.isCheckOpen()) {
            fiscalData.cancelCheque();
            ofdInfoWriter.writeOFDInfo(check);
            fiscalData.skipAndCut();
        }
    }

    private void putPayments(Check check) throws FiscalPrinterException {
        if (!check.isAnnul()) {
            for (Payment payment : check.getPayments()) {
                fiscalData.putCheckPayment((int) (payment.getIndexPaymentFDD100()), payment.getSum());
            }
        }
    }

    protected void printLinesListInDoc(List<FontLine> content) throws FiscalPrinterException {
        for (FontLine line : content) {
            if (line == null) {
                continue;
            }
            if (line.getBarcode() != null) {
                fiscalData.printBarcode(line.getBarcode());
            } else {
                fiscalData.printLine(line);
            }
        }
    }

    private void openDocument(FiscalDocument document) throws FiscalPrinterException {
        if (fiscalData.isCheckOpen()) {
            fiscalData.cancelCheque();
            fiscalData.skipAndCut();
        }
        if (document instanceof Check) {
            Check check = (Check) document;
            if (!check.isCopy()) {
                fiscalData.openCheck(check.getOperation(), check.getType(), check.getCashier(), check.getCheckNumber());
            }
        }
    }

    private void printLine(FontLine line) throws FiscalPrinterException {
        fiscalData.printLine(line);
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        return fiscalData.getMaxCharRow(font);
    }

    public List<PaymentType> getPayments() throws FiscalPrinterException {
        return fiscalData.getPayments();
    }

    @Override
    public boolean isMoneyDrawerOpen() throws FiscalPrinterException {
        if (printerView != null) {
            return printerView.isDrawerOpened();
        }
        return printer.isCashDrawerOpen();
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        if (printerView != null) {
            printerView.openMoneyDrawer();
        }
        fiscalData.openDrawer();
    }

    @Override
    public void beepCriticalError() {
        if (printerView != null) {
            printerView.beepCriticalError();
        }
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        fiscalData.resetShiftCounters();
        fiscalData.setShiftOpen();
        fiscalData.updateState();
        long result = fiscalData.getShiftNum();
        if (config.isRecreatePrinterFileOnShiftOpen()) {
            fiscalData.recreatePrinterFile();
        }
        try {
            // начнем контрольную ленту (КЛ)
            ((FiscalPrinterEmulatorData) fiscalData).setControlTapeCount(1L);
            getControlTapeWorker().createNewControlTape(1L);
        } catch (Exception e) {
            throwException(e);
        }
        return result;
    }

    protected void printLogo() {
        fiscalData.printLogo();
    }

    FiscalPrinterEmulatorData getFiscalPrinterData() {
        return (FiscalPrinterEmulatorData) fiscalData;
    }

    @Override
    public String getFactoryNum() {
        return "00" + innAddon + "00" + getProperties().getShopIndex().toString()
                + getProperties().getCashNumber().toString();
    }

    @Override
    public String getHardwareName() {
        return "Fiscal printer emulator " + innAddon;
    }

    @Override
    public boolean isOFDDevice() {
        if (printerView != null) {
            return printerView.isOFDMode();
        }
        return config.isOfdMode();
    }

    @Override
    public FiscalPrinterInfo getFiscalPrinterInfo() {
        FiscalPrinterInfo fiscalPrinterInfo = new FiscalPrinterInfo();
        fiscalPrinterInfo.setFirmware(getVerBios());
        fiscalPrinterInfo.setFnInfo(getFnInfo());
        fiscalPrinterInfo.setServiceInfo(getServiceInfo());
        fiscalPrinterInfo.setProxySoftware(new ProxySoftwareInfo("EmulatorProxy", "1.2.3"));
        return fiscalPrinterInfo;
    }

    private FnInfo getFnInfo() {
        final FnDocInfo firstNotSentDoc = new FnDocInfo();
        firstNotSentDoc.setDate("2017-05-19T10:15:30");
        firstNotSentDoc.setNumber(374L);
        FnInfo result = new FnInfo();
        result.setFirstNotSentDoc(firstNotSentDoc);
        result.setFnNumber("9999078900002856");

        FnStatus fnStatus = new FnStatus();
        fnStatus.setDocState(0L);
        fnStatus.setFnState(3L);
        fnStatus.setWarningFlag(0L);

        result.setFnStatus(fnStatus);
        result.setFnVersion("fn debug v 1.32");
        result.setLastFDNumber("374");
        result.setNotSentDocCount(getNotSentDocCount());
        return result;
    }

    private ServiceInfo getServiceInfo() {
        ServiceInfo result = new ServiceInfo();
        result.setBatteryVoltage(3172L);
        result.setCutsCount(1056L);
        result.setCutsCountTotal(1056L);
        result.setTemperature(30L);
        result.setThermoHeadResource(110100L);
        result.setThermoHeadResourceTotal(110100L);
        result.setVoltage(23947L);
        return result;
    }

    private Check validate(Check check) throws FiscalPrinterException {
        if (loyal.getLoyaltyProperties().isFz54Compatible()) {
            Set<String> violations = CheckUtils.validateCheck(check);
            if (!violations.isEmpty()) {
                throwException(new FiscalPrinterException("Нарушение ФЗ-54: " +
                        violations.stream().collect(Collectors.joining(", "))));
            }
        }
        return check;
    }

    private int getNotSentDocCount() {
        return ((FiscalPrinterEmulatorData) fiscalData).getNotSentDocCount();
    }

    @Override
    public void setPort(String port) {
    }

    @Override
    public void stop() throws FiscalPrinterException {
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return fiscalData.getKPK();
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        String result = fiscalData.getRegNum();
        if (result == null) {
            result = "NFM." + properties.getShopIndex().toString() + "." +
                    properties.getCashNumber().toString() + "." + innAddon + "." + System.currentTimeMillis();
            fiscalData.setRegNum(result);
        }
        return result;
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        try {
            return fiscalData.isShiftOpen();
        } catch (Exception e) {
            throwException(e);
        }
        return false;
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        return fiscalData.getShiftCounters();
    }

    @Override
    public int getPaymentLength() {
        return 20;
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        return fiscalData.getCountCashIn();
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        return fiscalData.getCountCashOut();
    }

    @Override
    public long getCountAnnul() throws FiscalPrinterException {
        return fiscalData.getCountAnnul();
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        return fiscalData.getCashAmount();
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) {

    }

    /**
     * Обработаем ошибку внутри ФР
     */
    protected void throwException(Exception e) throws FiscalPrinterException {
        if (e instanceof FiscalPrinterException) {
            LOG.error(e.getMessage(), e);
            throw (FiscalPrinterException) e;
        } else {
            LOG.error(e.getMessage(), e);
            throw new FiscalPrinterException(ResBundleFiscalPrinter.getString("UNKNOWN_ERROR"), CashErrorType.FISCAL_ERROR);
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        return null;
    }

}
