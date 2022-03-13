package ru.crystals.pos.visualization.login.password;

import ru.crystals.pos.keyboard.Keyboard;
import ru.crystals.pos.keyboard.KeyboardLock;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.input.InputPanelFlat;
import ru.crystals.pos.visualization.input.InputPanelFlat.InputStyle;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.ScaleFont;
import ru.crystals.pos.visualizationtouch.components.ElementFactory.AligmentX;
import ru.crystals.pos.visualizationtouch.components.ElementFactory.AligmentY;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;

/**
 * @author vmorzhantsev
 */
public class PasswordLoginComponent extends VisualPanel {

    private static final long serialVersionUID = 1L;
    private static final int WARNING_LABEL_WIDTH = 558;
    private JLabel shiftLabel;
    private Label warningLabel;
    private JLabel shiftNumberLabel;
    private JLabel checkPresentLabel;
    private JLabel jEmpty;
    private InputPanelFlat inputPanel;
    private JPanel topPanel;
    private JPanel topPanel3;
    private JLabel fiscalError;
    private Keyboard keyboard;

    public PasswordLoginComponent() {
        keyboard = Factory.getInstance().getKeyboard();
        inputPanel = new InputPanelFlat(ResBundleVisualization.getString("ENTER_PASSWORD_LOGIN"),
                ResBundleVisualization.getString("ENTER_PASSWORD"), InputStyle.TEXT);
        inputPanel.getInputField().setPasswordMode(true);
        Font font = inputPanel.getInputField().getFont();
        inputPanel.getInputField().setFont(ScaleFont.decorate(font));
        warningLabel = new Label();
        Style.setWarningLabelStyle(warningLabel);
        warningLabel.setPreferredSize(new ScaleDimension(WARNING_LABEL_WIDTH, 70));
        warningLabel.setAligmentY(AligmentY.Y_ALIGMENT_CENTER);
        warningLabel.setText(ResBundleVisualization.getString("LOGIN_FAILED"));
        warningLabel.setVisible(false);
        warningLabel.setAligmentX(AligmentX.X_ALIGMENT_LEFT);
        jEmpty = new Empty(1, 1);
        jEmpty.setPreferredSize(new ScaleDimension(598, 70));
        this.setPreferredSize(new ScaleDimension(640, 450));

        topPanel = new JPanel();
        topPanel.setOpaque(true);
        topPanel.setLayout(new BorderLayout());
        topPanel.setPreferredSize(new ScaleDimension(610, 50));
        topPanel.setBackground(Color.greyBackground);

        JPanel topPanel2 = new JPanel();
        topPanel2.setOpaque(true);
        topPanel2.setLayout(new BorderLayout());
        topPanel2.setPreferredSize(new ScaleDimension(620, 40));
        topPanel2.setBackground(Color.greyBackground);

        topPanel3 = new JPanel();
        topPanel3.setOpaque(true);
        topPanel3.setLayout(new BorderLayout());
        topPanel3.setPreferredSize(new ScaleDimension(620, 190));
        topPanel3.setBackground(Color.greyBackground);

        fiscalError = new JLabel();
        Style.setShiftNumberLabel(fiscalError);
        topPanel3.add(fiscalError);

        JPanel mainPanel = new JPanel();
        topPanel.setOpaque(true);
        mainPanel.setLayout(new FlowLayout());
        mainPanel.setBackground(Color.greyBackground);
        mainPanel.setPreferredSize(new ScaleDimension(620, 280));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(Scale.getY(3), Scale.getX(12), Scale.getY(0), Scale.getX(0)));
        mainPanel.add(topPanel);
        mainPanel.add(topPanel2);
        mainPanel.add(topPanel3);

        shiftLabel = new JLabel();
        shiftLabel.setVisible(false);
        shiftLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        Style.setWarningLabelStyle(shiftLabel);
        shiftLabel.setPreferredSize(new ScaleDimension(60, 40));

        addShiftNumberLabel(topPanel);
        addCheckPresentLabelLabel(topPanel2);

        setPasswordDefaultText();

        topPanel.add(shiftLabel, BorderLayout.EAST);
        this.add(mainPanel);
        this.add(warningLabel);
        this.add(jEmpty);
        this.add(inputPanel);

