package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;


import javax.swing.JOptionPane;
import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.spi.ui.UIForms;

public class InputFormShowcaseScenario extends ShowcaseScenario {

    public InputFormShowcaseScenario(UIForms forms) {
        super(forms);
    }

    @Override
    public void run() {
        formManager.getInputForms().showInputNumberForm(
                "Оплата электронными деньгами",
                "<br/><br/><i>Подтверждение платежа</i>",
                "Введите код, полученный в смс-сообщении",
                10,
                new InputListener() {
                    @Override
                    public void eventInputComplete(String number) {
                        JOptionPane.showMessageDialog(null, "Entered " + number);
                    }

                    @Override
                    public void eventCanceled() {
                        JOptionPane.showMessageDialog(null, "Cancelled");
                    }
                }
        );
    }
}
