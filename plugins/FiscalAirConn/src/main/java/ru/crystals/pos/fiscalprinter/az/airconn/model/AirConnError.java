package ru.crystals.pos.fiscalprinter.az.airconn.model;

/**
 * Коды ошибок возвращаемые фискализатором AirConn
 */
public enum AirConnError {

    /**
     * Операция успешно выполнена
     */
    SUCCESS(0),
    /**
     * Неизвестная ошибка
     */
    ERROR_UNKNOWN(1),
    /**
     * Превышен таймаут ожидания
     */
    TIMEOUT(2),
    /**
     * USB устройство не установлено
     */
    USB_DEVICE_NOT_INSTALLED(3),
    /**
     * Токен не подключен
     */
    TOKEN_CONNECTION_ERROR(4),
    /**
     * Ошибка памяти
     */
    MEMORY_ERROR(5),
    /**
     * Неверно сформирована команда фискльного драйвера
     */
    COMMAND_FORMAT_ERROR(6),
    /**
     * Невозможно разобрать JSON команды фискльного драйвера
     */
    JSON_FORMAT_ERROR(7),
    /**
     * Неверные аргументы запроса команды фискльного драйвера
     */
    PARAMETERS_ERROR(8),
    /**
     * Версия протокола фискального драйвера не поддерживается
     */
    VERSION_UNSUPPORTED(9),
    /**
     * Неизвестная операция фискльного драйвера
     */
    UNKNOWN_OPERATION(10),
    /**
     * Операция фискального драйвера завершилась с ошибкой
     */
    OPERATION_ERROR(11),
    /**
     * Не авторизован
     */
    NOT_AUTHORIZED(12),
    /**
     * Ошибка токена
     */
    TOKEN_ERROR(101),
    /**
     * Ошибка чтения/записи токена
     */
    TOKEN_IO_ERROR(102),
    /**
     * Смена уже открыта
     */
    SHIFT_OPEN_ERROR(103),
    /**
     * Смена закрыта
     */
    SHIFT_CLOSED_ERROR(104),
    /**
     * Превышена продолжительность смены
     */
    SHIFT_EXPIRED_ERROR(105),
    /**
     * Неверное состояние смены
     */
    SHIFT_WRONG_SATE(106),
    /**
     * Ошибка авторизации
     */
    AUTHORIZE_ERROR(201),
    /**
     * Неподдерживаемый уровень доступа
     */
    UNSUPPORTED_ACCESS_LEVEL(202),
    /**
     * Неверный ключ доступа
     */
    WRONG_ACCESS_KEY(203),
    /**
     * PIN заблокирован
     */
    PIN_BLOCKED(204),
    /**
     * PUK код заблокирован
     */
    PUP_BLOCKED(205),
    /**
     * Неверный PIN
     */
    WRONG_PIN(206),
    /**
     * Неверный PUK
     */
    WRONG_PUK(207),
    /**
     * Плохой документ
     */
    DOCUMENT_ERROR(301),
    /**
     * Неверная структура документа
     */
    DOCUMENT_STRUCT_ERROR(302),
    /**
     * Неверный регистрационный номер токена в документе
     */
    WRONG_DOCUMENT_TOKEN(303),
    /**
     * Неверный регистрационный номер кассы в документе
     */
    DOCUMENT_WRONG_CASH_REGNUM(304),
    /**
     * Новый документ не может быть младше предыдущего
     */
    DOCUMENT_NUM_ERROR(305),
    /**
     * Превышен интервал нахождения в режиме офлайн
     */
    OFFLINE_EXPIRED(306),
    /**
     * Переполнение документа
     */
    DOCUMENT_OVERFLOW_ERROR(307),
    /**
     * Неподдерживаемая валюта
     */
    UNSUPPORTED_CURRENCY(308),
    /**
     * Неверная сумма всего по документу
     */
    DOCUMENT_SUM_ERROR(309),
    /**
     * Превышена длина имени кассира
     */
    NAME_LENGTH_ERROR(310),
    /**
     * Превышена длина кода товара
     */
    ITEM_CODE_LENGTH_ERROR(311),
    /**
     * Документ пустой
     */
    DOCUMENT_EMPTY(312),
    /**
     * Неверная сумма товарной позиции
     */
    WRONG_ITEM_SUM(313),
    /**
     * Неверный тип кода документа
     */
    WRONG_DOCUMENT_TYPE(314),
    /**
     * Сумма налогов противоречит сумме всего по документу
     */
    TAX_SUM_ERROR(315),
    /**
     * Сумма товаров противоречит сумме всего по документу
     */
    ITEMS_SUM_ERROR(316),
    /**
     * Превышена длина родительского документа
     */
    DOCUMENT_LENGTH_ERROR(317),
    /**
     * Процент налога не представлен в товарах
     */
    MISSING_TAX_VALUE(318),
    /**
     * Маржа не может быть больше цены товара
     */
    MARGIN_VALUE_ERROR(319),
    /**
     * Сумма маржи не может быть больше суммы товара
     */
    MARGIN_SUM_ERROR(320),
    /**
     * Сумма маржи не соответствует значению маржи
     */
    MARGIN_WRONG_VALUE(321),
    /**
     * Ответ сервера отвергнут токеном
     */
    SERVER_RESPONSE_REJECTED(401),
    /**
     * Неверный сертификат
     */
    WRONG_CERTIFICATE(501),
    /**
     * Сертификат сервера истек
     */
    CERTIFICATE_EXPIRED(502);

    private final int code;

    AirConnError(int code) {
        this.code = code;
    }

    public static AirConnError getErrorByCode(int code) {
        for (AirConnError error : AirConnError.values()) {
            if (error.getCode() == code) {
                return error;
            }
        }
        return ERROR_UNKNOWN;
    }

    public int getCode() {
        return code;
    }
}
