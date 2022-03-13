package ru.crystals.pos.customerdisplay.csibarcodescanner;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.HardwareModule;
import ru.crystals.pos.barcodescanner.BarcodeScannerImpl;
import ru.crystals.pos.barcodescanner.exception.BarcodeScannerException;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.keyboard.Keyboard;

import java.io.BufferedInputStream;
import java.util.LinkedList;
import java.util.List;


/**
 * Позволяет работать с дисплеем покупателя и сканером штрикодов через 1 общий COM порт.
 * Плагин дисплея открывает порт и запускает плагин сканера передавая ему inputStream в {@link CustomerDisplayPluginImpl#startBarcodeScannerModule()}.
 * В итоге плагин дисплея только пишет в порт, плагин сканера только читает с порта.
 */
public class CustomerDisplayPluginImpl extends TextCustomerDisplayPluginAbstract {

    /*
     * Для корректного отображения на устройстве нужно выставить все джампера вниз, кроме 4 и 5
     */

    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int ESC = 0x1B;

    private static final int RUS = 0x52;

    private static final int ENG = 0x41;


    /**
     * Поля конфигурации сканера заполняются из customerDisplay-csi_display_scanner-config.xml
     * с помощью PluginsLoader
     **/
    private String barcodePrefix;
    private String barcodeSuffix;

    private boolean ean13LeadingZero;
    private boolean ean13ControlNumber;
    private boolean ean8ControlNumber;
    private boolean upcaLeadingZero;
    private boolean upcaControlNumber;
    private boolean upceLeadingZero;
    private boolean upceControlNumber;

    private BarcodeScannerConfig dbsConfig;
    private BarcodeScannerImpl barcodeScanner;

    public CustomerDisplayPluginImpl() {
    }

    private void createBarcodeScannerConfig() throws CustomerDisplayPluginException {
        dbsConfig = new BarcodeScannerConfig();
        try {
            dbsConfig.setBarcodeSuffix(barcodeSuffix);
            dbsConfig.setBarcodePrefix(barcodePrefix);
            dbsConfig.setEan8ControlNumber(ean8ControlNumber);
            dbsConfig.setEan13ControlNumber(ean13ControlNumber);
            dbsConfig.setEan13LeadingZero(ean13LeadingZero);
            dbsConfig.setUpcaControlNumber(upcaControlNumber);
            dbsConfig.setUpceControlNumber(upceControlNumber);
            dbsConfig.setUpcaLeadingZero(upcaLeadingZero);
            dbsConfig.setUpceLeadingZero(upceLeadingZero);
        } catch (Exception e) {
            throw new CustomerDisplayPluginException("Incorrect display scanner config");
        }
    }

    private void startBarcodeScannerModule() throws CustomerDisplayPluginException {
        try {
            createBarcodeScannerConfig();
            if (getBufferedInputStream() == null) {
                throw new CustomerDisplayPluginException("Customer display inputStream is null");
            }
            barcodeScanner = new BarcodeScannerImpl();
            BundleManager.applyWhenAvailable(Keyboard.class, barcodeScanner::setKeyboard);
            List<HardwareModule> hardwareModules = new LinkedList<>();
            BarcodeScannerService barcodeScannerService = new BarcodeScannerService(dbsConfig, getBufferedInputStream());
            barcodeScannerService.setBarcodeScannerListener(barcodeScanner);
            hardwareModules.add(barcodeScannerService);
            barcodeScanner.setProviders(hardwareModules);
            barcodeScanner.start();
        } catch (BarcodeScannerException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }

    }

    @Override
    protected BufferedInputStream getBufferedInputStream() {
        return super.getBufferedInputStream();
    }

    @Override
    public void open() throws CustomerDisplayPluginException {
        super.open();
        startBarcodeScannerModule();
    }


    /**
     * Данный плагин не может считывать данные от дисплея, т.к inputStream использует сканер
     */
    @Override
    public byte[] executeCommandWithAnswer(String command, int answerLenght) throws CustomerDisplayPluginException {
        throw new CustomerDisplayPluginException("Command don't support");
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x6C, (byte) (column + 1), (byte) (row + 1)};
        executeCommand(new String(hex) + text);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {0x0C};
        executeCommand(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setCodeSet();
        setFontSet();
        clearText();
        setOverwriteMode();
        setCursorOff();
    }

    private void setCodeSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x63, RUS};
        executeCommand(new String(hex));
    }

    private void setFontSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x66, ENG};
        executeCommand(new String(hex));
    }

    private void setOverwriteMode() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x11};
        executeCommand(new String(hex));
    }

    private void setCursorOff() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5F, 0x00};
        executeCommand(new String(hex));
    }

    public String getBarcodePrefix() {
        return barcodePrefix;
    }

    public void setBarcodePrefix(String barcodePrefix) {
        this.barcodePrefix = barcodePrefix;
    }

    public String getBarcodeSuffix() {
        return barcodeSuffix;
    }

    public void setBarcodeSuffix(String barcodeSuffix) {
        this.barcodeSuffix = barcodeSuffix;
    }

    public boolean isEan13LeadingZero() {
        return ean13LeadingZero;
    }

    public void setEan13LeadingZero(boolean ean13LeadingZero) {
        this.ean13LeadingZero = ean13LeadingZero;
    }

    public boolean isEan13ControlNumber() {
        return ean13ControlNumber;
    }

    public void setEan13ControlNumber(boolean ean13ControlNumber) {
        this.ean13ControlNumber = ean13ControlNumber;
    }

    public boolean isEan8ControlNumber() {
        return ean8ControlNumber;
    }

    public void setEan8ControlNumber(boolean ean8ControlNumber) {
        this.ean8ControlNumber = ean8ControlNumber;
    }

    public boolean isUpcaLeadingZero() {
        return upcaLeadingZero;
    }

    public void setUpcaLeadingZero(boolean upcaLeadingZero) {
        this.upcaLeadingZero = upcaLeadingZero;
    }

    public boolean isUpcaControlNumber() {
        return upcaControlNumber;
    }

    public void setUpcaControlNumber(boolean upcaControlNumber) {
        this.upcaControlNumber = upcaControlNumber;
    }

    public boolean isUpceLeadingZero() {
        return upceLeadingZero;
    }

    public void setUpceLeadingZero(boolean upceLeadingZero) {
        this.upceLeadingZero = upceLeadingZero;
    }

    public boolean isUpceControlNumber() {
        return upceControlNumber;
    }

    public void setUpceControlNumber(boolean upceControlNumber) {
        this.upceControlNumber = upceControlNumber;
    }


    @Override
    public void verifyDevice() {
        //
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

}
