package ru.crystals.pos.bank.tusson.serviceoperations;

import ru.crystals.pos.bank.tusson.ResBundleBankTusson;
import ru.crystals.pos.bank.tusson.exception.TussonServiceOperationException;

import java.util.List;

public class ServiceOperationRunner {
    private static BankTussonServiceOperation currentOperation;

    public synchronized static List<List<String>> doOperation(BankTussonServiceOperation operation) throws TussonServiceOperationException {
        try {
            if (currentOperation == null) {
                currentOperation = operation;
            } else {
                throw new TussonServiceOperationException(ResBundleBankTusson.getString("TERMINAL_BUSY"));
            }
            return currentOperation.process();
        } finally {
            currentOperation = null;
        }
    }

    public static void suspendCurrentOperation() {
        if (currentOperation != null) {
            currentOperation.suspend();
            currentOperation = null;
        }
    }
}
