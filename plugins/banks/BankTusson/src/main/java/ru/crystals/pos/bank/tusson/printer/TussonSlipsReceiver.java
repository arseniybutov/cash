package ru.crystals.pos.bank.tusson.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.tusson.ResBundleBankTusson;
import ru.crystals.pos.bank.tusson.exception.TussonPrinterException;
import ru.crystals.pos.bank.tusson.exception.TussonPrinterInterruptedException;
import ru.crystals.pos.utils.Timer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TussonSlipsReceiver implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TussonSlipsReceiver.class);
    //Constants
    private static final byte EOT = 0x04;
    private int port = 7070;

    private Thread connectionAcceptorThread;
    private ServerSocket socket;
    private SlipsContainer slipsContainer;
    private CountDownLatch locker;
    private ExecutorService slipsCheckerExecutor;

    public TussonSlipsReceiver(int port) {
        this.port = port;
    }

    @Override
    public void run() throws TussonPrinterException {
        LOG.trace("Slips receiver starting");
        prepareToRun();
        runConnectionAcceptor();
        LOG.trace("Slips receiver started");
        try {
            LOG.trace("Waiting for external interruption");
            locker.await();
            LOG.trace("Receive unlock. Server will be stopped");
            connectionAcceptorThread.interrupt();
        } catch (Exception e) {
            LOG.warn("Unexpected exception!", e);
        }
        close(socket);
    }

    /**
     * Выполняет предварительную настройку приемщика слипов
     *
     * @throws TussonPrinterException
     */
    void prepareToRun() throws TussonPrinterException {
        try {
            LOG.trace("Starting server on " + port + " port");
            socket = new ServerSocket(port);
            slipsContainer = new SlipsContainer();
            locker = new CountDownLatch(1);
            LOG.trace("Server started!");
        } catch (Exception e) {
            LOG.error("Failed to create server");
            throw new TussonPrinterException("Failed to create server", e);
        }
    }

    /**
     * Запускает поток, который отвечает за коммуникацию с терминалом
     */
    void runConnectionAcceptor() {
        connectionAcceptorThread = new Thread(() -> {
            try {
                while (true) {
                    Socket s = socket.accept();
                    new Thread(new AnswerReceiver(s)).start();
                }
            } catch (Exception e) {
                LOG.warn("Unexpected exception!", e);
            }
        });
        connectionAcceptorThread.start();
    }


    /**
     * Возвратит все документы типа requiredDocuments, как только хотябы по 1 из требуемых типов будут получены,
     * либо все документы требуемых типов, которые успеют придти до истечения timeout * timeUnit
     * И остановит поток, ожидающий чеки.
     *
     * @param requiredDocuments Список необходимых типов документов
     * @param timeout           максимальное время ожидания документов
     * @param timeUnit          единица измерения максимального времени ожидания документов
     * @return
     */
    public List<TerminalSlip> getSlips(Collection<DocumentType> requiredDocuments, Long timeout, TimeUnit timeUnit) {
        slipsCheckerExecutor = createNewExecutor();
        List<TerminalSlip> result = null;
        Future<List<TerminalSlip>> getSlipsFuture = slipsCheckerExecutor.submit(new SlipsChecker(requiredDocuments));
        try {
            result = getSlipsFuture.get(timeout, timeUnit);
        } catch (Exception e) {
            result = slipsContainer.getSlipsByType(requiredDocuments);
        } finally {
            stopWaitSlip();
        }
        return result;
    }

    /**
     * Возвратит все документы типа requiredDocuments, как только хотябы по 1 из требуемых типов будут получены.
     * И остановит поток, ожидающий чеки
     *
     * @param requiredDocuments Список необходимых типов документов
     * @return Слипы от терминала
     */
    public List<TerminalSlip> getSlips(Collection<DocumentType> requiredDocuments) {
        slipsCheckerExecutor = createNewExecutor();
        Future<List<TerminalSlip>> getSlipsFuture = slipsCheckerExecutor.submit(new SlipsChecker(requiredDocuments));
        try {
            return getSlipsFuture.get();
        } catch (InterruptedException | CancellationException | ExecutionException e) {
            throw new TussonPrinterInterruptedException("", e);
        } catch (Exception e) {
            throw new TussonPrinterException(ResBundleBankTusson.getString("UNEXPECTED_CRITICAL_ERROR"));
        } finally {
            stopWaitSlip();
        }
    }

    /**
     * Прерывает поток ожидания слипов
     */
    public void stopWaitSlip() {
        LOG.trace("Awaiting services termination");
        if (slipsCheckerExecutor != null) {
            slipsCheckerExecutor.shutdownNow();
        }
        locker.countDown();
        LOG.trace("Services terminated");
    }

    private class AnswerReceiver implements Runnable {
        private static final int DATA_WAITING_SLEEP_TIME_MILLIS = 100;
        private static final int DATA_RECEIVE_TIMEOUT_MILLIS = 3500;
        private static final String ENCODING = "cp1251";
        private Socket client;
        private Timer receiveAnswerTimer = new Timer(DATA_RECEIVE_TIMEOUT_MILLIS);
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public AnswerReceiver(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {

            try {
                inputStream = client.getInputStream();
                outputStream = client.getOutputStream();
                ByteArrayOutputStream slipBuffer = new ByteArrayOutputStream();
                int currentByteOrderNumber = 0;
                DocumentType documentType = DocumentType.UNDEFINED;
                while (!receiveAnswerTimer.isExpired()) {
                    if (inputStream.available() > 0) {
                        int readByte = inputStream.read();
                        if (currentByteOrderNumber == 0) {
                            //Первый байт это идентификатор документа
                            documentType = DocumentType.getTypeByCode(readByte);
                            LOG.trace("Received docType " + documentType);
                            if (documentType == DocumentType.UNDEFINED) {
                                //Какой-то неизвестный документ
                                break;
                            }
                        } else if (readByte == EOT) {
                            //Получили последний байт
                            break;
                        } else {
                            slipBuffer.write(readByte);
                        }
                        currentByteOrderNumber++;
                    } else {
                        Thread.sleep(DATA_WAITING_SLEEP_TIME_MILLIS);
                    }
                }

                if (slipBuffer.size() > 0) {
                    List<String> slipContent = new ArrayList(Arrays.asList(slipBuffer.toString(ENCODING).split("\n\r")));
                    slipsContainer.addSlip(new TerminalSlip(documentType, slipContent));
                }
                //Независимо от того получили все данные или нет ответим терминалу, что все хорошо
                outputStream.write(EOT);
            } catch (Exception e) {
                //
            } finally {
                close(inputStream);
                close(outputStream);
                close(client);
            }
        }

    }

    private class SlipsChecker implements Callable<List<TerminalSlip>> {
        private Collection<DocumentType> documentTypes;
        private static final long SLIPS_CHECK_TIMEOUT_MILLIS = 500L;

        public SlipsChecker(Collection<DocumentType> documentTypes) {
            this.documentTypes = documentTypes;
        }

        @Override
        public List<TerminalSlip> call() throws Exception {
            while (true) {
                if (slipsContainer.allSlipsReceived(documentTypes)) {
                    return slipsContainer.getSlipsByType(documentTypes);
                }
                Thread.sleep(SLIPS_CHECK_TIMEOUT_MILLIS);
            }
        }
    }

    void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    ExecutorService createNewExecutor() {
        return Executors.newFixedThreadPool(1);
    }
}
