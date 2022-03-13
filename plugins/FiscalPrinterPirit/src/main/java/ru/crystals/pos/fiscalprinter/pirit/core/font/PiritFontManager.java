package ru.crystals.pos.fiscalprinter.pirit.core.font;


import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextStyle;
import ru.crystals.pos.fiscalprinter.pirit.core.PiritPluginConfig;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Управление шрифтами Пиритов/википринтов: расчет максимального числа знаков, вычисление параметров для команд печати
 */
public class PiritFontManager {

    private final Logger log = LoggerFactory.getLogger(PiritFontManager.class);

    /**
     * Максимальное число знаков, когда все пошло не так
     */
    private static final int DEFAULT_MAX_CHAR = 42;

    /**
     * Номер дизайна, с которого начинаются загружаемые пользователем дизайны
     * (для них не работает команда {@link ExtendedCommand#GET_MAX_CHAR_FOR_FONT} - возвращает погоду на Марсе)
     */
    private static final int USER_DEFINED_DESIGN_NUMBER = 16;

    /**
     * Полубайт, хранящий номер шрифта
     */
    private static final int FONT_NUMBER_HALF_BYTE = 0b0000_1111;

    /**
     * Полубайт с атрибутами шрифта
     */
    private static final int ATTRIBUTES_HALF_BYTE = 0b1111_0000;

    static final int DOUBLE_WIDTH_BIT = 0b0010_0000;
    private static final int DOUBLE_HEIGHT_BIT = 0b0001_0000;
    private static final int UNDERLINE_BIT = 0b1000_0000;

    /**
     * Номер нормального шрифта
     */
    private static final int NORMAL_FONT = 0;
    /**
     * Номер уменьшенного шрифта
     */
    static final int SMALL_FONT = 1;

    private Map<Integer, Integer> userDefinedSize = Collections.emptyMap();

    /**
     * Максимальные длины строк для полного атрибута шрифта (включая биты двойной ширины/высоты и другие биты)
     */
    private final Map<Integer, Integer> preparedSizes = new HashMap<>();
    private final Map<Integer, Integer> requestedSizes = new HashMap<>();
    private boolean canBeRequested;
    private Map<Integer, Integer> defaultBaseFonts = Collections.emptyMap();
    private int defaultMaxChar;
    private PiritConfig piritConfig;
    private FontConfiguration fontConfiguration;


    public void configure(PiritConfig piritConfig, PiritPluginConfig config, FontConfiguration fontConfiguration, int designNumber) {
        this.piritConfig = piritConfig;
        this.fontConfiguration = fontConfiguration;
        if (MapUtils.isNotEmpty(config.getMaxCharRowMap())) {
            userDefinedSize = new HashMap<>(config.getMaxCharRowMap());
        }
        this.canBeRequested = fontConfiguration.isCanBeRequested() && designNumber < USER_DEFINED_DESIGN_NUMBER;

        defaultBaseFonts = fontConfiguration.getDefaultFonts();
        defaultMaxChar = defaultBaseFonts.getOrDefault(NORMAL_FONT, DEFAULT_MAX_CHAR);

        getByFullFontAttribute(NORMAL_FONT);
        getByFullFontAttribute(SMALL_FONT);
        getByFullFontAttribute(fontConfiguration.getBoldNormalFontNumber());
        getByFullFontAttribute(fontConfiguration.getBoldSmallFontNumber());
        getByFullFontAttribute(DOUBLE_WIDTH_BIT);
        getByFullFontAttribute(DOUBLE_HEIGHT_BIT);
    }

    private int getByFullFontAttribute(int fontAttribute) {
        return preparedSizes.computeIfAbsent(fontAttribute, key -> getByFontAttribute(fontAttribute));
    }

    private Integer getRequested(int fontAttribute) {
        final int result = requestedSizes.computeIfAbsent(fontAttribute, key -> piritConfig.getMaxCharCount(fontAttribute).orElse(-1));
        if (result == -1) {
            return null;
        }
        return result;
    }

    private int getByFontAttribute(int fullFontAttribute) {
        final Integer userDefinedSize = this.userDefinedSize.get(fullFontAttribute);
        if (userDefinedSize != null) {
            // если нашли полное совпадение с конфигом - то возвращаем его без вопросов
            log.debug("Found configured value {} for attribute {}", userDefinedSize, fullFontAttribute);
            return userDefinedSize;
        }
        int baseFontNumber = fullFontAttribute & FONT_NUMBER_HALF_BYTE;
        boolean hasAttributes = (fullFontAttribute & ATTRIBUTES_HALF_BYTE) != 0;

        if (!hasAttributes) {
            // если шрифт без атрибутов, то просто запрашиваем по базовому шрифту
            return requestOrGetDefault(baseFontNumber);
        }

        final Integer userDefinedBaseSize = this.userDefinedSize.get(baseFontNumber);
        if (userDefinedBaseSize != null) {
            // если для базового шрифта есть конфиг - то используем его
            boolean doubleWidth = ((fullFontAttribute & DOUBLE_WIDTH_BIT) == DOUBLE_WIDTH_BIT);
            final int converted = convert(userDefinedBaseSize, doubleWidth);
            log.debug("Found configured value {} for base font {}, converted to {} for attribute {}", userDefinedBaseSize, baseFontNumber,
                    converted, fullFontAttribute);
            return converted;
        }
        return requestOrGetDefault(fullFontAttribute);
    }

