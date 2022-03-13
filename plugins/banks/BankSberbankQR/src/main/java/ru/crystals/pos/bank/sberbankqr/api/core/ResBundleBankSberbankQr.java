package ru.crystals.pos.bank.sberbankqr.api.core;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankSberbankQr {

    private static final String BUNDLE_NAME = "res-bank-sberbank-qr";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static String getForErrorCode(String errorCode) {
        String errorPrefix = errorCode.substring(0, 2);
        return getWithPrefix(errorPrefix, "EC_");
    }

    public static String getForResponseCode(String responseCode) {
        return getWithPrefix(responseCode, "RC_");
    }

    private static String getWithPrefix(String code, String prefix) {
        String key = prefix + code;
        if (resourceBundle.containsKey(key)) {
            return resourceBundle.getString(key);
        }
        return resourceBundle.getString(prefix + "UNKNOWN");
    }
}
