package ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions;

/**
 * Ошибки данного типа сигнализируют о какой-то ошибке вычисления/обработки данных при попытке "общения" с ФР семейства "Штрих".
 * <p/>
 * Т.е., это ошибки не связанные непосредственно с инфо-обменом с внешним устройством.
 * 
 * @author aperevozchikov
 */
public class ShtrihInternalProcessingException extends ShtrihException {
    private static final long serialVersionUID = 1L;

    public ShtrihInternalProcessingException(String message) {
        super(message);
    }
    public ShtrihInternalProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}