package ru.crystals.pos.bank.odengiqr;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankODengiQR {

    private static final String BUNDLE_NAME = "res-bank-odengi-qr";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
}
