package ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;

/**
 * Описывает ошибку, что была получена от ФР (фискального регистратора) семейства "Штрих" в ответ на какой-нибудь запрос.
 * 
 * @author aperevozchikov
 */
public class ShtrihResponseException extends ShtrihException {
    
    /**
     * Код ошибки: Отсутствует ФП 1
     */
    public static final byte FIRST_FISCAL_BOARD_IS_ABSENT = (byte) 0x02;
    
    /**
     * Код ошибки: Отсутствует ФП 2
     */
    public static final byte SECOND_FISCAL_BOARD_IS_ABSENT = (byte) 0x03;
    
    /**
     * Код ошибки: Текущая дата меньше даты последней записи в ФП. Видимо, может возникнуть, если время "сбросилось" или часы перевели назад.
     */
    public static final byte INTERNAL_CLOCK_WAS_RESET = (byte) 0x13;
    
    /**
     * Код ошибки: Область сменных итогов ФП переполнена
     */
    public static final byte SHIFT_REPORTS_MEMORY_OVERFLOW = (byte) 0x14;
    
    /**
     * Код ошибки: Область перерегистраций в ФП переполнена
     */
    public static final byte FISCALIZE_COUNT_OVERFLOW = (byte) 0x1A;

    /**
     * Код ошибки: Область перерегистраций в ФП переполнена. Видимо, данный код ошибки зарезервирован - т.к., ни в одной версии протокола пока не
     * использована
     */
    public static final byte FISCALIZE_COUNT_OVERFLOW_NOT_SUPPORTED = (byte) 0x1E;
    
    /**
     * Код ошибки: Область активизаций переполнена. Х.з. чем отличается от перерегистрации
     */
    public static final byte ACTIVATION_SEG_OVERFLOW = (byte) 0x24;
    
    /**
     * Код ошибки: ФП отсутствует
     */
    public static final byte FISCAL_BOARD_IS_ABSENT = (byte) 0x64;
    
    /**
     * Код ошибки: Ошибка связи с ФП
     */
    public static final byte FISCAL_BOARD_COMMUNICATION_ERROR = (byte) 0x67;
    
    /**
     * Код ошибки: нет чековой ленты
     */
    public static final byte RECEIPT_RIBBON_ABSENT_ERROR = (byte) 0x6B;

    /**
     * Код ошибки: Команда не поддерживается в данном подрежиме
     */
    public static final byte WRONG_SUBSTATE = (byte) 0x72;

    /**
     * Код ошибки: Команда не поддерживается в данном режиме
     */
    public static final byte WRONG_STATE = (byte) 0x73;
    
    /**
     * Код ошибки: Ошибка связи с ФП. Видимо, разновидность ошибки?
     */
    public static final byte FISCAL_BOARD_COMMUNICATION_ERROR_80 = (byte) 0x80;
    
    /**
     * Код ошибки: Ошибка связи с ФП. Видимо, разновидность ошибки?
     */
    public static final byte FISCAL_BOARD_COMMUNICATION_ERROR_81 = (byte) 0x81;
    
    /**
     * Код ошибки: Ошибка связи с ФП. Видимо, разновидность ошибки?
     */
    public static final byte FISCAL_BOARD_COMMUNICATION_ERROR_82 = (byte) 0x82;
    /**
     * Код ошибки: Ошибка связи с ФП. Видимо, разновидность ошибки?
     */
    public static final byte FISCAL_BOARD_COMMUNICATION_ERROR_83 = (byte) 0x83;
    
    /**
     * Код ошибки: ЭКЛЗ отсутствует
     */
    public static final byte EKLZ_IS_ABSENT = (byte) 0xA1;
    
    private static final long serialVersionUID = 1L;

    /**
     * самое главное поле в этом классе: собственно код ошибки, что вернул ФР
     */
    private byte errorCode;

    /**
     * Команда, вызов которой спровоцировал эту ошибку
     */
    private byte command;

    /**
     * Состояние ФР на момент возникновения ошибки - отсюда можно получить дополнительную информацию о "контексте"/причинах возникновения ошибки
     */
    private ShtrihStateDescription deviceState;

    /**
     * Минималистский вариант конструктора: получаем информацию только о коде ошибки
     * 
     * @param errorCode
     *            код ошибки, спровоцировавший этот Exception
     */
    public ShtrihResponseException(byte errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Наиболее полная версия конструктора.
     * 
     * @param errorCode
     *            код ошибки, спровоцировавший этот Exception
     * @param command
     *            Команда, вызов которой спровоцировал эту ошибку
     * @param deviceState
     *            Состояние ФР на момент возникновения ошибки - отсюда можно получить дополнительную информацию о "контексте"/причинах возникновения
     *            ошибки
     */
    public ShtrihResponseException(byte errorCode, byte command, ShtrihStateDescription deviceState) {
        this.errorCode = errorCode;
        this.command = command;
        this.deviceState = deviceState;
    }
    
    @Override
    public String toString() {
        return String.format("shtrih-exception [error-code: 0x%02X; command: 0x%02X; device-state: %s]", 
            errorCode, command, deviceState);
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public byte getCommand() {
        return command;
    }

    public ShtrihStateDescription getDeviceState() {
        return deviceState;
    }
}