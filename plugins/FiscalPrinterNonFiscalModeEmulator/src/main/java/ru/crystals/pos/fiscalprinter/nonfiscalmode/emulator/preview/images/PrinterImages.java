package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.images;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

public enum PrinterImages {
	
	LOGO("/images/logo.bmp"),
    FISCAL_SIGN("/images/fiscal_sign.bmp");
    
    private String path;
    private BufferedImage icon = null;

    PrinterImages(String path) {
        this.path = path;
    }

    public void setImagePath(String path) {
        icon = null;
        this.path = path;
    }

    public BufferedImage getImage() {
        if (icon == null) {
            URL myurl;
            try {
                myurl = PrinterImages.class.getResource(path);
                if (myurl != null) {
                    icon = ImageIO.read(myurl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return icon;
    }
}