    private int convert(int size, boolean doubleWidth) {
        if (doubleWidth) {
            // Для шрифтов двойной ширины число знаков в два раза меньше (с округлением в меньшую сторону)
            return size / 2;
        }
        return size;
    }

    private int requestOrGetDefault(int fontAttribute) {
        if (!canBeRequested) {
            return getDefaultSize(fontAttribute);
        }
        final Integer maxCharCount = getRequested(fontAttribute);
        if (maxCharCount != null) {
            log.debug("Read value {} from device for attribute {}", maxCharCount, fontAttribute);
            return maxCharCount;
        }
        final int baseFontNumber = fontAttribute & FONT_NUMBER_HALF_BYTE;
        if (baseFontNumber == fontAttribute) {
            return getDefaultSize(fontAttribute);
        }

        Integer maxCharCountForBase = getRequested(baseFontNumber);
        if (maxCharCountForBase == null) {
            return getDefaultSize(fontAttribute);
        }
        final boolean doubleWidth = ((fontAttribute & DOUBLE_WIDTH_BIT) == DOUBLE_WIDTH_BIT);
        final int converted = convert(maxCharCountForBase, doubleWidth);
        log.debug("Read value {} from device for base font {}, converted to {} for attribute {}",
                maxCharCountForBase, baseFontNumber, converted, fontAttribute);
        return converted;
    }

    private int getDefaultSize(int fontAttribute) {
        final Integer defaultByFullAttribute = defaultBaseFonts.get(fontAttribute);
        if (defaultByFullAttribute != null) {
            return defaultByFullAttribute;
        }
        final boolean doubleWidth = ((fontAttribute & DOUBLE_WIDTH_BIT) == DOUBLE_WIDTH_BIT);
        final int baseFontNumber = fontAttribute & FONT_NUMBER_HALF_BYTE;

        final int size = getDefaultBaseFontSize(baseFontNumber);
        final int converted = convert(size, doubleWidth);
        if (baseFontNumber == fontAttribute) {
            log.debug("Used default value {} for attribute {}", size, fontAttribute);
        } else {
            log.debug("Used default value {} for base font {}, converted to {} for attribute {}", size, baseFontNumber, converted, fontAttribute);
        }
        return converted;
    }

    private int getDefaultBaseFontSize(int baseFontNumber) {
        final Integer defaultSize = defaultBaseFonts.get(baseFontNumber);
        if (defaultSize != null) {
            return defaultSize;
        }
        final int effectiveFontNumber = fontConfiguration.getDefaultNumberGetter().apply(baseFontNumber);
        return defaultBaseFonts.getOrDefault(effectiveFontNumber, defaultMaxChar);
    }

    public int getMaxCharRow(Integer extendedFont) {
        return getByFullFontAttribute(extendedFont);
    }

    public int getMaxCharRow(Text row) {
        return getByFullFontAttribute(getTextAttributes(row));
    }

    public int getMaxCharRow(Font font) {
        return getByFullFontAttribute(getFontAttribute(font));
    }

    public int getTextAttributes(Text text) {
        if (text.getConcreteFont() != null) {
            return text.getConcreteFont();
        }
        return calculateAttributes(text);
    }

    private int calculateAttributes(Text text) {
        int result = calculateBaseFont(text);
        if (text.getStyle() == TextStyle.LINE_TEXT) {
            result = result | UNDERLINE_BIT;
        }
        if (text.getSize() == TextSize.FULL_DOUBLE) {
            return result | DOUBLE_HEIGHT_BIT | DOUBLE_WIDTH_BIT;
        } else if (text.getSize() == TextSize.DOUBLE_HEIGHT) {
            return result | DOUBLE_HEIGHT_BIT;
        } else if (text.getSize() == TextSize.DOUBLE_WIDTH) {
            return result | DOUBLE_WIDTH_BIT;
        }
        return result;
    }

    private int calculateBaseFont(Text text) {
        final boolean bold = text.getStyle() == TextStyle.BOLD;
        if (text.getSize() == TextSize.SMALL) {
            return bold ? fontConfiguration.getBoldSmallFontNumber() : SMALL_FONT;
        }
        return bold ? fontConfiguration.getBoldNormalFontNumber() : NORMAL_FONT;
    }

    public int getFontAttribute(FontLine line) {
        if (line.getConcreteFont() != null) {
            return line.getConcreteFont();
        }
        return getFontAttribute(line.getFont());
    }

    private int getFontAttribute(Font font) {
        switch (font) {
            case SMALL:
                return SMALL_FONT;
            case DOUBLEHEIGHT:
                return DOUBLE_HEIGHT_BIT;
            case DOUBLEWIDTH:
                return DOUBLE_WIDTH_BIT;
            case UNDERLINE:
                return UNDERLINE_BIT;
            case NORMAL:
            default:
                return NORMAL_FONT;
        }
    }

}
