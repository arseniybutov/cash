package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.FontType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

/**
 * Текст.
 **/
public class StringDocumentEntry extends CommonDocumentEntry {

    public static final String TYPE_NAME = DTO_PREFIX + "StringDocumentEntry";

    /**
     * Тип шрифта.
     **/
    private FontType fontType;


    public FontType getFontType() {
        return fontType;
    }

    public void setFontType(FontType fontType) {
        this.fontType = fontType;
    }

    @Override
    public String toString() {
        return "StringDocumentEntry{" +
                "fontType=" + fontType +
                ", data='" + data + '\'' +
                '}';
    }
}
