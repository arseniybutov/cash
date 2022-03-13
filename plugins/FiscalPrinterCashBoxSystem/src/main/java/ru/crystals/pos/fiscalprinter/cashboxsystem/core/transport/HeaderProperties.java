package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport;

/**
 * Обязательные HTTP заголовки для запросов в CBS.
 * Если какого то из заголовков не будет, cbs вернет HTTP код 400 или проигнорирует запрос.
 */
public interface HeaderProperties {
    String CONTENT_TYPE = "Content-Type";
    String USER_AGENT = "User-Agent";
    String EDS = "EDS";
}
