package ru.crystals.pos.fiscalprinter.az.airconn;

import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleFiscalAirConn {
    private static final String BUNDLE_NAME = "res-fiscal-az-airconn";

    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinter.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
