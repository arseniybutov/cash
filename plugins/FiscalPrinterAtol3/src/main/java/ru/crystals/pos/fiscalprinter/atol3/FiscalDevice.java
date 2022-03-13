package ru.crystals.pos.fiscalprinter.atol3;

import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddAdjustmentCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddPaymentCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddPositionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddRequisiteCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddReturnPositionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddSalePositionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.AddTaxCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.BeginPositionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.CancelDocumentCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.CashInCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.CashOutCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.CloseDocumentCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.CutPaperCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetDeviceTypeCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetFNNumberCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetFiscalSummary;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetImageArrayStatusCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetModeCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetPictureParamsCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetRegisterCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetStatusCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetTableCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.GetVersionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.ModeReturnCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.OpenDocumentCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.OpenDrawerCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.OpenShiftCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintBarcodeCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintBarcodeContinueCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintFieldCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintImageCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintKKTRegistrationSummaryCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintLineCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.PrintPictureCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.RegisterPositionCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.SetDateCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.SetModeCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.SetTableCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.SetTimeCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.XReportCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.ZReportCommand;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.DeviceType;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.FiscalSummary;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.ImageArrayStatus;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Mode;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Status;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Value;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Version;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Manager;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.utils.Alignment;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FiscalDevice {
    private final Logger logger = LoggerFactory.getLogger(FiscalPrinter.class);

    private static final int RECOMMENDED_POLL_PERIOD_FOR_REPORTS = 500;
    private static final int MODE_2_2 = 0x22;
    private static final int MODE_2_0 = 0x02;
    private static final int MODE_3_2 = 0x23;
    private static final int MODE_7_1 = 0x17;

    // TODO: following const must be revised
    private static final int MAX_SETTING_LENGTH = 64;
    private static final int MAX_PAYMENT_LENGTH = 64;
    private long linesBeforeCut = 8;
    private static final boolean PRINT_DISCOUNTS_IN_REPORTS = true;

    private int maxTextLength = 48;

    private static final int TAX_COUNT = 6;
    private static final int PAYMENT_COUNT = 9;
    private static final int DEFAULT_PRINT_IMAGE_BYTES = 51;
    private static final int MAX_CODE_39_LENGTH = 21;
    private static final int BARCODE_DIVISOR = 1;
    private static final int BARCODE_MIN_HEIGHT = 0;
    private static final int BARCODE_MAX_HEIGHT = 0;
    static final int DEFAULT_QR_CODE_SCALE_FACTOR = 8;
    private static final int CLICHE_COUNT = 20;

    private static final Long PRINT_Z_BREAK_ERROR_CODE = 26L;

    // TODO: read it from device
    protected String name = "АТОЛ 77Ф";

    private final byte[] password = new byte[] { 0, 0 };
    private final String passwordN = "30";
    private int qrCodeScaleFactor = DEFAULT_QR_CODE_SCALE_FACTOR;
    protected int maxPrintImageBytes = DEFAULT_PRINT_IMAGE_BYTES;

    /**
     * Флаг-признак: умеет ли текущая модель ФР печатать ШК (2D - QR) через команду "Печать штрих-кодов" (0xC1):
     * <p/>
     * в документации сказано, что команду 0xC1 поддерживают только ККТ FPrint-55K, FPrint-22K, FPrint-11ПТК1, FPrint-77ПТК и FPrintPay-01ПТК. Но мы
     * даем шанс всем моделям - но только один. как только они не смогут выполнить печать по причине любой ошибки, то до перегрузки кассы они более не
     * будут пытаться выполнить команду 0xC1
     */
    private boolean canPrintQRCodes = true;

    /**
     * Эффективное значение количества символов в строке
     */
    private Integer symbolsPerLine = null;


    private List<String> requisites;

    protected final Manager manager = new Manager();

    public void open(String portName, int baudRate, boolean useFlowControl) throws FiscalPrinterException {
        manager.open(portName, baudRate, useFlowControl);
    }

    public void close() throws FiscalPrinterException {
        manager.close();
    }

    public String getName() {
        return name;
    }

    public void setQrCodeScaleFactor(int qrCodeScaleFactor) {
        if (qrCodeScaleFactor <= 0) {
            logger.error("an attempt to set QR-code scale factor to a wrong value: {} was detected; The previous value will remain: {}", qrCodeScaleFactor, this.qrCodeScaleFactor);
        }

        this.qrCodeScaleFactor = qrCodeScaleFactor;
    }

    public void setRequisites(Map< RequisiteType, List<String>> requisites) {
        try {
            setMode(StateMode.PROGRAMMING);
            List<String> cliche = requisites.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
            for (int index = 0; index < CLICHE_COUNT; index++) {
                String str;
                if (index < cliche.size()) {
                    str = String.format("%-" + getMaxLengthField(MaxLengthField.REQUISIT) + "s",
                            StringUtils.center(cliche.get(index), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
                } else {
                    str = String.format("%-" + getMaxLengthField(MaxLengthField.REQUISIT) + "s", "");// "строка "+index);
                }
                setTableValue(6, index + 1, 1, str);
            }
        } catch (FiscalPrinterException e) {
            logger.error("", e);
        }

        fillRequisites();
    }

    private void fillRequisites() {
        requisites = new ArrayList<String>();
        try {
            setMode(StateMode.PROGRAMMING);
            for (int i = 0; i < CLICHE_COUNT; i++) {
                String s = getTableValue(6, i + 1, 1).get(ValueDecoder.ATOL_STRING).substring(2, getMaxLengthField(MaxLengthField.REQUISIT));
                requisites.add(s);
            }
        } catch (FiscalPrinterException e) {
            e.printStackTrace();
        }
        ListIterator<String> iterator = requisites.listIterator(requisites.size());
        while (iterator.hasPrevious() && iterator.previous().trim().isEmpty()) {
            iterator.remove();
        }
    }

    public List<String> getRequisites() {
        if (requisites == null) {
            fillRequisites();
        }
        return requisites;
    }

    public void setTableValue(int numTable, int numRow, int numColumn, String value) throws FiscalPrinterException {
        value = value.substring(0, Math.min(value.length(), getMaxLengthField(MaxLengthField.PROGRAMMING_TABLE)));
        setMode(StateMode.PROGRAMMING);
        manager.addTask(password, new SetTableCommand(numTable, numRow, numColumn, value));
    }

    public void setTableValue(int numTable, int numRow, int numColumn, byte value) throws FiscalPrinterException {
        manager.addTask(password, new SetTableCommand(numTable, numRow, numColumn, value));
    }

    public void setTableValue(int numTable, int numRow, int numColumn, float value) throws FiscalPrinterException {
        manager.addTask(password, new SetTableCommand(numTable, numRow, numColumn, value, 2));
    }

    public Value getTableValue(int numTable, int numRow, int numColumn) throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetTableCommand(numTable, numRow, numColumn));
    }

    public void setMaxTextLength(int length) {
        maxTextLength = length;
    }

    public int getMaxLengthField(MaxLengthField maxLen) {
        if (maxLen == MaxLengthField.DEFAULTTEXT) {
            return maxTextLength;
        } else if (maxLen == MaxLengthField.PAYMENTNAME) {
            return MAX_PAYMENT_LENGTH;
        } else {
            return MAX_SETTING_LENGTH;
        }
    }

    public int getTaxCount() {
        return TAX_COUNT;
    }

    public int getPaymentsCount() {
        return PAYMENT_COUNT;
    }

    public void setMode(StateMode mode) throws FiscalPrinterException {
        try {
            if (getSpecificData().getMode() == mode) {
                return;
            }

            modeReturn();
        } catch (FiscalPrinterException ex) {
            if (!PRINT_Z_BREAK_ERROR_CODE.equals(ex.getErrorCode())) {
                throw ex;
            }
        }
        manager.addTask(password, new SetModeCommand(mode, passwordN));
    }

    private void modeReturn() throws FiscalPrinterException {
        manager.addTaskAndWaitForAsyncResult(password, new ModeReturnCommand());
    }

    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        final long numberToSet = Math.min(cashNumber, 99);
        final Long currentNumber = getTableValue(2, 1, 1).get(ValueDecoder.LONG);
        if (Objects.equals(currentNumber, numberToSet)) {
            return;
        }
        setTableValue(2, 1, 1, (byte) numberToSet);
    }

    public Value getParameter(int regNumber) throws FiscalPrinterException {
        return getParameter(regNumber, 0, 0);
    }

    public Value getParameter(int regNumber, int param1) throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetRegisterCommand(regNumber, param1, 0));
    }

    public Value getParameter(int regNumber, int param1, int param2) throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetRegisterCommand(regNumber, param1, param2));
    }

    public String getINN() throws FiscalPrinterException {
        FiscalSummary fiscalSummary = manager.addTaskAndWaitForAsyncResult(password, new GetFiscalSummary());
        return fiscalSummary.inn;
    }

    public long getCountAnnul() throws FiscalPrinterException {
        return getParameter(Registers.REGISTATION_COUNT.getRegisterNumber(), 1).get(ValueDecoder.LONG);
    }

    public long getLastFiscalDocumentNum() throws FiscalPrinterException {
        return getParameter(52).get(ValueDecoder.LONG, 0, 5);
    }

    public void setDateTime(Date date) throws FiscalPrinterException {
        manager.addTask(password, new SetDateCommand(date));
        manager.addTask(password, new SetTimeCommand(date));
    }

    public Date getDateTime() throws FiscalPrinterException {
        return getSpecificData().dateTime;
    }

    public Status getSpecificData() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetStatusCommand());
    }

    public Version getVersionInfo() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetVersionCommand(0x91));
    }

    public boolean isShiftOpen() throws FiscalPrinterException {
        return getSpecificData().isShiftOpen();
    }

    public long getShiftNumber() throws FiscalPrinterException {
        return getSpecificData().getShiftNumber();
    }

    public String getSerialNumber() throws FiscalPrinterException {
        return getSpecificData().serialNumber;
    }

    public String getEKLZNumber() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetFNNumberCommand()).get(ValueDecoder.ASCII_STRING);
    }

    public String getVersion() throws FiscalPrinterException {
        return getVersionInfo().getFormattedVersion();
    }

    public void openShift() throws FiscalPrinterException {
        logger.debug("openShift start");
        setMode(StateMode.REGISTRATION);

        manager.addTaskAndWaitForAsyncResult(password, new OpenShiftCommand(0, ""));

        logger.debug("openShift end");
    }

    public void addTax(int taxIndex, boolean positional, long taxSum) throws FiscalPrinterException {
        manager.addTask(password, new AddTaxCommand(0, positional, taxIndex, taxSum));
    }

    public boolean isDocOpen() throws FiscalPrinterException {
        boolean result = getSpecificData().isDocOpen();
        logger.debug("isDocOpen: " + result);
        return result;
    }

    public void addClientRequisite(String requisite) throws FiscalPrinterException {
        if (requisite != null) {
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1008, requisite));
        }
    }

    public void addCashierName(String name) throws FiscalPrinterException {
        // кассир с паролем 123456789 имеет пустое имя
        if (!name.isEmpty()) {
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1021, name));
        }
    }

    public void addCashierInn(String inn) throws FiscalPrinterException {
        if (StringUtils.isNotBlank(inn) && inn.length() <= 12) {
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1203, formatINN(inn)));
        }
    }

    public void addCodingMark(String codingMark) throws FiscalPrinterException {
        logger.debug("addCodingMark start");
        if (!codingMark.isEmpty()) {
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1162, codingMark));
        }
        logger.debug("addCodingMark end");
    }

    public void addAgentSign(Integer agentSign) throws FiscalPrinterException {
        logger.debug("addAgentSign start");
        if (agentSign != null) {
            byte[] data = {agentSign.byteValue()};
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1222, new String(data)));
        }
        logger.debug("addAgentSign end");
    }

    public void addDebitorINN(String debitorINN) throws FiscalPrinterException {
        logger.debug("addDebitorINN start");
        if (!debitorINN.isEmpty() && debitorINN.length() <= 12) {
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1226, formatINN(debitorINN)));
        }
        logger.debug("addDebitorINN end");
    }

    /**
     * Дополняет ИНН до 12 знаков, с учетом протокола Атол
     */
    private String formatINN(String inn) {
        return StringUtils.rightPad(inn, 12, ' ');
    }

    public void addDebitorData(String debitorPhone, String debitorName) throws FiscalPrinterException {
        logger.debug("addDebitorPhone start");
        if (!debitorPhone.isEmpty() && !debitorName.isEmpty()) {
            String debitorData = UtilsAtol.createTLVPacket(1171, debitorPhone);
            debitorData += UtilsAtol.createTLVPacket(1225, debitorName);
            manager.addTask(password, new AddRequisiteCommand(1, 0, 1224, debitorData));
        }
        logger.debug("addDebitorPhone end");
    }

    public void closeDocument(boolean cutFlag) throws FiscalPrinterException {
        logger.debug("closeDocument start");
        manager.addTaskAndWaitForAsyncResult(password, new CloseDocumentCommand(0, 1, 0));
        logger.debug("closeDocument end");
    }

    public void cancelDocument() throws FiscalPrinterException {
        logger.debug("cancelDocument Start");
        manager.addTaskAndWaitForAsyncResult(password, new CancelDocumentCommand());
        logger.debug("cancelDocument end");
    }

    public void printBarCode(BarCode barcode) throws FiscalPrinterException {
        long stopWatch = System.currentTimeMillis();

        logger.trace("entering printBarCode(BarCode). The argument is: barcode [{}]", barcode);
        if (barcode == null) {
            logger.error("leaving printBarCode(BarCode): The argument is NULL!");
            return;
        }

//        if (BarCodeType.QR.equals(barcode.getType()) && !device.isQRCodeSupport()) {
//            logger.warn("leaving printBarCode(BarCode): printing QR-codes is not supported by this device!");
//            return;
//        }

//        int maxBarcodeBytesLength = MAX_PRINT_IMAGE_BYTES;
//        if (maxBarcodeBytesLength < 1) {
//            logger.warn("leaving printBarCode(BarCode): this device does not support printing images");
//            return;
//        }

        // 0. запомним текущий режим ФР
        StateMode originalMode = null;
        try {
            originalMode = getSpecificData().getMode();
        } catch (Throwable t) {
            logger.error("printBarCode(BarCode): failed to read original mode!", t);
        }
        logger.trace("printBarCode(BarCode): original mode was: {}", originalMode);

        // 1. установим текущий режим в "Регистрация документов"
        setMode(StateMode.REGISTRATION);

        boolean printTextAtTop = TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.TOP_TEXT.equals(barcode.getTextPosition());
        boolean printTextAtBottom = (TextPosition.TOP_AND_BOTTOM_TEXT.equals(barcode.getTextPosition()) || TextPosition.BOTTOM_TEXT.equals(barcode.getTextPosition()));
        printTextAtBottom &= !BarCodeType.QR.equals(barcode.getType());

        int maxCode39Length = MAX_CODE_39_LENGTH;
        boolean allowCode39 = maxCode39Length > 0;

        // 2. сама печать ШК - вместе с текстом НАД и ПОД ШК:
        if (printTextAtTop) {
            addPrintString(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }
        if (BarCodeType.QR.equals(barcode.getType())) {
            printQR(barcode);
            logger.trace("2D barcode was printed in {} [ms]", System.currentTimeMillis() - stopWatch);
        } else if (printBarcode(barcode, maxPrintImageBytes, maxCode39Length, allowCode39)) {
            logger.trace("1D barcode was printed in {} [ms]", System.currentTimeMillis() - stopWatch);
        }
        if (printTextAtBottom) {
            addPrintString(StringUtils.center(barcode.getBarcodeLabel(), getMaxLengthField(MaxLengthField.DEFAULTTEXT)));
        }

        // 3. вернем режим в исходное состояние - если сможем
        //  вообще, это следовало бы делать в finally-блоке, но если при печати ШК возникнут ошибки, то исходный режим уже не так важен:
        //  сценарий реакции на исключительную ситуацию сам поставим режим какой ему нужен
        if (originalMode != null) {
            logger.trace("printBarCode(BarCode): setting mode back to: {}", originalMode);
            try {
                setMode(originalMode);
            } catch (Throwable t) {
                // операция (печать ШК) все равно прошла успешно
                logger.warn(String.format("printBarCode(BarCode): failed to set the mode back to %s", originalMode), t);
            }
        }

        logger.trace("leaving printBarCode(BarCode): barcode was printed in {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private boolean printBarcode(BarCode barcode, int maxBarcodeBytesLength, int maxCode39Length, boolean allowCode39) throws FiscalPrinterException {
        // +10 для того, чтобы высота была схожа с ПИРИТ-ом
        long barcodeHeight = checkBarCodeHeight(barcode.getHeight() + 20);

        try {
            String barcodeStr = barcode.getValue();
            if (barcode.getType() == BarCodeType.Code39) {
                if (NumberUtils.isNumber(barcodeStr) && !allowCode39) {
                    if (barcodeStr.length() == 13) {
                        barcode.setType(BarCodeType.EAN13);
                    } else if (barcodeStr.length() == 8) {
                        barcode.setType(BarCodeType.EAN8);
                    } else if (barcodeStr.length() == 12 && barcodeStr.charAt(0) == '0') {
                        barcode.setType(BarCodeType.UPCA);
                    } else if (barcodeStr.length() == 12 && (barcodeStr.charAt(0) == '0' || barcodeStr.charAt(0) == '1')) {
                        barcode.setType(BarCodeType.UPCE);
                    }
                }
                if (barcode.getType() == BarCodeType.Code39 && !allowCode39) {
                    return true;
                }

                if (barcode.getType() == BarCodeType.Code39 && barcode.getValue().length() > maxCode39Length) {
                    return true;
                }
            }

            byte[] raster = BarCodeGenerator.getBarcodeBytes(barcode, maxBarcodeBytesLength);
            if (raster == null || raster.length == 0) {
                return true;
            }

            manager.addTask(password, new PrintImageCommand(1, barcodeHeight, 0, raster));
        } catch (IOException e) {
            e.printStackTrace();
            throw new FiscalPrinterException(e.getMessage());
        }

        return false;
    }

    private void printQR(BarCode barcode) throws FiscalPrinterException {
        long stopWatch = System.currentTimeMillis();

        if (printQRBarcode(barcode)) {
            logger.trace("QR-code was printed through 0xc1 & 0xc2 commands in {} [ms]", System.currentTimeMillis() - stopWatch);
            return;
        }
        logger.trace("it is necessary to print QR-code as a picture...");

        BitMatrix barcodeAsBitMatrix = GraphicsUtils.getBarcodeAsBitMatrix(barcode);

        List<byte[]> pictureLines = GraphicsUtils.getPictureLines(barcodeAsBitMatrix, Alignment.CENTER, qrCodeScaleFactor, 8 * maxPrintImageBytes, true);
        for (byte[] pictureLine : pictureLines) {
            long rasterScale = getRasterRepeatValue(qrCodeScaleFactor); // +10 для того,
            manager.addTaskAndWaitForAsyncResult(password, new PrintImageCommand(1, rasterScale, 0, pictureLine));
        }

        logger.trace("QR-code was printed as a picture in {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private int getRasterRepeatValue(int scale) {
        return 1;
//        if (device.getId() == DeviceType.FPrint_22K.getValue()) {
//            // у этой модели под повтором растра понимается повтор на количество ПИКСЕЛЕЙ (вернуть надо аргумент)
//            //  пример модели: FPrint-22K, FPrint-22PTK
//            return scale;
//        } else {
//            // у этой модели под повтором растра понимается повтор на количество СТРОК
//            //  пример модели: Bixolon
//            //  сейчас ткпо вернет 1 строку, но по факту надо бы возвращать что-то типа: scale / symbolHeight
//            return 1;
//        }
    }

    private long checkBarCodeHeight(long barcodeHeight) {
        // TODO: it always return barcodeHeight without changing now
        barcodeHeight /= BARCODE_DIVISOR;
        if (barcodeHeight < BARCODE_MIN_HEIGHT) {
            return BARCODE_MIN_HEIGHT;
        }
        int barcodeMaxHeight = BARCODE_MAX_HEIGHT;
        if (barcodeMaxHeight > 0 && barcodeHeight > barcodeMaxHeight) {
            return barcodeMaxHeight;
        }
        return barcodeHeight;
    }

    private boolean printQRBarcode(BarCode barcode) {
        boolean result = false;

        logger.trace("entering print2DBarcode(BarCode). The argument is: barcode [{}]", barcode);

        if (!canPrintQRCodes) {
            // уже знаем, что эта модель команду 0xC1 выполнить не в состоянии
            logger.trace("leaving print2DBarcode(BarCode). (IN-MEMORY): this device is unable to execute 0xc1 command");
            return false;
        }

        if (barcode == null || barcode.getType() == null || barcode.getValue() == null) {
            logger.warn("leaving print2DBarcode(BarCode). The argument [{}] is INVALID!", barcode);
            return false;
        }
        if (!BarCodeType.QR.equals(barcode.getType())) {
            // будем только QR-коды печатать через эту команду
            logger.trace("leaving print2DBarcode(BarCode). The argument [{}] is of wrong type", barcode);
            return false;
        }

        // здесь реально распечататем QR
        try {
            printQRInner(barcode.getValue());

            // успех!
            result = true;
        } catch (Throwable t) {
            // этот ФР не умеет печатать ШК
            logger.warn("This device is unable to print barcodes!", t);
            result = false;

            // больше не будем пытаться печатать ШК через 0xC1
            canPrintQRCodes = false;
        }

        logger.trace("leaving print2DBarcode(BarCode). The result is: {}", result);

        return result;
    }

    private void printQRInner(String barcode) throws FiscalPrinterException {
        List<String> barcodePieces = new LinkedList<>();
        while (barcode.length() > 100) {
            barcodePieces.add(barcode.substring(0, 100));
            barcode = barcode.substring(100);
        }
        if (barcode.length() != 0) {
            barcodePieces.add(barcode);
        }
        logger.trace("printQRInner(String). The argument [\"{}\"]was split into {} pieces: {}", barcode, barcodePieces.size(), barcodePieces);

        //  Тип штрихкода(1): мл. полубайт: 0 - QR-код; 7й бит ст.: 0 - если ШК короче 100 символов, иначе - 1 - ШК будет "дозагружен" командами 0xC2
        int type = barcodePieces.size() < 2 ? (byte) 0x00 : (byte) 0x80;

        manager.addTask(password, new PrintBarcodeCommand(type, 0x02, qrCodeScaleFactor, 0, 0x0000,
                4, barcodePieces.get(0)));

        for (int idx = 1; idx < barcodePieces.size(); idx++) {
            String piece = barcodePieces.get(idx);
            manager.addTaskAndWaitForAsyncResult(password, new PrintBarcodeContinueCommand(idx == barcodePieces.size() - 1, piece));
        }
    }

    private int getSymbolsPerLine() {
        if (symbolsPerLine != null && symbolsPerLine >= 0) {
            logger.trace("symbols per line (in-memory): {}", symbolsPerLine);
            return symbolsPerLine;
        }
        // считаем из таблицы
        logger.trace("reading symbols-per-line from tables...");
        Integer result = null;
        StateMode originalMode = null;
        try {
            originalMode = getSpecificData().getMode();
        } catch (Throwable t) {
            logger.error("symbols per line: cannot read from table", t);
        }
        if (originalMode == null) {
            logger.trace("using \"symbols-per-line\" from device config: {} 'cause failed to read it from table", maxTextLength);
            result = maxTextLength;
            return result;
        }

        try {
            setMode(StateMode.PROGRAMMING);
            symbolsPerLine = getTableValue(2, 1, 55).get(ValueDecoder.LONG).intValue();
        } catch (Throwable t) {
            logger.warn("failed to read symbols per line value", t);
        } finally {
            // вернем режим - каким он был:
            logger.trace("setting mode back to {}", originalMode);
            try {
                setMode(originalMode);
            } catch (Throwable t) {
                logger.error("failed to switch to the original mode", t);
            }
        }

        // если не удалось считать из таблицы - возьмем из device:
        if (result == null) {
            logger.trace("using \"symbols-per-line\" from device config: {}", maxTextLength);
            result = maxTextLength;
        }

        return result;
    }

    public void addPrintString(String text) throws FiscalPrinterException {
        logger.debug("addPrintString: \"{}\"", text);
        if (text == null) {
            return;
        }
        if (text.isEmpty()) {
            text = " ";
        }
        String dataSendStr = text.replace('ё', 'е').replace('Ё', 'Е');
        if (dataSendStr.length() > getMaxLengthField(MaxLengthField.DEFAULTTEXT)) {
            dataSendStr = dataSendStr.substring(0, getMaxLengthField(MaxLengthField.DEFAULTTEXT));
        }

        manager.addTaskAndWaitForAsyncResult(password, new PrintLineCommand(dataSendStr));

        logger.debug("addPrintString end");
    }

    public void addPrintString(Text text) throws FiscalPrinterException {
        if (text == null) {
            logger.error("addPrintString \"" + "text = null");
            return;
        }
        logger.debug("addPrintString \"" + text.getValue() + "\"; size: " + text.getSize() + " style: " + text.getStyle() + " start");
        if (StringUtils.isEmpty(text.getValue())) {
            text.setValue(StringUtils.leftPad("", getMaxLengthField(MaxLengthField.DEFAULTTEXT), " "));
        }
        String dataSendStr = text.getValue().replace('ё', 'е').replace('Ё', 'Е');
        if (dataSendStr.length() > getMaxLengthField(MaxLengthField.DEFAULTTEXT)) {
            dataSendStr = dataSendStr.substring(0, getMaxLengthField(MaxLengthField.DEFAULTTEXT));
        }

        manager.addTaskAndWaitForAsyncResult(password, new PrintFieldCommand(0, 1, 0,
                text.getSize() == TextSize.DOUBLE_HEIGHT ? 1 : 0, 0, 0, 1, 1, dataSendStr));

        logger.debug("addPrintString end");
    }

    public long addPayment(byte codePayment, long amount, String string) throws FiscalPrinterException {
        logger.debug("addPayment start");

        manager.addTaskAndWaitForAsyncResult(password, new AddPaymentCommand(0, codePayment, amount));

        logger.debug("addPayment end");
        return 0;
    }

    public void setDiscount(long value, boolean positional) throws FiscalPrinterException {
        setAdjustment(new AddAdjustmentCommand(0, 1, 1, 0, value), positional);
    }

    public void setMargin(long value, boolean positional) throws FiscalPrinterException {
        setAdjustment(new AddAdjustmentCommand(0, 1, 1, 1, value), positional);
    }

    private void setAdjustment(AddAdjustmentCommand command, boolean positional) throws FiscalPrinterException {
        logger.debug("setAdjustment start");

        if (!positional) {
            throw new UnsupportedOperationException("It is unavailable for FFD 1.0");
        }

        manager.addTaskAndWaitForAsyncResult(password, command);

        logger.debug("setAdjustment end");
    }

    public void addPosition(CheckType checkType, long price, long amount) throws FiscalPrinterException {
        logger.debug("addPosition start");

        if (checkType == CheckType.SALE) {
            manager.addTaskAndWaitForAsyncResult(password, new AddSalePositionCommand(0, price, amount, 0));
        } else {
            manager.addTaskAndWaitForAsyncResult(password, new AddReturnPositionCommand(2, price, amount));
        }

        logger.debug("addPosition end");
    }

    public void beginPosition() throws FiscalPrinterException {
        logger.debug("beginPosition start");
        manager.addTask(password, new BeginPositionCommand());
        logger.debug("beginPosition end");
    }

    public void addPosition(long price, long amount, long cost, int taxType, long taxValue, int section, long discount, String name) throws FiscalPrinterException {
        logger.debug("addPosition start");

        if (name.length() > RegisterPositionCommand.GOOD_NAME_MAX_LENGTH) {
            name = name.substring(0, RegisterPositionCommand.GOOD_NAME_MAX_LENGTH);
        }

        manager.addTaskAndWaitForAsyncResult(password, new AddPositionCommand(
                0, price, amount, cost, taxType, 0, section, discount, name));

        logger.debug("addPosition end");
    }

    public void openDocument(CheckType checkType) throws FiscalPrinterException {
        logger.debug("openDocument start");

        setMode(StateMode.REGISTRATION);
        manager.addTaskAndWaitForAsyncResult(password, new OpenDocumentCommand(0, checkType == CheckType.SALE ? 1 : 2));

        logger.debug("openDocument end");
    }

    public void addMoneyInOut(long amount) throws FiscalPrinterException {
        setMode(StateMode.REGISTRATION);

        if (amount > 0) {
            manager.addTaskAndWaitForAsyncResult(password, new CashInCommand(0, amount));
        } else if (amount < 0) {
            manager.addTaskAndWaitForAsyncResult(password, new CashOutCommand(0, -amount));
        } else {
            throw new IllegalArgumentException("zero is not allowed");
        }
    }

    public void printXReport() throws FiscalPrinterException {
        try {
            setMode(StateMode.XREPORTS);

            manager.addTaskAndWaitForAsyncResult(password, new XReportCommand(1));

            Mode mode;
            while ((mode = getMode()).mode == MODE_2_2) {
                TimeUnit.MILLISECONDS.sleep(RECOMMENDED_POLL_PERIOD_FOR_REPORTS);
            }

            if (mode.mode == MODE_2_0) {
                if ((mode.flags & 1) != 0) {
                    logger.error("Error while printZReport: no paper");
                    throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("ERROR_CODE_103"));
                } else if ((mode.flags & 2) != 0) {
                    logger.error("Error while printZReport: connection is lost");
                }
            } else {
                logger.error("Error while printZReport");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Waiting for printXReport was interrupted");
        }
    }

    public void printZReport() throws FiscalPrinterException {
        try {
            setMode(StateMode.ZREPORTS);

            manager.addTaskAndWaitForAsyncResult(password, new ZReportCommand());

            Mode mode;
            while ((mode = getMode()).mode == MODE_3_2) {
                TimeUnit.MILLISECONDS.sleep(RECOMMENDED_POLL_PERIOD_FOR_REPORTS);
            }

            if (mode.mode != MODE_7_1) {
                if ((mode.flags & 1) != 0) {
                    logger.error("Error while printZReport: no paper");
                    throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("ERROR_CODE_103"));
                } else if ((mode.flags & 2) != 0) {
                    logger.error("Error while printZReport: connection is lostn");
                } else if ((mode.flags & 4) != 0) {
                    logger.error("Error while printZReport: mechanical error");
                } else {
                    logger.error("Error while printZReport");
                }

                return;
            }

            while (getMode().mode == MODE_7_1) {
                TimeUnit.MILLISECONDS.sleep(RECOMMENDED_POLL_PERIOD_FOR_REPORTS);
            }

            logger.info("Z report successfully completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Waiting for printZReport was interrupted");
        }
    }

    private Mode getMode() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetModeCommand());
    }

    public void openDrawer() throws FiscalPrinterException {
        manager.addTaskAndWaitForAsyncResult(password, new OpenDrawerCommand());
    }

    public void printReport(long startShiftID, long endShiftID, String password, boolean isFullReport) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        String tmpPass = passwordN;
