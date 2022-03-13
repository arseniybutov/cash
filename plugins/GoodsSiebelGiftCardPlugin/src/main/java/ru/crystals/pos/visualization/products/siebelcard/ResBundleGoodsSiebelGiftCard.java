package ru.crystals.pos.visualization.products.siebelcard;

import java.util.ResourceBundle;
import ru.crystals.pos.catalog.service.ResBundleGoods;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleGoodsSiebelGiftCard {
    private static final String BUNDLE_NAME = "res-goods-siebelCard";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleGoods.getBundle());

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return resourceBundle;
    }
}
