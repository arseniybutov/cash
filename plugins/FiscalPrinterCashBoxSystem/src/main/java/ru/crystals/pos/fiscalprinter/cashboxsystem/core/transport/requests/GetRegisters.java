package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Register;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Settings;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.RegistersResponse;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.MDEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос данных текущей смены (регистров ккм)
 */
public class GetRegisters extends BaseRequest {

    /**
     * sha512 пароля от режима настроек в base64 виде
     */
    @JsonProperty("password_settings")
    private String passwordSettings;

    /**
     * Список запрашиваемых регистров ккм
     */
    @JsonProperty("registers")
    private List<Integer> registers = new ArrayList<>();

    public GetRegisters() {
        setPassword(Settings.DEFAULT_PASSWORD);
    }

    @Override
    public String getTarget() {
        return "/api/registers";
    }
    @Override
    public Class<RegistersResponse> getResponseClass() {
        return RegistersResponse.class;
    }

    public void setPassword(Integer password) {
        passwordSettings = MDEncoder.encodeInSHA512Base64(password.toString());
    }

    public void addRegister(Register register) {
        registers.add(register.getCode());
    }

    public String getPasswordSettings() {
        return passwordSettings;
    }

    public List<Integer> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Integer> registers) {
        this.registers = registers;
    }

    public void addRegisters(List<Register> registers) {
        for (Register register : registers) {
            addRegister(register);
        }
    }
}
