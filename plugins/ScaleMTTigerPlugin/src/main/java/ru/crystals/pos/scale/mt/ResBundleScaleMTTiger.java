package ru.crystals.pos.scale.mt;

import java.util.ResourceBundle;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScaleMTTiger {

    private static final String BUNDLE_NAME = "res-scale-mttiger";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME);

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
