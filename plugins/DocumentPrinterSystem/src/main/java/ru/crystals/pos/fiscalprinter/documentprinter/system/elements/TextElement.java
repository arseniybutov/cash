package ru.crystals.pos.fiscalprinter.documentprinter.system.elements;

import ru.crystals.pos.fiscalprinter.documentprinter.system.PDocumentElement;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;

/**
 *
 * @author dalex
 */
public class TextElement implements PDocumentElement {

    private String text[];

    public TextElement(String text) {
        if (text == null) {
            text = "";
        }
        this.text = text.split("\n");
    }

    @Override
    public int print(Graphics2D g2d, PageFormat pageFormat, int y) {
        int rowAdd = (int) (g2d.getFont().getSize() * 1.2);
        for (String t : text) {
            g2d.drawString(t, 0, y + rowAdd);
            y += rowAdd;
        }
        return y + rowAdd;
    }

}
