package ru.crystals.pos.fiscalprinter.documentprinter.system.elements;

import ru.crystals.pos.fiscalprinter.documentprinter.system.PDocumentElement;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

/**
 *
 * @author dalex
 */
public class FontElement implements PDocumentElement {

    private Font font;

    public FontElement(Font font) {
        this.font = font;
    }

    @Override
    public int print(Graphics2D g2d, PageFormat pageFormat, int y) {
        g2d.setFont(font);
        return y;
    }
}
