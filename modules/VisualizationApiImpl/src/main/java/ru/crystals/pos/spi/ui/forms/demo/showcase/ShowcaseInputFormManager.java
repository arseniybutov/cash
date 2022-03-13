package ru.crystals.pos.spi.ui.forms.demo.showcase;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.api.ui.listener.InputScanNumberFormListener;
import ru.crystals.pos.api.ui.listener.ScanFormListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XKeyListenerInt;
import ru.crystals.pos.spi.IncorrectStateException;
import ru.crystals.pos.spi.ui.forms.InputForm;
import ru.crystals.pos.spi.ui.forms.ScanForm;
import ru.crystals.pos.spi.ui.forms.SelectionForm;
import ru.crystals.pos.spi.ui.input.UIInputForms;
import ru.crystals.pos.visualizationtouch.components.inputfield.MaskFormatter;
import ru.crystals.pos.visualizationtouch.components.inputfield.NumberFormatter;


public class ShowcaseInputFormManager implements UIInputForms {
    /**
     * Контейнер для формочек визуализации.
     */
    private JPanel parentContainer;
    /**
     * Заглушечная реализация событийной модели для ретрансляции формочкам клавиатурных событий.
     */
    private List<XKeyListenerInt> keyboardSubscribers = new ArrayList<>();

    public ShowcaseInputFormManager(JPanel container) {
        parentContainer = container;
    }

    public void onKeyPressed(KeyEvent e) {
        for(XKeyListenerInt subscriber : keyboardSubscribers) {
            subscriber.keyPressedNew(new XKeyEvent(parentContainer, 0,0, 0, e.getKeyCode(), e.getKeyChar()));
        }
    }

    public void clearKeyboardSubscribers() {
        keyboardSubscribers.clear();
    }

    @Override
    public void showScanForm(String caption, String text, ScanFormListener listener) throws IncorrectStateException {
        ScanForm sc = new ScanForm();
        sc.setCaption(caption);
        sc.setText(text);
        sc.setScanFormListener(listener);
        keyboardSubscribers.clear();
        keyboardSubscribers.add(sc);
        replaceComponent(sc);
    }

    @Override
    public void showInputNumberForm(String caption, String text, String inputFieldHint, int maxLength, InputListener listener) throws IncorrectStateException {
        InputForm form = new InputForm();
        form.setCaption(caption);
        form.setText(text);
        form.setInputFieldHint(inputFieldHint);
        form.setMaxInputLength(maxLength);
        form.setFormatter(new NumberFormatter());
        form.setInputListener(listener);
        keyboardSubscribers.clear();
        keyboardSubscribers.add(form);
        replaceComponent(form);
    }

    @Override
    public void showInputScanNumberForm(String caption, String text, String inputFieldHint, int maxLength, InputScanNumberFormListener listener) throws IncorrectStateException {
        showInputNumberForm(caption, text, inputFieldHint, maxLength, listener);
    }

    @Override
    public void showPatternInputForm(String caption, String text, String defaultValue, String inputHint, String pattern, InputListener listener) throws IncorrectStateException {
        InputForm form = new InputForm();
        form.setCaption(caption);
        form.setText(text);
        form.setInputFieldHint(inputHint);
        form.setFormatter(new MaskFormatter(pattern));
        form.getFormatter().setValue(defaultValue);
        form.setInputListener(listener);
        keyboardSubscribers.clear();
        keyboardSubscribers.add(form);
        replaceComponent(form);
    }

    @Override
    public void showSelectionForm(String caption, Map<String, List<String>> items, InputListener listener) throws IncorrectStateException {
        SelectionForm selectionForm = new SelectionForm(items, listener);
        selectionForm.setCaption(caption);
        replaceComponent(selectionForm);
    }

    private void replaceComponent(JPanel component) {
        BorderLayout layout = (BorderLayout)parentContainer.getLayout();
        parentContainer.remove(layout.getLayoutComponent(BorderLayout.CENTER));
        parentContainer.add(component, BorderLayout.CENTER);
        parentContainer.revalidate();
        parentContainer.repaint();
    }

}
