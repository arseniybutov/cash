package ru.crystals.pos.visualization.payments.bankcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.utils.ChildrenCardsCheckUtils;
import ru.crystals.pos.payments.ChildrenCardPaymentEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentChildrenCard;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;

/**
 * Created by agaydenger on 23.11.16.
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.CHILDREN_CARD_PAYMENT_ENTITY, mainEntity = ChildrenCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class ChildrenCardPaymentPluginAdapter extends BaseCardPaymentPluginAdapter {
    @Autowired
    ChildrenCardPaymentPluginAdapter(BankCardPaymentController controller) {
        super(controller);
    }

    @Override
    protected String getResourcePaymentString() {
        return ResBundlePaymentChildrenCard.getString("CHILDREN_CARD_PAYMENT");
    }

    @Override
    public String getBankNameTitle(String bankid) {
        return "";
    }

    @Override
    protected String getResourceRefundString() {
        return ResBundlePaymentChildrenCard.getString("REFUND_CHILDREN_CARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        // Здесь проверим доступность терминала, получим Ид банка и засетаем его.
        if (getBankModule() != null) {
            String bankPlugin = getBankModule().getChildCardBankId();
            boolean isChildrenCardPaymentAllowed = getCurrentChildrenPositionsSum() > 0 && !isChildrenPositionsPaid() && !isPurchaseHaveAnotherPayments();
            if (bankPlugin != null && getBankModule().isProviderAvailable(bankPlugin, BankPaymentType.CARD) && isChildrenCardPaymentAllowed) {
                setBankId(bankPlugin);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getBankId() {
        String bankId = super.getBankId();
        String childCardBankId;
        if (getBankModule() != null) {
            childCardBankId = getBankModule().getChildCardBankId();
            if ((bankId == null && childCardBankId != null) || (bankId != null && !bankId.equals(childCardBankId))) {
                super.setBankId(childCardBankId);
                bankId = childCardBankId;
            }
        }
        return bankId;
    }

    @Override
    public String getDisabledReason() {
        if (getCurrentChildrenPositionsSum() <= 0) {
            return ResBundlePaymentChildrenCard.getString("CHILDREN_POSITIONS_NOT_FOUND");
        } else if (isChildrenPositionsPaid()) {
            return ResBundlePaymentChildrenCard.getString("CHILDREN_POSITIONS_ALREADY_PAYED");
        }
        return null;
    }

    private boolean isChildrenPositionsPaid() {
        return ChildrenCardsCheckUtils.isChildrenPositionsPaid(getFactory().getTechProcessImpl().getCheck());
    }

    private long getCurrentChildrenPositionsSum() {
        return ChildrenCardsCheckUtils.getChildrenPositionsSum(getFactory().getTechProcessImpl().getCheck());
    }

    private long getCurrentChildrenPositionsSum(Long surcharge) {
        PurchaseEntity check = getFactory().getTechProcessImpl().getCheck();
        Long childrenPositionSum = getCurrentChildrenPositionsSum() - ChildrenCardsCheckUtils.getChildrenPaymentsSum(check);
        return childrenPositionSum.compareTo(surcharge) >= 0L ? surcharge : childrenPositionSum;
    }

    @Override
    public void setSurcharge(Long surcharge) {
        surcharge = getCurrentChildrenPositionsSum(surcharge);
        super.setSurcharge(surcharge);
    }

    private boolean isPurchaseHaveAnotherPayments() {
        return ChildrenCardsCheckUtils.haveAnotherPayments(getFactory().getTechProcessImpl().getCheck());
    }
}
