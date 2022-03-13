package ru.crystals.pos.bank.zvt;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankZVT {
    private static final String BUNDLE_NAME = "res-bank-zvt";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static String getRC(String rc) {
        final String key = "RC_" + rc;
        if (resourceBundle.containsKey(key)) {
            return resourceBundle.getString(key);
        }
        try {
            final int i = Integer.parseInt(rc, 16);
            if (i >= 0x01 && i <= 0x63) {
                // errorcodes from network-operator system/authorisation-system
                return resourceBundle.getString("RC_01_63");
            }
        } catch (Exception ignore) {
            // ignore
        }
        return resourceBundle.getString("UNKNOWN_ERROR");
    }
}
