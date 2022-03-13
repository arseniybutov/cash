package ru.crystals.scales.bizerba;

import java.util.ResourceBundle;

import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleScaleBizerbaCS300 {
	private static final String BUNDLE_NAME = "res-scale-bizerba-cs300";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleScaleBizerbaCS300.getBundle());

	public static String getString(String key){
		return resourceBundle.getString(key);
	}

	public static ResourceBundle getBundle(){
		return resourceBundle;
	}
}
