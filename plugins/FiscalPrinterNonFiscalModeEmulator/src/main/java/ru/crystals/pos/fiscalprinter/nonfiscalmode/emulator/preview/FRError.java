package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

/**
 *
 * @author dalex
 */
public enum FRError {
    NONE("Нет"),
    BEFORE_FISCALIZE("До фискализации"),
    PAPER_EMPTY("Кончилась бумага"),
    AFTER_FISCALIZE("После фискализации");

    private String name;

    FRError(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
