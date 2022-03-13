package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XListener;

public class ConsumerCreditOnscreenFIOInputForm extends AbstractPaymentOnscreenKeyboardInputForm {
    public ConsumerCreditOnscreenFIOInputForm(XListener outerListener, String labelText, String welcomeText) {
        super(outerListener);
        footer.setLabelText(labelText);
        footer.setWelcomeText(welcomeText);
        addAWTListener();
    }

    @Override
    public void proceed() {
        if (!footerPanel.getTextValue().isEmpty()) {
            getController().processFilledFIO(footer.getTextValue());
        }
    }

    @Override
    public void cancel() {
        getController().processCancelFillFIO();
    }
}
