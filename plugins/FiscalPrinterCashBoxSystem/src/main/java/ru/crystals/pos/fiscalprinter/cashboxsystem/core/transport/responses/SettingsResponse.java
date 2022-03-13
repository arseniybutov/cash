package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Settings;

import java.util.Map;

/**
 * Ответ на запрос текущих настроек кассы или их изменения
 */
public class SettingsResponse extends BaseResponse {
    /**
     * Значения указанных в запросе настроек в виде key = value. key - Enum Settings. Значение всегда строкового типа.
     */
    @JsonProperty("data")
    private Map<Integer, String> settings;

    public Map<Integer, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<Integer, String> settings) {
        this.settings = settings;
    }

    public String getSettingValue(Settings key) {
        if(settings == null) {
            return null;
        }
        return settings.get(key.getCode());
    }
}
