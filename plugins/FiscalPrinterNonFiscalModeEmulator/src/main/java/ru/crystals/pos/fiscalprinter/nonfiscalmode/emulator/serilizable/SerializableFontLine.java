package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable;

import ru.crystals.pos.fiscalprinter.CheckLine;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;

import java.io.Serializable;

public class SerializableFontLine implements Serializable, CheckLine {
    private static final long serialVersionUID = -6149390926527964444L;
    private String content;
    private Font font;

    public SerializableFontLine(String content, Font font) {
        this.setContent(content);
        this.setFont(font);
    }

    public SerializableFontLine(String content) {
        this.setContent(content);
        this.setFont(Font.NORMAL);
    }

    public SerializableFontLine(FontLine line) {
        this(line.getContent(), line.getFont());
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }
}
