package ru.crystals.comportemulator.mstar;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.MstarConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Коды команда Mstar.
 */
public enum MstarCommand {

    /**
     * Запрос сменных счетчиков и регистров
     */
    GET_COUNTERS(0x01),
    /**
     * Запрос сведений
     */
    GET_INFO(0x02),
    /**
     * Запрос данных по чеку
     */
    GET_RECEIPT_DATA(0x03, MstarConnector.READ_TIME_OUT, DateUtils.MILLIS_PER_SECOND),
    /**
     * Запрос состояния печатающего устройства
     */
    GET_PRINTER_STATE(0x04),
    /**
     * Запрос флагов статуса
     */
    GET_STATUS(0x05),
    /**
     * Начало работы
     */
    START_WORK(0x10),
    /**
     * Чтение таблицы настроек
     */
    GET_CONFIGURATION_TABLE(0x11),
    /**
     * Запись таблицы настроек
     */
    SET_CONFIGURATION_TABLE(0x12),
    /**
     * Чтение даты / времени
     */
    GET_DATE(0x13),
    /**
     * Запись даты / времени
     */
    SET_DATE(0x14),
    /**
     * Загрузить логотип
     */
    SET_LOGO(0x17),
    /**
     * Печатать логотип
     */
    PRINT_LOGO(0x18),
    /**
     * Распечатать отчет без гашения (Х-отчет)
     */
    PRINT_X_REPORT(0x20),
    /**
     * Распечатать отчет с гашением (Z-отчет)
     */
    PRINT_Z_REPORT(0x21, 60 * DateUtils.MILLIS_PER_SECOND),
    /**
     * Открыть документ
     */
    OPEN_DOCUMENT(0x30),
    /**
     * Завершить документ
     */
    CLOSE_DOCUMENT(0x31, 20 * DateUtils.MILLIS_PER_SECOND),
    /**
     * Аннулировать документ
     */
    CANCEL_DOCUMENT(0x32, 20 * DateUtils.MILLIS_PER_SECOND),
    /**
     * Добавить реквизиты товарной позиции. Поддерживает теги 1214, 1212, 1162, 1191, 1226, 1230, 1231, 1229
     */
    SET_ITEM_ATTRIBUTES(0x37, true),
    /**
     * Печать текста
     */
    PRINT_STRING(0x40, true),
    /**
     * Печатать штрих-код
     */
    PRINT_BARCODE(0x41, true),
    /**
     * Добавить товарную позицию
     */
    ADD_ITEM(0x42, true),
    /**
     * Подытог
     */
    SUBTOTAL(0x44, true),
    /**
     * Оплата
     */
    ADD_PAYMENT(0x47, true),
    /**
     * Внесение / изъятие суммы
     */
    ADD_MONEY_IN_OUT(0x48, true),
    /**
     * Сравнить сумму по чеку
     */
    COMPARE_CHECK_SUM(0x52, true),
    /**
     * Открыть смену
     */
    OPEN_SHIFT_IN_FN(0x54),
    /**
     * Печать QR-кода
     */
    PRINT_QR(0x55),
    /**
     * Открыть денежный ящик
     */
    OPEN_MONEY_DRAWER(0x80),
    /**
     * Авторизация
     */
    KKM_AUTHORIZATION(0x90),
    /**
     * Установить скорость обмена
     */
    SET_BAUD_RATE(0x93),
    /**
     * Аварийное закрытие смены
     */
    EMERGENCY_SHIFT_CLOSE(0xA0),
    /**
     * Печать копии последнего Z-отчета
     */
    PRINT_LAST_Z_REPORT(0xA1),
    /**
     * Технологическое обнуление
     */
    TECHNOLOGICAL_RESET(0xA2),
    /**
     * Запрос дампа данных
     */
    REQUEST_DATA_DUMP(0xA3),
    /**
     * Получение дампа данных
     */
    GETTING_DATA_DUMP(0xA4),
    /**
     * Сброс состояния фискальной памяти
     */
    RESETTING_STATE_FISCAL_MEMORY(0xA5),
    /**
     * Аварийное закрытие архива ФН
     */
    EMERGENCY_CLOSE_ARCHIVE_FN(0x63),
    /**
     * Запрос состояния ФН
     */
    GET_FN_STATE(0xB0),
    /**
     * Запрос параметров текущей смены
     */
    GET_PARAMETER_CURRENT_SHIFT(0xB1),
    /**
     * Запрос параметров информационного обмена с ОФД
     */
    GET_EXCHANGE_PARAM_OFD(0xB2),
    /**
     * Первоначальная регистрация ФН
     */
    INITIAL_REGISTRATION_FN(0xB3),
    /**
     * Перерегистрация ФН
     */
    REREGISTRATION_FN(0xB4),
    /**
     * Закрытие фискального режима
     */
    CLOSE_FISCAL_MODE(0xB5),
    /**
     * Запрос итогов регистрации (перерегистрации)
     */
    GET_RESULT_REGISTRATION(0xB6),
    /**
     * Сброс состояния ФН
     */
    RESETTING_FN_STATE(0xB7),
    /**
     * Установить электронный адрес покупателя
     */
    SET_BUYER_EMAIL(0xB8),
    /**
     * Формирование чека коррекции
     */
    PRINT_CORRECTION_RECEIPT(0xB9),
    /**
     * Формирование отчёта о состоянии расчётов
     */
    REPORT_STATUS_SETTLEMENTS(0xBA),
    /**
     * Запрос документа из архива ФН
     */
    DOCUMENT_FROM_FN_ARCHIVE(0xBB),
    /**
     * Запрос квитанции на документ от ОФД
     */
    RECEIPT_FOR_DOCUMENT_FROM_OFD(0xBC),
    /**
     * Установка атрибута фискального документа
     */
    SET_FISCAL_DOCUMENT_ATTRIBUTES(0xBD),
    /**
     * Печать документа из архива фискального накопителя
     */
    PRINT_DOC_FROM_FISCAL_STORAGE(0xBE),
    /**
     * Установка типа системы налогообложения
     */
    SET_TAX_SYSTEM(0xBF),
    /**
     * Установить признак агента
     */
    SET_AGENT_CHARACTERISTIC(0xC0),
    /**
     * Запрос параметров последнего фискального документа
     */
    GET_LAST_FISCAL_DOC_PARAMETERS(0xC1),
    /**
     * Печать контрольной ленты из архива фискального накопителя
     */
    PRINT_CONTROL_TAPE_FROM_FN_ARCHIVE(0xC2);

