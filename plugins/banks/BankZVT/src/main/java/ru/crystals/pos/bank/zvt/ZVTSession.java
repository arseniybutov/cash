package ru.crystals.pos.bank.zvt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.zvt.commands.Command;
import ru.crystals.pos.bank.zvt.commands.RegistrationCommand;
import ru.crystals.pos.bank.zvt.protocol.ResponseCodes;
import ru.crystals.pos.bank.zvt.protocol.TransactionDataParser;
import ru.crystals.pos.bank.zvt.protocol.TransactionField;
import ru.crystals.pos.bank.zvt.protocol.ZVTPacket;
import ru.crystals.pos.bank.zvt.protocol.ZVTResponse;
import ru.crystals.pos.bank.zvt.utils.EncodingUtils;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.utils.time.Timer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ZVTSession implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ZVTSession.class);

    /**
     * Time between a command and the response 80-00/84-XX
     */
    private Duration timeoutT3;
    /**
     * Time between 80-00 from PT and completion-command
     */
    private Duration timeoutT4;

    private TransactionDataParser parser = new TransactionDataParser();

    private TCPPortAdapter tcp;
    private ExecutorService executor;

    public ZVTSession(TCPPortAdapter tcp, ExecutorService executor, ZVTTerminalConfig terminalConfig) {

        timeoutT3 = Duration.ofSeconds(Math.max(terminalConfig.getTimeoutT3(), 5));
        timeoutT4 = Duration.ofSeconds(Math.max(terminalConfig.getTimeoutT4(), 180));
        this.tcp = tcp;
        this.executor = executor;
    }

    ZVTResponse sendRequestWithLoginIfRequired(Command request, String password) throws BankException {
        ZVTResponse response = sendRequest(request);
        if (response.isSuccessful() || !ResponseCodes.NEED_REGISTRATION.equals(response.getResponseCode())) {
            return response;
        }
        ZVTResponse regResponse = sendRequest(new RegistrationCommand(password));
        if (!regResponse.isSuccessful()) {
            final String errorMessage = Optional.ofNullable(regResponse.getFields().get(TransactionField.ADDITIONAL_TEXT))
                    .map(EncodingUtils::decodeHexAscii)
                    .map(EncodingUtils::extractMessage)
                    .orElseGet(() -> ResBundleBankZVT.getRC(regResponse.getResponseCode()));
            log.error("Unable to login to terminal: RC={} ({})", regResponse.getResponseCode(), errorMessage);
            throw new BankAuthorizationException(errorMessage);
        }
        waitForTerminalReadyAfterRegistration();
        return sendRequest(request);
    }

    private void waitForTerminalReadyAfterRegistration() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    ZVTResponse sendRequest(Command request) throws BankCommunicationException {
        try {
            final String plainRequest = request.toString();
            log.debug("Sending request: {}", plainRequest);
            tcp.write(EncodingUtils.hexToBytes(plainRequest));
        } catch (Exception e) {
            log.error("Unable to send request", e);
            throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
        }
        BlockingQueue<ZVTPacket> readQueue = new ArrayBlockingQueue<>(5);
        final Future<?> readerFuture = startReader(readQueue);

        try {
            final ZVTResponse firstResponse = waitForFirstResponse(readQueue);
            if (!firstResponse.isSuccessful()) {
                return firstResponse;
            }
            return waitForFinalResponse(readQueue, request.hasStatus());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
        } finally {
            readerFuture.cancel(true);
        }
    }

    private ZVTResponse waitForFinalResponse(BlockingQueue<ZVTPacket> readQueue, boolean hasStatus) throws InterruptedException, BankCommunicationException {
        final Timer overallTimer = Timer.of(timeoutT4);
        ZVTResponse statusInformation = null;
        while (!Thread.currentThread().isInterrupted()) {
            if (overallTimer.isExpired()) {
                log.error("T4 expired");
                throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_TIMEOUT"));
            }
            ZVTPacket readPacket = readQueue.poll(100, TimeUnit.MILLISECONDS);
            if (readPacket == null) {
                continue;
            }

            if (hasStatus) {
                if (Arrays.equals(readPacket.getControlField(), new byte[]{0x04, (byte) 0xFF})) {
                    try {
                        tcp.write(new byte[]{(byte) 0x80, 0x00, 0x00});
                    } catch (IOException e) {
                        log.error("Unable to write 8000 response", e);
                        throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
                    }
                    overallTimer.restart();
                    continue;
                }

                if (Arrays.equals(readPacket.getControlField(), new byte[]{0x04, 0x0F})) {
                    statusInformation = new ZVTResponse(parser.parse(EncodingUtils.fromBytes(readPacket.getData())));
                    try {
                        tcp.write(new byte[]{(byte) 0x80, 0x00, 0x00});
                    } catch (IOException e) {
                        log.error("Unable to write 8000 response", e);
                        throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
                    }
                    overallTimer.restart();
                    continue;
                }
            }

            if (Arrays.equals(readPacket.getControlField(), new byte[]{0x06, (byte) 0x0F})) {
                if (statusInformation != null) {
                    return statusInformation;
                }
                return new ZVTResponse(ResponseCodes.OK, parser.parse(EncodingUtils.fromBytes(readPacket.getData())));
            } else if (Arrays.equals(readPacket.getControlField(), new byte[]{0x06, (byte) 0x1E})) {
                if (statusInformation != null) {
                    return new ZVTResponse(EncodingUtils.fromByte(readPacket.getData()[0]), statusInformation.getFields());
                }
                return new ZVTResponse(EncodingUtils.fromByte(readPacket.getData()[0]));
            } else if (Arrays.equals(readPacket.getControlField(), new byte[]{0x06, (byte) 0xD1})
                    || Arrays.equals(readPacket.getControlField(), new byte[]{0x06, (byte) 0xD3})) {
                log.error("Printing related packet: {}", readPacket);
            } else {
                log.error("Unexpected packet received: {}", readPacket);
                throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
            }

        }
        log.error("Interrupted");
        throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
    }

    private ZVTResponse waitForFirstResponse(BlockingQueue<ZVTPacket> readQueue) throws InterruptedException, BankCommunicationException {
        final Timer firstAnswerTimer = Timer.of(timeoutT3);
        ZVTPacket readPacket;
        while (!Thread.currentThread().isInterrupted()) {
            if (firstAnswerTimer.isExpired()) {
                log.error("T3 expired");
                throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_TIMEOUT"));
            }
            readPacket = readQueue.poll(100, TimeUnit.MILLISECONDS);
            if (readPacket == null) {
                continue;
            }
            log.trace("--> {}", readPacket);
            if (Arrays.equals(readPacket.getControlField(), new byte[]{(byte) 0x80, 0x00}) ||
                    Arrays.equals(readPacket.getControlField(), new byte[]{(byte) 0x84, 0x00})
            ) {
                return new ZVTResponse(ResponseCodes.OK);
            }
            if (Objects.equals(readPacket.getControlField()[0], (byte) 0x84)) {
                return new ZVTResponse(EncodingUtils.fromByte(readPacket.getControlField()[1]));
            }
            log.error("Unexpected packet received: {}", readPacket);
            throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
        }
        log.error("Interrupted");
        throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"));
    }


    private Future<?> startReader(Queue<ZVTPacket> readPackets) {
        return executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (tcp.getInputStreamBufferSize() == 0) {
                        Thread.sleep(100);
                        continue;
                    }
                    byte[] header = tcp.readFully(3);
                    logData("<--", "read", "header", header);
                    int length;
                    if (header[2] != (byte) 0xFF) {
                        length = header[2] & 0xFF;
                    } else {
                        logData("<--", "read", "extendedLength", header);
                        byte[] extendedLength = tcp.readFully(2);
                        length = (extendedLength[0] & 0xFF);
                        length |= (extendedLength[1] & 0xFF00);
                    }
                    byte[] data = new byte[0];
                    if (length > 0) {
                        data = tcp.readFully(length);
                        logData("<--", "read", "data", data);
                    }

                    final ZVTPacket zvtPacket = new ZVTPacket(Arrays.copyOfRange(header, 0, 2), length, data);
                    log.debug("{}", zvtPacket);
                    readPackets.add(zvtPacket);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private void logData(String directionArrow, String directionMessage, String extraMessage, byte[] data) {
        if (data.length == 0 || !log.isTraceEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        log.trace("{} {}({} {} bytes):\n{}", directionArrow, extraMessage, directionMessage, data.length, sb.toString());
    }

    @Override
    public void close() {
        tcp.close();
    }
}