        initKeyboardLockerListener();
    }

    private KeyboardLock keyboardLock;

    // В экране ввода пароля клавиатура должна работать всегда
    // а после логина возвращать блокировку в прежнее состояние
    private void initKeyboardLockerListener(){
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                keyboardLock = keyboard.getKeyboardLock();
                if (PasswordLoginComponent.this.isShowing()) {
                    keyboard.unlockKeyboard();
                } else if (keyboardLock != null) {
                    keyboard.lockKeyboard(keyboardLock, "PasswordLoginComponent.initKeyboardLockerListener");
                }
            }
        });
    }

    private void addShiftNumberLabel(JPanel topPanel) {
        shiftNumberLabel = new JLabel();
        Style.setShiftNumberLabel(shiftNumberLabel);
        topPanel.add(shiftNumberLabel, BorderLayout.WEST);
    }

    public void updateShiftLabelText() {
        if (Factory.getTechProcessImpl().isShiftOpen()) {
            String labelText = ResBundleVisualization.getString("OPENED_SHIFT_NUMBER");
            labelText = labelText.concat(String.valueOf(Factory.getTechProcessImpl().getShift().getNumShift()));
            if (labelText != null) {
                if (shiftNumberLabel != null)
                    shiftNumberLabel.setText(labelText);
                else
                    shiftNumberLabel = new JLabel(labelText);
            }
            // получение лейбла об док-те, который был удален
            String docRemoved = Factory.getTechProcessImpl().getDocumentTypePendingPurge();
            if (docRemoved != null) {
                wrapLabelText(fiscalError, docRemoved, 610);
            } else {
                wrapLabelText(fiscalError, " ", 610);
            }
            return;
        }
        String errorStatus = Factory.getTechProcessImpl().getFiscalPrinterInitialState();
        if (errorStatus == null) {
            errorStatus = Factory.getTechProcessImpl().getShiftSyncState();
            if (errorStatus != null && errorStatus.length() > 0) {
                String l = ResBundleVisualization.getString("SHIFT_EMERGENCY_CLOSE");
                errorStatus = l.replace("{shiftNum}", errorStatus);
            }
        }
        if (errorStatus != null && errorStatus.length() > 0) {
            wrapLabelText(fiscalError, errorStatus, 610);
        } else {
            wrapLabelText(fiscalError, " ", 610);
            if (shiftNumberLabel != null)
                        shiftNumberLabel.setText("");
                    else
                        shiftNumberLabel = new JLabel("");
                }
            }

    private void addCheckPresentLabelLabel(JPanel topPanel) {
        checkPresentLabel = new JLabel();
        updateCheckPresentLabelText();
        Style.setCheckPresentLabel(checkPresentLabel);
        topPanel.add(checkPresentLabel, BorderLayout.WEST);
    }

    public void updateCheckPresentLabelText() {
        if (Factory.getTechProcessImpl().getCheck() != null) {
            checkPresentLabel.setText(ResBundleVisualization.getString("UNCOMPLITED_CHECK_PRESENT"));
        } else {
            checkPresentLabel.setText("");
        }
    }

    public void setPassword(String password) {
        inputPanel.setField(password);
    }

    public void setPasswordDefaultText() {
        inputPanel.setField("");
        inputPanel.setInput(ResBundleVisualization.getString("ENTER_PASSWORD"));
    }

    public void setPasswordMistakeText() {
        inputPanel.setField("");
        inputPanel.setInput(ResBundleVisualization.getString("ENTER_PASSWORD_AGAIN"));
    }

    public void setLoginFailed(Boolean failed, String reason) {
        if (!failed) {
            setPasswordDefaultText();
        } else {
            setPasswordMistakeText();
        }
        jEmpty.setVisible(!failed);
        warningLabel.setText(reason);
        warningLabel.setVisible(failed);
        warningLabel.repaint();
    }

    public void showShiftIcon() {
        shiftLabel.setVisible(true);
    }

    public void hideShiftIcon() {
        shiftLabel.setVisible(false);
    }

    public InputPanelFlat getInputPanel() {
        return inputPanel;
    }
}
