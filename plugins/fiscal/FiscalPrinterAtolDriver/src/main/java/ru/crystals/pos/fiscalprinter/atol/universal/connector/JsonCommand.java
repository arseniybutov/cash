package ru.crystals.pos.fiscalprinter.atol.universal.connector;

enum JsonCommand {

    OVERALL_TOTALS("getOverallTotals"),
    SHIFT_TOTALS("getShiftTotals");

    private final String command;

    JsonCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
