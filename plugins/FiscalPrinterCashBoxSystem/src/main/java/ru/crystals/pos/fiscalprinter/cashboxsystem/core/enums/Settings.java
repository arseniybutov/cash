package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список настроек CBS
 */
public enum Settings {
    /**
     * Идентификатор кассы
     */
    SETTINGS_KKMID(1),
    /**
     * Токен
     */
    SETTINGS_TOKEN(2),
    /**
     * Автоматическое изъятие средств из денежного ящика в кассе после закрытия смены
     */
    SETTINGS_IS_AUTO_WITHDRAWAL(3),
    /**
     * Количество оффлайн пакетов в кассе
     */
    SETTINGS_COUNT_OFFLINE_PACKETS(4),
    /**
     * Разрешить работу через API не с localhost
     */
    SETTINGS_IS_ALLOW_EXTERNAL_API_CONNECTIONS(5),
    /**
     * Псевдо-оффлайн режим для быстрой обработки чековых операций
     */
    SETTINGS_IS_ENABLE_PSEUDO_OFFLINE(6);

    /**
     * Пароль от режима настроек. Нужен для запросов получения и изменения настроек CBS
     */
    public static final Integer DEFAULT_PASSWORD = 3333;

    private final int code;

    Settings(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
