package ru.crystals.pos.bank.gazpromsbp;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleBankGazpromSBP {
    private static final String BUNDLE_NAME = "res-bank-gazpromsbp";

    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static String getForStatus(String status, String rawMessage) {
        final String key = "STATUS_" + status;
        final String statusMessage = resourceBundle.containsKey(key) ? resourceBundle.getString(key) : resourceBundle.getString("STATUS_DECLINED");
        final String rawMessageNormalized = StringUtils.trimToNull(rawMessage);
        if (rawMessageNormalized == null) {
            return statusMessage;
        }
        return statusMessage + ": " + rawMessageNormalized;
    }
}
