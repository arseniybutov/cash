package ru.crystals.pos.scale.rbs;

import java.util.ResourceBundle;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScaleRBS {

    private static final String BUNDLE_NAME = "res-scale-rbs";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME);

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
