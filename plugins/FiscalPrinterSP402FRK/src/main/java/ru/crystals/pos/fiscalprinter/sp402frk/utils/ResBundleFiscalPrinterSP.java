package ru.crystals.pos.fiscalprinter.sp402frk.utils;

import java.util.ResourceBundle;

import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleFiscalPrinterSP {
    private static final String BUNDLE_NAME = "res-fiscalPrinter-sp402frk";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleFiscalPrinterSP.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
