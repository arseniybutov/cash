package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

/**
 * Просто обертка, чтобы оставить основной конфиг POJO, но при этом добавить доп. логику для заполнения конфига по умолчанию
 */
public class DefaultPrinterConfig extends EscPosPrinterConfig {

    public DefaultPrinterConfig addCommand(PrinterCommandType command, ByteSequence definition) {
        getCommands().put(command, definition);
        return this;
    }

    public DefaultPrinterConfig addMissingSymbolReplacement(char from, char to) {
        getMissingSymbolsReplacement().put(from, to);
        return this;
    }
}
