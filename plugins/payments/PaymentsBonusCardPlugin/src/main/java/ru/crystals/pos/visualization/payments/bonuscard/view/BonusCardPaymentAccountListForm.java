package ru.crystals.pos.visualization.payments.bonuscard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.component.BonusCardPaymentSelectAccountListComponent;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import java.awt.event.KeyEvent;

/**
 * Created by a.gaydenger on 30.09.2014.
 */
public class BonusCardPaymentAccountListForm extends
        AbstractPaymentForm<BonusCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                BonusCardPaymentController> {
    private BonusCardPaymentSelectAccountListComponent bonusAccountSelectionComponent;

    public BonusCardPaymentAccountListForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentBonusCard.getString("BONUS_PAYMENT"), ResBundlePaymentBonusCard.getString("REFUND_BONUS_PAYMENT"));
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        bonusAccountSelectionComponent = new BonusCardPaymentSelectAccountListComponent();
        return bonusAccountSelectionComponent;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        CommonPaymentToPayPanel result = new CommonPaymentToPayPanel();
        result.setPreferredSize(new ScaleDimension(240, 150));
        result.getjOperationLabel().setPreferredSize(new ScaleDimension(220, 33));
        result.getjSummaLabel().setPreferredSize(new ScaleDimension(220, 80));
        result.getjSummaLabel().setFont(MyriadFont.getRegular(36F));
        return result;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return null;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            bonusAccountSelectionComponent.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            getController().setSelectedAccount(bonusAccountSelectionComponent.getSelectedRow());
            return true;
        }
        return false;
    }


    @Override
    public void showForm(BonusCardPaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        bonusAccountSelectionComponent.fillTable(((BonusCardPaymentInfo) info).getCardInfo().getBonusAccounts());
    }
}
