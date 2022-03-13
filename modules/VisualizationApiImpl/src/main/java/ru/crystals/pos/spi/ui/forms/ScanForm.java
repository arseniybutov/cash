package ru.crystals.pos.spi.ui.forms;

import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import ru.crystals.pos.api.ui.listener.ScanFormListener;
import ru.crystals.pos.listeners.XBarcodeListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.spi.ui.utils.TextWrapper;
import ru.crystals.pos.visualization.utils.ScaleRectangle;

/**
 * Форма сканирования.<br/>
 * Отображает пользователю окно с заголовком и поясняющим текстом, которое слушает события сканирования
 * баркода и отмены действия.
 */
public class ScanForm extends ContextPanelWithCaption implements XBarcodeListener {

    private JLabel messageLabel;
    private ScanFormListener scanFormListener;

    public ScanForm() {
        super();
        messageLabel = ControlFactory.getInstance().createLabel("", Fonts.ITALIC_LARGE, SwingConstants.CENTER);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
        this.add(messageLabel, new ScaleRectangle(60, 60, WIDTH_DEFAULT - 120, HEIGHT_DEFAULT - 60));
    }

    /**
     * Возвращает поясняющий текст, отображаемый на форме.
     * @return текст с формы.
     */
    public String getText() {
        return messageLabel.getText();
    }

    /**
     * Устанавливает поясняющий текст, отображаемый на форме.
     * @param text отображаемый на форме текст.
     */
    public void setText(String text) {
        TextWrapper.wrapLabelText(messageLabel, text);
    }

    /**
     * Возвращает слушатель событий этой формы.
     * @return
     */
    public ScanFormListener getScanFormListener() {
        return scanFormListener;
    }

    /**
     * Устанавливает слушатель событий этой формы.
     * @param scanFormListener слушатель событий.
     */
    public void setScanFormListener(ScanFormListener scanFormListener) {
        this.scanFormListener = scanFormListener;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if(scanFormListener != null) {
            scanFormListener.eventBarcodeScanned(barcode);
        }
        return true;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && scanFormListener != null) {
            scanFormListener.eventCanceled();
        }
        // Просто проглатываем событие, чтобы оно не ушло глубже в стек формочек.
        return true;
    }
}
