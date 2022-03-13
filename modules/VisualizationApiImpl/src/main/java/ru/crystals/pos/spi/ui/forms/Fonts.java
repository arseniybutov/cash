package ru.crystals.pos.spi.ui.forms;

import java.awt.Font;
import ru.crystals.pos.visualizationtouch.controls.font.MyriadFont;

/**
 * Коллекция шрифтов, используемых в приложении.
 * По возможности, воздержитесь от использования шрифтов не из коллекции.
 */
public class Fonts {
    private static final float SIZE_EXTRA_LARGE = 71f;
    private static final float SIZE_LARGE = 30f;
    private static final float SIZE_MEDIUM = 28f;
    private static final float SIZE_SMALL = 22f;

    /**
     * Самый большой обычный шрифт.
     */
    public static final Font REGULAR_EXTRA_LARGE = MyriadFont.getRegular(SIZE_EXTRA_LARGE);
    /**
     * Большой обычный шрифт.
     */
    public static final Font REGULAR_LARGE = MyriadFont.getRegular(SIZE_LARGE);

    /**
     * Обычный средний шрифт.
     */
    public static final Font REGULAR_MEDIUM = MyriadFont.getRegular(SIZE_MEDIUM);

    /**
     * Большой курсивный шрифт. Он такой же, как большой обычный, только курсивный.
     */
    public static final Font ITALIC_LARGE = MyriadFont.getItalic(SIZE_LARGE);
    /**
     * Маленький курсивный шрифт.
     */
    public static final Font ITALIC_SMALL = MyriadFont.getItalic(SIZE_SMALL);

    /**
     * Шрифт для подписей кнопок.
     */
    public static final Font BUTTON_TEXT = MyriadFont.getRegular(24f);

    /**
     * Шрифт для вводимого текста.
     */
    public static final Font INPUT_TEXT = MyriadFont.getRegular(44f);

    /**
     * Шрифт для текстовой подсказки, которая отображается в текстовом поле в случае, когда оно пустое.
     */
    public static final Font INPUT_HINT = MyriadFont.getRegular(44f);

    private Fonts() {
        // Nothing to see here
    }
}
