package ru.crystals.pos.visualization.payments.supra.view;

import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.supra.controller.SupraCardPaymentController;

/**
 * Created by s.pavlikhin on 09.06.2017.
 */
public abstract class SupraCardPaymentBaseForm extends
        AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                SupraCardPaymentController> {
    public SupraCardPaymentBaseForm(XListener outerListener) {
        super(outerListener);
    }
}
