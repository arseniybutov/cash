package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import java.util.Arrays;

public class SimpleCommand {

    private final byte[] command;

    public SimpleCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getCommand() {
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
        SimpleCommand that = (SimpleCommand) o;
        return Arrays.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(command);
    }

    @Override
    public String toString() {
        return "SimpleCommand{" +
                "command=" + Arrays.toString(command) +
                '}';
    }
}
