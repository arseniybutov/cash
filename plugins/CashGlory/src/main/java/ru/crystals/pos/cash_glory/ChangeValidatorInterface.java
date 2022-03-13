package ru.crystals.pos.cash_glory;

import ru.crystals.pos.cash_machine.exceptions.CashMachineException;

public interface ChangeValidatorInterface {

    boolean isChangeAvailable(Long amount) throws CashMachineException;

}
