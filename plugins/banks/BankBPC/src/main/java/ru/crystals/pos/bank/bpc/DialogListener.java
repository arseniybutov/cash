package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogEvent;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.BankEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public class DialogListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BankBPCServiceImpl.class);
    private static final int PREFIX = 0x02;
    private static final int SUFFIX = 0x03;
    private static final String RESPONSE_NULL_VALUE = "NULL";
    private static final String COMMON_CANCEL_RESPONSE = "eventKey=33;pBuf=NULL;";
    public static final String BINARY_SELECTION_COMMIT_VALUE = "49";
    private ServerSocket serverSocket;
    private static final int SOCKET_PORT = 6517;
    private Socket socket;
    private Collection<BankEvent> bankListeners = new ArrayList<>();
    private InputStream inputStream;
    private BankDialogEvent serviceOperationListener;
    private String dialogCharset = "cp866";

    public void start() {
        try {
            serverSocket = new ServerSocket(SOCKET_PORT);
        } catch (IOException ignore) {
            throw new RuntimeException("Could not listen on port " + SOCKET_PORT);
        }
    }

    @Override
    public void run() {
        enableShowingDialogs();
        closeConnection();
    }

    private void enableShowingDialogs() {

        while (!Thread.interrupted()) {
            try {
                acceptConnection();
                inputStream = socket.getInputStream();
                while (inputStream.available() == 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) {
                    }
                }
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                String rawDialogParams = new String(bytes, Charset.forName(dialogCharset));
                log.info(rawDialogParams);
                if (StringUtils.trimToNull(rawDialogParams) != null) {
                    BPCBankDialog dialog = BPCBankDialog.createDialog(rawDialogParams);
                    showDialogScreen(dialog);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to show dialog", e);
            }
        }
    }

    private void showDialogScreen(BankDialog dialog) {
        for (BankEvent bankEvent : bankListeners) {
            bankEvent.showCustomProcessScreen(dialog);
        }
        if (serviceOperationListener != null) {
            serviceOperationListener.showCustomProcessScreen(dialog);
        }
    }

    private void acceptConnection() {
        try {
            log.info("accepting connection");
            socket = serverSocket.accept();
            log.info("connection accepted");
        } catch (IOException ignore) {
            throw new RuntimeException("Could not listen on port " + SOCKET_PORT);
        }
    }

    private void closeConnection() {
        try {
            inputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            log.error("Unable to close connection", e);
        }
    }

    public void addListeners(Collection<BankEvent> bankListeners) {
        this.bankListeners.addAll(bankListeners);
    }

    public void removeBankListeners() {
        bankListeners.clear();
    }

    public void addServiceOperationListener(BankDialogEvent serviceOperationsListener) {
        serviceOperationListener = serviceOperationsListener;
    }

    public void removeServiceOperationListener() {
        serviceOperationListener = null;
    }

    public void answer(BankDialogType dialogType, String message) throws IOException {
        String response = generateResponse(dialogType, message);
        send(response);
    }

    private String generateResponse(BankDialogType dialogType, String message) {
        String response = "eventKey=%s;pBuf=%s;";
        switch (dialogType) {
            case BINARY_SELECTION:
                response = String.format(response, BINARY_SELECTION_COMMIT_VALUE, RESPONSE_NULL_VALUE);
                break;
            case LIST_SELECTION:
                response = String.format(response, (int) message.charAt(0), RESPONSE_NULL_VALUE);
                break;
            case PERCENT_INPUT:
            case SUM_INPUT:
                response = String.format(response, "32", message);
                break;
            case STRING_INPUT:
                response = message.isEmpty() ? String.format(response, "32", RESPONSE_NULL_VALUE) : String.format(response, "32", message);
                break;
            default:
                break;
        }
        return response;
    }

    private void send(String response) throws IOException {
        if (socket != null && !socket.isClosed()) {
            log.debug(response);
            ByteArrayOutputStream resultBuffer = new ByteArrayOutputStream();
            resultBuffer.write(PREFIX);
            resultBuffer.write(response.length() + 3);
            resultBuffer.write(response.getBytes());
            resultBuffer.write(SUFFIX);
            OutputStream outputStream = socket.getOutputStream();
            for (byte b : resultBuffer.toByteArray()) {
                log.trace(" 0x{}", StringUtils.leftPad(Integer.toHexString(b & 0xFF).toUpperCase(), 2, '0'));
            }
            outputStream.write(resultBuffer.toByteArray());
            outputStream.flush();
            log.info("close");
            outputStream.close();
        } else {
            log.warn("Can't send response {}, cause socket is not available", response);
        }
    }

    public void closeDialog() {
        try {
            send(COMMON_CANCEL_RESPONSE);
        } catch (IOException e) {
            log.debug("", e);
        }
    }

    public void setDialogCharset(String dialogCharset) {
        this.dialogCharset = dialogCharset;
    }

    public String getDialogCharset() {
        return dialogCharset;
    }
}
