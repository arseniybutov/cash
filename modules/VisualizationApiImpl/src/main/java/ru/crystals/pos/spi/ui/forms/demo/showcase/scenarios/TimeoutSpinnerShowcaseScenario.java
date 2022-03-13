package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.spi.ui.UIForms;

/**
 * Сценарий демонстрации окна ожидания выполнения долговременной операции с обратным отсчетом.
 */
public class TimeoutSpinnerShowcaseScenario extends ShowcaseScenario {

    public TimeoutSpinnerShowcaseScenario(UIForms forms) {
        super(forms);
    }

    @Override
    public void run() {
        formManager.showTimingOutForm("Выполняется обращение к процессингу, пожалуйста, подождите...", 10_000,
                        () -> formManager.showMessageForm("Подождите ещё немного...", null));
    }
}
