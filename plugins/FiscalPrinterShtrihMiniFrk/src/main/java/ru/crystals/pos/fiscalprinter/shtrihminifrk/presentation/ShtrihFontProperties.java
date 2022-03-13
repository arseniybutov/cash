package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Свойства/параметры шрифта.
 * 
 * @author aperevozchikov
 */
public class ShtrihFontProperties {
    /**
     * Номер этого шрифта
     */
    private int fontNumber;

    /**
     * Ширина области печати этим шрифтом, в точках
     */
    private int printableAreaWidth;

    /**
     * Ширина символа этого шрифта с учетом межсимвольного интервала, в точках
     */
    private int symbolWidth;

    /**
     * Высота символов этого шрифта с учетом межстрочного интервала, в точках
     */
    private int symbolHeight;

    /**
     * Общее количество шрифтов в ФР
     */
    private int fontsCount;
    
    @Override
    public String toString() {
        return String.format("shtrih-font [font-num: %s; printable-area-width: %s; symbol-width: %s; symbol-height: %s; fonts-count: %s]", 
            getFontNumber(), getPrintableAreaWidth(), getSymbolWidth(), getSymbolHeight(), getFontsCount());
    }

    public int getFontNumber() {
        return fontNumber;
    }

    public void setFontNumber(int fontNumber) {
        this.fontNumber = fontNumber;
    }

    public int getPrintableAreaWidth() {
        return printableAreaWidth;
    }

    public void setPrintableAreaWidth(int printableAreaWidth) {
        this.printableAreaWidth = printableAreaWidth;
    }

    public int getSymbolWidth() {
        return symbolWidth;
    }

    public void setSymbolWidth(int symbolWidth) {
        this.symbolWidth = symbolWidth;
    }

    public int getSymbolHeight() {
        return symbolHeight;
    }

    public void setSymbolHeight(int symbolHeight) {
        this.symbolHeight = symbolHeight;
    }

    public int getFontsCount() {
        return fontsCount;
    }

    public void setFontsCount(int fontsCount) {
        this.fontsCount = fontsCount;
    }
}