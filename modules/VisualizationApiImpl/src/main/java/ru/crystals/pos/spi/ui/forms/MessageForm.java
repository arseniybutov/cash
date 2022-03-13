package ru.crystals.pos.spi.ui.forms;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import ru.crystals.pos.api.ui.listener.CancelListener;
import ru.crystals.pos.api.ui.listener.ConfirmListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.spi.ui.utils.TextWrapper;
import ru.crystals.pos.visualization.utils.ScaleRectangle;

/**
 * Форма отображения информационного сообщения на контекстной панели кассы.
 */
public class MessageForm extends ContextPanelBase {
    private static final int BORDER_PADDING_PX = 3;
    private static final int PADDING_PX = 60;
    private ConfirmListener confirmListener;
    private CancelListener cancelListener;
    private JLabel iconLabel;
    private JLabel messageLabel;
    private boolean modal = false;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link MessageForm}.
     */
    public MessageForm() {
        super();
        messageLabel = ControlFactory.getInstance().createLabel(
                "",
                Fonts.ITALIC_LARGE,
                SwingConstants.CENTER
        );
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(new EmptyBorder(BORDER_PADDING_PX, BORDER_PADDING_PX, BORDER_PADDING_PX, BORDER_PADDING_PX));
        this.add(messageLabel, new ScaleRectangle(PADDING_PX, 0, WIDTH_DEFAULT - 120, HEIGHT_DEFAULT));
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link MessageForm}.
     * @param icon иконка, которая будет отображаться в этом сообщении. null, если иконка не требуется.
     * @param message сообщение, которое требуется отобразить на форме.
     */
    public MessageForm(ImageIcon icon, String message) {
        this();
        this.setIcon(icon);
        this.setMessage(message);
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link MessageForm}.
     * @see MessageForm#MessageForm(ImageIcon, String, CancelListener)
     * @param icon иконка информационного сообщения
     * @param message текст информационного сообщения
     * @param confirmListener слушатель нажатия клавиши "ВВОД" на клавиатуре.
     *
     */
    public MessageForm(ImageIcon icon, String message, ConfirmListener confirmListener) {
        this(icon, message);
        this.confirmListener = confirmListener;
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link MessageForm}.
     * @see MessageForm#MessageForm(ImageIcon, String, ConfirmListener)
     * @param icon иконка информационного сообщения
     * @param message текст информационного сообщения
     * @param cancelListener слушатель нажатия клавиши "ОТМЕНА" на клавиатуре.
     */
    public MessageForm(ImageIcon icon, String message, CancelListener cancelListener) {
        this(icon, message);
        this.cancelListener = cancelListener;
    }

    /**
     * Возвращает слушатель положительного выбора.
     * @return слушатель положительного выбора
     */
    public ConfirmListener getConfirmListener() {
        return confirmListener;
    }

    /**
     * Устанавливает слушатель положительного выбора
     * @param confirmListener слушатель положительного выбора.
     */
    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    /**
     * Возвращает слушатель отрицательного выбора.
     * @return слушатель отрицательного выбора
     */
    public CancelListener getCancelListener() {
        return cancelListener;
    }

    /**
     * Устанавливает слушатель отрицательного выбора
     * @param cancelListener слушатель отрицательного выбора
     */
    public void setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    /**
     * Возврашает поясняющее сообщение, расположенное на форме
     * @return поясняющее сообщение
     */
    public String getMessage() {
        return messageLabel.getText();
    }

    /**
     * Устанавливает поясняющее сообщение, расположенное на форме.
     * @param message поясняющее сообщение
     */
    public void setMessage(String message) {
        TextWrapper.wrapLabelText(this.messageLabel, message);
    }

    /**
     * Возвращает иконку окна
     * @return иконка окна
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

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
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
                new Rectangle(
                        20,
                        (HEIGHT_DEFAULT / 2) - (iconLabel.getIcon().getIconHeight() / 2),
                        iconLabel.getIcon().getIconWidth() + 5, /* Иначе почему-то иконка обрезается слева */
                        iconLabel.getIcon().getIconHeight()
                )
        );
        this.remove(messageLabel);
        this.add(messageLabel, new ScaleRectangle(100, 0, WIDTH_DEFAULT - 120, HEIGHT_DEFAULT));
    }

    private void removeIcon() {
        if(iconLabel != null) {
            this.remove(iconLabel);
            iconLabel = null;
            this.remove(messageLabel);
            this.add(messageLabel, new ScaleRectangle(PADDING_PX, 0, WIDTH_DEFAULT - 120, HEIGHT_DEFAULT));
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if(cancelListener != null && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelListener.eventCanceled();
            return true;
        }
        if(confirmListener != null && e.getKeyCode() == KeyEvent.VK_ENTER) {
            confirmListener.eventConfirmed();
            return true;
        }
        return modal;
    }
}
