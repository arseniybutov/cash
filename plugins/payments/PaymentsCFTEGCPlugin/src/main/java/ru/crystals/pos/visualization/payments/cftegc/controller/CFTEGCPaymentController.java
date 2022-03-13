package ru.crystals.pos.visualization.payments.cftegc.controller;

import ru.crystals.pos.cards.cft.CFTCardState;
import ru.crystals.pos.cards.cft.CFTGiftCardInfo;
import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.configurator.core.Constants.CFTType;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.model.cft.CFTEGCModel;
import ru.crystals.pos.payments.CFTEGCPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.cftegc.ResBundlePaymentCFTEGC;
import ru.crystals.pos.visualization.payments.cftegc.integration.CFTEGCPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.cftegc.model.CFTEGCPaymentInfo;
import ru.crystals.pos.visualization.payments.cftegc.model.CFTEGCPaymentModel;
import ru.crystals.pos.visualization.payments.cftegc.model.CFTEGCPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;

import java.math.BigDecimal;

public class CFTEGCPaymentController extends AbstractPaymentController<CFTEGCPaymentPluginAdapter, CFTEGCPaymentModel> {

    /**
     * Запускается процесс оплаты подарочной картой ЦФТ
     *
     * @param payment
     */
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        if (getAdapter().isRefund()) {
            getAdapter().setSum(payment.getSumPay());
            getModel().setState(CFTEGCPaymentState.REFUND);
        } else {
            getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
        }
    }

    public void processCancelPayment(PaymentEntity payment) {
        CFTEGCPaymentEntity entity = (CFTEGCPaymentEntity) payment;
        try {
            getCftModel().cancelAuthorize(entity);
        } catch (Exception e) {
            beepError("CFTPaymentsPlugin: error canceling payment - " + e.getMessage());
        }
    }

    public void applyPinCode(String pinCode) throws Exception {
        if (pinCode == null || pinCode.length() != 3) {
            throw new Exception(ResBundlePaymentCFTEGC.getString("BAD_EGC_PIN"));
        }
        ((CFTEGCPaymentInfo) getModel().getInfo()).setPinCode(pinCode);

    }

    public void applyCardNumber(CardType cardType, String cardNumber) throws CFTException {
        getCftModel().getCFTGiftCards().checkCardNumber(CFTType.CFTegc, cardNumber, cardType);
        CFTEGCPaymentInfo pInfo = (CFTEGCPaymentInfo) getModel().getInfo();
        pInfo.setCardNumber(cardNumber);
        pInfo.setCardType(cardType);
        getModel().setState(CFTEGCPaymentState.ENTER_PIN_CODE);
    }

    public void processGetCardInfo() throws Exception {
        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
        CFTEGCPaymentInfo pInfo = (CFTEGCPaymentInfo) getModel().getInfo();

        try {
            CFTGiftCardInfo info = getCftModel().getCardInfo(pInfo.buildTrack2(), CardType.TRACK2);

            if (info != null) {
                if (info.getCardState() != CFTCardState.ACTIVE) {
                    getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
                    throw new Exception(ResBundlePaymentCFTEGC.getString("CARD_BLOCKED"));
                } else if (info.getAmount() == null || (info.getAmount() == 0 && !check.isReturn())) {
                    getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
                    throw new Exception(ResBundlePaymentCFTEGC.getString("CARD_IS_EMPTY"));
                }
            } else {
                getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
                throw new Exception(ResBundlePaymentCFTEGC.getString("CARD_IS_EMPTY"));
            }
            if (!check.isReturn()) {
                getModel().getPayment().setSumPay(info.getAmount());
            }
        } catch (CFTException e) {
            getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
            throw new Exception(e.getMessage());
        }
        getModel().setState(CFTEGCPaymentState.PAYMENT);
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
                CFTEGCPaymentInfo pInfo = (CFTEGCPaymentInfo) getModel().getInfo();
                CFTGiftCardInfo info = getCftModel().authorize(amountLong, pInfo.buildTrack2(), CardType.TRACK2);
                ((CFTEGCPaymentEntity) getAdapter().getPayment()).applyAuthoriseData(info);
            }
        } else {
            getModel().setState(CFTEGCPaymentState.ENTER_CARD_NUMBER);
        }
        getAdapter().processPayment();
    }

    private CFTEGCModel getCftModel() {
        return getAdapter().getCFTEGCModel();
    }
}
