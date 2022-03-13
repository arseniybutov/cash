package ru.crystals.pos.spi.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.utils.TextWrapper;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.buttons.ButtonSimple;
import ru.crystals.pos.visualizationtouch.components.buttons.ButtonsGroup;

/**
 * Форма диалогового окна. Показывает пользователю текст и две кнопки.
 * Левая кнопка получает фокус по умолчанию
 * При нажатии на клавишу "ВВОД", выбором пользователя считается кнопка в фокусе
 * При нажатии на клавишу "ОТМЕНА" происходит отмена диалога.
 */
public class DialogForm extends ContextPanelBase {
    /**
     * Идентификатор левой кнопки в диалоге.
     * @see #BUTTON_RIGHT
     */
    public static final int BUTTON_LEFT = 0;
    /**
     * Идентификатор правой кнопки в диалоге.
     * @see #BUTTON_LEFT
     */
    public static final int BUTTON_RIGHT = 1;

    private ButtonsGroup buttonsGroup = new ButtonsGroup(ButtonsGroup.AXIS_HORIZONTAL, 5, 5);

    private JLabel iconLabel;
    private JLabel dialogLabel;
    private ButtonSimple leftButton;
    private ButtonSimple rightButton;

    private DialogListener dialogListener;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link DialogForm}
     */
    public DialogForm() {
        super();
        dialogLabel = ControlFactory.getInstance().createLabel(
                "",
                Fonts.ITALIC_LARGE,
                SwingConstants.CENTER
        );
        dialogLabel.setHorizontalAlignment(SwingConstants.LEFT);
        dialogLabel.setVerticalAlignment(SwingConstants.CENTER);
        this.add(dialogLabel, new ScaleRectangle(100, 20, 480, 190));
        leftButton = ControlFactory.getInstance().createButton("");
        rightButton = ControlFactory.getInstance().createButton("");
        buttonsGroup.addButton(leftButton, this::onPositiveScenarioSelected, false);
        buttonsGroup.addButton(rightButton, this::onNegativeScenarioSelected, false);

        this.add(buttonsGroup, new ScaleRectangle(10, HEIGHT_DEFAULT - 10 - 35,WIDTH_DEFAULT - 20, 35));
        setFocus(DialogForm.BUTTON_LEFT);
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link DialogForm}.
     * @param model модель данных для отображения
     * @param dialogListener слушатель пользовательского выбора
     */
    public DialogForm(DialogFormParameters model, DialogListener dialogListener) {
        this();
        this.dialogListener = dialogListener;
        setModel(model);
    }

    /**
     * Устанавливает новую модель данных для отображения на форме.
     * @param model модель данных для отображения на форме.
     */
    public void setModel(DialogFormParameters model) {
        leftButton.setText(model.getButton1Text());
        rightButton.setText(model.getButton2Text());
        this.setText(model.getMessage());
        setFocus(DialogForm.BUTTON_LEFT);
    }

    /**
     * Устанавливает фокус на кнопки в диалоговом окне.
     * @param button идентификатор кнопки на диалоговом окне. Должна быть одной из констант {@link #BUTTON_LEFT}, {@link #BUTTON_RIGHT}.
     */
    public void setFocus(int button) {
        if(BUTTON_RIGHT == button) {
            buttonsGroup.selectButton(rightButton);
            return;
        }
        if(BUTTON_LEFT == button) {
            buttonsGroup.selectButton(leftButton);
        }
    }

    private void onPositiveScenarioSelected(ActionEvent e) {
        if(dialogListener != null) {
            dialogListener.eventButton1pressed();
        }
    }

    private void onNegativeScenarioSelected(ActionEvent e) {
        if(dialogListener != null) {
            dialogListener.eventButton2pressed();
        }
    }

    /**
     * Возвращает слушатель пользовательского ввода.
     * @return слущатель пользовательского ввода.
     */
    public DialogListener getDialogListener() {
        return dialogListener;
    }

    /**
     * Устанавливает слушатель пользовательского ввода.
     * @param choiceListener слушатель пользовательского ввода
     */
    public void setDialogListener(DialogListener choiceListener) {
        this.dialogListener = choiceListener;
    }

    /**
     * Возвращает текст диалогового сообщения.
     * @return текст диалогового сообщения
     */
    public String getText() {
        return this.dialogLabel.getText();
    }

    /**
     * Устанавливает текст диалогового сообщения.
     * @param text текст диалогового сообщения
     */
    public void setText(String text) {
        TextWrapper.wrapLabelText(dialogLabel, text);
    }

    /**
     * Возвращает текст левой кнопки на диалоговом окне.
     * @return текст левой кнопки.
     */
    public String getButton1Text() {
        return leftButton.getText();
    }

    /**
     * Устанавливает текст левой кнопки на диалоговом окне.
     * @param text текст левой кнопки
     */
    public void setButton1Text(String text) {
        this.leftButton.setText(text);
    }

    /**
     * Возвращает текст правой кнопки на диалоговом окне.
     * @return текст правой кнопки.
     */
    public String getButton2Text() {
        return rightButton.getText();
    }

    /**
     * Устанавливает текст правой кнопки на диалоговом окне.
     * @param text текст правой кнопки.
     */
    public void setButton2Text(String text) {
        this.rightButton.setText(text);
    }

    /**
     * Возвращает иконку диалогового окна.
     * @return иконка диалогового окна.
     */
    public ImageIcon getIcon() {
        if(iconLabel == null) {
            return null;
        }
        return (ImageIcon)iconLabel.getIcon();
    }

    /**
     * Устанавливает иконку окна.
     * @param icon иконка окна или null, если иконка не требуется.
     */
    public void setIcon(ImageIcon icon) {
        updateIcon(icon);
    }

    private void updateIcon(ImageIcon icon) {
        if(icon == null) {
            if(iconLabel != null) {
                removeIcon();
            }
        } else {
            addIcon(icon);
        }
    }

    private void addIcon(ImageIcon icon) {
        if(iconLabel != null) {
            removeIcon();
        }
        iconLabel = ControlFactory.getInstance().createImage(icon);
        this.add(iconLabel,
                new ScaleRectangle(
                        20,
                        (HEIGHT_DEFAULT / 2) - (iconLabel.getIcon().getIconHeight() / 2),
                        iconLabel.getIcon().getIconWidth() + 5, /* Иначе почему-то иконка обрезается слева */
                        iconLabel.getIcon().getIconHeight()
                )
        );
    }

    private void removeIcon() {
        if(iconLabel != null) {
            this.remove(iconLabel);
            iconLabel = null;
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                buttonsGroup.selectNext();
                break;
            case KeyEvent.VK_LEFT:
                buttonsGroup.selectPrevious();
                break;
            case KeyEvent.VK_ESCAPE:
                if(dialogListener != null) {
                    dialogListener.eventCanceled();
                    return true;
                }
                break;
            case KeyEvent.VK_ENTER:
                if(buttonsGroup.getCurrentButton() != null) {
                    buttonsGroup.getCurrentButton().doClick();
                } else {
                    onPositiveScenarioSelected(null);
                }
                break;
        }
        return false;
    }
}
