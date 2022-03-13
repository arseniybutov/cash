package ru.crystals.pos.bank.ucs;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankUcs {
    private static final String BUNDLE_NAME = "res-bank-ucs";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
}
