package ru.crystals.pos.fiscalprinter.shtrihminifrk.transport;

/**
 * Управляющие/служебные символы используемы при "общении "с ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public class ShtrihControls {
    /**
     * Enquiry - что-то типа запроса на разрешение начать диалог (или ping'а)
     */
    public static final byte ENQ = 0x05;

    /**
     * Start of Text - маркирует начало передачи данных
     */
    public static final byte STX = 0x02;

    /**
     * Acknowledgement - [позитивное] подтверждение приема (позитивный ответ)
     */
    public static final byte ACK = 0x06;

    /**
     * Негативный ответ
     */
    public static final byte NAK = 0x15;

    /**
     * Конец потока на приеме, после приема EOF повторяем прием запрошенного байта
     */
    public static final byte EOF = -0x01;
}