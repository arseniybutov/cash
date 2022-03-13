package ru.crystals.pos.spi.ui.forms;

import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.api.ui.listener.InputScanNumberFormListener;
import ru.crystals.pos.listeners.XBarcodeListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XMSRListener;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.inputfield.Formatter;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputField;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.KeyEvent;

/**
 * Форма ввода произвльного текста.
 * Отображает форму с полем ввода, заголовком и поясняющим тесктом, позволяющую пользователю ввести текст руками.
 */
public class InputForm extends ContextPanelWithCaption implements XBarcodeListener, XMSRListener {

    private JLabel messageLabel;
    /**
     * Поле ввода на формочке.
     */
    private InputField inputField;
    /**
     * Максимальная длина вводимого текста в символах.
     */
    private int maxInputLength = Integer.MAX_VALUE;
    /**
     * Слушатель события о завершении пользователем ввода.
     */
    private InputListener inputListener;
    private InputScanNumberFormListener scanListener;

    private boolean permitScanning = true;
    private boolean permitMSR = true;
    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link InputForm}.
     */
    public InputForm() {
        super();
        inputField = ControlFactory.getInstance().createInputField();
        messageLabel = ControlFactory.getInstance().createLabel("", Fonts.ITALIC_LARGE, SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        this.add(messageLabel, new ScaleRectangle(20, 85, WIDTH_DEFAULT - 40, 105));
        this.add(inputField, new ScaleRectangle(20, 194, WIDTH_DEFAULT - 40, 50));
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
        this.messageLabel.setText(ControlFactory.textToHtml(text));
    }

    /**
     * Возвращает форматтер, который использует поле ввода в приложении.
     * @return форматтер
     */
    public Formatter getFormatter() {
        return inputField.getTextFormatter();
    }

    /**
     * Устанавливает форматтер, который использует поле ввода в приложении.
     * @param formatter форматтер.
     */
    public void setFormatter(Formatter formatter) {
        inputField.setTextFormatter(formatter);
        inputField.updateUI();
    }

    /**
     * Возвращает слушатель окончания пользовательского ввода.
     * @return слушатель окончания пользовательского ввода.
     */
    public InputListener getInputListener() {
        return inputListener;
    }

    /**
     * Устанавливает слушатель окончания пользовательского ввода.
     * @param inputListener слушатель окончания пользовтельского ввода.
     */
    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    /**
     * Возвращает текст подсказки, которая отображается в поле ввода в случае, если оно пустое.
     * @return текст подсказки
     */
    public String getInputFieldHint() {
        return inputField.getWelcomeText();
    }

    /**
     * Устанавливает текст подсказки, которое отображается в поле ввода в случае, если оно пустое.
     * @param hint текст подсказки.
     */
    public void setInputFieldHint(String hint) {
        this.inputField.setWelcomeText(hint);
    }

    /**
     * Возвращает максимальную длину ввода в символах.
     * @return максимальная длина ввода в символах.
     */
    public int getMaxInputLength() {
        return maxInputLength;
    }

    /**
     * Возвращает флаг, определяющий, следует ли трактовать события сканирования как ввод.
     * @return true если следует и false в противном случае.
     */
    public boolean isPermitScanning() {
        return permitScanning;
    }

    /**
     * Устанавливает флаг, определяющий, следует ли трактовать события сканирования как ввод.
     * @param permitScanning true если следует и false в протвном случае.
     */
    public void setPermitScanning(boolean permitScanning) {
        this.permitScanning = permitScanning;
    }

    /**
     * Возвращает флаг, определяющий, разрешено ли форме отлавливать событие "Прокатана карта с магнитной полосой"<br>
     * @return true, если отлавливать событие разрешено и false в противном случае.
     */
    public boolean isPermitMSR() {
        return permitMSR;
    }

    /**
     * Устанавливает флаг, определяющий, разрешено ли форме отлавливать событие "Прокатана карта с магнитной полосой"<br>
     * @param permitMSR true, если отлавливать событие рашено и false в противном случае.
     */
    public void setPermitMSR(boolean permitMSR) {
        this.permitMSR = permitMSR;
    }

    public InputScanNumberFormListener getScanListener() {
        return scanListener;
    }

    public void setScanListener(InputScanNumberFormListener scanListener) {
        this.scanListener = scanListener;
    }

    /**
     * Устанавливает максимальную длину ввода в символах.
     * @param length максимальная длина ввода в символах.
     */
    public void setMaxInputLength(int length) {
        this.maxInputLength = length;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && inputListener != null) {
            inputListener.eventInputComplete(inputField.getText());
            return true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && inputListener != null) {
            if (!inputField.clear()) {
                inputListener.eventCanceled();
            }
            return true;
        }
        if(inputField.getText() != null && inputField.getText().length() < maxInputLength || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            return inputField.press(e.getKeyChar(), e.getKeyCode());
        }
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (!permitScanning) {
            // Просто проглатываем событие.
            return true;
        }
        if (scanListener != null) {
            scanListener.eventBarcodeScanned(barcode);
        } else {
            inputListener.eventInputComplete(barcode);
        }
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (!permitMSR) {
            // Проглатываем событие, возвращая true
            return true;
        }
        if (scanListener != null) {
            scanListener.eventMagneticStripeRead(track1, track2, track3, track4);
            return true;
        }
        return false;
    }
}
