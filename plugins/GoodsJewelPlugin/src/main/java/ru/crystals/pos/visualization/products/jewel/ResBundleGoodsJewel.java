package ru.crystals.pos.visualization.products.jewel;

import java.util.ResourceBundle;

import ru.crystals.pos.catalog.service.ResBundleGoods;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleGoodsJewel {
	private static final String BUNDLE_NAME = "res-goods-jewel";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,ResBundleGoods.getBundle());
	
	public static String getString(String key){
		return resourceBundle.getString(key);
	}
	
	public static ResourceBundle getBundle(){
		return resourceBundle;
	}
}
