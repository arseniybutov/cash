package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * перечисление возможных под-режимов, в которых могут находиться ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public enum ShtrihSubState {

    /**
     * Бумага есть – ФР не в фазе печати операции – может принимать от хоста команды, связанные с печатью на том документе, датчик которого сообщает о
     * наличии бумаги
     */
    PAPER_PRESENT((byte) 0),

    /**
     * Пассивное отсутствие бумаги – ФР не в фазе печати операции – не принимает от хоста команды, связанные с печатью на том документе, датчик
     * которого сообщает об отсутствии бумаги.
     */
    PAPER_ABSENT_PASSIVELY((byte) 1),

    /**
     * Активное отсутствие бумаги – ФР в фазе печати операции – принимает только команды, не связанные с печатью. Переход из этого подрежима только в
     * подрежим {@link #WAITING}
     */
    PAPER_ABSENT_ACTIVELY((byte) 2),

    /**
     * После активного отсутствия бумаги – ФР ждет команду продолжения печати. Кроме этого принимает команды, не связанные с печатью.
     */
    WAITING((byte) 3),

    /**
     * Фаза печати операции полных фискальных отчетов – ФР не принимает от хоста команды, связанные с печатью, кроме команды прерывания печати
     */
    PRINTING_FULL_REPORTS((byte) 4),

    /**
     * Фаза печати операции – ФР не принимает от хоста команды, связанные с печатью
     */
    PRINTING((byte) 5);

    /**
     * Код под-режима
     */
    private byte code;

    private ShtrihSubState(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
    
    /**
     * Вернет enum по его коду
     * 
     * @param code {@link #getCode() код} enum'а, что надо вернуть
     * @return <code>null</code>, если нету enum'а с таким кодом
     */
    public static ShtrihSubState getByCode(byte code) {
        for (ShtrihSubState s : ShtrihSubState.values()) {
            if (code == s.getCode()) {
                return s;
            }
        }
        return null;
    }
}
