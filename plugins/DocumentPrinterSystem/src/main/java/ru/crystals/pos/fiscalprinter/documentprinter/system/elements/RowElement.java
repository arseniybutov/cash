package ru.crystals.pos.fiscalprinter.documentprinter.system.elements;

import ru.crystals.pos.fiscalprinter.documentprinter.system.PDocumentElement;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;

/**
 *
 * @author dalex
 */
public class RowElement implements PDocumentElement {

    private String text;

    public RowElement(String text) {
        this.text = text;
    }

    @Override
    public int print(Graphics2D g2d, PageFormat pageFormat, int y) {
        y += (int) (g2d.getFont().getSize() * 1.2);
        g2d.drawString(text, 0, y);
        return y;
    }

}
