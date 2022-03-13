package ru.crystals.pos.customerdisplay.ncr5976.core;

import ru.crystals.pos.customerdisplay.ResBundleCustomerDisplay;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleCustomerDisplayNCR {

    private static final String BUNDLE_NAME = "res-customerDisplay-ncr";

    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleCustomerDisplay.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
