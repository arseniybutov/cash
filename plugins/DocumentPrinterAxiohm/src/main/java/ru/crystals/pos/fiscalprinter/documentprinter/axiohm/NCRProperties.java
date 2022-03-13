package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NCRProperties extends PrinterProperties {

    private static final Path CONFIG_PATH = Paths.get("modules/fiscalPrinter/ncr.properties");

    @Override
    protected void setConfigPath() {
        configPath = CONFIG_PATH;
    }
}
