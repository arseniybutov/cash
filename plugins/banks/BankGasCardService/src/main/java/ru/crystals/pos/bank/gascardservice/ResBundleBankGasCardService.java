package ru.crystals.pos.bank.gascardservice;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankGasCardService {
    private static final String BUNDLE_NAME = "res-bank-gascardservice";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
