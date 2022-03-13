package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.UIForms;

public class DialogShowcaseScenario extends ShowcaseScenario {

    public DialogShowcaseScenario(UIForms formManager) {
        super(formManager);
    }

    @Override
    public void run() {
        showDialog();
    }

    private void showDialog() {
        DialogFormParameters model = new DialogFormParameters(
                "<html>Ошибка!<br/>Сервер не отвечает.<br/>Аннулировать чек без подтверждения процесса?</html>",
                "Аннулировать",
                "Повторить"
        );
        formManager.showDialogForm(model, new DialogListener() {
            @Override
            public void eventButton1pressed() {
                showPositiveScenario();
            }

            @Override
            public void eventButton2pressed() {
                showNegativeScenatio();
            }

            @Override
            public void eventCanceled() {
                cancelDialog();
            }
        });
    }

    private void showPositiveScenario() {
        formManager.showSpinnerFormWithCancel("Аннулирование чека", this::showDialog);
    }

    private void showNegativeScenatio() {
        formManager.showErrorForm("Нельзя повторить", this::showDialog);
    }

    private void cancelDialog() {formManager.showErrorForm("Отмена", this::showDialog);}
}
