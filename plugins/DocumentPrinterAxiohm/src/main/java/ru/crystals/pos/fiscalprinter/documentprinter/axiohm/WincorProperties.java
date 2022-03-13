package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WincorProperties extends PrinterProperties {

    private static final Path CONFIG_PATH = Paths.get("modules/fiscalPrinter/wincor.properties");
    private static final String PRINT_ON_CUT_ACTIVATED = "print.on.cut.activated";

    @Override
    protected void setConfigPath() {
        configPath = CONFIG_PATH;
    }

    public boolean isPrintLogoAfterCutActivated() {
        String printOnCutEnabled = properties.getProperty(PRINT_ON_CUT_ACTIVATED);
        if (printOnCutEnabled == null) {
            // проперти еще нет, запишем значение по умолчанию
            properties.setProperty(PRINT_ON_CUT_ACTIVATED, Boolean.FALSE.toString());
            writeProperties();
        }
        return Boolean.parseBoolean(printOnCutEnabled);
    }

    public void setPrintLogoAfterCutActivated(boolean status) {
        properties.setProperty(PRINT_ON_CUT_ACTIVATED, Boolean.toString(status));
        writeProperties();
    }
}
