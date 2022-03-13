package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Settings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.SettingsResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.MDEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Установка настроек в CBS. Количество настроек, передаваемых за один раз, может варьироваться.
 */
public class SetSettings extends BaseRequest {

    /**
     * sha512 пароля от режима настроек в base64 виде
     */
    @JsonProperty("password_settings")
    private String passwordSettings;

    /**
     * Значения настроек, которые должны быть изменены в виде key = value. key - Enum Settings. Значение всегда строкового типа.
     */
    @JsonProperty("settings")
    private Map<Integer, String> settings = new HashMap<>();

    public SetSettings() {
        setPassword(Settings.DEFAULT_PASSWORD);
    }

    @Override
    public String getTarget() {
        return "/api/settings/set";
    }
    @Override
    public Class<SettingsResponse> getResponseClass() {
        return SettingsResponse.class;
    }

    public void setPassword(Integer password) {
        passwordSettings = MDEncoder.encodeInSHA512Base64(password.toString());
    }

    public String getPasswordSettings() {
        return passwordSettings;
    }

    public void addSettingValue(Settings key, String value) {
        settings.put(key.getCode(), value);
    }

    public Map<Integer, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<Integer, String> settings) {
        this.settings = settings;
    }

    public void addSettings(Map<Settings, String> settings) {
        for (Map.Entry<Settings, String> setting : settings.entrySet()) {
            addSettingValue(setting.getKey(), setting.getValue());
        }
    }

}
