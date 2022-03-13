package ru.crystals.pos.visualization.products.setapi.goods.i18n;

import ru.crystals.pos.catalog.service.ResBundleGoods;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

/**
 * i18n для GUI плагинного типа товара
 *
 * @author aperevozchikov
 */
public class ResBundleSetApiGoods {
    private static final String BUNDLE_NAME = "res-set-api-goods";
    private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,ResBundleGoods.getBundle());

    public static String getString(String key){
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getBundle(){
        return resourceBundle;
    }
}
