package ru.crystals.scales.magellan;

import java.util.ResourceBundle;

import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScaleMagellan8400 {
	private static final String BUNDLE_NAME = "res-scale-magellan8400";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,ResBundleScaleMagellan8400.getBundle());

	public static String getString(String key){
		return resourceBundle.getString(key);
	}

	public static ResourceBundle getBundle(){
		return resourceBundle;
	}
}
