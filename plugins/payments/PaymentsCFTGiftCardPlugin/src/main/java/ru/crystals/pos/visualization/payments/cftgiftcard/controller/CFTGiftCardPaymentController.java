package ru.crystals.pos.visualization.payments.cftgiftcard.controller;

import ru.crystals.pos.cards.cft.CFTCardState;
import ru.crystals.pos.cards.cft.CFTGiftCardInfo;
import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.model.cft.CFTGiftCardsModel;
import ru.crystals.pos.payments.CFTGiftCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.cftgiftcard.ResBundlePaymentCftGiftCard;
import ru.crystals.pos.visualization.payments.cftgiftcard.integration.CFTGiftCardPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.cftgiftcard.model.CFTGiftCardPaymentModel;
import ru.crystals.pos.visualization.payments.cftgiftcard.model.CFTGiftCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;

import java.math.BigDecimal;

public class CFTGiftCardPaymentController extends AbstractPaymentController<CFTGiftCardPaymentPluginAdapter, CFTGiftCardPaymentModel> {
    private String cardNumber = null;
    private CardType enterType = null;

    /**
     * Запускается процесс оплаты подарочной картой ЦФТ
     *
     * @param payment
     */
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        if (getAdapter().isRefund()) {
            getAdapter().setSum(payment.getSumPay());
            getModel().setState(CFTGiftCardPaymentState.REFUND);
        } else {
            getModel().setState(CFTGiftCardPaymentState.ENTER_CARD_NUMBER);
        }
    }

    public void processCancelPayment(PaymentEntity payment) {
        CFTGiftCardPaymentEntity entity = (CFTGiftCardPaymentEntity) payment;
        try {
            getCftModel().cancelAuthorize(entity);
        } catch (Exception e) {
            beepError("CFTPaymentsPlugin: error canceling payment - " + e.getMessage());
        }
    }

    /**
     * Мы получили номер карты, теперь запросим информацию о карте в процессинге и покажем форму
     * подтверждения оплаты, либо покажем ошибку (карта не активна, неверный номер, ...)
     *
     * @param enterType
     * @param textStrings
     */
    public void processGetCardInfo(CardType enterType, String... textStrings) throws Exception {
        this.enterType = enterType;
        switch (enterType) {
            case BARCODE:
            case PAN:
                cardNumber = textStrings[0];
                break;
            default:
                //прокатали карту - имеем целый список дорожек
                int i = getFirstNotNull(textStrings);
                if (i < 0) {
                    beepError("CFTPaymentsPlugin: Cannot find not empty MSR track");
                    throw new CFTException(ResBundlePaymentCftGiftCard.getString("WRONG_CARD"));
                }
                cardNumber = textStrings[i];
                this.enterType = calculateCardType(i);
        }

        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();

        CFTGiftCardInfo info = getCftModel().getCardInfo(cardNumber, this.enterType);
        if (info != null) {
            if (info.getCardState() != CFTCardState.ACTIVE) {
                throw new Exception(ResBundlePaymentCftGiftCard.getString("CARD_BLOCKED"));
            } else if (info.getAmount() == null || (info.getAmount().longValue() == 0 && !check.isReturn())) {
                throw new Exception(ResBundlePaymentCftGiftCard.getString("CARD_IS_EMPTY"));
            }
        } else {
            throw new Exception(ResBundlePaymentCftGiftCard.getString("CARD_IS_EMPTY"));
        }

        if (!check.isReturn()) {
            getModel().getPayment().setSumPay(info.getAmount());
        }
        getModel().setState(CFTGiftCardPaymentState.PAYMENT);
    }

    /**
     * Все готово к процессингу оплаты, на форме подтверждения введена и проверена сумма оплаты.
     * Запускаем процесс оплаты - если будет ошибка - вернемся по эксепшену на вью, если все ок
     * то запускаем processPayment(), который выведет нас из плагина.
     * <p/>
     * Номер карты и тип ввода запоминаем с предыдущей операции проверки
     *
     * @param amount
     * @throws CFTException
     */
    public void processPayWithGiftCard(BigDecimal amount) throws Exception {
        long amountLong = BigDecimalConverter.convertMoney(amount);
        getAdapter().setCallDone(true);
        getAdapter().setSum(amountLong);
        //Добавим проверку на кратность введенной суммы минимальной денежной единице
        if (CurrencyUtil.checkPaymentRatio(amountLong)) {
            if (!Factory.getTechProcessImpl().getCheck().isReturn()) {
                CFTGiftCardInfo info = getCftModel().authorize(amountLong, cardNumber, enterType);
                ((CFTGiftCardPaymentEntity) getAdapter().getPayment()).setAuthoriseData(info);
            }
        } else {
            getModel().setState(CFTGiftCardPaymentState.ENTER_CARD_NUMBER);
        }
        getAdapter().processPayment();
    }

    private int getFirstNotNull(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null) {
                return i;
            }
        }
        return -1;
    }

    private CardType calculateCardType(int i) {
        switch (i) {
            case 0:
                return CardType.TRACK1;
            case 1:
                return CardType.TRACK2;
            default:
                return CardType.TRACK3;

        }
    }

    private CFTGiftCardsModel getCftModel() {
        return getAdapter().getCFTGiftModel();
    }
}
