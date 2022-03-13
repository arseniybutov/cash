package ru.crystals.pos.fiscalprinter.pirit.core;

/**
 * Created by Tatarinov Eduard on 06.12.16.
 */
public enum CommandService {
    START("start"),
    STOP("stop");
    private String command;

    CommandService(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
