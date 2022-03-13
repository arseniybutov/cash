package ru.crystals.pos.visualization.payments.voucher;

import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.payments.service.ResBundlePayments;

import java.util.ResourceBundle;

public class ResBundlePaymentVoucher {

    private static final String BUNDLE_NAME = "res-payments-voucher";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundlePayments.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }

}
