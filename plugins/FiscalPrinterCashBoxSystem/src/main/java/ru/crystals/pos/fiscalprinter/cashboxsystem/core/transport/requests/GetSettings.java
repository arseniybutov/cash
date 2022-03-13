package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Settings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.SettingsResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.MDEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос текущих настроек в CBS. Количество настроек, запрашиваемых за один раз, может варьироваться.
 */
public class GetSettings extends BaseRequest {

    /**
     * sha512 пароля от режима настроек в base64 виде
     */
    @JsonProperty("password_settings")
    private String passwordSettings;

    /**
     * Список настроек, которые должны быть получены
     */
    @JsonProperty("settings")
    private List<Integer> settings = new ArrayList<>();

    public GetSettings() {
        setPassword(Settings.DEFAULT_PASSWORD);
    }

    @Override
    public String getTarget() {
        return "/api/settings/get";
    }
    @Override
    public Class<SettingsResponse> getResponseClass() {
        return SettingsResponse.class;
    }

    public void setPassword(Integer password) {
        passwordSettings = MDEncoder.encodeInSHA512Base64(password.toString());
    }

    public void addSetting(Settings setting) {
        settings.add(setting.getCode());
    }

    public String getPasswordSettings() {
        return passwordSettings;
    }

    public List<Integer> getSettings() {
        return settings;
    }

    public void setSettings(List<Integer> settings) {
        this.settings = settings;
    }

    public void addSettings(List<Settings> settings) {
        for (Settings setting : settings) {
            addSetting(setting);
        }
    }
}
