package ru.crystals.pos.loyal.cash.calculation;

import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleLoyalCash {

	private static final String BUNDLE_NAME = "res-loyalty";
	
	public static String getString(String key){
		return CoreResBundle.getString(BUNDLE_NAME, key);
	}
	
}
