package ru.crystals.loyal.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.techprocess.TechProcessStage;

/**
 * Отладочный провайдер лояльности. Просто печатает в лог сообщения о состоянии чека после расчета скидок и при фискализации.
 */
public class DebugPrintLoyProvider extends AbstractLoyProvider {
    private static final Logger logger = LoggerFactory.getLogger(DebugPrintLoyProvider.class);

    @Autowired
    private CurrencyHandler currencyHandler;

    @Override
    public Purchase process(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) throws LoyProcessingException {
        return null;
    }

    @Override
    public String getProviderName() {
        return "DebugReceiptPrintLoyProvider";
    }

    @Override
    public CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        return CardTypes.CardNotFound;
    }

    @Override
    public boolean isLoyTransactionComplete(LoyTransactionEntity loyTransactionEntity) {
        return true;
    }

    @Override
    public boolean isLoyTransactionRequiredForStage(TechProcessStage stage) {
        return false;
    }

    @Override
    public boolean isPurchaseSuitable(Purchase purchase) {
        return true;
    }

    @Override
    public void cancelDiscount(PurchaseEntity purchase) {
        logger.info("Cancelling discount");
    }

    @Override
    public void checkCanceled(PurchaseEntity purchase) {
        logger.info("Receipt cancelled");
    }

    @Override
    public void confirmDiscount(PurchaseEntity purchaseEntity, LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime) {
        logger.info("Discount confirmed");
    }

    @Override
    public Purchase onDiscountCalculationFinished(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) throws LoyProcessingException {
        logger.info("FL54 adjustment enabled: {}", techProcess.getLoyaltyProperties().isFz54Compatible());
        logger.info(new DebugReceiptReport(currencyHandler).getReceiptReport(receipt));
        return null;
    }

    @Override
    public void purchaseFiscalized(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        logger.info(new DebugReceiptEntityReport().createReport(purchase));
    }
}