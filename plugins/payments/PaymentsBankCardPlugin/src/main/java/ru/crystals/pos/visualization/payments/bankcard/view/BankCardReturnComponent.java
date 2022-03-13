package ru.crystals.pos.visualization.payments.bankcard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanelNew;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentBankCard;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.KeyEvent;

public class BankCardReturnComponent extends VisualPanel {

    private JLabel labelError;
    private JPanel errorPanel;
    private CommonYesNoPanelNew buttonsPane;
    private CardLayout cardLayout;

    private BankCardPaymentController controller;
    private PaymentEntity cancelPayment;

    public BankCardReturnComponent() {
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        this.add(getErrorPanel(), "ErrorPanel");
        WaitComponent waitComponent = new WaitComponent(ResBundlePaymentBankCard.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
        this.add(waitComponent, waitComponent.getClass().getName());
    }

    private JPanel getErrorPanel() {
        if (errorPanel == null) {
            errorPanel = new VisualPanel();
            errorPanel.setLayout(new BorderLayout());
            initLabelError(ResBundlePaymentBankCard.getString("REPEAT_CANCEL_PURCHASE_REQUEST"));
            buttonsPane = new CommonYesNoPanelNew(ResBundlePaymentBankCard.getString("ANSWER_OK"), ResBundlePaymentBankCard.getString("ANSWER_CANCEL"));
            buttonsPane.selectYes();
            errorPanel.add(labelError, BorderLayout.CENTER);
            errorPanel.add(buttonsPane, BorderLayout.PAGE_END);
            setPreferredSize(Size.mainPanel);
            setBorder(new EmptyBorder(10, 10, 10, 10));
            new XListenerAdapter(errorPanel) {
                @Override
                public boolean keyPressedNew(XKeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        enter();
                        return true;
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        buttonsPane.changeSelection();
                        return true;
                    }
                    return false;
                }
            };
        }
        return errorPanel;
    }

    private void initLabelError(String messageError) {
        this.labelError = new JLabel(messageError);
        Style.setDialogTitleStyle(this.labelError);
        labelError.setPreferredSize(new ScaleDimension(600, 200));
        wrapLabelTextUsingSeparators(labelError, messageError);
    }

    public void enter() {
        if (!buttonsPane.isYes()) {
            controller.notExceptionalComplete();
        } else {
            cardLayout.show(this, WaitComponent.class.getName());
            controller.processCancelPayment(cancelPayment);
        }
        Factory.getInstance().getMainWindow().unlockComponent(null);
    }

    public void processCancelPayment(PaymentEntity cancelPayment) {
        this.cancelPayment = cancelPayment;
        Factory.getInstance().getMainWindow().getCheckContainer().showWaitComponent(ResBundlePaymentBankCard.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
        controller.processCancelPayment(cancelPayment);
        Factory.getInstance().getMainWindow().getCheckContainer().restoreAfterWaitSearchCard();
        while (controller.isExceptionalComplete()) {
            cardLayout.show(this, "ErrorPanel");
            buttonsPane.reset();
            Factory.getInstance().getMainWindow().showLockComponent(this);
        }
    }

    public void setController(BankCardPaymentController controller) {
        this.controller = controller;
    }
}
