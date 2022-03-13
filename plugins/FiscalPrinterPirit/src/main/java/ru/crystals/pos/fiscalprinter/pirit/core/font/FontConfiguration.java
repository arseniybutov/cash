package ru.crystals.pos.fiscalprinter.pirit.core.font;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

public class FontConfiguration {

    private static final int BOLD_AS_DOUBLE_WIDTH  = PiritFontManager.DOUBLE_WIDTH_BIT;
    private static final int BOLD_AS_DOUBLE_WIDTH_SMALL = PiritFontManager.DOUBLE_WIDTH_BIT | PiritFontManager.SMALL_FONT;

    /**
     * Шрифты 2Ф с широкой лентой (включая Пирит РБ)
     * <p>
     * На самом деле в РБ 0,1 шрифты поменяны местами с 7,8 (но длины совпадают)
     */
    private static final Map<Integer, Integer> PIRIT_2F_FONTS = new ImmutableMap.Builder<Integer, Integer>()
            .put(0, 44)
            .put(1, 57)
            .put(2, 44)
            .put(3, 57)
            .put(4, 72)
            .put(5, 24)
            .put(6, 24)
            .put(7, 44)
            .put(8, 57)
            .build();

    /**
     * Шрифты 2Ф с узкой лентой (включая Пирит РБ)
     * <p>
     * На самом деле в РБ 0,1 шрифты поменяны местами с 7,8 (но длины совпадают)
     */
    private static final Map<Integer, Integer> PIRIT_2F_NARROW_FONTS = new ImmutableMap.Builder<Integer, Integer>()
            .put(0, 30)
            .put(1, 40)
            .put(2, 30)
            .put(3, 40)
            .put(4, 50)
            .put(5, 16)
            .put(6, 16)
            .put(7, 30)
            .put(8, 40)
            .build();

    /**
     * У 2Ф все отсуствующие шрифты мапятся как 0 шрифт (на самом деле в РБ мапится на 7, но длина та же)
     */
    private static final Function<Integer, Integer> ZERO_FONT_DEFAULT_GETTER = input -> 0;

    /**
     * У 2Ф есть жирный шрифт нормального (2) и малого (3) размера, но пока мы под BOLD понимаем двойную ширину (для совместимости с Set API)
     */
    public static final FontConfiguration PIRIT_2F = new FontConfiguration(true, PIRIT_2F_FONTS, ZERO_FONT_DEFAULT_GETTER);
    public static final FontConfiguration PIRIT_RB = new FontConfiguration(false, PIRIT_2F_FONTS, ZERO_FONT_DEFAULT_GETTER);
    public static final FontConfiguration PIRIT_2F_NARROW = new FontConfiguration(true, PIRIT_2F_NARROW_FONTS, ZERO_FONT_DEFAULT_GETTER);
    public static final FontConfiguration PIRIT_RB_NARROW = new FontConfiguration(false, PIRIT_2F_NARROW_FONTS, ZERO_FONT_DEFAULT_GETTER);

    /**
     * У 1Ф и Википринтов все отсуствующие четные шрифты мапятся как 0 шрифт, а нечетные как 1
     */
    private static final Function<Integer, Integer> EVEN_FONTS_DEFAULT_GETTER = input -> {
        if (input % 2 == 0) {
            return 0;
        } else {
            return 1;
        }
    };

    /**
     * У 1Ф есть жирный шрифт нормального размера - 8, но пока мы под BOLD понимаем двойную ширину (для совместимости с Set API)
     */
    public static final FontConfiguration PIRIT_1F = new FontConfiguration(false,
            ImmutableMap.of(
                    0, 43,
                    1, 55,
                    8, 43
            ), EVEN_FONTS_DEFAULT_GETTER);

    public static final FontConfiguration VIKIPRINT_80 = new FontConfiguration(true,
            ImmutableMap.of(
                    0, 48,
                    1, 64
            ), EVEN_FONTS_DEFAULT_GETTER);

    private final boolean canBeRequested;
    private final Map<Integer, Integer> defaultFonts;
    /**
     * Номер уменьшенного полужирного шрифта (есть только в 2Ф, в Википринте и 1Ф нет - для них включаем уменьшенный с двойной высотой)
     */
    private final int boldNormalFontNumber;
    /**
     * Номер полужирного шрифта (есть только в 2Ф и 1Ф, в Википринте нет)
     */
    private final int boldSmallFontNumber;
    private final Function<Integer, Integer> defaultNumberGetter;

    private FontConfiguration(boolean canBeRequested,
                             Map<Integer, Integer> defaultFonts,
                             Function<Integer, Integer> defaultNumberGetter) {
        this.canBeRequested = canBeRequested;
        this.defaultFonts = defaultFonts;
        this.boldNormalFontNumber = BOLD_AS_DOUBLE_WIDTH;
        this.boldSmallFontNumber = BOLD_AS_DOUBLE_WIDTH_SMALL;
        this.defaultNumberGetter = defaultNumberGetter;
    }

    public FontConfiguration(boolean canBeRequested,
                             Map<Integer, Integer> defaultFonts,
                             int boldNormalFontNumber,
                             int boldSmallFontNumber,
                             Function<Integer, Integer> defaultNumberGetter) {
        this.canBeRequested = canBeRequested;
        this.defaultFonts = defaultFonts;
        this.boldNormalFontNumber = boldNormalFontNumber;
        this.boldSmallFontNumber = boldSmallFontNumber;
        this.defaultNumberGetter = defaultNumberGetter;
    }

    public boolean isCanBeRequested() {
        return canBeRequested;
    }

    public Map<Integer, Integer> getDefaultFonts() {
        return defaultFonts;
    }

    public int getBoldNormalFontNumber() {
        return boldNormalFontNumber;
    }

    public int getBoldSmallFontNumber() {
        return boldSmallFontNumber;
    }

    public Function<Integer, Integer> getDefaultNumberGetter() {
        return defaultNumberGetter;
    }
}
