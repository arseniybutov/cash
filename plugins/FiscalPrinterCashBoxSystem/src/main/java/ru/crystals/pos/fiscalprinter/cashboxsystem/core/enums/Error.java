package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список ошибок возвращаемых ПФ из документации CBS
 */
public enum Error {

    /**
     * Ошибки отсутствуют
     */
    API_ERROR_NO(0, 200),
    /**
     * API в данный момент заблокировано, возможно попытка выполнить одновременно более одного запроса
     */
    API_ERROR_API_LOCK(1, 452),
    /**
     * Ошибка выполнения
     */
    API_ERROR_RUNTIME_ERROR(2, 453),
    /**
     * Ошибка протокола
     */
    API_ERROR_PROTOCOL_ERROR(3, 454),
    /**
     * Ошибка выполнения операции продажи
     */
    API_ERROR_OPERATION_SALE(4, 455),
    /**
     * Ошибка выполнения операции возврата продажи
     */
    API_ERROR_OPERATION_SALE_RETURN(5, 456),
    /**
     * Ошибка выполнения операции продажи
     */
    API_ERROR_OPERATION_BUY(6, 457),
    /**
     * Ошибка выполнения операции возврата продажи
     */
    API_ERROR_OPERATION_BUY_RETURN(7, 458),
    /**
     * Ошибка выполнения операции закрытия смены
     */
    API_ERROR_OPERATION_CLOSE_SHIFT(8, 459),
    /**
     * Ошибка выполнения операции снятия X-Отчета
     */
    API_ERROR_OPERATION_REPORT_X(9, 460),
    /**
     * Операция не поддерживается или недоступна
     */
    API_ERROR_OPERATION_NOT_AVAILABLE(10, 461),
    /**
     * Ошибка выполнения операции печати последнего Z-Отчета
     */
    API_ERROR_OPERATION_REPORT_LAST_Z(11, 462),
    /**
     * Доступ запрещен. Если были переданы неверные авторизационные данные.
     */
    API_ERROR_ACCESS_DENIED(12, 463),
    /**
     * Неизвестная ошибка
     */
    API_ERROR_UNKNOWN(255, 499);

    private final int code;
    private final int httpCode;

    Error(int code, int httpCode) {
        this.code = code;
        this.httpCode = httpCode;
    }

    public int getCode() {
        return code;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public static Error getErrorByCode(int code) {
        for(Error error: Error.values()){
            if(error.getCode() == code)
                return error;
        }
        return API_ERROR_UNKNOWN;
    }
}
