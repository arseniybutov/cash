package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;

import java.util.Objects;

/**
 * Метод предназначен для передачи команды в устройство POS.
 * Тип запроса: POST
 */
public class ExecutePosCmdRequest {

    private final ExecutePosCmdHeader header;

    private final CommandParams params;

    public ExecutePosCmdRequest(ExecutePosCmdHeader header, CommandParams params) {
        this.header = header;
        this.params = params;
    }

    @JsonGetter
    public ExecutePosCmdHeader getHeader() {
        return header;
    }

    @JsonGetter
    public CommandParams getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutePosCmdRequest that = (ExecutePosCmdRequest) o;
        return Objects.equals(header, that.header) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, params);
    }

    @Override
    public String toString() {
        return "ExecutePosCmdRequest{" +
                "header=" + header +
                ", params=" + params +
                '}';
    }
}
