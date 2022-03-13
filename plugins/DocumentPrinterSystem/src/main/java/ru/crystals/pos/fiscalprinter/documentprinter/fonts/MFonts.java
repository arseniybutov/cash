package ru.crystals.pos.fiscalprinter.documentprinter.fonts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

/**
 *
 * @author dalex
 */
public class MFonts {

    private static final Logger LOG = LoggerFactory.getLogger(MFonts.class);

    private static final String FONTS_DIR = "/ru/crystals/pos/fiscalprinter/documentprinter/fonts/";

    private static Font monoFont = null;

    private static Font createFontFromResources(String path) {
        try {
            Font font = Font.createFont(Font.PLAIN, MFonts.class.getResourceAsStream(path));
            boolean registered = GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            if (registered) {
                LOG.info("Font [{}] was registered", font.toString());
            } else {
                LOG.error("Font [{}] WAS NOT registered", font.toString());
            }
            return font;
        } catch (FontFormatException | IOException e) {
            LOG.error("", e);
            return null;
        }
    }

    public static Font getMonoFont() {
        if (monoFont == null) {
            monoFont = createFontFromResources(FONTS_DIR + "FreeMono.ttf");
        }
        return monoFont;
    }

    public static Font getMonoFont(float size) {
        Font f = MFonts.getMonoFont();
        return f == null ? new Font(Font.MONOSPACED, Font.PLAIN, (int) size) : f.deriveFont(size);
    }
}
