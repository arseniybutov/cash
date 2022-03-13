package ru.crystals.scales.ncr7872;

import java.util.ResourceBundle;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScalesNCR7872 {
    private static final String BUNDLE_NAME = "res-scale-ncr7872";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME);

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
