package ru.crystals.pos.visualization.products.mobilepay;

import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.InputPanelFlat;
import ru.crystals.pos.visualization.input.InputPanelFlat.InputStyle;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.FormatHelper;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.JLabel;

public class MobilePayProductPaymentPanel extends VisualPanel {

    private static final long serialVersionUID = 1L;
    private JLabel jCodeLabel = null;
    private JLabel jNameLabel = null;
    private JLabel jNumberLabel = null;
    private JLabel jPhoneNumberLabel = null;
    private JLabel jPhoneLabel1 = null;
    private JLabel jPhoneLabel2 = null;
    private InputPanelFlat inputPanel = null;
    private JLabel jExpand2 = null;

    public MobilePayProductPaymentPanel() {
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        inputPanel = new InputPanelFlat(ResBundleGoodsMobilePay.getString("PAYMENT_AMOUNT"), ResBundleGoodsMobilePay.getString("ENTER_PAYMENT"), InputStyle.TEXT);
        jNameLabel = new JLabel();
        Style.setNameLabelStyle(jNameLabel);
        jCodeLabel = new JLabel();
        Style.setCodeLabelStyle(jCodeLabel);
        jNumberLabel = new JLabel();
        jNumberLabel.setText(ResBundleGoodsMobilePay.getString("NUMBER"));
        Style.setNumberLabelStyle(jNumberLabel);
        jPhoneLabel1 = new JLabel();
        jPhoneLabel1.setPreferredSize(new ScaleDimension(320, 50));
        jPhoneLabel2 = new JLabel();
        jPhoneLabel2.setPreferredSize(new ScaleDimension(320, 60));
        jPhoneNumberLabel = new JLabel();
        Style.setPhoneNumberLabelStyle(jPhoneNumberLabel);
        this.setSize(Size.middlePanel);
        this.setPreferredSize(Size.middlePanel);
        this.add(new Empty(640, 9), null);
        this.add(jCodeLabel, null);
        this.add(jNameLabel, null);
        this.add(jNumberLabel, null);
        this.add(jPhoneLabel1, null);
        this.add(jPhoneNumberLabel, null);
        this.add(jPhoneLabel2, null);
        this.add(new Empty(640, 9), null);
        jExpand2 = new Empty(600, 119);
        jExpand2.setVisible(false);
        this.add(jExpand2, null);
        this.add(inputPanel, null);
    }

    public void setItem(String item) {
        jCodeLabel.setText(item);
    }

    public void setName(String name) {
        jNameLabel.setText(name);
    }

    public void setSumma(Double summa) {
        inputPanel.setField(FormatHelper.formatInputSumma(summa));
    }

    public void setPhoneNumber(String phone) {
        jPhoneNumberLabel.setText(FormatHelper.formatPhone(phone));
    }

    public InputPanelFlat getInputPanel() {
        return inputPanel;
    }

    public void expand() {
        jExpand2.setVisible(true);
        this.setPreferredSize(new ScaleDimension(640, 360));
    }

    public void collapse() {
        jExpand2.setVisible(false);
        this.setPreferredSize(Size.middlePanel);
    }

    public void setNotEditing(boolean b) {
        inputPanel.setNotEditing(b);
    }
}
