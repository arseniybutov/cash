package ru.crystals.pos.spi.ui.forms.demo.showcase;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import ru.crystals.pos.api.ui.listener.CancelListener;
import ru.crystals.pos.api.ui.listener.ConfirmListener;
import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.api.ui.listener.TimeoutListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XKeyListenerInt;
import ru.crystals.pos.spi.IncorrectStateException;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.UIForms;
import ru.crystals.pos.spi.ui.forms.DialogForm;
import ru.crystals.pos.spi.ui.forms.MessageForm;
import ru.crystals.pos.spi.ui.forms.TimingOutForm;
import ru.crystals.pos.spi.ui.input.UIInputForms;
import ru.crystals.pos.spi.ui.payment.UIPaymentForms;
import ru.crystals.pos.visualization.styles.IconStyle;

public class ShowcaseFormManager implements UIForms {
    private JPanel showcaseContainer;
    /**
     * Заглушечная реализация событийной модели для ретрансляции формочкам клавиатурных событий.
     */
    private List<XKeyListenerInt> keyboardSubscribers = new ArrayList<>();

    private ShowcasePaymentFormManager paymentFormManager;
    private ShowcaseInputFormManager inputFormManager;

    private MessageForm messageForm = new MessageForm();
    private DialogForm dialogForm = new DialogForm();
    private TimingOutForm progressForm = new TimingOutForm();

    public ShowcaseFormManager(JPanel showcaseContainer) {
        this.showcaseContainer = showcaseContainer;
        this.inputFormManager = new ShowcaseInputFormManager(this.showcaseContainer);
        this.paymentFormManager = new ShowcasePaymentFormManager(this.showcaseContainer);

        this.showcaseContainer.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Blank
            }

            @Override
            public void keyPressed(KeyEvent e) {
                for(XKeyListenerInt subscriber : keyboardSubscribers) {
                    if(subscriber.keyPressedNew(new XKeyEvent(showcaseContainer, 0,0, 0, e.getKeyCode(), e.getKeyChar()))) {
                        break;
                    }
                }
                inputFormManager.onKeyPressed(e);
                paymentFormManager.onKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Blank
            }
        });
    }

    public void clearKeyboardSubscribers() {
        keyboardSubscribers.clear();
        inputFormManager.clearKeyboardSubscribers();
        paymentFormManager.clearKeyboardSubscribers();
    }

    @Override
    public UIInputForms getInputForms() {
        clearKeyboardSubscribers();
        return inputFormManager;
    }

    @Override
    public UIPaymentForms getPaymentForms() {
        clearKeyboardSubscribers();
        return paymentFormManager;
    }

    @Override
    public void showTimingOutForm(String message, int timeoutMs, TimeoutListener timeoutListener) {
        clearKeyboardSubscribers();
        progressForm.setMessage(message);
        progressForm.setTimeout(timeoutMs);
        progressForm.setTimeoutListener(timeoutListener);
        progressForm.start();
        replaceComponent(progressForm);
    }

    @Override
    public void showSpinnerForm(String s) throws IncorrectStateException {
        clearKeyboardSubscribers();
        keyboardSubscribers.add(messageForm);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.LOADING_BIG));
        messageForm.setMessage(s);
        messageForm.setCancelListener(null);
        messageForm.setConfirmListener(null);

        replaceComponent(messageForm);
    }

    @Override
    public void showSpinnerFormWithCancel(String s, CancelListener cancelListener) throws IncorrectStateException {
        clearKeyboardSubscribers();
        keyboardSubscribers.add(messageForm);
        messageForm.setConfirmListener(null);
        messageForm.setCancelListener(cancelListener);
        messageForm.setMessage(s);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.LOADING_BIG));
        replaceComponent(messageForm);
    }

    @Override
    public void showErrorForm(String s, ConfirmListener confirmListener) throws IncorrectStateException {
        clearKeyboardSubscribers();
        keyboardSubscribers.add(messageForm);
        messageForm.setMessage(s);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        messageForm.setConfirmListener(confirmListener);
        messageForm.setCancelListener(null);
        replaceComponent(messageForm);
    }

    @Override
    public void showDialogForm(DialogFormParameters dialogFormParameters, DialogListener dialogListener) throws IncorrectStateException {
        clearKeyboardSubscribers();
        keyboardSubscribers.add(dialogForm);
        dialogForm.setDialogListener(dialogListener);
        dialogForm.setModel(dialogFormParameters);
        dialogForm.setFocus(DialogForm.BUTTON_LEFT);
        replaceComponent(dialogForm);
    }

    @Override
    public void showMessageForm(String message, ConfirmListener confirmListener) throws IncorrectStateException {
        clearKeyboardSubscribers();
        keyboardSubscribers.add(messageForm);
        messageForm.setMessage(message);
        messageForm.setIcon(null);
        messageForm.setConfirmListener(confirmListener);
        messageForm.setCancelListener(null);
        replaceComponent(messageForm);
    }

    private void replaceComponent(JPanel component) {
        BorderLayout layout = (BorderLayout)showcaseContainer.getLayout();
        showcaseContainer.remove(layout.getLayoutComponent(BorderLayout.CENTER));
        showcaseContainer.add(component, BorderLayout.CENTER);
        showcaseContainer.revalidate();
        showcaseContainer.repaint();
    }
}
