package ru.crystals.pos.service;

/**
 * Сервис для получения конфигурации сервиса по работе с внешними системами
 */
public interface ExtServiceConfigManager {

    /**
     * Добавить обработчик конфигурации на получение конфигурации сервиса по работе с внешними системами
     */
    void subscribe(SettingsHandler settingsHandler);

}