//        try {
//            passwordN = password;
//            setMode(StateMode.ACCESS_FP);
//            DataPacket packet = new DataPacket();
//            packet.putByteValue(1);
//            packet.putLongValue(startShiftID, 2);
//            packet.putLongValue(endShiftID, 2);
//            trans.sendPacket(Connector.FRCommand.FIS_REP_BY_SHIFT, packet.getBytes());
//            getAnswer(Connector.FRCommand.FIS_REP_BY_SHIFT);
//            wait4Finish();
//        } catch (FiscalPrinterException e) {
//            passwordN = tmpPass;
//            throw e;
//        }
    }

    public void printReport(Date startDate, Date endDate, String password, boolean isFullReport) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        String tmpPass = passwordN;
//        try {
//            passwordN = password;
//            setMode(StateMode.ACCESS_FP);
//            DataPacket packet = new DataPacket();
//            packet.putByteValue(1);
//            packet.putDateValue(startDate);
//            packet.putDateValue(endDate);
//            trans.sendPacket(Connector.FRCommand.FIS_REP_BY_DATE, packet.getBytes());
//            getAnswer(Connector.FRCommand.FIS_REP_BY_DATE);
//            wait4Finish();
//        } catch (FiscalPrinterException e) {
//            passwordN = tmpPass;
//            throw e;
//        }
    }

    public boolean isDrawerOpen(boolean inverted) throws FiscalPrinterException {
        return getSpecificData().isDrawerOpen(inverted);
    }


    public void printEKLZReport(Date startDate, Date endDate, boolean isFullReport) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        setMode(ru.crystals.pos.fiscalprinter.atol.support.StateMode.ACCESS_EKLZ);
