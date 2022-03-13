package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.spi.ui.UIForms;

import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputSelectorFormShowcaseScenario extends ShowcaseScenario {

    public InputSelectorFormShowcaseScenario(UIForms forms) {
        super(forms);
    }

    @Override
    public void run() {
        Map<String, List<String>> choices = new HashMap<>();
        choices.put("1", Arrays.asList("Скидка на день рождения", "10%"));
        choices.put("2", Arrays.asList("Накопительная скидка", "5%"));
        formManager.getInputForms().showSelectionForm("Выберите купон для применения", choices, new InputListener() {
            @Override
            public void eventInputComplete(String value) {
                JOptionPane.showMessageDialog(null, "Entered " + String.join("", choices.get(value)));
            }

            @Override
            public void eventCanceled() {
                JOptionPane.showMessageDialog(null, "Cancelled");
            }
        });
    }

}
