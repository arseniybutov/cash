package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ на запрос версии. Отличается от других настроек отсутствием полей с ошибками
 */
public class ApiVersionResponse {
    /**
     * Версия протокола, который использует касса
     */
    @JsonProperty("version")
    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
