package ru.crystals.pos.fiscalprinter.nfd.checkdata;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.FontType;

public class NfdReceiptString {

    private String data;

    private FontType fontType;

    public NfdReceiptString(String data, FontType fontType) {
        this.data = data;
        this.fontType = fontType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public FontType getFontType() {
        return fontType;
    }

    public void setFontType(FontType fontType) {
        this.fontType = fontType;
    }
}
