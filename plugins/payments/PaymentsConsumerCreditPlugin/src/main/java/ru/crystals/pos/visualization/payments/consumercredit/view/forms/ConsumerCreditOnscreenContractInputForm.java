package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XListener;

public class ConsumerCreditOnscreenContractInputForm extends AbstractPaymentOnscreenKeyboardInputForm {
    public ConsumerCreditOnscreenContractInputForm(XListener outerListener, String labelText, String welcomeText) {
        super(outerListener);
        footer.setLabelText(labelText);
        footer.setWelcomeText(welcomeText);
        addAWTListener();
    }

    @Override
    public void proceed() {
        if (!footerPanel.getTextValue().isEmpty()) {
            getController().processFilledContract(footer.getTextValue());
        }
    }

    @Override
    public void cancel() {
        getController().processCancelFillContract();
    }
}
