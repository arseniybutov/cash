package ru.crystals.pos.visualization.products.mobilepay;


import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.InputPanelFlat;
import ru.crystals.pos.visualization.input.InputPanelFlat.InputStyle;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.*;


public class MobilePayProductPhonePanel extends VisualPanel {

    private static final long serialVersionUID = 1L;
    private JLabel jCodeLabel = null;
    private JLabel jNameLabel = null;
    private InputPanelFlat inputPanel = null;

    /**
     * This method initializes
     */
    public MobilePayProductPhonePanel() {
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        inputPanel = new InputPanelFlat(ResBundleGoodsMobilePay.getString("PHONE_NUMBER"), ResBundleGoodsMobilePay.getString("ENTER_PHONE_NUMBER"), InputStyle.PHONE);
        jNameLabel = new JLabel();
        Style.setNameLabelStyle(jNameLabel);
        jCodeLabel = new JLabel();
        Style.setCodeLabelStyle(jCodeLabel);
        this.add(new Empty(640, 9), null);
        this.add(jCodeLabel, null);
        this.add(jNameLabel, null);
        this.add(new Empty(640, 119));
        this.add(inputPanel);
        this.setPreferredSize(Size.middlePanel);
    }

    public void setPhoneNumber(String phoneNumber) {
        inputPanel.setField(phoneNumber);
    }

    public void setItem(String item) {
        jCodeLabel.setText(item);
    }

    public void setName(String name) {
        jNameLabel.setText(name);
    }
}
