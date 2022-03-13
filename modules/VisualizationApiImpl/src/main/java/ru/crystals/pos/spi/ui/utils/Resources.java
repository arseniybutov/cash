package ru.crystals.pos.spi.ui.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Resources {
    private static final int STUB_IMAGE_SIZE = 64;

    private Resources() {

    }

    public static ImageIcon getImageIcon(String icon) {
        try {
            return new ImageIcon(Resources.class.getClassLoader().getResource(icon)); // NOSONAR
        } catch (Exception ex) {
            ex.printStackTrace(System.err); //NOSONAR
            return createImageIconStub();
        }
    }

    private static ImageIcon createImageIconStub() {
        BufferedImage img = new BufferedImage(STUB_IMAGE_SIZE, STUB_IMAGE_SIZE, BufferedImage.TYPE_3BYTE_BGR);
        img.getGraphics().setColor(Color.WHITE);
        img.getGraphics().fillRect(0, 0, STUB_IMAGE_SIZE, STUB_IMAGE_SIZE);
        img.getGraphics().setColor(Color.BLACK);
        img.getGraphics().drawRect(0, 0, STUB_IMAGE_SIZE, STUB_IMAGE_SIZE);
        img.getGraphics().drawLine(0, 0, STUB_IMAGE_SIZE, STUB_IMAGE_SIZE);
        img.getGraphics().drawLine(STUB_IMAGE_SIZE, 0, 0, STUB_IMAGE_SIZE);
        return new ImageIcon(img);
    }
}
