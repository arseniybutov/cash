package ru.crystals.pos.visualization.products.spirits;

import ru.crystals.pos.catalog.service.ResBundleGoods;
import ru.crystals.pos.localization.CoreResBundle;

import java.util.ResourceBundle;

public class ResBundleGoodsSpirits {
	private static final String BUNDLE_NAME = "res-goods-spirits";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,ResBundleGoods.getBundle());

	public static String getString(String key){
		return resourceBundle.getString(key);
	}

	public static ResourceBundle getBundle(){
		return resourceBundle;
	}
}
