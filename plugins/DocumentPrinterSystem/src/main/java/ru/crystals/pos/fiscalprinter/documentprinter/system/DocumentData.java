package ru.crystals.pos.fiscalprinter.documentprinter.system;

import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.documentprinter.fonts.MFonts;
import ru.crystals.pos.fiscalprinter.documentprinter.system.elements.BarCodeElement;
import ru.crystals.pos.fiscalprinter.documentprinter.system.elements.FontElement;
import ru.crystals.pos.fiscalprinter.documentprinter.system.elements.RowElement;
import ru.crystals.pos.fiscalprinter.documentprinter.system.elements.TextElement;

import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dalex
 */
public class DocumentData {

    private List<PDocumentElement> elements = new LinkedList<>();

    void print(Graphics2D g2d, PageFormat pageFormat, int y) {
        g2d.setFont(MFonts.getMonoFont(12));
        for (PDocumentElement e : elements) {
            y = e.print(g2d, pageFormat, y);
        }
    }

    void appendRow(String text) {
        elements.add(new RowElement(text));
    }

    void changeFont(Font font) {

        java.awt.Font awtFont = null;
        if (font == Font.NORMAL) {
            awtFont = MFonts.getMonoFont(10);
        } else if (font == Font.SMALL) {
            awtFont = MFonts.getMonoFont(8);
        } else if (font == Font.UNDERLINE) {
            Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            awtFont = MFonts.getMonoFont(10).deriveFont(fontAttributes);
        }

        if (awtFont != null) {
            elements.add(new FontElement(awtFont));
        }
    }

    void appendText(String text) {
        elements.add(new TextElement(text));
    }

    void appendBarcode(BarCode barcode) {
        elements.add(new BarCodeElement(barcode));
    }

}
