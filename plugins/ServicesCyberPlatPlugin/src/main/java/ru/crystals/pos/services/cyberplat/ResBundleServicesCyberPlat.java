package ru.crystals.pos.services.cyberplat;

import java.util.ResourceBundle;

import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.services.ResBundleServices;

public class ResBundleServicesCyberPlat {
	private static final String BUNDLE_NAME = "res-services-cyberplat";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME, ResBundleServices.getBundle());
	
	public static String getString(String key){
		return resourceBundle.getString(key);
	}
	
	public static ResourceBundle getBundle(){
		return resourceBundle;
	}	

}
