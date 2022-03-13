package ru.crystals.pos.fiscalprinter.mstar.core;

import java.util.ResourceBundle;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleFiscalPrinterMstar {

    private static final String BUNDLE_NAME = "res-fiscalPrinter-mstar";

    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinter.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
