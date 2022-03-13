package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XListener;

/**
 * Created by myaichnikov on 21.11.2014.
 */
public class ConsumerCreditContractInputForm extends AbstractPaymentInputForm {
    public ConsumerCreditContractInputForm(XListener outerListener, String labelText, String welcomeText) {
        super(outerListener);
        footerPanel.setLabelText(labelText);
        footerPanel.setWelcomeText(welcomeText);
    }

    @Override
    public void proceed() {
        getController().processFilledContract(footerPanel.getTextValue());
    }

    @Override
    public void cancel() {
        getController().processCancelFillContract();
    }
}
