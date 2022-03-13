package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;

import java.util.Objects;

public class ExecutePosCmdHeader {

    private final Command command;

    public ExecutePosCmdHeader(Command command) {
        this.command = command;
    }

    @JsonGetter("command")
    public Command getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutePosCmdHeader that = (ExecutePosCmdHeader) o;
        return command == that.command;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command);
    }

    @Override
    public String toString() {
        return "ExecutePosCmdHeader{" +
                "command=" + command +
                '}';
    }
}
