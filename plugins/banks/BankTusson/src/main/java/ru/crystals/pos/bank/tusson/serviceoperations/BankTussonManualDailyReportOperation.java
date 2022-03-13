package ru.crystals.pos.bank.tusson.serviceoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.tusson.exception.TussonPrinterInterruptedException;
import ru.crystals.pos.bank.tusson.printer.DocumentType;
import ru.crystals.pos.bank.tusson.printer.TerminalSlip;
import ru.crystals.pos.bank.tusson.printer.TussonSlipsReceiver;
import ru.crystals.pos.bank.tusson.protocol.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankTussonManualDailyReportOperation extends BankTussonServiceOperation {
    private static final Logger log = LoggerFactory.getLogger(BankTussonManualDailyReportOperation.class);

    private static final long WAIT_THREAD_STARTED_TIMEOUT = 200L;
    private static final Collection<DocumentType> requiredDocuments = Operation.DAILY_LOG_DOCUMENTS;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService eventDailyLogExecutor = Executors.newSingleThreadExecutor();

    private TussonSlipsReceiver chequeReceiver;
    private Runnable onDailyLogFinished;

    public BankTussonManualDailyReportOperation(TussonSlipsReceiver chequeReceiver, Runnable onDailyLogFinished) {
        this.chequeReceiver = chequeReceiver;
        this.onDailyLogFinished = onDailyLogFinished;
    }

    @Override
    public String getCommandTitle() {
        return "Сверка итогов";
    }

    @Override
    public String getFormTitle() {
        return "Выполните сверку итогов на терминале или нажмите отмена";
    }

    @Override
    public String getSpinnerMessage() {
        return "Ожидание данных от терминала";
    }

    @Override
    public List<List<String>> process() {
        List<List<String>> result = new ArrayList<>();
        executor.submit(chequeReceiver);
        try {
            Thread.sleep(WAIT_THREAD_STARTED_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
        try {
            List<TerminalSlip> slips = chequeReceiver.getSlips(requiredDocuments);
            for (TerminalSlip slip : slips) {
                result.add(slip.getContent());
            }
        } catch (TussonPrinterInterruptedException ex) {
            log.warn("Process DailyLog operation interrupted! Empty collection will be returned! \n See trace log for more detail!");
            return result;
        }
        eventDailyLogComplete();
        return result;
    }

    @Override
    public void suspend() {
        chequeReceiver.stopWaitSlip();
    }

    private void eventDailyLogComplete() {
        eventDailyLogExecutor.execute(onDailyLogFinished);
    }
}
