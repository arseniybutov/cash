package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.view;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import ru.crystals.pos.bl.keylisteners.EnterListener;
import ru.crystals.pos.bl.keylisteners.EscKeyListener;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonInputPanelExt;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonHeaderPanel;
import ru.crystals.pos.visualization.styles.Color;

/**
 * Форма (панель) "Ввод номера карты".
 */
public class CardNumberEnterForm extends JPanel {
    private static final long serialVersionUID = -6330175306253132952L;

    private CommonInputPanelExt inputPanel;
    private EscKeyListener escListener;
    private EnterListener<String> enterListener;

    public CardNumberEnterForm(String headerText) {
        setLayout(new BorderLayout());
        setBackground(Color.greyBackground);

        CommonHeaderPanel headerPanel = new CommonHeaderPanel();
        headerPanel.setHeaderText(headerText);

        inputPanel = CommonInputPanelExt.createCardNumberInputPanel();
        inputPanel.setEscListener(() -> {
            if (escListener != null) {
                escListener.esc();
            }
        });
        inputPanel.setEnterListener(number -> {
            if (enterListener != null) {
                enterListener.enter(number);
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void setEscListener(EscKeyListener escListener) {
        this.escListener = escListener;
    }

    public void setEnterListener(EnterListener<String> enterListener) {
        this.enterListener = enterListener;
    }

    /**
     * Сообщение над строкой ввода.
     */
    public void setHintText(String text) {
        inputPanel.setHintText(text);
    }

    public void setHandEnterEnable(boolean enabled) {
        inputPanel.setHandEnterEnabled(enabled);
    }

    public void setCardNumber(String cardNumber) {
        inputPanel.setValue(cardNumber);
    }

    public void clear() {
        inputPanel.clear();
    }
}