//        DataPacket packet = new DataPacket();
//        long type = 1 | 2;
//        packet.putLongValue(type, 1);
//        packet.putDateValue(startDate);
//        packet.putDateValue(endDate);
//        trans.sendPacket(0xAC, packet.getBytes());
//        getAnswer(0xAC);
//        wait4Finish();
    }

    public void printEKLZReport(long startShiftID, long endShiftID, boolean isFullReport) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        setMode(ru.crystals.pos.fiscalprinter.atol.support.StateMode.ACCESS_EKLZ);
//        DataPacket packet = new DataPacket();
//        long type = 1 | 2;
//        packet.putLongValue(type, 1);
//        packet.putLongValue(startShiftID, 2);
//        packet.putLongValue(endShiftID, 2);
//        trans.sendPacket(0xAD, packet.getBytes());
//        getAnswer(0xAD);
//        wait4Finish();
    }

    public void printReportEKLZByActivaited() throws FiscalPrinterException {
        setMode(StateMode.ACCESS_FN);

        manager.addTaskAndWaitForAsyncResult(password, new PrintKKTRegistrationSummaryCommand());

        modeReturn();
    }

    public void printControlTape(long shiftID) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        setMode(ru.crystals.pos.fiscalprinter.atol.support.StateMode.ACCESS_EKLZ);
