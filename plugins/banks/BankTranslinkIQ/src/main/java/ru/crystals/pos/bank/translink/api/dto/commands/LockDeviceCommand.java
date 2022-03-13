package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Команда выполняет блокировку POS, с возможностью отображения на экране сообщения, переданного в параметре idleText.
 */
public class LockDeviceCommand implements CommandParams {

    /**
     * Сообщение, которое будет отображаться на экране POS
     */
    private final String idleText;

    public LockDeviceCommand(String idleText) {
        this.idleText = idleText;
    }

    @JsonGetter("idleText")
    public String getIdleText() {
        return idleText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LockDeviceCommand that = (LockDeviceCommand) o;
        return Objects.equals(idleText, that.idleText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idleText);
    }

    @Override
    public String toString() {
        return "LockDeviceCommand{" +
                "idleText='" + idleText + '\'' +
                '}';
    }
}
