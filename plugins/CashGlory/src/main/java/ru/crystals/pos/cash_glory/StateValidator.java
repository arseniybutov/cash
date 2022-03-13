package ru.crystals.pos.cash_glory;

import java.util.Arrays;

import org.apache.commons.lang.exception.ExceptionUtils;

import ru.crystals.pos.cash_machine.Constants;
import ru.crystals.pos.cash_machine.StatusChange;
import ru.crystals.pos.cash_machine.exceptions.CashMachineException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineUnsupportedOperationException;

public enum StateValidator {
	CANCEL_CASH_REQUEST(3, 4, 6),
    RETURN_TO_CASH( 3, 4),
    RESET(0, 13);
    private static StatusChange status;
    private int[] statuses;

	private StateValidator(int... statuses) {
		this.statuses = statuses;
		Arrays.sort(this.statuses);
	}

	public void validate(StatusChange statusChange) throws CashMachineException {
		Constants.LOG.debug("[Cash Glory] Validate current status[" + statusChange.getStatus() + "] for " + this.toString() + " command.");
		Integer status = statusChange.getStatus();
		boolean validated = false;
		for (int s : statuses) {
			if (s >= status) {
				validated = s == status;
				break;
			}
		}
		Constants.LOG.debug("[Cash Glory] Validate " + (validated ? "" : "NOT ") + "SUCCESS");
		if (!validated) {
			throw new CashMachineUnsupportedOperationException();
		}
	}

    public static void setStatus(StatusChange status) {
        StateValidator.status = status;
        for (StateValidator v : values()) {
            synchronized (v) {
                v.notify();
            }
        }
    }

    public void waitValidate() {
        while (true) {
            try {
                validate(status);
                return;
            } catch (CashMachineException e) {
                Constants.LOG.error(ExceptionUtils.getFullStackTrace(e));
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e1) {
                        Constants.LOG.error(ExceptionUtils.getFullStackTrace(e1));
                    }
                }
            }
        }
    }
}
