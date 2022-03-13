package ru.crystals.pos.visualization.payments.bonuscard;

import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.payments.service.ResBundlePayments;

import java.util.ResourceBundle;

public class ResBundlePaymentBonusCard {

    private static final String BUNDLE_NAME = "res-payments-bonusCard";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundlePayments.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }

    public static String getCommonString(String key) {
        return CoreResBundle.getStringCommon(key);
    }

}
