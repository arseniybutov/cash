package ru.crystals.pos.fiscalprinter.atol.universal;

import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleFiscalPrinterAtolUniversal {

    private static final String BUNDLE_NAME = "res-fiscalPrinter-atol-universal";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,
            ResBundleFiscalPrinterAtolUniversal.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
