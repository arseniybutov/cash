package ru.crystals.pos.fiscalprinter.documentprinter.system.elements;

import com.google.zxing.common.BitMatrix;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.documentprinter.fonts.MFonts;
import ru.crystals.pos.fiscalprinter.documentprinter.system.PDocumentElement;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;

/**
 *
 * @author dalex
 */
public class BarCodeElement implements PDocumentElement {

    private static final int PAPER_WIDTH_PX = 203;
    private BarCode barcode;

    public BarCodeElement(BarCode barcode) {
        this.barcode = barcode;
    }

    @Override
    public int print(Graphics2D g2d, PageFormat pageFormat, int y) {
        y += 3;
        if (barcode.getType() == BarCodeType.Code39
                || barcode.getType() == BarCodeType.EAN13
                || barcode.getType() == BarCodeType.EAN8
                || barcode.getType() == BarCodeType.PDF_417
                || barcode.getType() == BarCodeType.CODE_128) {
            int barcodeHeight = 20;
            BitMatrix bitMatrix = GraphicsUtils.getBarcodeAsBitMatrix(barcode);
            y = printMatrix(g2d, bitMatrix, PAPER_WIDTH_PX, PAPER_WIDTH_PX, barcode.getType() == BarCodeType.PDF_417 ? 1 : barcodeHeight, y, null);
            if (barcode.getBarcodeLabel() != null && barcode.getBarcodeLabel().length() > 0) {
                Font oldFont = g2d.getFont();
                Font font = MFonts.getMonoFont(8);
                g2d.setFont(font);

                int rowAdd = (int) (g2d.getFont().getSize() * 1.2);
                int stringWidth = g2d.getFontMetrics().stringWidth(barcode.getBarcodeLabel());
                g2d.drawString(barcode.getBarcodeLabel(), (PAPER_WIDTH_PX - stringWidth) / 2, y + rowAdd);

                g2d.setFont(oldFont);

                y += rowAdd;
            }
            return y;
        } else if (barcode.getType() == BarCodeType.QR) {
            int matrixWidth = (int) (barcode.getWidth() * 50);
            BitMatrix bitMatrix = GraphicsUtils.getBarcodeAsBitMatrix(barcode);
            y = printMatrix(g2d, bitMatrix, PAPER_WIDTH_PX, matrixWidth, 1, y, null);
            return y;
        }
        return y;
    }

    private int printMatrix(Graphics2D g2d, BitMatrix bitMatrix, int paperWidth, int barcodeFullWidthPX, int barcodeElemHeight, int y, Double scalePreset) {
        AffineTransform oldXForm = g2d.getTransform();
        double scale = scalePreset == null ? (((double) barcodeFullWidthPX) / ((double) bitMatrix.getWidth())) : scalePreset;
        g2d.scale(scale, scale);
        int xStart = (int) (((paperWidth - barcodeFullWidthPX) / 2) / scale);
        int yStart = (int) (y / scale);
        g2d.setStroke(new BasicStroke(0.01f));
        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                if (bitMatrix.get(i, j)) {
                    g2d.fillRect(xStart + i, yStart + j, 1, barcodeElemHeight);
                }
            }
        }
        // Restore transform
        g2d.setTransform(oldXForm);

        if (barcodeElemHeight == 0) {
            barcodeElemHeight = 1;
        }

        return y + (int) (bitMatrix.getHeight() * barcodeElemHeight * scale);
    }
}
