package ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.components.keyboard.QwertyFlat;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.AbstractUnitPanel;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.interfaces.FormFilledHandler;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/**
 * Панелька с QWERTY клавиатурой для CommonPayment Plugin'а
 * Тут используется контрол с тач кассы
 */
public class CommonOnscreenQwertyPanel extends AbstractUnitPanel implements XListener {
    private final FormFilledHandler handler;
    private QwertyFlat qwerty;

    public CommonOnscreenQwertyPanel(InputFieldInterface input, final FormFilledHandler handler) {
        this.handler = handler;
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 60, 0, 0));
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        qwerty = new QwertyFlat(input, true, 60) {
            /**
             * Этот метод вызывается в тач кассе когда пользователь нажимает далее
             * т.е. мы должны сообщить плагины что форма заполнена
             */
            @Override
            public void enter() {
                handler.proceed();
            }
        };
        qwerty.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        qwerty.reset();
        this.add(qwerty, BorderLayout.CENTER);
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return false;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    public void setPaid(PaymentInfo info) {
        //
    }

    @Override
    public void setRefund(boolean refund) {
        //
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        qwerty.keyPressed(e);
        return false;
    }
}