    private static final Logger LOG = LoggerFactory.getLogger(MstarCommand.class);

    private static Map<Integer, MstarCommand> commands = new HashMap<>();

    static {
        for (MstarCommand v : MstarCommand.values()) {
            commands.put(v.getCode(), v);
        }
    }

    private final int commandCode;
    private boolean throwErrorRequired = false;
    private long readTimeOut = MstarConnector.READ_TIME_OUT;
    private long pingTimeOut = MstarConnector.PING_TIME_OUT;

    MstarCommand(int commandId) {
        this.commandCode = commandId;
    }

    MstarCommand(int commandId, Boolean throwErrorRequired) {
        this.commandCode = commandId;
        if (throwErrorRequired != null) {
            this.throwErrorRequired = throwErrorRequired;
        }
    }

    MstarCommand(int commandCode, long readTimeOut, long pingTimeOut) {
        this.commandCode = commandCode;
        this.readTimeOut = readTimeOut;
        this.pingTimeOut = pingTimeOut;
    }

    MstarCommand(int commandCode, long readTimeOut) {
        this.commandCode = commandCode;
        this.readTimeOut = readTimeOut;
    }

    public static MstarCommand getCommandID(int commandId) {
        MstarCommand mstarCommand = commands.get(commandId);
        if (mstarCommand == null) {
            LOG.error("Command not found. CommandID " + commandId);
        }
        return mstarCommand;
    }

    @Override
    public String toString() {
        return name() + String.format("(%02X)", commandCode);
    }

    public int getCode() {
        return commandCode;
    }

    public long getReadTimeOut() {
        return readTimeOut;
    }

    public long getPingTimeOut() {
        return pingTimeOut;
    }

    public boolean isThrowErrorRequired() {
        return throwErrorRequired;
    }
}
