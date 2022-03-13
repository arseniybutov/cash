package ru.crystals.pos.fiscalprinter.documentprinter.system;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;

/**
 *
 * @author dalex
 */
public interface PDocumentElement {

    public int print(Graphics2D g2d, PageFormat pageFormat, int y);

}
