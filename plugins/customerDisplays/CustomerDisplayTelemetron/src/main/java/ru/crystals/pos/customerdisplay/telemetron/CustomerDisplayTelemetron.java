package ru.crystals.pos.customerdisplay.telemetron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.LineDisplayConfig;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.simple.SerialPortConfiguration;
import ru.crystals.pos.utils.simple.SimpleSerialPortAdapter;

import java.nio.charset.StandardCharsets;

public class CustomerDisplayTelemetron extends TextCustomerDisplayPluginAbstract {

    private static final Logger log = LoggerFactory.getLogger(CustomerDisplayTelemetron.class);

    private static final String END_OF_COMMAND = "\n\r";

    private SimpleSerialPortAdapter serialPortAdapter;

    public CustomerDisplayTelemetron() {
        super();
        this.serialPortAdapter = new SimpleSerialPortAdapter(log);
    }

    @Override
    public void open() throws CustomerDisplayPluginException {
        final LineDisplayConfig config = getConfig();
        serialPortAdapter.setConfiguration(SerialPortConfiguration.builder()
                .port(config.getPort())
                .baudRate(2400)
                .dataBits(8)
                .stopBits(1)
                .parity(0)
                .build());
        try {
            serialPortAdapter.openPort();
        } catch (Exception e) {
            log.error("Unable to open port", e);
            throw new CustomerDisplayPluginException(e.getMessage());
        }
        configureDisplay();
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        resetDisplay();
        hideQrCode();
    }

    /**
     * Показывать QR код (на данный момент вызывается только для оплаты через Тинькофф)
     *
     * @param payload      текст QR
     * @param bankProvider имя банка
     */
    @Override
    public void showQR(String payload, String bankProvider) throws CustomerDisplayPluginException {
        if ("tinkoff_sbp_sp".equals(bankProvider) || "raiffeisen_sbp_sp".equals(bankProvider)) {
            executeCommand(("qr=" + payload + END_OF_COMMAND).getBytes());
            displayBacklightOn();
        }
    }

    /**
     * Спрятать QR код
     */
    @Override
    public void hideQR() throws CustomerDisplayPluginException {
        hideQrCode();
    }

    public void resetDisplay() throws CustomerDisplayPluginException {
        displayBacklightOff();
        executeCommand(("reset" + END_OF_COMMAND).getBytes());
    }

    private void hideQrCode() throws CustomerDisplayPluginException {
        displayBacklightOff();
    }

    private void displayBacklightOn() throws CustomerDisplayPluginException {
        executeCommand(("light=on" + END_OF_COMMAND).getBytes());
    }

    private void displayBacklightOff() throws CustomerDisplayPluginException {
        executeCommand(("light=off" + END_OF_COMMAND).getBytes());
    }

    private void executeCommand(byte[] bytes) throws CustomerDisplayPluginException {
        try {
            serialPortAdapter.write(bytes);
            byte[] outputBytes = serialPortAdapter.readBytes();
            // Считываем и логгируем ответы на команды
            if (outputBytes != null) {
                String outputMessage = new String(outputBytes, StandardCharsets.UTF_8);
                log.debug(outputMessage);
            }
        } catch (PortAdapterException e) {
            log.error("", e);
        }
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
    }

    @Override
    public void executeCommand(String s) throws CustomerDisplayPluginException {
    }

    @Override
    protected int getMaxCharPerLine() {
        return 20;
    }

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
    }

    @Override
    public boolean canShowQRCode() {
        return true;
    }

}
