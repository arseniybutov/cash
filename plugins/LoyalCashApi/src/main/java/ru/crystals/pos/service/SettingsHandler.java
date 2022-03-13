package ru.crystals.pos.service;

/**
 * Интерфейс обработчика конфигурации сервиса по работе с внешними системами
 */
public interface SettingsHandler {

    /**
     * Имя сервиса (провайдера) в конфиге внешних систем (register-external-systems.xml), для чтения настроек, приходящих с сервера
     */
    String getProviderName();

    /**
     * Имя конфига сервиса для чтения локально заданных настроек.
     * Если {@code null}, то параметры из конфига не считываются
     */
    default String getConfigFileName() {
        return null;
    }

    /**
     * Имя модуля в sales_management_properties для чтения настроек из sales_management_properties (как альтернатива чтению конфига внешних систем)
     * Если {@code null}, то параметры из sales_management_properties не считываются
     */
    default String getSmpModuleName() {
        return null;
    }

    /**
     * Событие "Конфигурация перечитаны", означает, что следом для каждого параметра будет вызван {@link #setSetting(String, String)}.
     * По этому событию, например, можно создать новый объект конфигурации
     */
    void onSettingsReloaded();

    /**
     * Установка значения параметра после перечитывания конфигурации
     *
     * @param key   имя параметра, значение которого был перечитано
     * @param value значение параметра
     * @return {@code true} если параметр и значение были обработаны, {@code false} - если передан неизвестный параметр или значение некорректно
     */
    boolean setSetting(String key, String value);
}
