// copy of ru.crystals.pos.fiscalprinter.atol.utils.ResBundleFiscalPrinterAtol

package ru.crystals.pos.fiscalprinter.atol3;

import java.util.ResourceBundle;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleFiscalPrinterAtol {
    private static final String BUNDLE_NAME = "res-fiscalPrinter-atol3";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinterAtol.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
