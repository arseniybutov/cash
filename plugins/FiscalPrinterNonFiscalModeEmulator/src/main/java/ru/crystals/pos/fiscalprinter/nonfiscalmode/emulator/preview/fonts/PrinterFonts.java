package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.fonts;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public class PrinterFonts {

	private static Font andaleMono = null;
	private static Font freeMono = null;
	
	/**
	 * 
	 * @return шрифт Andale Mono
	 */
	public static Font getAndaleMono() {
		if (andaleMono == null) {
			try {
				andaleMono = Font.createFont(Font.PLAIN, PrinterFonts.class.getResourceAsStream("/fonts/Andale Mono.ttf"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return andaleMono;
	}
	
	/**
	 * 
	 * @return шрифт Free Mono
	 */
	public static Font getFreeMono() {
		if (freeMono == null) {
            try {
                freeMono = Font.createFont(Font.PLAIN, PrinterFonts.class.getResourceAsStream("/fonts/FreeMono.ttf"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return freeMono;
	}
}
