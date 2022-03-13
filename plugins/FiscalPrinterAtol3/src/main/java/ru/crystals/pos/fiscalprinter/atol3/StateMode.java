package ru.crystals.pos.fiscalprinter.atol3;

public enum StateMode {
    /** 0 - Режим выбора */
    CHOICE,
    /** 1 - Режим регистрации. */
    REGISTRATION,
    /**2 - Режим отчетов без гашения.*/
    XREPORTS,
    /**3 - Режим отчетов с гашением.*/
    ZREPORTS,
    /**4 - Режим программирования.*/
    PROGRAMMING,
    /**5 - Режим доступа к ФП.*/
    ACCESS_FP,
    /**6 - Режим доступа к ЭКЛЗ. */
    ACCESS_FN,
    /**7 - Дополнительный режим непонятного состояния **/
    ADDITIONAL
}
