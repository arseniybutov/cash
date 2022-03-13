package ru.crystals.pos.loyal.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.enums.MessageDisplayTime;
import ru.crystals.loyal.calculation.PurchaseUtils;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.providers.FeedbackBundle;
import ru.crystals.loyal.providers.LoyProvider;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.sc.SCService;
import ru.crystals.pos.sc.exception.SCException;

import java.util.ArrayList;

/**
 * Провайдер лояльности Smart Checkout.
 */
public class SCLoyProvider implements LoyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SCLoyProvider.class);

    @Autowired(required = false)
    private SCService scService;

    /**
     * Для запуска отправки подтверждения
     */
    @Autowired
    private InternalCashPoolExecutor executor;

    public SCLoyProvider() {
    }

    @Override
    public Purchase process(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess loyTechProcess) {
        if (!isEnabled()) {
            LOG.trace("No SC discounts: SC service disabled");
            return null;
        }
        try {
            return scService.calcDiscount(receipt, "" + originalReceipt.getDateCreate().getTime(), originalReceipt.getId(), loyTechProcess);
        } catch (SCException e) {
            Purchase result = receipt.cloneWithDisc();
            PurchaseUtils.removeAllDiscounts(result);
            MessageAdvertisingActionResultEntity message = new MessageAdvertisingActionResultEntity();
            message.setAdvertisingActionGUID(scService.getSettings().getActionGuid());
            message.setPurchase(result);
            message.setDisplayTime(MessageDisplayTime.SUBTOTAL);
            message.getOperatorMsg().add(e.getMessage());
            ArrayList<AdvertisingActionResultEntity> actionResult = new ArrayList<>();
            actionResult.add(message);
            result.setAdvertisingActionResults(actionResult);
            return result;
        }
    }

    CardTypeEntity getFakeSCCardType() {
        return CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, SCService.PROVIDER_NAME);
    }

    @Override
    public String getProviderName() {
        return SCService.PROVIDER_NAME;
    }

    @Override
    public CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        CardTypes result = CardTypes.CardNotFound;
        if (isEnabled() && !couponData.isBarcodeDecoded() && scService.isSmChCoupon(couponData.getCardNumber())) {
            result = CardTypes.ExternalCoupon;
        }
        return result;
    }

    @Override
    public void confirmDiscount(final PurchaseEntity purchaseEntity, final LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime) {
        if (isEnabled() && feedbackTime == FeedbackTime.AFTER_FISCALIZE) {
            //Нас интересует только после фискализации. Выполнять будем в отдельном потоке
            Runnable confirmDiscountTask = () -> scService.commitDiscounts(loyTransactionEntity, purchaseEntity);
            if (executor != null) {
                executor.submit(confirmDiscountTask);
            } else {
                new Thread(confirmDiscountTask).start();
            }
        }
    }

    @Override
    public void sendFeedback(FeedbackBundle feedback) {
        if (isEnabled()) {
            scService.commitDiscounts(feedback.getFeedback());
        } else {
            LOG.warn("Can't send feedback {} now. SCService is not available", feedback);
        }
    }

    @Override
    public void checkCanceled(PurchaseEntity purchase) {
        if (isEnabled()) {
            scService.checkCanceled(purchase);
        }
    }

    private boolean isEnabled() {
        return scService != null && scService.isEnabled();
    }
}
