package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.CheckLine;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.MarginType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.CheckState;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.InternalCheck;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.InternalDisc;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.InternalGoods;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.InternalMargin;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check.InternalPayments;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ExceptionArea;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ManualExceptionAppender;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ManualFiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.IPrinterView;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableBarCode;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableFontLine;
import ru.crystals.util.JsonMappers;
import ru.crystals.utils.time.DateConverters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

public class FiscalPrinterEmulatorData extends ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData implements Remote, ManualExceptionAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(FiscalPrinter.class);

    private static final int PAYMENT_COUNT = 15;

    private static final TypeReference<Map<String, FiscalMarkValidationResult>> MARK_VALIDATION_RESULTS_TYPE = new TypeReference<Map<String, FiscalMarkValidationResult>>() {};
    private static final FiscalMarkValidationResult MARK_OK = new FiscalMarkValidationResult(null, 15, 15, 0, 1);

    private final String countersFile;
    private Properties counters;
    private InternalCheck check;
    private final Path markValidationResultsFile;
    private LocalDateTime markValidationResultsFileLastModified;
    private final Map<String, FiscalMarkValidationResult> fiscalMarkValidationResults = new HashMap<>();

    private SimpleDateFormat datetimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private DecimalFormat currencyFormatter = new DecimalFormat("#########0.00");
    private DecimalFormat quantityFormatter = new DecimalFormat("#########0.000");

    private List<PaymentType> payments;
    private boolean zOnClosedShift = false;
    private String inn = "781234567890";
    private int maxCharRow = 46;

    private ManualFiscalPrinterException manualException;

    private boolean isExceptionThrown = false;
    private Registry registry;
    private IPrinterView printerView;
    private FilePrinterEmulator fpe;
    private String manualRegNumber;
    private int manualNotSentDocCount = 1;

    public FiscalPrinterEmulatorData(FilePrinterEmulator fpe, long index) {
        super(index);
        this.fpe = fpe;
        countersFile = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + "counters_" + index + ".emulator.properties";
        counters = new Properties();
        markValidationResultsFile = Paths.get(Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + "mark_validation_results.json");
    }

    public String getCountersFile() {
        return countersFile;
    }

    public boolean isZOnClosedShift() {
        return zOnClosedShift;
    }

    @Override
    public void setZOnClosedShift(boolean isZOnClosedShift) {
        this.zOnClosedShift = isZOnClosedShift;
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        if (manualRegNumber != null) {
            return manualRegNumber;
        }
        return getStringProperty("RegNum");
    }

    @Override
    public void setEklz(String fnNum) {
        setStringProperty("FNNum", fnNum);
    }

    @Override
    public String getEklz() throws FiscalPrinterException {
        return getStringProperty("FNNum");
    }

    @Override
    public void setRegNum(String regNum) {
        setStringProperty("RegNum", regNum);
    }

    public void setManualRegNumber(String regNumber) {
        this.manualRegNumber = regNumber;
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        return getBooleanProperty("ShiftOpen");
    }

    @Override
    public void setShiftOpen() {
        setBooleanProperty("ShiftOpen", true);
        setDateTimeProperty("OpenDateTime", Calendar.getInstance().getTime());
    }

    private void setDateTimeProperty(String name, Date value) {
        setStringProperty(name, datetimeFormat.format(value));
    }

    private Date getOpenShiftTime() throws Exception {
        return getDateTimeProperty("OpenDateTime");
    }

    private boolean isShiftExpired() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar shiftExpTime = Calendar.getInstance();
            shiftExpTime.setTime(getOpenShiftTime());
            shiftExpTime.add(Calendar.HOUR_OF_DAY, 24);

            System.out.println("shiftExpTime: " + datetimeFormat.format(shiftExpTime.getTime()));
            System.out.println("currentTime: " + datetimeFormat.format(currentTime.getTime()));

            return currentTime.compareTo(shiftExpTime) > 0;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    private Date getDateTimeProperty(String name) throws FiscalPrinterException {
        String tmp = getStringProperty(name);
        if (tmp == null) {
            setBooleanProperty(name, false);
            tmp = getStringProperty(name);
        }

        if (tmp != null && !tmp.isEmpty()) {
            try {
                return datetimeFormat.parse(getStringProperty(name));
            } catch (Exception e) {
                throw new FiscalPrinterException(e.getMessage());
            }
        }
        return Calendar.getInstance().getTime();
    }

    @Override
    public void setShiftClose() {
        setBooleanProperty("ShiftOpen", false);
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        return getLongProperty("ShiftNum");
    }

    @Override
    public void setShiftNum(Long shiftNum) {
        setLongProperty("ShiftNum", shiftNum);
    }

    @Override
    public void incShiftNum() throws FiscalPrinterException {
        setLongProperty("ShiftNum", getShiftNum() + 1);
    }

    @Override
    public long getSPND() throws FiscalPrinterException {
        return getLongProperty("SPND");
    }

    private void setSPND(long docNum) {
        setLongProperty("SPND", docNum);
    }

    @Override
    public void incSPND() throws FiscalPrinterException {
        setLongProperty("SPND", getSPND() + 1);
    }

    @Override
    public long getKPK() throws FiscalPrinterException {
        return getLongProperty("KPK");
    }

    private void setKPK(long kpk) {
        setLongProperty("KPK", kpk);
    }

    @Override
    public void incKPK() throws FiscalPrinterException {
        setLongProperty("KPK", getKPK() + 1);
    }

    @Override
    public long getCountAnnul() throws FiscalPrinterException {
        return getLongProperty("CountAnnul");
    }

    private void setCountAnnul(long value) {
        setLongProperty("CountAnnul", value);
    }

    @Override
    public void incCountAnnul() throws FiscalPrinterException {
        setLongProperty("CountAnnul", getCountAnnul() + 1);
    }

    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        return getLongProperty("CountCashIn");
    }

    private void setCountCashIn(long value) {
        setLongProperty("CountCashIn", value);
    }

    @Override
    public void incCountCashIn() throws FiscalPrinterException {
        setLongProperty("CountCashIn", getCountCashIn() + 1);
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        return getLongProperty("CountCashOut");
    }

    private void setCountCashOut(long value) {
        setLongProperty("CountCashOut", value);
    }

    @Override
    public void incCountCashOut() throws FiscalPrinterException {
        setLongProperty("CountCashOut", getCountCashOut() + 1);
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        return getLongProperty("CashAmount");
    }

    @Override
    public void setCashAmount(long cashAmount) {
        setLongProperty("CashAmount", cashAmount);
    }

    @Override
    public void incCashAmount(long l) throws FiscalPrinterException {
        long start = getCashAmount();
        setLongProperty("CashAmount", start + l);
    }

    @Override
    public void decCashAmount(long l) throws FiscalPrinterException {
        long amount = getCashAmount() - l;
        if (amount < 0) {// нельзя уходить в минус при изятии
            amount = 0;
        }
        setLongProperty("CashAmount", amount);
    }

    private void setSumCashIn(long value) {
        setLongProperty("SumCashIn", value);
    }

    private void setSumCashOut(long value) {
        setLongProperty("SumCashOut", value);
    }

    private void setSumSale(long value) {
        setLongProperty("SumSale", value);
    }

    @Override
    public long getSumSale() throws FiscalPrinterException {
        return getLongProperty("SumSale");
    }

    @Override
    public void incSumSale(long l) throws FiscalPrinterException {
        long start = getSumSale();
        setSumSale(start + l);
    }

    @Override
    public void setCountSale(long value) {
        setLongProperty("CountSale", value);
    }

    @Override
    public long getCountSale() throws FiscalPrinterException {
        return getLongProperty("CountSale");
    }

    @Override
    public void incCountSale() throws FiscalPrinterException {
        long start = getCountSale();
        setCountSale(start + 1);
    }

    private void setSumReturn(long value) {
        setLongProperty("SumReturn", value);
    }

    @Override
    public long getSumReturn() throws FiscalPrinterException {
        return getLongProperty("SumReturn");
    }

    @Override
    public void incSumReturn(long l) throws FiscalPrinterException {
        long start = getSumReturn();
        setSumReturn(start + l);
    }

    @Override
    public void setCountReturn(long value) {
        setLongProperty("CountReturn", value);
    }

    @Override
    public long getCountReturn() throws FiscalPrinterException {
        return getLongProperty("CountReturn");
    }

    @Override
    public void incCountReturn() throws FiscalPrinterException {
        long start = getCountReturn();
        setCountReturn(start + 1);
    }

    @Override
    public void resetShiftCounters() {
        setCountAnnul(0L);
        setCountCashIn(0L);
        setCountCashOut(0L);

        setSumCashIn(0L);
        setSumCashOut(0L);

        setSumSale(0L);
        setCountSale(0L);
        setSumReturn(0L);
        setCountReturn(0L);
        setSumExpense(0L);
        setCountExpense(0L);
        setSumReturnExpense(0L);
        setCountReturnExpense(0L);

        setSumCashlessSale(0L);
        setSumCashSale(0L);
        setSumPrePaySale(0L);
        setSumPostPaySale(0L);
        setSumOtherPaySale(0L);
        setSumCashlessReturn(0L);
        setSumCashReturn(0L);
        setSumPrePayReturn(0L);
        setSumPostPayReturn(0L);
        setSumOtherPayReturn(0L);
        setSumCashlessExpense(0L);
        setSumCashExpense(0L);
        setSumOtherPayExpense(0L);
        setSumCashlessReturnExpense(0L);
        setSumCashReturnExpense(0L);
        setSumOtherPayReturnExpense(0L);

        setCountCashlessSale(0L);
        setCountCashSale(0L);
        setCountPrePaySale(0L);
        setCountPostPaySale(0L);
        setCountOtherPaySale(0L);
        setCountCashlessReturn(0L);
        setCountCashReturn(0L);
        setCountPrePayReturn(0L);
        setCountPostPayReturn(0L);
        setCountOtherPayReturn(0L);
        setCountCashlessExpense(0L);
        setCountCashExpense(0L);
        setCountOtherPayExpense(0L);
        setCountCashlessReturnExpense(0L);
        setCountCashReturnExpense(0L);
        setCountOtherPayReturnExpense(0L);
    }

    private void setInitialState() {
        setShiftClose();
        setShiftNum(1L);
        setSPND(1L);
        setKPK(1L);
        setIncrescentTotalSale(0L);
        setIncrescentTotalReturn(0L);
        setIncrescentTotalExpense(0L);
        setIncrescentTotalReturnExpense(0L);
        setCashAmount(0L);
        setEklz(UUID.randomUUID().toString());
        setStartZ(false);
    }

    private void loadCheckFile() throws Exception {
        check = new InternalCheck();
    }

    @Override
    public void loadState() throws Exception {
        File file = new File(countersFile);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                counters.load(is);
            }
        } else {
            setInitialState();
            resetShiftCounters();
            updateState();
        }
        loadMarkValidationResults();
        loadManualException();
        loadCheckFile();
    }

    private void loadMarkValidationResults() {
        try {
            if (Files.exists(markValidationResultsFile)) {
                readMarkCodeValidationResults();
            } else {
                Files.createFile(markValidationResultsFile);
                fiscalMarkValidationResults.put("010123456789012321XHe\"ImQ>*A&jOL91808B92BCBr3YRDprM1AAWPkjE" +
                        "/RatPM7XyltEtqOTV4Y9bOtnegQLzeh1OVuOZHMfQDSMqTnXjIcM8Yb20qLr4d+Ykfg==", MARK_OK);
                String result = JsonMappers.getDefaultMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(fiscalMarkValidationResults);
                Files.write(markValidationResultsFile, result.getBytes());
            }
            markValidationResultsFileLastModified = DateConverters.toLocalDateTime(markValidationResultsFile.toFile().lastModified());
        } catch (IOException e) {
            LOGGER.error("Exception occurred while working with file {}", markValidationResultsFile.getFileName().toString());
        }
    }

    private void readMarkCodeValidationResults() throws IOException {
        InputStream is = Files.newInputStream(markValidationResultsFile);
        fiscalMarkValidationResults.clear();
        fiscalMarkValidationResults.putAll(JsonMappers.getDefaultMapper().readValue(is, MARK_VALIDATION_RESULTS_TYPE));
    }

    @Override
    public void updateState() throws FiscalPrinterException {
        try (OutputStream out = new FileOutputStream(countersFile)) {
            counters.store(out, "Fiscal printer state");
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    protected long getLongProperty(String name) throws FiscalPrinterException {
        String p = getStringProperty(name);
        if (p == null) {
            setLongProperty(name, 0L);
            p = getStringProperty(name);
        }

        try {
            return Long.parseLong(p);
        } catch (Exception e) {
            throw new FiscalPrinterException("Incorrect value of property: " + "file - " + countersFile + "/n" + "property name - " + name + "/n"
                    + "property value - " + p + ", instead of long value./n");
        }
    }

    protected void setLongProperty(String name, long value) {
        setStringProperty(name, Long.toString(value));
    }

    private boolean getBooleanProperty(String name) throws FiscalPrinterException {
        String p = getStringProperty(name);
        if (p == null) {
            setBooleanProperty(name, false);
            p = getStringProperty(name);
        }

        return "TRUE".equalsIgnoreCase(p);
    }

    private void loadManualException() {
        String exceptionMessage = counters.getProperty("ExceptionMessage");
        if (exceptionMessage != null) {
            String exceptionArea = counters.getProperty("ExceptionArea");
            String boolFatal = counters.getProperty("ExceptionFatal");
            String boolThrown = counters.getProperty("ExceptionThrowed");
            boolean exceptionFatal = "TRUE".equalsIgnoreCase(boolFatal);
            if (exceptionArea != null && ExceptionArea.contains(exceptionArea)) {
                manualException = new ManualFiscalPrinterException(exceptionMessage, ExceptionArea.valueOf(exceptionArea), exceptionFatal);
                isExceptionThrown = (boolThrown != null && boolThrown.equalsIgnoreCase("TRUE"));
            } else {
                manualException = null;
                isExceptionThrown = false;
            }
        } else {
            manualException = null;
            isExceptionThrown = false;
        }
        if (manualException != null) {
            LOGGER.info("Manual Exception has been restored. " + manualException + "." + (isExceptionThrown ? " Exception has been thrown" : " Exception has not been " +
                    "thrown"));
        } else {
            LOGGER.info("Manual Exception has been restored. Exception has been canceled");
        }
    }

    private void saveManualException() {
        try {
            if (manualException != null) {
                counters.setProperty("ExceptionMessage", manualException.getMessage());
                counters.setProperty("ExceptionArea", manualException.getExceptionArea().toString());
                counters.setProperty("ExceptionFatal", Boolean.toString(manualException.isFatal()));
                counters.setProperty("ExceptionThrowed", Boolean.toString(isExceptionThrown));
            } else {
                counters.setProperty("ExceptionMessage", "");
                counters.setProperty("ExceptionArea", "");
                counters.setProperty("ExceptionFatal", "");
                counters.setProperty("ExceptionThrowed", "");
            }
            updateState();
        } catch (FiscalPrinterException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    private void setBooleanProperty(String name, boolean value) {
        setStringProperty(name, Boolean.toString(value));
    }

    private String getStringProperty(String name) throws FiscalPrinterException {
        throwManualException(null);
        return counters.getProperty(name);
    }

    private void setStringProperty(String name, String value) {
        counters.setProperty(name, value);
        try {
            updateState();
        } catch (FiscalPrinterException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public Long getSumCashSale() throws FiscalPrinterException {
        return getLongProperty("SumCashSale");
    }

    public Long getSumCashlessSale() throws FiscalPrinterException {
        return getLongProperty("SumCashlessSale");
    }

    public Long getSumPrePaySale() throws FiscalPrinterException {
        return getLongProperty("SumPrePaySale");
    }

    public Long getSumPostPaySale() throws FiscalPrinterException {
        return getLongProperty("SumPostPaySale");
    }

    public Long getSumOtherPaySale() throws FiscalPrinterException {
        return getLongProperty("SumOtherPaySale");
    }

    public Long getSumCashReturn() throws FiscalPrinterException {
        return getLongProperty("SumCashReturn");
    }

    public Long getSumCashlessReturn() throws FiscalPrinterException {
        return getLongProperty("SumCashlessReturn");
    }

    public Long getSumPrePayReturn() throws FiscalPrinterException {
        return getLongProperty("SumPrePayReturn");
    }

    public Long getSumPostPayReturn() throws FiscalPrinterException {
        return getLongProperty("SumPostPayReturn");
    }

    public Long getSumOtherPayReturn() throws FiscalPrinterException {
        return getLongProperty("SumOtherPayReturn");
    }

    public Long getSumCashIn() throws FiscalPrinterException {
        return getLongProperty("SumCashIn");
    }

    public Long getSumCashOut() throws FiscalPrinterException {
        return getLongProperty("SumCashOut");
    }

    public Long getCountCashSale() throws FiscalPrinterException {
        return getLongProperty("CountCashSale");
    }

    public Long getCountCashlessSale() throws FiscalPrinterException {
        return getLongProperty("CountCashlessSale");
    }

    public Long getCountPrePaySale() throws FiscalPrinterException {
        return getLongProperty("CountPrePaySale");
    }

    public Long getCountPostPaySale() throws FiscalPrinterException {
        return getLongProperty("CountPostPaySale");
    }

    public Long getCountOtherPaySale() throws FiscalPrinterException {
        return getLongProperty("CountOtherPaySale");
    }

    public Long getCountCashReturn() throws FiscalPrinterException {
        return getLongProperty("CountCashReturn");
    }

    public Long getCountCashlessReturn() throws FiscalPrinterException {
        return getLongProperty("CountCashlessReturn");
    }

    public Long getCountPrePayReturn() throws FiscalPrinterException {
        return getLongProperty("CountPrePayReturn");
    }

    public Long getCountPostPayReturn() throws FiscalPrinterException {
        return getLongProperty("CountPostPayReturn");
    }

    public Long getCountOtherPayReturn() throws FiscalPrinterException {
        return getLongProperty("CountOtherPayReturn");
    }

    public void setCashSale(long value) {
        setLongProperty("SumCashSale", value);
    }

    public void setSumCashSale(long value) {
        setLongProperty("SumCashSale", value);
    }

    public void setSumCashlessSale(long value) {
        setLongProperty("SumCashlessSale", value);
    }

    public void setSumPrePaySale(long value) {
        setLongProperty("SumPrePaySale", value);
    }

    public void setSumPostPaySale(long value) {
        setLongProperty("SumPostPaySale", value);
    }

    public void setSumOtherPaySale(long value) {
        setLongProperty("SumOtherPaySale", value);
    }

    public void setSumCashReturn(long value) {
        setLongProperty("SumCashReturn", value);
    }

    public void setSumCashlessReturn(long value) {
        setLongProperty("SumCashlessReturn", value);
    }

    public void setSumPrePayReturn(long value) {
        setLongProperty("SumPrePayReturn", value);
    }

    public void setSumPostPayReturn(long value) {
        setLongProperty("SumPostPayReturn", value);
    }

    public void setSumOtherPayReturn(long value) {
        setLongProperty("SumOtherPayReturn", value);
    }

    public void setCountCashSale(long value) {
        setLongProperty("CountCashSale", value);
    }

    public void setCountCashlessSale(long value) {
        setLongProperty("CountCashlessSale", value);
    }

    public void setCountPrePaySale(long value) {
        setLongProperty("CountPrePaySale", value);
    }

    public void setCountPostPaySale(long value) {
        setLongProperty("CountPostPaySale", value);
    }

    public void setCountOtherPaySale(long value) {
        setLongProperty("CountOtherPaySale", value);
    }

    public void setCountCashReturn(long value) {
        setLongProperty("CountCashReturn", value);
    }

    public void setCountCashlessReturn(long value) {
        setLongProperty("CountCashlessReturn", value);
    }

    public void setCountPrePayReturn(long value) {
        setLongProperty("CountPrePayReturn", value);
    }

    public void setCountPostPayReturn(long value) {
        setLongProperty("CountPostPayReturn", value);
    }

    public void setCountOtherPayReturn(long value) {
        setLongProperty("CountOtherPayReturn", value);
    }

    public void incSumCashSale(long value) throws FiscalPrinterException {
        setLongProperty("SumCashSale", getLongProperty("SumCashSale") + value);
    }

    public void incSumCashlessSale(long value) throws FiscalPrinterException {
        setLongProperty("SumCashlessSale", getLongProperty("SumCashlessSale") + value);
    }

    public void incSumPrePaySale(long value) throws FiscalPrinterException {
        setLongProperty("SumPrePaySale", getLongProperty("SumPrePaySale") + value);
    }

    public void incSumPostPaySale(long value) throws FiscalPrinterException {
        setLongProperty("SumPostPaySale", getLongProperty("SumPostPaySale") + value);
    }

    public void incSumOtherPaySale(long value) throws FiscalPrinterException {
        setLongProperty("SumOtherPaySale", getLongProperty("SumOtherPaySale") + value);
    }

    public void incSumCashReturn(long value) throws FiscalPrinterException {
        setLongProperty("SumCashReturn", getLongProperty("SumCashReturn") + value);
    }

    public void incSumCashlessReturn(long value) throws FiscalPrinterException {
        setLongProperty("SumCashlessReturn", getLongProperty("SumCashlessReturn") + value);
    }

    public void incSumPrePayReturn(long value) throws FiscalPrinterException {
        setLongProperty("SumPrePayReturn", getLongProperty("SumPrePayReturn") + value);
    }

    public void incSumPostPayReturn(long value) throws FiscalPrinterException {
        setLongProperty("SumPostPayReturn", getLongProperty("SumPostPayReturn") + value);
    }

    public void incSumOtherPayReturn(long value) throws FiscalPrinterException {
        setLongProperty("SumOtherPayReturn", getLongProperty("SumOtherPayReturn") + value);
    }

    public void incSumCashIn(long value) throws FiscalPrinterException {
        setLongProperty("SumCashIn", getSumCashIn() + value);
    }

    public void incSumCashOut(long value) throws FiscalPrinterException {
        setLongProperty("SumCashOut", getSumCashOut() + value);
    }

    public void incCountCashSale() throws FiscalPrinterException {
        setLongProperty("CountCashSale", getLongProperty("CountCashSale") + 1);
    }

    public void incCountCashlessSale() throws FiscalPrinterException {
        setLongProperty("CountCashlessSale", getLongProperty("CountCashlessSale") + 1);
    }

    public void incCountPrePaySale() throws FiscalPrinterException {
        setLongProperty("CountPrePaySale", getLongProperty("CountPrePaySale") + 1);
    }

    public void incCountPostPaySale() throws FiscalPrinterException {
        setLongProperty("CountPostPaySale", getLongProperty("CountPostPaySale") + 1);
    }

    public void incCountOtherPaySale() throws FiscalPrinterException {
        setLongProperty("CountOtherPaySale", getLongProperty("CountOtherPaySale") + 1);
    }

    public void incCountCashReturn() throws FiscalPrinterException {
        setLongProperty("CountCashReturn", getLongProperty("CountCashReturn") + 1);
    }

    public void incCountCashlessReturn() throws FiscalPrinterException {
        setLongProperty("CountCashlessReturn", getLongProperty("CountCashlessReturn") + 1);
    }

    public void incCountPrePayReturn() throws FiscalPrinterException {
        setLongProperty("CountPrePayReturn", getLongProperty("CountPrePayReturn") + 1);
    }

    public void incCountPostPayReturn() throws FiscalPrinterException {
        setLongProperty("CountPostPayReturn", getLongProperty("CountPostPayReturn") + 1);
    }

    public void incCountOtherPayReturn() throws FiscalPrinterException {
        setLongProperty("CountOtherPayReturn", getLongProperty("CountOtherPayReturn") + 1);
    }

    public void setControlTapeCount(long controlTapeCount) throws FiscalPrinterException {
        setLongProperty("ControlTapeCount", controlTapeCount);
    }

    public long getControlTapeCount() throws FiscalPrinterException {
        return getLongProperty("ControlTapeCount");
    }

    @Override
    public boolean isCheckOpen() throws FiscalPrinterException {
        throwManualException(null);
        return check != null && check.isOpen();
    }

    @Override
    public void closeCheck(Check purchase, BarCode barcode) throws FiscalPrinterException {
        throwManualException(ExceptionArea.CLOSE_CHECK_BEFORE_SAVE);

        try {
            long cashSum = check.getCashSum();
            long cashSumFull = check.getCashSum(false);
            long cashlessSum = check.getCashlessSum();
            long checkSum = check.getCheckSum();
            long prePaySum = check.getPrePaymentSum();
            long postPaySum = check.getPostPaymentSum();
            long otherPaySum = check.getOtherPaySum();

            if (checkSum > cashlessSum + cashSum + prePaySum + postPaySum + otherPaySum) {
                throw new FiscalPrinterException("Сумма оплат недостаточна");
            }

            incKPK();
            incSPND();
            if (check.type == CheckType.SALE) {
                if (check.getOperation() == CashOperation.EXPENSE) {
                    updateExpenseReceiptCounters(checkSum, cashSum, cashlessSum, otherPaySum);
                } else {
                    updateSaleCounters(checkSum, cashSum, cashlessSum, prePaySum, postPaySum, otherPaySum);
                }
            } else {
                if (check.getOperation() == CashOperation.EXPENSE) {
                    updateReturnExpenseReceiptCounters(checkSum, cashSum, cashlessSum, otherPaySum);
                } else {
                    updateReturnCounters(checkSum, cashSum, cashlessSum, prePaySum, postPaySum, otherPaySum);
                }
            }

            setStringProperty("lastCheckType", check.type.toString());
            setLongProperty("lastCheckSumm", check.getCheckSum());

            if (cashSumFull > checkSum - cashlessSum) {
                String s = "  СДАЧА";
                s += StringUtils.leftPad(" = " + currencyFormatter.format((cashSumFull + cashlessSum - checkSum) / 100D), maxCharRow - s.length(), " ");
                SerializableFontLine line = new SerializableFontLine(s, Font.NORMAL);
                fpe.writeToFile(line);
                printTextToView(line);
            }
            if (barcode != null && barcode.getValue() != null && !barcode.getValue().isEmpty()) {
                printBarcode(barcode);
            }
            printFiscalSign(purchase);

            Thread.sleep(Timeouts.CLOSE_CHECK_INTERVAL);

            throwManualException(ExceptionArea.CLOSE_CHECK_AFTER_SAVE);

            check.clear();
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    private void updateSaleCounters(long checkSum, long cashSum, long cashlessSum,
            long prePaySum, long postPaySum, long otherPaySum) throws FiscalPrinterException {
        incIncrescentTotalSale(checkSum);
        incCashAmount(cashSum);
        incSumSale(checkSum);
        incCountSale();
        if (cashSum > 0) {
            incSumCashSale(cashSum);
            incCountCashSale();
        }
        if (cashlessSum > 0) {
            incSumCashlessSale(cashlessSum);
            incCountCashlessSale();
        }
        if (prePaySum > 0) {
            incSumPrePaySale(prePaySum);
            incCountPrePaySale();
        }
        if (postPaySum > 0) {
            incSumPostPaySale(postPaySum);
            incCountPostPaySale();
        }
        if (otherPaySum > 0) {
            incSumOtherPaySale(otherPaySum);
            incCountOtherPaySale();
        }
    }

    private void updateExpenseReceiptCounters(long checkSum, long cashSum, long cashlessSum, long otherPaySum) throws FiscalPrinterException {
        incIncrescentTotalExpense(checkSum);
        decCashAmount(cashSum);
        incSumExpense(checkSum);
        incCountExpense();
        if (cashSum > 0) {
            incSumCashExpense(cashSum);
            incCountCashExpense();
        }
        if (cashlessSum > 0) {
            incSumCashlessExpense(cashlessSum);
            incCountCashlessExpense();
        }
        if (otherPaySum > 0) {
            incSumOtherPayExpense(otherPaySum);
            incCountOtherPayExpense();
        }
    }

    private void updateReturnCounters(long checkSum, long cashSum, long cashlessSum,
            long prePaySum, long postPaySum, long otherPaySum) throws FiscalPrinterException {
        incIncrescenTotalReturn(checkSum);
        decCashAmount(cashSum);
        incSumReturn(checkSum);
        incCountReturn();
        if (cashSum > 0) {
            incSumCashReturn(cashSum);
            incCountCashReturn();
        }
        if (cashlessSum > 0) {
            incSumCashlessReturn(cashlessSum);
            incCountCashlessReturn();
        }
        if (prePaySum > 0) {
            incSumPrePayReturn(prePaySum);
            incCountPrePayReturn();
        }
        if (postPaySum > 0) {
            incSumPostPayReturn(postPaySum);
            incCountPostPayReturn();
        }
        if (otherPaySum > 0) {
            incSumOtherPayReturn(otherPaySum);
            incCountOtherPayReturn();
        }
    }

    private void updateReturnExpenseReceiptCounters(long checkSum, long cashSum, long cashlessSum, long otherPaySum) throws FiscalPrinterException {
        incIncrescentTotalReturnExpense(checkSum);
        incCashAmount(cashSum);
        incSumReturnExpense(checkSum);
        incCountReturnExpense();
        if (cashSum > 0) {
            incSumCashReturnExpense(cashSum);
            incCountCashReturnExpense();
        }
        if (cashlessSum > 0) {
            incSumCashlessReturnExpense(cashlessSum);
            incCountCashlessReturnExpense();
        }
        if (otherPaySum > 0) {
            incSumOtherPayReturnExpense(otherPaySum);
            incCountOtherPayReturnExpense();
        }
    }

    @Override
    public void openCheck(CashOperation operation, CheckType type, Cashier cashier, Long checkNumber) throws FiscalPrinterException {
        throwManualException(ExceptionArea.OPEN_CHECK);
        if (isCheckOpen()) {
            throw new FiscalPrinterException("Присутствует открытый документ");
        }
        if (isShiftExpired()) {
            throw new FiscalPrinterException("Смена больше 24 часов", CashErrorType.SHIFT_OPERATION_NEED);
        }
        try {
            check.setState(CheckState.OPEN);
            check.setType(type);
            check.setOperation(operation);
            check.setShiftNumber(getShiftNum());

            List<SerializableFontLine> text = new ArrayList<>(getHeaderLines(cashier));
            text.add(new SerializableFontLine("  " + getCheckTypeText()
                    + (checkNumber == null ? (getSPND() + 1) : checkNumber), Font.NORMAL));
            List<CheckLine> lines = text.stream().map(e -> (CheckLine) e).collect(Collectors.toList());
            fpe.writeToFile(lines);
            printTextToView(text);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private String getCheckTypeText() {
        if (CashOperation.EXPENSE.equals(check.getOperation())) {
            return check.type == CheckType.SALE ? "ЧЕК РАСХОДА N " : "ЧЕК ВОЗВРАТА РАСХОДА N ";
        }
        return check.type == CheckType.SALE ? "ЧЕК ПРОДАЖИ N " : "ЧЕК ВОЗВРАТА N ";
    }

    @Override
    public void putCheckPosition(String goodsName, long quantity, long price, Long depart) throws FiscalPrinterException {
        throwManualException(null);

        if (!isCheckOpen()) {
            throw new FiscalPrinterException("Документ не открыт");
        }
        if (check.getState() != CheckState.OPEN) {
            throw new FiscalPrinterException("Неверное состояние документа");
        }
        throwManualException(ExceptionArea.APPEND_POSITION);

        try {
            InternalGoods pos = new InternalGoods();
            pos.departNumber = depart;
            pos.goodsName = goodsName;
            pos.price = price;
            pos.quantity = quantity;
            check.addGoods(pos);

            List<SerializableFontLine> text = new ArrayList<>();
            text.add(new SerializableFontLine(goodsName, Font.NORMAL));
            String s = "  ";
            s += StringUtils.leftPad(currencyFormatter.format(price / 100D), 15, " ") + " * ";
            s += StringUtils.rightPad(quantityFormatter.format(quantity / 1000D), 11, " ") + " = ";
            s += StringUtils.rightPad(currencyFormatter.format((quantity * price) / 1000D / 100D), maxCharRow - s.length(), " ");
            text.add(new SerializableFontLine(s, Font.NORMAL));

            List<CheckLine> lines = text.stream().map(e -> (CheckLine) e).collect(Collectors.toList());
            fpe.writeToFile(lines);
            printTextToView(text);

        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public void putCheckPayment(int indexPayment, long sum) throws FiscalPrinterException {
        throwManualException(ExceptionArea.APPEND_PAYMENT);
        if (!isCheckOpen()) {
            throw new FiscalPrinterException("Документ не открыт");
        }
        try {
            if (check.getState() == CheckState.OPEN) {
                String s = "  ИТОГО";
                s += StringUtils.leftPad(" = " + currencyFormatter.format(check.getCheckSum() / 100D), maxCharRow - s.length(), " ");
                SerializableFontLine line = new SerializableFontLine(s, Font.NORMAL);
                fpe.writeToFile(line);
                printTextToView(line);
                check.setState(CheckState.SUBTOTAL);
            }
            throwManualException(null);

            InternalPayments pay = new InternalPayments();
            pay.index = indexPayment;
            pay.value = sum;
            check.addPayment(pay);

            String s = "  ";
            try {
                s += getPayments().get(indexPayment).getName();
            } catch (IndexOutOfBoundsException e) {
                LOGGER.warn("getPayments().get(" + indexPayment + ").getName() exception is " + ExceptionUtils.getFullStackTrace(e));
            }
            s += StringUtils.leftPad(" = " + currencyFormatter.format(sum / 100D), maxCharRow - s.length(), " ");
            SerializableFontLine line = new SerializableFontLine(s, Font.NORMAL);
            fpe.writeToFile(line);
            printTextToView(line);

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public void doCashOperation(InventoryOperationType operationType, long value, Cashier cashier) throws FiscalPrinterException {
        throwManualException(ExceptionArea.CASH_OPERATION_BEFORE_SAVE);
        if (isCheckOpen()) {
            throw new FiscalPrinterException("Присутствует открытый чек");
        }
        try {
            incSPND();

            if (operationType == InventoryOperationType.CASH_IN) {
                setCashAmount(getCashAmount() + value);
                incCountCashIn();
                incSumCashIn(value);
            } else if (operationType == InventoryOperationType.CASH_OUT) {
                setCashAmount(getCashAmount() - value);
                incCountCashOut();
                incSumCashOut(value);
            }
            SerializableFontLine line = new SerializableFontLine(StringUtils.leftPad("= " + currencyFormatter.format(value / 100D), maxCharRow), Font.NORMAL);
            fpe.writeToFile(line);
            printTextToView(line);

            printFiscalSign(null);
            Thread.sleep(Timeouts.CLOSE_CHECK_INTERVAL);
            throwManualException(ExceptionArea.CASH_OPERATION_AFTER_SAVE);
            printCutter();

        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    private void printFiscalSign(Check purchase) throws FiscalPrinterException {
        throwManualException(null);
        try {
            List<FontLine> text = new ArrayList<>();
            Date currentDate = Calendar.getInstance().getTime();
            String d = dateFormat.format(currentDate) + " " + timeFormat.format(currentDate);
            String s = "#" + StringUtils.leftPad(String.valueOf(getKPK()), 4, "0") + " ДОК. " + StringUtils.leftPad(String.valueOf(getSPND()), 7, "0");
            s += StringUtils.leftPad(d, maxCharRow - s.length(), " ");
            text.add(new FontLine(s, Font.NORMAL));
            String[] regnum = getRegNum().split("\\.");
            s = " KKM " + StringUtils.rightPad(regnum.length > 3 ? regnum[3] : regnum[0], 16);
            s += StringUtils.leftPad("ИНН " + getINN(), maxCharRow - s.length());
            text.add(new FontLine(s, Font.NORMAL));

            if (purchase != null && purchase.getClientRequisites() != null) {
                s = "АДРЕС ПОКУПАТЕЛЯ: " + purchase.getClientRequisites();
                text.add(new FontLine(s, Font.NORMAL));
                printTextToView(new SerializableFontLine(s, Font.NORMAL));
            }
            List<CheckLine> lines = text.stream().map(e -> (CheckLine) e).collect(Collectors.toList());
            fpe.writeToFile(lines);
            printFiscalToView(getShiftNum(), getSPND(), getKPK());
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    public String getINN() {
        return inn;
    }

    @Override
    public void setINN(String inn) {
        this.inn = inn;
    }

    @Override
    public void zReport(Cashier cashier) throws FiscalPrinterException {
        throwManualException(ExceptionArea.CLOSE_SHIFT_BEFORE_SAVE);
        if (!isZOnClosedShift() && !isShiftOpen()) {
            throw new FiscalPrinterException("Смена не открыта");
        }
        try {
            incShiftNum();
            incKPK();
            incSPND();
            setStartZ(true);
            printZXReport(cashier, true);
            resetShiftCounters();
            setShiftClose();
            throwManualException(ExceptionArea.CLOSE_SHIFT_AFTER_SAVE);
            setStartZ(false);
        } catch (FiscalPrinterException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public void xReport(Cashier cashier) throws FiscalPrinterException {
        printZXReport(cashier, false);
        incSPND();
    }

    private void printZXReport(Cashier cashier, boolean isZ) throws FiscalPrinterException {
        throwManualException(null);
        try {
            List<SerializableFontLine> text = new ArrayList<>(getHeaderLines(cashier));
            text.add(new SerializableFontLine(StringUtils.center(isZ ? "Z-ОТЧЁТ" : "X-ОТЧЁТ", maxCharRow), Font.NORMAL));
            text.add(new SerializableFontLine(StringUtils.repeat("-", maxCharRow), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" ИТОГО ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРИХОД          :" + formatRightAndFillBySpace(getCountSale() + " чек", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ ПРИХОДА :" + formatRightAndFillBySpace(getCountReturn() + " чек", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД          :" + formatRightAndFillBySpace(getCountExpense()+ " чек", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА :" + formatRightAndFillBySpace(getCountReturnExpense()+ " чек", maxCharRow - 17), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" НАЛИЧНЫЕ ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖ   :" + formatRightAndFillBySpace(getCountCashSale() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТОВ:" + formatRightAndFillBySpace(getCountCashReturn() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД   :" + formatRightAndFillBySpace(getCountCashExpense()+ " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА :" + formatRightAndFillBySpace(getCountCashReturnExpense() + " чек", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖА  :" + formatRightAndFillBySpace(formatAmount(getSumCashSale()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ  :" + formatRightAndFillBySpace(formatAmount(getSumCashReturn()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД   :" + formatRightAndFillBySpace(formatAmount(getSumCashExpense()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА :" + formatRightAndFillBySpace(getSumCashReturnExpense() + " руб", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ   :" + formatRightAndFillBySpace(formatAmount(
                    (getSumCashSale() + getSumCashReturnExpense()) - (getSumCashReturn() + getSumCashExpense())) + " руб", maxCharRow - 10), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" БЕЗНАЛИЧНЫМИ ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖ   :" + formatRightAndFillBySpace(getCountCashlessSale() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТОВ:" + formatRightAndFillBySpace(getCountCashlessReturn() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД   :" + formatRightAndFillBySpace(getCountCashlessExpense()+ " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА :" + formatRightAndFillBySpace(getCountCashlessReturnExpense() + " чек", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖА  :" + formatRightAndFillBySpace(formatAmount(getSumCashlessSale()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ  :" + formatRightAndFillBySpace(formatAmount(getSumCashlessReturn()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД   :" + formatRightAndFillBySpace(formatAmount(getSumCashlessExpense()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА :" + formatRightAndFillBySpace(
                    formatAmount(getSumCashlessReturnExpense()) + " руб", maxCharRow - 17), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ   :" + formatRightAndFillBySpace(formatAmount(
                    (getSumCashlessSale() + getSumCashlessReturnExpense()) - (getSumCashlessReturn() + getSumCashlessExpense())) + " руб", maxCharRow - 10), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" ПРЕДВАРИТЕЛЬНАЯ ОПЛАТА (АВАНС)  ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖ   :" + formatRightAndFillBySpace(getCountPrePaySale() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТОВ:" + formatRightAndFillBySpace(getCountPrePayReturn() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖА  :" + formatRightAndFillBySpace(formatAmount(getSumPrePaySale()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ  :" + formatRightAndFillBySpace(formatAmount(getSumPrePayReturn()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ   :" + formatRightAndFillBySpace(formatAmount(getSumPrePaySale() - getSumPrePayReturn()) + " руб",
                    maxCharRow - 10), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" ПОСЛЕДУЮЩАЯ ОПЛАТА (КРЕДИТ) ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖ   :" + formatRightAndFillBySpace(getCountPostPaySale() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТОВ:" + formatRightAndFillBySpace(getCountPostPayReturn() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖА  :" + formatRightAndFillBySpace(formatAmount(getSumPostPaySale()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ  :" + formatRightAndFillBySpace(formatAmount(getSumPostPayReturn()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ   :" + formatRightAndFillBySpace(formatAmount(getSumPostPaySale() - getSumPostPayReturn()) + " руб",
                    maxCharRow - 10), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" ИНАЯ ОПЛАТА (ВСТРЕЧНЫМ ПРЕДЛОЖЕНИЕМ) ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖ   :" + formatRightAndFillBySpace(getCountOtherPaySale() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТОВ:" + formatRightAndFillBySpace(getCountOtherPayReturn() + " чек", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ПРОДАЖА  :" + formatRightAndFillBySpace(formatAmount(getSumOtherPaySale()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ  :" + formatRightAndFillBySpace(formatAmount(getSumOtherPayReturn()) + " руб", maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ   :" + formatRightAndFillBySpace(formatAmount(getSumOtherPaySale() - getSumOtherPayReturn()) + " руб",
                    maxCharRow - 10), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.repeat("-", maxCharRow), Font.NORMAL));
            text.add(new SerializableFontLine("ПРИХОД ВСЕГО          :" + formatRightAndFillBySpace(formatAmount(getSumSale()) + " руб", maxCharRow - 23), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ ПРИХОДА ВСЕГО :" + formatRightAndFillBySpace(formatAmount(getSumReturn()) + " руб", maxCharRow - 23), Font.NORMAL));
            text.add(new SerializableFontLine("РАСХОД ВСЕГО          :" + formatRightAndFillBySpace(formatAmount(getSumExpense()) + " руб", maxCharRow - 23), Font.NORMAL));
            text.add(new SerializableFontLine("ВОЗВРАТ РАСХОДА ВСЕГО :" + formatRightAndFillBySpace(formatAmount(getSumReturnExpense()) + " руб", maxCharRow - 23), Font.NORMAL));
            text.add(new SerializableFontLine("ОБОРОТ ВСЕГО          :" + formatRightAndFillBySpace(formatAmount(
                    (getSumSale() + getSumReturnExpense()) - (getSumReturn() + getSumExpense())) + " руб", maxCharRow - 23), Font.NORMAL));

            text.add(new SerializableFontLine(StringUtils.center(" АННУЛИРОВАНО ", maxCharRow, '-'), Font.NORMAL));
            text.add(new SerializableFontLine("ЧЕКОВ    :" + formatRightAndFillBySpace(Long.toString(getCountAnnul()), maxCharRow - 10), Font.NORMAL));
            text.add(new SerializableFontLine(StringUtils.repeat("-", maxCharRow), Font.NORMAL));

            text.add(new SerializableFontLine("ИТОГО ВНЕСЕНО  :" + formatRightAndFillBySpace(formatAmount(getSumCashIn()) + " руб", maxCharRow - 16), Font.NORMAL));
            text.add(new SerializableFontLine("ИТОГО ИЗЪЯТО   :" + formatRightAndFillBySpace(formatAmount(getSumCashOut()) + " руб", maxCharRow - 16), Font.NORMAL));
            text.add(new SerializableFontLine("ДЕНЕГ СЕЙЧАС   :" + formatRightAndFillBySpace(formatAmount(getCashAmount()) + " руб", maxCharRow - 16), Font.NORMAL));

            text.add(new SerializableFontLine("\nНАРАСТ. ПРИХОД :" + formatRightAndFillBySpace(formatAmount(getIncrescentTotalSale()) + " руб", maxCharRow - 18), Font.NORMAL));
            text.add(new SerializableFontLine("\nНАРАСТ. ВОЗВРАТ:" + formatRightAndFillBySpace(formatAmount(getIncrescentTotalReturn()) + " руб", maxCharRow - 18), Font.NORMAL));
            text.add(new SerializableFontLine("\nНАРАСТ. РАСХОД :" + formatRightAndFillBySpace(formatAmount(getIncrescentTotalExpense()) + " руб", maxCharRow - 18), Font.NORMAL));
            text.add(new SerializableFontLine("\nНАРАСТ. ВОЗВРАТ РАСХОД :" + formatRightAndFillBySpace(formatAmount(getIncrescentTotalReturnExpense()) + " руб", maxCharRow - 26), Font.NORMAL));

            throwManualException(ExceptionArea.PRINT_LINE);

            for (SerializableFontLine line : text) {
                fpe.writeToFile(line);
            }
            printTextToView(text);

            printFiscalSign(null);
            printCutter();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private List<SerializableFontLine> getHeaderLines(Cashier cashier) throws FiscalPrinterException {
        List<SerializableFontLine> text = new ArrayList<>();
        text.add(new SerializableFontLine(dateFormat.format(check.getDate()) + StringUtils.repeat(" ", maxCharRow - 15) + timeFormat.format(check.getDate()),
                Font.NORMAL));
        String s = StringUtils.leftPad(cashier.getTabNum(), 2, "0") + " " + cashier.getName();
        String[] regnum = getRegNum().split("\\.");
        String cashNumber = "КАССА:" + StringUtils.leftPad(regnum.length > 2 ? regnum[2] : regnum[0], 4, "0") + "/01";
        text.add(new SerializableFontLine(s + StringUtils.repeat(" ", maxCharRow - s.length() - cashNumber.length()) + cashNumber, Font.NORMAL));
        return text;
    }

    private void printCutter() throws FiscalPrinterException {
        throwManualException(null);
        fpe.writeToFile(getFileCutter());
        printCutterToView();
    }

    private String formatAmount(Long value) {
        return currencyFormatter.format(value / 100D);
    }

    private String formatRightAndFillBySpace(String string, int length) {
        return StringUtils.leftPad(string, length, " ");
    }

    @Override
    public void putMargin(boolean positional, String name, MarginType type, long value) throws FiscalPrinterException {
        throwManualException(ExceptionArea.APPEND_DISCOUNT);
        InternalMargin margin = new InternalMargin();
        margin.type = type;
        margin.name = name;
        margin.value = value;
        try {
            check.addMargin(positional, margin);
            String s = "  " + (name != null && !name.isEmpty() ? name : "НАЦЕНКА" + (type == MarginType.SUMMA ? " НА СУММУ " : " ПРОЦЕНТ "));
            s += StringUtils.leftPad("= " + currencyFormatter.format(value / 100D), maxCharRow - s.length(), " ");
            SerializableFontLine line = new SerializableFontLine(s, Font.NORMAL);
            fpe.writeToFile(line);
            printTextToView(line);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public void putDiscount(boolean positional, String name, DiscType type, long value) throws FiscalPrinterException {
        throwManualException(ExceptionArea.APPEND_DISCOUNT);
        InternalDisc disc = new InternalDisc();
        disc.type = type;
        disc.name = name;
        disc.value = value;
        try {
            check.addDiscount(positional, disc);
            String s = "  " + (name != null && !name.isEmpty() ? name : "СКИДКА" + (type == DiscType.SUMMA ? " НА СУММУ " : " ПРОЦЕНТ "));
            s += StringUtils.leftPad("= " + currencyFormatter.format(value / 100D), maxCharRow - s.length(), " ");
            SerializableFontLine line = new SerializableFontLine(s, Font.NORMAL);
            fpe.writeToFile(line);
            printTextToView(line);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public void cancelCheque() throws FiscalPrinterException {
        throwManualException(null);
        if (!isCheckOpen()) {
            throw new FiscalPrinterException("Документ не открыт");
        }
        try {
            incCountAnnul();
            incSPND();
            check.clear();
            SerializableFontLine line = new SerializableFontLine(StringUtils.center("ЧЕК АННУЛИРОВАН", maxCharRow), Font.DOUBLEHEIGHT);
            fpe.writeToFile(line);
            printTextToView(line);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    @Override
    public List<PaymentType> getPayments() {
        if (payments == null) {
            payments = new ArrayList<>();
            for (int i = 0; i <= PAYMENT_COUNT; i++) {
                PaymentType payment = new PaymentType();
                payment.setIndexPayment(i);
                switch (i) {
                    case 0:
                        payment.setName("НАЛИЧНЫЕ");
                        break;
                    case 1:
                        payment.setName("БЕЗНАЛИЧНЫМИ");
                        break;
                    case 13:
                        payment.setName("ПРЕДВАРИТЕЛЬНАЯ ОПЛАТА (АВАНС)");
                        break;
                    case 14:
                        payment.setName("ПОСЛЕДУЮЩАЯ ОПЛАТА (КРЕДИТ)");
                        break;
                    case 15:
                        payment.setName("ИНАЯ ОПЛАТА (ВСТРЕЧНЫМ ПРЕДЛОЖЕНИЕМ)");
                        break;
                    default:
                        payment.setName("ЭЛЕКТРОННЫЕ");
                        break;
                }
                payments.add(payment);
            }
        }
        return payments;
    }

    @Override
    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }

    @Override
    public int getMaxCharRow(Font font) {
        return getMaxCharRowFromView(font);
    }

    @Override
    public void printLine(FontLine value) throws FiscalPrinterException {
        throwManualException(ExceptionArea.PRINT_LINE);
        fpe.writeToFile(value);
        printTextToView(new SerializableFontLine(value));
    }

    public void printLines(List<FontLine> list) throws FiscalPrinterException {
        throwManualException(ExceptionArea.PRINT_LINE);
        List<CheckLine> lines = list.stream().map(e -> (CheckLine) e).collect(Collectors.toList());
        fpe.writeToFile(lines);
        List<SerializableFontLine> serlist = new ArrayList<>();
        for (FontLine fl : list) {
            serlist.add(new SerializableFontLine(fl));
        }
        printTextToView(serlist);
    }

    @Override
    public void printLine(String value) throws FiscalPrinterException {
        throwManualException(ExceptionArea.PRINT_LINE);
        SerializableFontLine line = new SerializableFontLine(value, Font.NORMAL);
        fpe.writeToFile(line);
        printTextToView(line);
    }

    private void throwManualException(ExceptionArea exceptionArea) throws FiscalPrinterException {
        if ((manualException != null && isExceptionThrown && manualException.isFatal())
                || (manualException != null && exceptionArea != null && exceptionArea == manualException.getExceptionArea())) {
            FiscalPrinterException tmpException = new FiscalPrinterException(manualException.getMessage());
            tmpException.setExceptionType(manualException.getExceptionType());
            tmpException.setRegistrationComplete(exceptionArea.isRegistrationComplete());
            isExceptionThrown = true;
            saveManualException();
            throw tmpException;
        }
    }

    @Override
    public void printBarcode(BarCode barcode) throws FiscalPrinterException {
        throwManualException(null);
        if (barcode != null && StringUtils.isNotEmpty(barcode.getValue())) {
            fpe.writeToFile(StringUtils.center(" BARCODE_" + barcode.getType() + "(" + barcode.getValue() + ") ", maxCharRow));
            printBarcodeToView(new SerializableBarCode(barcode));
            if (barcode.getBarcodeLabel() != null && barcode.getType() != BarCodeType.QR) {
                fpe.writeToFile(StringUtils.center(barcode.getBarcodeLabel(), maxCharRow));
                printTextToView(new SerializableFontLine(barcode.getBarcodeLabel()));
            }
        }
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        printCutter();
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        throwManualException(null);
        StatusFP status = new StatusFP();
        checkManualException(status);
        return status;
    }

    private void checkManualException(StatusFP status) {
        if (manualException != null && manualException.getExceptionStatus() != null) {
            switch (manualException.getExceptionStatus()) {
                case END_PAPER:
                    status.setStatus(StatusFP.Status.END_PAPER);
                    break;
                case OPEN_COVER:
                    status.setStatus(StatusFP.Status.OPEN_COVER);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean isBarCodeSupported() {
        return true;
    }

    @Override
    public Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        return Optional.of(new IncrescentTotal(getIncrescentTotalSale(),
                getIncrescentTotalReturn(),
                getIncrescentTotalExpense(),
                getIncrescentTotalReturnExpense()));
    }

    @Override
    public void throwException(ManualFiscalPrinterException exception) {
        setManualException(exception);
        LOGGER.info("throwException(" + exception + ")");
    }

    public ManualFiscalPrinterException getManualException() {
        return manualException;
    }

    public void setManualException(ManualFiscalPrinterException manualException) {
        this.manualException = manualException;
        isExceptionThrown = false;
        saveManualException();
    }

    @Override
    public void resetException() {
        setManualException(null);
        isExceptionThrown = false;
        LOGGER.info("resetException()");
    }

    @Override
    public boolean isCashDrawerOpen() throws FiscalPrinterException {
        return getBooleanProperty("drawerOpened");
    }

    @Override
    public void setCashDrawerOpen(boolean open) {
        setBooleanProperty("drawerOpened", open);
        LOGGER.info("setCashDrawerOpen(" + open + ")");
    }

    @Override
    public void openDrawer() throws FiscalPrinterException {
        throwManualException(ExceptionArea.OPEN_DRAWER);
        setCashDrawerOpen(true);
    }

    @Override
    public boolean isDrawerOpened() throws RemoteException {
        try {
            boolean result = isCashDrawerOpen();
            LOGGER.info("isDrawerOpened(): " + result);
            return result;
        } catch (FiscalPrinterException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void recreatePrinterFile() {
        fpe.recreateFile();
    }

    @Override
    public void printLogo() {
        printLogoToView();
    }

    public void setIPrinterView(IPrinterView printerView) {
        this.printerView = printerView;
    }

    private void printBarcodeToView(SerializableBarCode barcode) {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendBarcode(barcode);
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendBarcode(barcode);
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void printTextToView(SerializableFontLine line) {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendText(line);
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendText(line);
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void printTextToView(List<SerializableFontLine> lines) {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendText(lines);
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendText(lines);
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void printFiscalToView(Long shiftNum, long spnd, long kpk) {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendFiscal(shiftNum, spnd, kpk);
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendFiscal(shiftNum, spnd, kpk);
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void printLogoToView() {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendLogo();
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendLogo();
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void printCutterToView() {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.appendCutter();
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.appendCutter();
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void pingToView() {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                printerView.ping();
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    printerView.ping();
                    LOGGER.error(e.getLocalizedMessage(), e);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private int getMaxCharRowFromView(Font font) {
        if (registry != null || printerView != null) {
            try {
                if (printerView == null) {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                }
                return printerView.getMaxRowChars(font);
            } catch (NoSuchObjectException e) {
                try {
                    printerView = (IPrinterView) registry.lookup("PrinterView");
                    LOGGER.error(e.getLocalizedMessage(), e);
                    return printerView.getMaxRowChars(font);
                } catch (Exception er) {
                    LOGGER.error(er.getLocalizedMessage(), er);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        return maxCharRow;
    }

    public Registry getRegistry() {
        return registry;
    }

    @Override
    public void setRegistry(Registry registry) {
        this.registry = registry;
        pingToView();
    }

    @Override
    public String getPrintedDocumentWithOffset(int offset) throws RemoteException {
        LOGGER.info("getPrintedDocumentWithOffset() invoked");
        try {
            return fpe.getPrintedDocumentWithOffset(getFileCutter(), offset);
        } catch (FiscalPrinterException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private FontLine getFileCutter() {
        return new FontLine(StringUtils.repeat("~", maxCharRow), Font.NORMAL);
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        String type = getStringProperty("lastCheckType");

        FiscalDocumentType fiscalDocumentType;
        if (CheckType.SALE.toString().equals(type)) {
            fiscalDocumentType = FiscalDocumentType.SALE;
        } else if (CheckType.RETURN.toString().equals(type)) {
            fiscalDocumentType = FiscalDocumentType.REFUND;
        } else {
            return null;
        }

        FiscalDocumentData fiscalDocumentData = new FiscalDocumentData();
        fiscalDocumentData.setType(fiscalDocumentType);
        fiscalDocumentData.setSum(getLongProperty("lastCheckSumm"));
        fiscalDocumentData.setNumFD(getKPK());
        return fiscalDocumentData;
    }

    // признак что была попытка распечатать аппаратный Z отчет
    public boolean isStartZ() throws FiscalPrinterException {
        return Boolean.parseBoolean(getStringProperty("startZ"));
    }

    public void setStartZ(boolean start) {
        setStringProperty("startZ", Boolean.toString(start));
    }

    // Нарастающий итог
    private void incIncrescentTotalExpense(long value) throws FiscalPrinterException {
        setIncrescentTotalExpense(getIncrescentTotalExpense() + value);
    }

    private long getIncrescentTotalExpense() throws FiscalPrinterException {
        return getLongProperty("IncrescentTotalExpense");
    }

    private void setIncrescentTotalExpense(long value) {
        setLongProperty("IncrescentTotalExpense", value);
    }

    private void incIncrescentTotalReturnExpense(long value) throws FiscalPrinterException {
        setIncrescentTotalReturnExpense(getIncrescentTotalReturnExpense() + value);
    }

    private long getIncrescentTotalReturnExpense() throws FiscalPrinterException {
        return getLongProperty("IncrescentTotalReturnExpense");
    }

    private void setIncrescentTotalReturnExpense(long value) {
        setLongProperty("IncrescentTotalReturnExpense", value);
    }

    private long getIncrescentTotalReturn() throws FiscalPrinterException {
        return getLongProperty("IncrescentTotalReturn");
    }

    private void incIncrescenTotalReturn(long value) throws FiscalPrinterException {
        setIncrescentTotalReturn(getIncrescentTotalReturn() + value);
    }

    private void setIncrescentTotalReturn(long value) {
        setLongProperty("IncrescentTotalReturn", value);
    }

    private long getIncrescentTotalSale() throws FiscalPrinterException {
        return getLongProperty("IncrescentTotalSale");
    }

    private void incIncrescentTotalSale(long value) throws FiscalPrinterException {
        setIncrescentTotalSale(getIncrescentTotalSale() + value);
    }

    private void setIncrescentTotalSale(long value) {
        setLongProperty("IncrescentTotalSale", value);
    }

    public void setManualNotSentDocCount(int count) {
        this.manualNotSentDocCount = count;
    }

    @Override
    public FiscalMarkValidationResult getFiscalMarkValidationResult(String rawMark) {
        reloadMarkCodeValidationResults();
        return fiscalMarkValidationResults.getOrDefault(rawMark, MARK_OK);
    }

    @Override
    public void addManualMarkValidationResult(String rawMark, FiscalMarkValidationResult validationResult) {
        fiscalMarkValidationResults.put(rawMark, validationResult);
    }

    @Override
    public void resetManualMarkValidationResults() {
        fiscalMarkValidationResults.clear();
    }

    private void reloadMarkCodeValidationResults() {
        if (markValidationResultsFileLastModified.isBefore(
                DateConverters.toLocalDateTime(markValidationResultsFile.toFile().lastModified()))) {
            try {
                readMarkCodeValidationResults();
            } catch (IOException e) {
                LOGGER.error("Exception occurred while reading file {}", markValidationResultsFile.getFileName().toString());
            }
        }
    }

    public int getNotSentDocCount() {
        return manualNotSentDocCount;
    }

    public long getSumExpense() throws FiscalPrinterException {
        return getLongProperty("SumExpense");
    }

    public void setSumExpense(long value) {
        setLongProperty("SumExpense", value);
    }

    public void incSumExpense(long value) throws FiscalPrinterException {
        setSumExpense(getSumExpense() + value);
    }

    public long getCountExpense() throws FiscalPrinterException {
        return getLongProperty("CountExpense");
    }

    public void setCountExpense(long value) {
        setLongProperty("CountExpense", value);
    }

    public void incCountExpense() throws FiscalPrinterException {
        setCountExpense(getCountExpense() + 1);
    }

    public long getSumCashlessExpense() throws FiscalPrinterException {
        return getLongProperty("SumCashlessExpense");
    }

    public void setSumCashlessExpense(long value) {
        setLongProperty("SumCashlessExpense", value);
    }

    public void incSumCashlessExpense(long value) throws FiscalPrinterException {
        setSumCashlessExpense(getSumCashlessExpense() + value);
    }

    public long getSumCashExpense() throws FiscalPrinterException {
        return getLongProperty("SumCashExpense");
    }

    public void setSumCashExpense(long value) {
        setLongProperty("SumCashExpense", value);
    }

    public void incSumCashExpense(long value) throws FiscalPrinterException {
        setSumCashExpense(getSumCashExpense() + value);
    }

    public long getSumOtherPayExpense() throws FiscalPrinterException {
        return getLongProperty("SumOtherPayExpense");
    }

    public void setSumOtherPayExpense(long value) {
        setLongProperty("SumOtherPayExpense", value);
    }

    public void incSumOtherPayExpense(long value) throws FiscalPrinterException {
        setSumOtherPayExpense(getSumOtherPayExpense() + value);
    }

    public long getCountCashlessExpense() throws FiscalPrinterException {
        return getLongProperty("CountCashlessExpense");
    }

    public void setCountCashlessExpense(long value) {
        setLongProperty("CountCashlessExpense", value);
    }

    public void incCountCashlessExpense() throws FiscalPrinterException {
        setCountCashlessExpense(getCountCashlessExpense() + 1);
    }

    public long getCountCashExpense() throws FiscalPrinterException {
        return getLongProperty("CountCashExpense");
    }

    public void setCountCashExpense(long value) {
        setLongProperty("CountCashExpense", value);
    }

    public void incCountCashExpense() throws FiscalPrinterException {
        setCountCashExpense(getCountCashExpense() + 1);
    }

    public long getCountOtherPayExpense() throws FiscalPrinterException {
        return getLongProperty("CountOtherPayExpense");
    }

    public void setCountOtherPayExpense(long value) {
        setLongProperty("CountOtherPayExpense", value);
    }

    public void incCountOtherPayExpense() throws FiscalPrinterException {
        setCountOtherPayExpense(getCountOtherPayExpense() + 1);
    }

    // возврат расхода
    public long getSumReturnExpense() throws FiscalPrinterException {
        return getLongProperty("SumReturnExpense");
    }

    public void setSumReturnExpense(long value) {
        setLongProperty("SumReturnExpense", value);
    }

    public void incSumReturnExpense(long value) throws FiscalPrinterException {
        setSumReturnExpense(getSumReturnExpense() + value);
    }

    public long getCountReturnExpense() throws FiscalPrinterException {
        return getLongProperty("CountReturnExpense");
    }

    public void setCountReturnExpense(long value) {
        setLongProperty("CountReturnExpense", value);
    }

    public void incCountReturnExpense() throws FiscalPrinterException {
        setCountReturnExpense(getCountReturnExpense() + 1);
    }

    public long getSumCashlessReturnExpense() throws FiscalPrinterException {
        return getLongProperty("SumCashlessReturnExpense");
    }

    public void setSumCashlessReturnExpense(long value) {
        setLongProperty("SumCashlessReturnExpense", value);
    }

    public void incSumCashlessReturnExpense(long value) throws FiscalPrinterException {
        setSumCashlessReturnExpense(getSumCashlessReturnExpense() + value);
    }

    public long getSumCashReturnExpense() throws FiscalPrinterException {
        return getLongProperty("SumCashReturnExpense");
    }

    public void setSumCashReturnExpense(long value) {
        setLongProperty("SumCashReturnExpense", value);
    }

    public void incSumCashReturnExpense(long value) throws FiscalPrinterException {
        setSumCashReturnExpense(getSumCashReturnExpense() + value);
    }

    public long getCountCashlessReturnExpense() throws FiscalPrinterException {
        return getLongProperty("CountCashlessReturnExpense");
    }

    public void setCountCashlessReturnExpense(long value) {
        setLongProperty("CountCashlessReturnExpense", value);
    }

    public void incCountCashlessReturnExpense() throws FiscalPrinterException {
        setCountCashlessReturnExpense(getCountCashlessReturnExpense() + 1);
    }

    public long getCountCashReturnExpense() throws FiscalPrinterException {
        return getLongProperty("CountCashReturnExpense");
    }

    public void setCountCashReturnExpense(long value) {
        setLongProperty("CountCashReturnExpense", value);
    }

    public void incCountCashReturnExpense() throws FiscalPrinterException {
        setCountCashReturnExpense(getCountCashReturnExpense() + 1);
    }

    public long getSumOtherPayReturnExpense() throws FiscalPrinterException {
        return getLongProperty("SumOtherPayReturnExpense");
    }

    public void setSumOtherPayReturnExpense(long value) {
        setLongProperty("SumOtherPayReturnExpense", value);
    }

    public void incSumOtherPayReturnExpense(long value) throws FiscalPrinterException {
        setSumOtherPayReturnExpense(getSumOtherPayReturnExpense() + value);
    }

    public long getCountOtherPayReturnExpense() throws FiscalPrinterException {
        return getLongProperty("CountOtherPayReturnExpense");
    }

    public void setCountOtherPayReturnExpense(long value) {
        setLongProperty("CountOtherPayReturnExpense", value);
    }

    public void incCountOtherPayReturnExpense() throws FiscalPrinterException {
        setCountOtherPayReturnExpense(getCountOtherPayReturnExpense() + 1);
    }
}
