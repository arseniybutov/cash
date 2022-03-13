package ru.crystals.pos.bank.tusson.protocol;

/**
 * Описывает возможные коды ответов терминала на команды
 */
public enum ResponseStatus {
    // В операции отказано
    CANCELED((byte) 0x00),
    // Операция в процессе
    IN_PROGRESS((byte) 0x01),
    // Успешно завершена
    SUCCESS((byte) 0x02),
    // В операции отказано, терминал занят
    BUSY((byte) 0x03);
    private byte statusCode;

    private ResponseStatus(byte statusCode) {
        this.statusCode = statusCode;
    }

    public static ResponseStatus getStatusByCode(byte statusCode) {
        for (ResponseStatus responseStatus : ResponseStatus.values()) {
            if (responseStatus.statusCode == statusCode) {
                return responseStatus;
            }
        }
        return CANCELED;
    }

}
