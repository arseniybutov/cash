package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import java.util.EnumSet;
import java.util.Set;

public enum CharacterReplacement {

    NCR_SCHWA_CAPITAL("Ə", "Ä", "schwa_capital.txt", 13),
    NCR_SCHWA_SMALL("ə", "ä", "schwa_small.txt", 13),
    NCR_G_CAPITAL("Ğ", "Ď", "g_capital.txt", 13),
    NCR_G_SMALL("ğ", "ď", "g_small.txt", 13),
    NCR_I_SMALL("ı", "ţ", "i_small.txt", 13),

    NCR_SCHWA_CAPITAL_NARROW("Ə", "Ą", "schwa_capital_narrow.txt", 10),
    NCR_SCHWA_SMALL_NARROW("ə", "ą", "schwa_small_narrow.txt", 10),
    NCR_G_CAPITAL_NARROW("Ğ", "Ř", "g_capital_narrow.txt", 10),
    NCR_G_SMALL_NARROW("ğ", "ř", "g_small_narrow.txt", 10),
    NCR_I_SMALL_NARROW("ı", "ŕ", "i_small_narrow.txt", 10),

    WINCOR_SCHWA_CAPITAL("Ə", "Ä", "schwa_capital.txt", 13),
    WINCOR_SCHWA_SMALL("ə", "ä", "schwa_small.txt", 13),

    WINCOR_SCHWA_CAPITAL_NARROW("Ə", "Å", "schwa_capital_narrow.txt", 10),
    WINCOR_SCHWA_SMALL_NARROW("ə", "å", "schwa_small_narrow.txt", 10);

    static final Set<CharacterReplacement> NCR_NORMAL = EnumSet.of(
            NCR_SCHWA_CAPITAL,
            NCR_SCHWA_SMALL,
            NCR_G_CAPITAL,
            NCR_G_SMALL,
            NCR_I_SMALL);

    static final Set<CharacterReplacement> NCR_SMALL = EnumSet.of(
            NCR_SCHWA_CAPITAL_NARROW,
            NCR_SCHWA_SMALL_NARROW,
            NCR_G_CAPITAL_NARROW,
            NCR_G_SMALL_NARROW,
            NCR_I_SMALL_NARROW);

    static final Set<CharacterReplacement> WINCOR_NORMAL = EnumSet.of(
            WINCOR_SCHWA_CAPITAL,
            WINCOR_SCHWA_SMALL);

    static final Set<CharacterReplacement> WINCOR_SMALL = EnumSet.of(
            WINCOR_SCHWA_CAPITAL_NARROW,
            WINCOR_SCHWA_SMALL_NARROW);

    /**
     * Кастомный символ, который нарисован в файле
     */
    private final String symbol;
    /**
     * Символ, существующий в кодировке принтера, который будет заменен
     */
    private final String place;
    /**
     * Файл с нарисованным кастомным символом
     */
    private final String fileName;
    /**
     * Количество колонок в изображении символа
     */
    private final int colNum;

    CharacterReplacement(String symbol, String place, String fileName, int colNum) {
        this.symbol = symbol;
        this.place = place;
        this.fileName = fileName;
        this.colNum = colNum;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPlace() {
        return place;
    }

    public String getFileName() {
        return fileName;
    }

    public int getColNum() {
        return colNum;
    }
}
