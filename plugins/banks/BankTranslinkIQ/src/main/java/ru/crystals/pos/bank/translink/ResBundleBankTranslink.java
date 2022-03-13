package ru.crystals.pos.bank.translink;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.translink.api.dto.ResultCode;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankTranslink {
    private static final String BUNDLE_NAME = "res-bank-translink";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static String getForResultCode(ResultCode rc) {
        final String key = "RC_" + rc;
        if (resourceBundle.containsKey(key)) {
            return resourceBundle.getString(key);
        }
        return resourceBundle.getString("RC_UNKNOWN");
    }
}
