package ru.crystals.pos.fiscalprinter.wincor.core;

import java.util.ResourceBundle;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleFiscalPrinterWincor {

    private static final String BUNDLE_NAME = "res-fiscalPrinter-wincor";

    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinter.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
