package ru.crystals.pos.bank.inpas.smartsale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.utils.time.Timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TCPConnector {
    private static final long EXECUTION_STATUS_SUCCESS = 0L;
    private static final long EXECUTION_STATUS_NO_DATA = 1L;
    private static final long EXECUTION_STATUS_FAIL = 2L;
    private static final byte COMMAND_MODE_TO_TERMINAL = 1;
    private static final byte COMMAND_MODE_TO_HOST = 0;
    private static final byte COMMAND_MODE_OPEN_CONNECTION = 1;
    private static final long COMMAND_MODE2_SERVER_CONNECTION = 16L;
    private static final long COMMAND_MODE_2_SEND_DATA = 17L;
    private static Logger log = LoggerFactory.getLogger(TCPConnector.class);
    private static final String TERMINAL_CHARSET = "cp1251";
    private TCPPortAdapter port = new TCPPortAdapter();
    private boolean isReading = false;
    private boolean isWaiting = false;
    private byte[] answer;
    private Future future;
    private FieldCollection dataToFill;

    public byte[] readPacket() throws BankException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        int len = 0;
        int stateRead = 1;
        isReading = true;
        try {
            while (!Thread.interrupted()) {
                if (port.getInputStreamBufferSize() > 0) {
                    byte c = (byte) port.read();
                    response.write(c);
                    if (stateRead == 1) {
                        len = (c & 0xFF);
                        stateRead = 2;
                    } else {
                        len |= ((c << 8) & 0xFF00);
                        byte[] read = new byte[len];
                        Timer timer = Timer.of(Duration.ofSeconds(5));
                        while (true) {
                            if (Thread.interrupted()) {
                                throw new IllegalStateException("Unexpectedly interrupted");
                            }
                            // если данные не успели придти, ждем их 5 секунд
                            if (port.getInputStreamBufferSize() < len) {
                                Thread.sleep(100);
                                if (timer.isExpired()) {
                                    // если ожидаемые данные так и не пришли
                                    isReading = false;
                                    throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"));
                                }
                            } else {
                                break;
                            }
                        }
                        port.read(read);
                        response.write(read);
                        response.flush();
                        isReading = false;
                        return response.toByteArray();
                    }
                }
            }
            throw new IllegalStateException("Unexpectedly interrupted");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"), e);
        } catch (Exception e) {
            isReading = false;
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"), e);
        }
    }

    public boolean startSession() {
        try {
            port.openPort();
            future = Executors.newCachedThreadPool().submit(new TcpConnectorListener());
            return true;
        } catch (Exception e) {
            log.error(ResBundleBankInpas.getString("ERROR_OPEN_TCP_PORT"), e);
        }
        return false;
    }

    public void close() {
        while (isReading) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
        isWaiting = false;
        port.close();
    }

    private static String out(String s) {
        return "-> " + s;
    }

    public void setConnectionParams(InetSocketAddress connectionParams) {
        port.setTcpAddress(connectionParams.getAddress().getHostAddress());
        port.setTcpPort(connectionParams.getPort());
    }

    public void sendBytes(byte[] bytes) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(out(new String(bytes, TERMINAL_CHARSET)));
        }
        port.write(bytes);
    }

    public void fillData(FieldCollection terminalResponse, FieldCollection dataToFill) throws Exception {
        this.dataToFill = dataToFill;
        this.dataToFill.setCommandMode2(terminalResponse.getCommandMode2());
        /*
         * commandMode2 = 16 - установка или разрыв соединения с сервером, 17 - Переслать данные внешней системе от
         * терминала и обратно
         */
        try {
            if (terminalResponse.getCommandMode2().equals(COMMAND_MODE2_SERVER_CONNECTION)) {
                processServerConnection(terminalResponse);
            } else if (terminalResponse.getCommandMode2().equals(COMMAND_MODE_2_SEND_DATA)) {
                processDataSend(terminalResponse);
            }
        } catch (Exception e) {
            log.error("", e);
            close();
            throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"), e);
        }
    }

    void processServerConnection(FieldCollection fieldCollection) throws BankCommunicationException {
        long executionStatus = EXECUTION_STATUS_SUCCESS;
        /*
         * commandMode = 1 - установка соединения с сервером, 0 - разрыв соединения
         */
        if (fieldCollection.getCommandMode() == COMMAND_MODE_OPEN_CONNECTION) {
            log.info("Connect to server");
            log.info("ConnectionParams:" + fieldCollection.getTcpString());
            String[] rows = fieldCollection.getTcpString().split(";");
            InetSocketAddress connectionParams;
            if (rows.length > 1) {
                //Иногда в конце строки параметров присутствует символ 0, а иногда нет
                if (rows[1].indexOf(0) >= 0) {
                    rows[1] = rows[1].substring(0, rows[1].indexOf(0));
                }
                connectionParams = new InetSocketAddress(rows[0], Integer.parseInt(rows[1]));
            } else {
                throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_CONNECTION_CONFIG"));
            }
            setConnectionParams(connectionParams);
            executionStatus = startSession() ? EXECUTION_STATUS_SUCCESS : EXECUTION_STATUS_FAIL;
        } else {
            log.info("Close connection to server");
            close();
        }
        dataToFill.setCommandExecutionStatus(executionStatus);
    }

    void processDataSend(FieldCollection result) throws IOException {
        /*
         * commandMode = 1 - пересылка данных от хоста к терминалу, 0 - от терминала к хосту
         */
        if (result.getCommandMode() == COMMAND_MODE_TO_TERMINAL) {
            log.info("Request of data send from host to terminal");
            if (answer == null) {
                dataToFill.setCommandExecutionStatus(EXECUTION_STATUS_NO_DATA);
            } else {
                dataToFill.setTcpParams(answer);
                dataToFill.setCommandExecutionStatus(EXECUTION_STATUS_SUCCESS);
                answer = null;
            }
        }
        if (result.getCommandMode() == COMMAND_MODE_TO_HOST) {
            sendBytes(result.getTcpParams());
            setWaiting(true);
            dataToFill.setCommandExecutionStatus(EXECUTION_STATUS_SUCCESS);
        }
    }

    private class TcpConnectorListener implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    if (isWaiting) {
                        isWaiting = false;
                        setAnswer(readPacket());
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

    //Для тестов

    FieldCollection getDataToFill() {
        return dataToFill;
    }

    void setDataToFill(FieldCollection dataToFill) {
        this.dataToFill = dataToFill;
    }

    public void setAnswer(byte[] answer) {
        this.answer = answer;
    }

    public void setWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }


    public Future getFuture() {
        return future;
    }
}
