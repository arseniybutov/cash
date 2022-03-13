package ru.crystals.pos.scale.cw;

import java.util.ResourceBundle;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScaleCheckWay {

    private static final String BUNDLE_NAME = "res-scale-checkway";
    private static final ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME);

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
