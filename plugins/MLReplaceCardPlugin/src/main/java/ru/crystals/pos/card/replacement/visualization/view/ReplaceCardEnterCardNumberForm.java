package ru.crystals.pos.card.replacement.visualization.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.components.Empty;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardEnterCardNumberForm extends CommonForm {

    private CommonProductInputPanel inputPanel;

    public ReplaceCardEnterCardNumberForm(XListener outerListener) {
        super(outerListener);
        inputPanel = new CommonProductInputPanel(CommonProductInputPanel.InputType.NUMBER_WITH_LEADING_ZEROES_ALLOWED, "", "");
        this.add(new Empty(600, 160));
        this.add(inputPanel, BorderLayout.SOUTH);
    }

    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if (Character.isDigit(e.getKeyChar()) || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            inputPanel.keyPressed(e);
            return true;
        }
        return e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_ESCAPE;
    }

    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    public void clear() {
        inputPanel.clear();
    }

    public String getEnteredValue() {
        return inputPanel.getTextValue();
    }

    void setText(String welcomeText, String labelText) {
        inputPanel.setLabelText(labelText);
        inputPanel.setWelcomeText(welcomeText);
    }

}
