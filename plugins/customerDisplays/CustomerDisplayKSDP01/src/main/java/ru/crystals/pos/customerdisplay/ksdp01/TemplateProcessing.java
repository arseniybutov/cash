package ru.crystals.pos.customerdisplay.ksdp01;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.ContentType;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.FontSize;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.Item;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.Screen;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TemplateProcessing {

    private static final int NUM_COLOR = 16;
    private static final String sep = File.separator;
    private static final String IMG_ETALON_BMP = CustomerDisplayPluginImpl.FILE_PATH_PREFIX + "templates" + sep + "img" + sep + "etalon.bmp";
    private static CDImageListener cdImageListener;

    public static byte[] processTemplate(Screen screen, HashMap<String, Object> map) throws CustomerDisplayPluginException {
        try {
            BufferedImage img = ImageIO.read(new File(IMG_ETALON_BMP));

            if (screen != null && screen.getItem() != null && !screen.getItem().isEmpty()) {
                for (Item item : screen.getItem()) {
                    if (item.getType() == ContentType.IMG) {
                        processImg(item, img.getGraphics());
                    } else {
                        clear(img.getGraphics(), item);
                        processStr(item, map, img.getGraphics());
                        filterImg(img.getSubimage(item.getX().intValue(), item.getY().intValue(), item.getWidth().intValue(), item.getHeight().intValue()), item);
                    }
                }
            }
            if (cdImageListener != null) {
                cdImageListener.draw(img);
            }
            return ((DataBufferByte) (img).getRaster().getDataBuffer()).getData();
        } catch (IOException e) {
            e.printStackTrace();
            CustomerDisplayPluginException ne = new CustomerDisplayPluginException("No such file: " + IMG_ETALON_BMP);
            ne.setStackTrace(e.getStackTrace());
            throw ne;
        }
    }

    public static void setCdImageListener(CDImageListener cdImageListener) {
        TemplateProcessing.cdImageListener = cdImageListener;
    }

    private static void clear(Graphics g, Item item) {
        g.setColor(Color.BLACK);
        g.fillRect(item.getX().intValue(), item.getY().intValue(), item.getWidth().intValue(), item.getHeight().intValue());
    }

    private static void processStr(Item item, HashMap<String, Object> map, Graphics g) {
        int x = item.getX().intValue();
        Font font = Font.Font10x16;
        String text;

        if (item.getFontsize() == FontSize.FS_10_X_16) {
            font = Font.Font10x16;
        } else if (item.getFontsize() == FontSize.FS_12_X_24) {
            font = Font.Font12x24;
        }

        text = getElementValue(item, map);

        if (text.length() * font.getWidth() > item.getWidth().intValue()) {
            text = text.substring(0, item.getWidth().intValue() / font.getWidth());
        }

        switch (item.getAlign()) {
            case RIGHT:
                if (text.length() * font.getWidth() < item.getWidth().intValue()) {
                    x += item.getWidth().intValue() - text.length() * font.getWidth();
                }
                break;
            case CENTER:
                if (text.length() * font.getWidth() < item.getWidth().intValue()) {
                    x += (item.getWidth().intValue() - text.length() * font.getWidth()) / 2;
                }
                break;
            default:
                break;
        }
        drawStr(text, g, x, item.getY().intValue(), font);
    }

    private static String getElementValue(Item item, HashMap<String, Object> map) {
        String text;
        if (item.getType() == ContentType.TEXT) {
            return item.getElement();
        }
        String itemName = item.getElement().toLowerCase();
        text = (String) (map.get(itemName));
        if (text == null) {
            return "-n-";
        }
        return text;
    }

    private static void processImg(Item item, Graphics g) throws CustomerDisplayPluginException {
        try {
            BufferedImage img = ImageIO.read(new File(item.getElement()));
            ((Graphics2D) g).drawImage(img, null, item.getX().intValue(), item.getY().intValue());
            g.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            CustomerDisplayPluginException ne = new CustomerDisplayPluginException("No such file: " + item.getElement());
            ne.setStackTrace(e.getStackTrace());
            throw ne;
        }
    }

    private static void drawStr(String text, Graphics g, int xStart, int yStart, Font font) {
        for (int i = 0; i < text.length(); i++) {
            ((Graphics2D) g).drawImage(font.getCharImg(text.charAt(i)), null, xStart + i * font.getWidth(), yStart);
        }
        g.dispose();
    }

    private static void filterImg(final BufferedImage charImg, Item item) {
        boolean isNeedFilter = false;
        if ((item.getBackgroundcolor() != null && item.getBackgroundcolor() != 0) || (item.getTextcolor() != null && item.getTextcolor() != 15)) {
            isNeedFilter = true;
        }

        if (isNeedFilter) {
            int backGroundColor = getRGBColor(item.getBackgroundcolor());
            int textColor = getRGBColor(item.getTextcolor());
            for (int j = charImg.getMinY(); j < charImg.getHeight(); j++) {
                for (int i = charImg.getMinX(); i < charImg.getWidth(); i++) {
                    if (charImg.getRGB(i, j) == Color.BLACK.getRGB()) {
                        charImg.setRGB(i, j, backGroundColor);
                    } else if (charImg.getRGB(i, j) == Color.WHITE.getRGB()) {
                        charImg.setRGB(i, j, textColor);
                    }
                }
            }
        }
    }

    private static int getRGBColor(int color) {
        return Color.BLACK.getRGB() | (color * NUM_COLOR << 16) | (color * NUM_COLOR << 8) | color * NUM_COLOR;
    }

    public static void main(String[] args) {
        try {
            BufferedImage etalon = ImageIO.read(new File(IMG_ETALON_BMP));
            drawStr("sgsgf", etalon.getGraphics(), 0, 0, Font.Font10x16);
            ImageIO.write(etalon, "BMP", new File("img/result.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
