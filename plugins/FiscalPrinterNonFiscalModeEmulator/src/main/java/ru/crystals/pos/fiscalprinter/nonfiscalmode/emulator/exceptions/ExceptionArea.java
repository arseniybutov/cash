package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions;

import java.util.EnumSet;

public enum ExceptionArea {
    PRINT_LINE,
    OPEN_CHECK,
    APPEND_POSITION,
    APPEND_PAYMENT,
    APPEND_DISCOUNT,
    CLOSE_CHECK_BEFORE_SAVE,
    CLOSE_CHECK_AFTER_SAVE,
    CLOSE_SHIFT_BEFORE_SAVE,
    CLOSE_SHIFT_AFTER_SAVE,
    CASH_OPERATION_BEFORE_SAVE,
    CASH_OPERATION_AFTER_SAVE,
    OPEN_DRAWER;

    private static final EnumSet<ExceptionArea> REG_COMPLETE_AREA = EnumSet.of(
            CLOSE_CHECK_AFTER_SAVE,
            CLOSE_SHIFT_AFTER_SAVE,
            CASH_OPERATION_AFTER_SAVE);

    public static boolean contains(String exceptionArea) {
        for (ExceptionArea ea : ExceptionArea.values()) {
            if (ea.toString().equals(exceptionArea)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRegistrationComplete() {
        return REG_COMPLETE_AREA.contains(this);
    }
}
