package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class PrinterItem extends BufferedImage {

	private PrinterItemType itemType;

	public PrinterItem(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
	}

	public PrinterItem(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public PrinterItem(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public PrinterItemType getItemType() {
		return itemType;
	}

	public void setItemType(PrinterItemType itemType) {
		this.itemType = itemType;
	}

}