//        DataPacket packet = new DataPacket();
//        packet.putLongValue(shiftID, 2);
//        trans.sendPacket(0xAA, packet.getBytes());
//        getAnswer(0xAA);
//        wait4Finish();
    }

    public void printByKPK(long kpk) throws FiscalPrinterException {
        throw new UnsupportedOperationException();
//        setMode(ru.crystals.pos.fiscalprinter.atol.support.StateMode.ACCESS_EKLZ);
//        DataPacket packet = new DataPacket();
//        packet.putLongValue(kpk, 4);
//        trans.sendPacket(Connector.FRCommand.PRINT_DOC_BY_KPK, packet.getBytes());
//        getAnswer(Connector.FRCommand.PRINT_DOC_BY_KPK);
    }

    public void printLogo(int imageIndex) throws FiscalPrinterException {
        ImageArrayStatus imageArrayStatus = getImageArrayStatus();

        if (imageArrayStatus.lastNumber < imageIndex) {
            logger.warn("Ошибка печати картинки: картинка с индеком {} не существует", imageIndex);
            return;
        }

        if (imageArrayStatus.lastNumber == imageIndex && !imageArrayStatus.lastClosed) {
            logger.warn("Ошибка печати картинки: картинка с индеком {} добавлена в память не полностью", imageIndex);
            return;
        }

        int imgWidth = getImageWidth(imageIndex);
        float offSet1 = Math.max(0, (maxPrintImageBytes - imgWidth) / 2f);
        int offSet = Math.round(offSet1 * 8);

        setMode(StateMode.REGISTRATION);

        manager.addTaskAndWaitForAsyncResult(password, new PrintPictureCommand(1, 1, offSet));
    }

    protected ImageArrayStatus getImageArrayStatus() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetImageArrayStatusCommand());
    }

    private int getImageWidth(int numImage) throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetPictureParamsCommand(numImage)).width;
    }

    public int getMaxLenthForText(TextSize size) {
        return getMaxLengthField(MaxLengthField.DEFAULTTEXT);
    }

    public void closeFiscalDocument() throws FiscalPrinterException {
        // TODO:
    }

    public void cutPaper() throws FiscalPrinterException {
        for (int i = 0; i < getLinesBeforeCut(); ++i ) {
            printLine("");
        }
        manager.addTask(password, new CutPaperCommand(false));
    }

    public void printLine(String line) throws FiscalPrinterException {
        manager.addTask(password, new PrintLineCommand(line));
    }

    public DeviceType getDeviceType() throws FiscalPrinterException {
        return manager.addTaskAndWaitForAsyncResult(password, new GetDeviceTypeCommand());
    }

    public void abort() throws FiscalPrinterException {
        manager.abort();
    }

    public long getLinesBeforeCut() {
        return linesBeforeCut;
    }

    public void setLinesBeforeCut(long linesBeforeCut) {
        this.linesBeforeCut = linesBeforeCut;
    }

    public boolean isPrintDiscountsInReports() {
        return PRINT_DISCOUNTS_IN_REPORTS;
    }

    public static String getErrorString(int errorCode) {
        try {
            return ResBundleFiscalPrinterAtol.getString("ERROR_CODE_" + errorCode);
        } catch (Exception e) {
            return "" + errorCode;
        }
    }
}
