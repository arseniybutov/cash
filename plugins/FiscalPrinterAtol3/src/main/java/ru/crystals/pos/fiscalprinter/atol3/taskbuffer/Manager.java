package ru.crystals.pos.fiscalprinter.atol3.taskbuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.atol3.FiscalDevice;
import ru.crystals.pos.fiscalprinter.atol3.ResBundleFiscalPrinterAtol;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.Command;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Result;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.AbortAction;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.AckResponseAction;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.Action;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.AddTaskAction;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.ReqTaskStatusAction;
import ru.crystals.pos.fiscalprinter.atol3.transport.Connector;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;

public class Manager {
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    private final Connector connector = new Connector();
    private static final int MIN_TID = 0;
    private static final int MAX_TID = 0xDF;
    private int tid = MIN_TID;

    private final int NEED_RESULT = 0x1;
    private final int IGNORE_ERROR = 0x2;
    private final int WAIT_ASYNC_DATA = 0x4;

    private final Map<Integer, Thread> threadTasks = new HashMap<>();

    public void open(String portName, int baudRate, boolean useFlowControl) throws FiscalPrinterException {
        try {
            connector.open(portName, baudRate, useFlowControl);
        } catch (IOException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("NO_COMMUNICATION_WITH_PRINTER"),
                    CashErrorType.FISCAL_ERROR, FiscalPrinterExceptionType.UNKNOWN);
        }
    }

    public void close() throws FiscalPrinterException {
        try {
            connector.close();
        } catch (IOException e) {
            logger.warn("Exception while close manager: {}", e);
        }
    }

    public void abort() throws FiscalPrinterException {
        perform(new AbortAction());
    }

    public <T extends Result, C extends Command<T>> T addTaskAndWaitForAsyncResult(byte[] password, C command) throws FiscalPrinterException {
        byte tid = addTask(password, NEED_RESULT, command);
        Response response = waitForAsyncResult(tid);
        if (response.isAsyncError() && threadTasks.get(response.getTId()) == Thread.currentThread()) {
            int errorCode = response.getErrorCode();
            throw new FiscalPrinterException(FiscalDevice.getErrorString(errorCode), CashErrorType.FISCAL_ERROR, (long) errorCode);
        }

        T result = command.parseResult(response);
        ackResponse(tid);
        return result;
    }

    public int addTask(byte[] password, Command command) throws FiscalPrinterException {
        return addTask(password, 0, command);
    }

    private synchronized byte addTask(byte[] password, int flags, Command command) throws FiscalPrinterException {
        perform(new AddTaskAction(tid, flags, password, command));
        byte id = (byte) tid;
        threadTasks.put(tid, Thread.currentThread());
        incrementTid();
        return id;
    }

    private Response ackResponse(int tid) throws FiscalPrinterException {
        return perform(new AckResponseAction(tid));
    }

    private Response getTaskStatus(int id) throws FiscalPrinterException {
        return perform(new ReqTaskStatusAction(id));
    }

    private Response waitForAsyncResult(byte tid) throws FiscalPrinterException {
        try {
            Response response = connector.waitForAsyncResult(r -> r.isAsyncResultFor(tid));
            if (response == null) {
                response = getTaskStatus(tid);
            }
            return response;
        } catch (IOException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("NO_COMMUNICATION_WITH_PRINTER"),
                    CashErrorType.FISCAL_ERROR, FiscalPrinterExceptionType.UNKNOWN);
        } catch (InterruptedException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("UNKNOWN_ERROR"),
                    CashErrorType.FISCAL_ERROR, FiscalPrinterExceptionType.UNKNOWN);
        }
    }

    private Response perform(Action action) throws FiscalPrinterException {
        try {
            while (true) {
                Response response = connector.call(action);
                if (response == null) {
                    throw new IOException("Response timeouted");
                }

                if (response.isAsyncError() && threadTasks.get(response.getTId()) == Thread.currentThread()) {
                    connector.clearPendingAsyncError();

                    abort();

                    int errorCode = response.getErrorCode();
                    throw new FiscalPrinterException(FiscalDevice.getErrorString(errorCode), CashErrorType.FISCAL_ERROR, (long) errorCode);
                }

                if (!response.isError()) {
                    return response;
                }

                Response.Status status = response.getStatus();
                switch (status) {
                    case ILLEGAL_VALUE:
                    case ALREADY_EXISTS:
                    case OVERFLOW:
                        throw new IllegalStateException("Transport layer status: " + response.getStatus());
                }

                logger.warn("Buffer in error state: {}, received response: {}", status, response);
            }
        } catch (IOException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("NO_COMMUNICATION_WITH_PRINTER"),
                    CashErrorType.FISCAL_ERROR, FiscalPrinterExceptionType.UNKNOWN);
        } catch (InterruptedException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("UNKNOWN_ERROR"),
                    CashErrorType.FISCAL_ERROR, FiscalPrinterExceptionType.UNKNOWN);
        }
    }

    private void incrementTid() {
        if (tid < MAX_TID) {
            tid += 1;
        } else {
            tid = MIN_TID;
        }
    }
}
