package ru.crystals.pos.spi.ui.forms.demo.showcase;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import ru.crystals.pos.api.ui.listener.SumToPayFormListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XKeyListenerInt;
import ru.crystals.pos.spi.IncorrectStateException;
import ru.crystals.pos.spi.ui.forms.payment.ExtSumToPayFormParameters;
import ru.crystals.pos.spi.ui.forms.payment.SumToPayForm;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;
import ru.crystals.pos.spi.ui.payment.UIPaymentForms;

public class ShowcasePaymentFormManager implements UIPaymentForms {

    /**
     * Контейнер для формочек визуализации.
     */
    private JPanel parentContainer;
    /**
     * Заглушечная реализация событийной модели для ретрансляции формочкам клавиатурных событий.
     */
    private List<XKeyListenerInt> keyboardSubscribers = new ArrayList<>();

    public ShowcasePaymentFormManager(JPanel parentContainer) {
        this.parentContainer = parentContainer;
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
    public void showSumToPayForm(SumToPayFormParameters formParameters, SumToPayFormListener sumToPayFormListener) throws IncorrectStateException {
        SumToPayForm form = new SumToPayForm();
        ExtSumToPayFormParameters params = new ExtSumToPayFormParameters(formParameters);
        params.setSum(BigDecimal.valueOf(1000000.145));
        params.setPaid(BigDecimal.valueOf(0));
        form.setModel(params);
        form.setListener(sumToPayFormListener);
        keyboardSubscribers.clear();
        keyboardSubscribers.add(form);
        replaceComponent(form);
    }

    private void replaceComponent(JPanel component) {
        BorderLayout layout = (BorderLayout)parentContainer.getLayout();
        parentContainer.remove(layout.getLayoutComponent(BorderLayout.CENTER));
        parentContainer.add(component, BorderLayout.CENTER);
        parentContainer.revalidate();
        parentContainer.repaint();
    }
}
