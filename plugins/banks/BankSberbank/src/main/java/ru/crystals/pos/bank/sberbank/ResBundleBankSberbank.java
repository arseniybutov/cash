package ru.crystals.pos.bank.sberbank;

import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResBundleBankSberbank {

    private static final String BUNDLE_NAME = "res-bank-sberbank";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleBank.getBundle());
    private static String customBundleName;
    private static ResourceBundle customResBundle;

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }

    public static String getString(String key) {
        if (customResBundle != null) {
            try {
                return customResBundle.getString(key);
            } catch (Exception ignore) {

            }
        }
        return resourceBundle.getString(key);
    }

    public static void setCustomResBundle(String customBundleName, Locale locale) {
        if (customResBundle == null || !locale.equals(customResBundle.getLocale()) || !customBundleName.equals(ResBundleBankSberbank.customBundleName)) {
            ResBundleBankSberbank.customBundleName = customBundleName;
            customResBundle = ResourceBundle.getBundle(customBundleName, locale);
        }
    }
}
