package ru.crystals.pos.visualization.payments.bankcard;

import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundlePaymentChildrenCard {

    private static final String BUNDLE_NAME = "res-payments-childrenCard";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundlePaymentBankCard.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }

}