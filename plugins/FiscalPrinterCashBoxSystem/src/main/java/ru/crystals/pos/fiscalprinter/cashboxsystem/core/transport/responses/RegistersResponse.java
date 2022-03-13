package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Register;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Ответ на запрос данных текущей смены
 */
public class RegistersResponse extends BaseResponse {
    /**
     * Объект, у которого ключами выступаю значения/коды перечислений.
     */
    @JsonProperty("data")
    private Map<Integer, String> registers;

    public Map<Integer, String> getRegisters() {
        return registers;
    }

    public void setRegisters(Map<Integer, String> registers) {
        this.registers = registers;
    }

    public String getRegisterValue(Register key) {
        if(registers == null) {
            return null;
        }
        return registers.get(key.getCode());
    }

    public BigDecimal getRegisterAsBigDecimal(Register key) {
        if(registers == null) {
            return null;
        }
        return new BigDecimal(registers.get(key.getCode()));
    }

    public Long getRegisterLong(Register key) {
        if(registers == null) {
            return null;
        }
        return Long.valueOf(registers.get(key.getCode()));
    }
}
