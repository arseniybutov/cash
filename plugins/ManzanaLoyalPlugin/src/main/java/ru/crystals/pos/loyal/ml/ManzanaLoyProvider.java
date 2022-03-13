package ru.crystals.pos.loyal.ml;

import org.apache.commons.lang.StringUtils;
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
import ru.crystals.loyal.providers.LoyProcessingException;
import ru.crystals.loyal.providers.LoyProcessingTryItAgainException;
import ru.crystals.loyal.providers.LoyProvider;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.CardAlreadyAddedException;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.ml.exception.MLConnectionException;
import ru.crystals.pos.ml.exception.MLInternalException;
import ru.crystals.pos.ml.exception.MLShouldApplyFakeCardException;

import java.util.ArrayList;

/**
 * Created by agaydenger on 25.07.16.
 */
public class ManzanaLoyProvider implements LoyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ManzanaLoyProvider.class);

    @Autowired(required = false)
    private MLService mlService;

    @Autowired
    private CheckService checkService;

    /**
     * Для запуска отправки подтверждения
     */
    @Autowired
    private InternalCashPoolExecutor executor;

    public ManzanaLoyProvider() {
    }

    @Override
    public Purchase process(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess loyTechProcess) throws LoyProcessingException {
        if (!isEnabled()) {
            LOG.trace("No Manzana Loyalty discounts: the service is disabled");
            return null;
        }
        try {
            long checkNum;
            if(originalReceipt.getNumber() != null && originalReceipt.getNumber() > 0) {
                checkNum = originalReceipt.getNumber();
            } else {
                checkNum = checkService.getExpectedDocNum();
            }
            return mlService.calcDiscount(receipt, "" + checkNum, originalReceipt.getId(), loyTechProcess);
        } catch (MLConnectionException | MLInternalException e) {
            Purchase result = receipt.cloneWithDisc();
            PurchaseUtils.removeAllDiscounts(result);
            MessageAdvertisingActionResultEntity message = new MessageAdvertisingActionResultEntity();
            message.setAdvertisingActionGUID(mlService.getSettings().getActionGuid());
            message.setPurchase(result);
            message.setDisplayTime(MessageDisplayTime.SUBTOTAL);
            message.getOperatorMsg().add(e.getMessage());
            ArrayList<AdvertisingActionResultEntity> actionResult = new ArrayList<>();
            actionResult.add(message);
            result.setAdvertisingActionResults(actionResult);
            return result;
        } catch (MLShouldApplyFakeCardException e) {
            //Нужно прикрепить фейковую карту
            try {
                checkService.addCard(mlService.getFakeCardNo(), getFakeMLCardType(), checkService.getCurrentCheckNum(), null);
            } catch (CardAlreadyAddedException e1) {
                //Ну это совсем неожиданно
                LOG.error("Unexpected exception on add card!", e);
            }
            throw new LoyProcessingTryItAgainException(e.getMessage());
        }
    }

    CardTypeEntity getFakeMLCardType() {
        return CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME);
    }

    @Override
    public String getProviderName() {
        return MLService.PROVIDER_NAME;
    }

    @Override
    public CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        CardTypes result = CardTypes.CardNotFound;
        if (isEnabled() && !couponData.isBarcodeDecoded() && mlService.isMlCoupon(couponData.getCardNumber())) {
            result = CardTypes.ExternalCoupon;
        }
        return result;
    }

    @Override
    public void confirmDiscount(final PurchaseEntity purchaseEntity, final LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime) {
        //Отправлять в манзану только те чеки, в которых есть карта или купон
        if (isEnabled()
                && isNeedToSendToML(purchaseEntity)) {
            if (feedbackTime == FeedbackTime.AFTER_FISCALIZE) {
                //Нас интересует только после фискализации. Выполнять будем в отдельном потоке
                Runnable confirmDiscountTask = () -> mlService.commitDiscounts(loyTransactionEntity, purchaseEntity);
                executor.submit(confirmDiscountTask);
            }
        }
    }

    private boolean isNeedToSendToML(PurchaseEntity purchaseEntity) {
        return !purchaseEntity.getOperationType() || StringUtils.isNotEmpty(mlService.getMlCardNo(purchaseEntity))
                || StringUtils.isNotEmpty(mlService.getMlCouponNo(purchaseEntity));
    }

    @Override
    public void sendFeedback(FeedbackBundle feedback) {
        if (isEnabled()) {
            mlService.commitDiscounts(feedback.getFeedback());
        } else {
            LOG.warn("Can't send feedback {} now. Manzana loyalty service is not available", feedback);
        }
    }

    @Override
    public boolean isPurchaseSuitable(Purchase purchase) {
        if (!isEnabled()) {
            return true;
        }
        return !purchase.getOperationType() || StringUtils.isNotEmpty(mlService.getMlCardNo(purchase)) || StringUtils.isNotEmpty(mlService.getMlCouponNo(purchase));
    }

    private boolean isEnabled() {
        return mlService != null && mlService.isEnabled();
    }
}
