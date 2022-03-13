package ru.crystals.pos.customerdisplay.ksdp01;

import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public enum Font {
    Font10x16(CustomerDisplayPluginImpl.FILE_PATH_PREFIX + "templates" + File.separator + "img" + File.separator + "Font-10x16-w.bmp", 10, 16, 32),
    Font12x24(CustomerDisplayPluginImpl.FILE_PATH_PREFIX + "templates" + File.separator + "img" + File.separator + "Font-Bold-12x24-w.bmp", 12, 24, 32);

    private final BufferedImage img;
    private final int height;
    private final int width;
    private final int numberInRow;
    private final HashMap<Character, Point> map = new HashMap<Character, Point>();

    Font(String fileName, int width, int height, int num) {
        this.height = height;
        this.width = width;
        this.numberInRow = num;

        initializeMap();

        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            img = temp;
        }


    }

    private void initializeMap() {
        for (int i = 32, j = 0; i <= 0x2122; i++) {
            //₭  -
            //₮ - знак рубля
            char c = (char) i;
            if ((c >= 0x007f && c <= 0x009f) || (c >= 'Ĉ' && c <= 'ċ') ||
                    (c == 'Ĕ') || (c == 'ĕ') || (c >= 'Ĝ' && c <= 'ġ') ||
                    (c >= 'Ĥ' && c <= 'ĩ') || (c == 'Ĭ') || (c == 'ĭ') ||
                    (c >= 'İ' && c <= 'ĵ') || (c == 'ĸ') ||
                    (c == 'Ŀ') || (c == 'ŀ') || (c >= 'ŉ' && c <= 'ŋ') ||
                    (c == 'Ŏ') || (c == 'ŏ') || (c == 'Ŝ') || (c == 'ŝ') ||
                    (c >= 'Ŧ' && c <= 'ũ') || (c == 'Ŭ') || (c == 'ŭ') ||
                    (c >= 'Ŵ' && c <= 'ŷ') || (c >= 'ſ' && c <= 'ʃ') ||
                    (c >= 'ʅ' && c <= 0x02c5) || (c >= 0x02c8 && c <= 0x02d7) ||
                    (c >= 0x02de && c <= 'Ѐ') || (c == 'Ѝ') || (c == 'ѐ') ||
                    (c == 'ѝ') || (c >= 'Ѡ' && c <= 'ҏ') ||
                    (c >= 'Ғ' && c <= 0x2012) || (c >= '―' && c <= '‗') ||
                    (c == '‛') || (c == 0x201f) || (c >= 0x2023 && c <= 0x2025) ||
                    (c >= 0x2027 && c <= 0x202f) || (c >= 0x2031 && c <= 0x2038) ||
                    (c >= 0x203b && c <= 0x20ab) || (c >= 0x20af && c <= 0x2116) ||
                    (c >= 0x2117 && c <= 0x2121)) {
                map.put(c, new Point(0, 0));
            } else {
                map.put(c, new Point(((j % numberInRow) * width), ((j / numberInRow) * height)));
                j++;
            }
        }
    }

    public BufferedImage getCharImg(char c) {

        if (map.get(c) == null) {
            return img.getSubimage(map.get(' ').x, map.get(' ').y, width, height);
        } else {
            return img.getSubimage(map.get(c).x, map.get(c).y, width, height);
        }
    }

    public BufferedImage getImg() {
        return img;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getNumberInRow() {
        return numberInRow;
    }

    public HashMap<Character, Point> getMap() {
        return map;
    }
}
