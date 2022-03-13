package ru.crystals.pos.visualization.payments.bankcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.service.BankCardPaymentPluginInitMark;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentBankCard;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.payments.bankcard.view.BankCardReturnComponent;

import java.util.List;
import java.util.Objects;

/**
 * Created by agaydenger on 17.11.16.
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.BANK_CARD_PAYMENT_ENTITY, mainEntity = BankCardPaymentEntity.class)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BankCardPaymentPluginAdapter extends BaseCardPaymentPluginAdapter implements BankCardPaymentPluginInitMark {

    private BankCardReturnComponent bankCardReturnComponent;

    @Autowired
    BankCardPaymentPluginAdapter(BankCardPaymentController controller) {
        super(controller);
    }

    private BankCardReturnComponent getBankCardReturnComponent() {
        if (bankCardReturnComponent == null) {
            bankCardReturnComponent = new BankCardReturnComponent();
        }
        return bankCardReturnComponent;
    }

    @Override
    public boolean isActivated() {
        return isActivated(getBankId());
    }

    @Override
    public boolean isActivated(String providerId) {
        final Bank bankModule = getBankModule();
        if (bankModule == null) {
            return false;
        }
        return bankModule.isProviderAvailable(providerId, BankPaymentType.CARD) && canBeUsedWithOtherBanks(bankModule, providerId);
    }

    /**
     * По умолчанию мы не хотим иметь проблем при смешанной оплат несколькими банками, поэтому запрещаем их (для возврат можно все).
     *
     * Можно оплатить банком
     * - если еще нет банковской оплаты в чеке
     * - если банковская оплата есть, но она по тому же банку
     * - если банк позволяет смешивать себя с другимим
     * - если все банки из уже примененных оплат позволяют смешивать себя с другими
     * @return {@code true} - можно оплатить этим банком
     */
    private boolean canBeUsedWithOtherBanks(Bank bankModule, String bankId) {
        if (bankId == null) {
            return false;
        }
        if (bankModule.canBeUsedWithOtherBanks(bankId)) {
            return true;
        }
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();
        if (purchase == null || purchase.isReturn()) {
            return true;
        }
        List<PaymentEntity> payments = purchase.getPayments();
        if (payments.isEmpty()) {
            return true;
        }
        return payments.stream().allMatch(payment -> canBeUsed(bankModule, bankId, payment));
    }

    private boolean canBeUsed(Bank bankModule, String bankId, PaymentEntity otherPayment) {
        if (!BankCardPaymentEntity.isBankCardPayment(otherPayment)) {
            return true;
        }
        final String otherBankId = ((BankCardPaymentEntity) otherPayment).getBankid();
        return Objects.equals(bankId, otherBankId) || bankModule.canBeUsedWithOtherBanks(otherBankId);
    }

    public String getBankNameTitle(String bankName) {
        return (bankName != null ? ", " + ResBundlePaymentBankCard.getString("BANK") + " " + bankName : "");
    }

    protected String getResourceRefundString() {
        return ResBundlePaymentBankCard.getString("REFUND_TO_CARD");
    }

    protected String getResourcePaymentString() {
        return ResBundlePaymentBankCard.getString("PAYMENT_BY_CARD");
    }

    @Override
    public String getDisabledReason(String providerId) {
        final Bank bankModule = getBankModule();
        if (bankModule == null) {
            return null;
        }
        if (!canBeUsedWithOtherBanks(bankModule, providerId)) {
            return ResBundlePaymentBankCard.getString("ALREADY_APPLIED");
        }
        return null;
    }

    @Override
    public void cancelPurchase(PaymentEntity cancelPayment) {
        getBankCardReturnComponent().setController(getController());
        getBankCardReturnComponent().processCancelPayment(cancelPayment);
    }

    @Override
    protected Long recalcSurchargeIfPurchaseSplitted(PurchaseEntity check, Long surcharge) {
        if (isRefund()) {
            return surcharge;
        } else {
            if (needCashOut(check)) {
                return surcharge;
            }
            return super.recalcSurchargeIfPurchaseSplitted(check, surcharge);
        }
    }
}
