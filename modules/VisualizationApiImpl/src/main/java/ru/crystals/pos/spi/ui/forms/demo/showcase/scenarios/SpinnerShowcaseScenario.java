package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.spi.ui.UIForms;

public class SpinnerShowcaseScenario extends ShowcaseScenario {

    public SpinnerShowcaseScenario(UIForms f) {
        super(f);
    }

    @Override
    public void run() {
        showBusyBox();
    }

    private void showBusyBox() {
        formManager.showSpinnerFormWithCancel("Operation in progress, please wait...", this::showOperationAbortedMessage);
    }

    private void showOperationAbortedMessage() {
        formManager.showErrorForm("Operation has been aborted", this::showBusyBox);
    }
}
