package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import java.util.ResourceBundle;

import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.localization.CoreResBundle;

public class ResBundleFiscalPrinterShtrihMiniFrk {
	private static final String BUNDLE_NAME = "res-fiscalPrinter-shtrihminifrk";
	private static ResourceBundle resourceBundle = CoreResBundle.getResBundleForName(BUNDLE_NAME,ResBundleFiscalPrinter.getBundle());
	
	public static String getString(String key){
		return resourceBundle.getString(key);
	}
	
	public static ResourceBundle getBundle(){
		return resourceBundle;
	}
}
