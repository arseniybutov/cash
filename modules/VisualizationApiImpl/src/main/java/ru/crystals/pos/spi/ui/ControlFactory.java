package ru.crystals.pos.spi.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import ru.crystals.pos.spi.ui.forms.Fonts;
import ru.crystals.pos.visualization.components.inputfield.InputFieldFlat;
import ru.crystals.pos.visualizationtouch.components.buttons.ButtonSimple;
import ru.crystals.pos.visualizationtouch.components.inputfield.CurrencyFormatter;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputField;

/**
 * Фабрика по производству контролов для UI.
 * @implNote нужна для того, чтобы все контролы были как можно более единообразны и инстанцировались из единого места.
 * Таким образом, в случае смены дизайна контрола, нет нужды искать все места, где этот контрол создаётся и настраивается.
 */
public class ControlFactory {
    private static final int BIG_DECIMAL_SCALE = 2;
    private static final long VALUE_MAX = 100000000000L;

    private static ControlFactory factory;

    /**
     * Возвращает экземпляр фабрики по производству контролов для UI.
     * @return
     */
    // Затем, чтобы не инстанцировать фабрику во всяких местах. И затем, чтобы один и тот же экземпляр фабрики мог дёргаться из разных мест в коде,
    // притом фабрика могла бы инстанцироваться параметрами, которые выясняются в рантайме. Из конфига, например. Здесь это не сделано, просто заделка на.
    public static ControlFactory getInstance() {
        if(factory == null) {
            factory = new ControlFactory();
        }
        return factory;
    }

    /**
     * Создаёт экземпляр {@link JLabel}.
     * @param text текст, который будет ему назначен
     * @param font шрифт. См. {@link ru.crystals.pos.spi.ui.forms.Fonts}
     * @param horizontalAlignment выравнивание. Возможные значения: {@link javax.swing.SwingConstants#LEFT}, {@link javax.swing.SwingConstants#RIGHT}.
     * @param textColor цвет текста. См. {@link ru.crystals.pos.visualization.components.ColorSchema}
     * @return новый экземпляр класса {@link JLabel}.
     */
    public JLabel createLabel(String text, Font font, int horizontalAlignment, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setHorizontalAlignment(horizontalAlignment);
        label.setForeground(textColor);
        return label;
    }

    /**
     * Создаёт экземпляр {@link JLabel}.
     * @see #createLabel(String, Font, int, Color)
     * @param text текст, который будет ему назначен.
     * @param font шрифт. См. {@link ru.crystals.pos.spi.ui.forms.Fonts}.
     * @param horizontalAlignment выравнивание. Возможные значения: {@link javax.swing.SwingConstants#LEFT}, {@link javax.swing.SwingConstants#RIGHT}.
     * @return новый экземпляр класса {@link JLabel}.
     */
    public JLabel createLabel(String text, Font font, int horizontalAlignment) {
        return createLabel(text, font, horizontalAlignment, ru.crystals.pos.visualization.styles.Color.secondTextColor);
    }

    /**
     * Создаёт контрол, отображающий изображение. Размер контрола равен размеру изображения.
     * @param image изображение, которое будет отображать контрол.
     * @return новый экземпляр {@link JLabel}, который отображает указанное изображение.
     */
    public JLabel createImage(ImageIcon image) {
        JLabel label = new JLabel(image);
        label.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
        return label;
    }

    /**
     * Создаёт поле для ввода текста.
     * @return новый экземпляр поля для ввода текста.
     */
    public <T> InputFieldFlat<T> createInputField() {
        InputFieldFlat<T> inputField = new InputFieldFlat<>(false);
        inputField.setFont(Fonts.INPUT_TEXT);
        inputField.setWelcomeFont(Fonts.INPUT_HINT);
        return inputField;
    }

    /**
     * Создаёт поле ввода валюты.
     * @return новый экземпляр поля ввода валюты.
     * @see CurrencyFormatter
     * @see InputField#getTextFormatter()
     * @see ru.crystals.pos.visualizationtouch.components.inputfield.Formatter
     */
    public InputFieldFlat<BigDecimal> createCurrencyInputField() {
        CurrencyFormatter formatter = new CurrencyFormatter(
                BIG_DECIMAL_SCALE,
                BigDecimal.valueOf(0L, BIG_DECIMAL_SCALE),
                BigDecimal.valueOf(VALUE_MAX, BIG_DECIMAL_SCALE)
        );
        InputFieldFlat<BigDecimal> inputField = createInputField();
        inputField.setTextFormatter(formatter);
        return inputField;
    }

    /**
     * Создаёт кнопку.
     * @param text текст кнопки
     * @return новый экземпляр кнопки.
     */
    public ButtonSimple createButton(String text) {
        ButtonSimple button = new ButtonSimple(text);
        button.setFont(Fonts.BUTTON_TEXT);
        button.setBackground(ru.crystals.pos.visualization.styles.Color.buttonBackGround);
        button.setForeground(ru.crystals.pos.visualization.styles.Color.buttonForeGround);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(200, 35));
        return button;
    }

    /**
     * Обрамляет тест html тегами (для лейблов).
     * @param text текст
     * @return текст
     */
    public static String textToHtml(String text) {
        if (text != null) {
            if (!text.startsWith("<html>")) {
                text = "<html>" + text;
            }
            if (!text.endsWith("</html>")) {
                text = text + "</html>";
            }
            return text;
        } else {
            return "<html></html>";
        }
    }
}