package ru.crystals.pos.spi.ui.utils;

import java.awt.Font;
import javax.swing.JLabel;
import org.apache.commons.lang.StringUtils;

/**
 * Утилита для переноса текста в {@link JLabel}.
 * Из коробки JLabel в перенос текста не умеет, нужно обрамлять текст в &lt;html&gt;&lt;/html&gt;
 * Тогда перенос текста становится доступным, но теряется шрифт, назначенный JLabel. Этот утилитный класс
 * форматирует текст так, чтобы были доступны переносы с сохранением шрифта притом.
 */
public class TextWrapper {

    private TextWrapper() {

    }

    /**
     * Устанавливает текст с переносами и сохранением шрифта для указанного
     * экземпляра {@link JLabel}.
     * @param label экземпляр {@link JLabel}, форматированный текст которому нужно установить.
     * @param text текст, который следует отформатировать и установить текстом у указанного экземпляра {@link JLabel}.
     */
    public static void wrapLabelText(JLabel label, String text) {
        label.setText(wrapText(text, label.getFont()));
    }

    /**
     * Форматирует текст так, чтобы он был пригоден для установки в {@link JLabel}
     * с сохранением шрифтов и переноса по словам притом.
     * @param text текст, который следует отфоратировать
     * @param font шрифт, которым следует рисовать текст.
     * @return отформатированный текст, пригодный для установки в JLabel с поддержкой переноса и шрифтов.
     */
    public static String wrapText(String text, Font font) {
        if(StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder real = new StringBuilder("<html><body style='width:100%'><font face='")
        .append(font.getFontName()).append("'>")
        .append(text)
        .append("</font></body></html>");
        return real.toString();
    }
}
