package ru.crystals.pos.spi.ui.forms;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.visualization.utils.ScaleRectangle;

/**
 * Форма контекстной панели с заголовком.
 */
public abstract class ContextPanelWithCaption extends ContextPanelBase {
    /**
     * Отображалка заголовка формы.
     */
    protected JLabel formCaptionLabel;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link ContextPanelWithCaption}.
     */
    public ContextPanelWithCaption() {
        super();
        formCaptionLabel = ControlFactory.getInstance().createLabel("Lower Form Caption goes here", Fonts.ITALIC_LARGE, SwingConstants.LEFT);
        formCaptionLabel.setVerticalAlignment(SwingConstants.TOP);
        this.add(formCaptionLabel, new ScaleRectangle(20, 10, WIDTH_DEFAULT - 40, 75));
    }

    /**
     * Возвращает заголовок формы.
     * @return заголовок формы.
     */
    public String getCaption() {
        return formCaptionLabel.getText();
    }

    /**
     * Устанавливает заголовок формы.
     * @param caption заголовок формы.
     */
    public void setCaption(String caption) {
        this.formCaptionLabel.setText(ControlFactory.textToHtml(caption));
    }
}
