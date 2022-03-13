package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.spi.ui.UIForms;

public class PatternFormShowcaseScenario extends ShowcaseScenario {

    public PatternFormShowcaseScenario(UIForms forms) {
        super(forms);
    }

    @Override
    public void run() {
        formManager.getInputForms().showPatternInputForm(
                "Введите номер мобильного телефона",
                "Номер мобильного телефона",
                "9090570001",
                "Вводить сюда",
                "+'7'(###)###-##-##",
                new InputListener() {
                    @Override
                    public void eventInputComplete(String s) {

                    }

                    @Override
                    public void eventCanceled() {

                    }
                });
    }
}
