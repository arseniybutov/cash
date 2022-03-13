package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.Optional;
import java.util.ResourceBundle;

public class ResBundleUZFiscalDrive {
    private static final String BUNDLE_NAME = "res-uz-fiscal-drive-api";

    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinter.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }

    public static Optional<String> getOptionalString(String key) {
        if (resourceBundle.containsKey(key)) {
            return Optional.of(resourceBundle.getString(key));
        }
        return Optional.empty();
    }
}
