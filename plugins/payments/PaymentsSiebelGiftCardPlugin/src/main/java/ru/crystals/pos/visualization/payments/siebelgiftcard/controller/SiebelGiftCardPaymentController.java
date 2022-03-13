package ru.crystals.pos.visualization.payments.siebelgiftcard.controller;

import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.cards.siebel.SiebelGiftCardResult;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.cards.siebel.exception.SiebelServiceException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.SiebelGiftCardPaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.pos.visualization.payments.siebelgiftcard.ResBundlePaymentSiebelGiftCard;
import ru.crystals.pos.visualization.payments.siebelgiftcard.model.SiebelGiftCardPaymentInfo;
import ru.crystals.pos.visualization.payments.siebelgiftcard.model.SiebelGiftCardPaymentState;
import ru.crystals.siebel.SiebelUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@ConditionalOnBean({SiebelService.class})
public class SiebelGiftCardPaymentController extends AbstractPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SiebelGiftCardPaymentController.class);

    /**
     * Максимальное число попыток подтверждения карты.
     */
    private static final int MAX_ATTEMPTS = 3;

    private final SiebelService service;

    @Autowired
    SiebelGiftCardPaymentController(SiebelService service) {
        this.service = service;
    }

    /**
     * Запускается процесс оплаты подарочной картой Siebel.
     *
     * @param payment
     */
    @Override
    public void processPayment(PaymentEntity payment) {
        if (getAdapter().isRefund()) {
            // Возврат возможен только наличными
            return;
        }
        getModel().setPayment(payment);
        getModel().setState(SiebelGiftCardPaymentState.ENTER_CARD_NUMBER);
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        if (!(payment instanceof SiebelGiftCardPaymentEntity)) {
            log.error("Error canceling payment: incorrect payment received {}", payment);
            return;
        }

        SiebelGiftCardPaymentEntity entity = (SiebelGiftCardPaymentEntity) payment;

        try {
            service.refundGiftCard(getPurchase(), entity.getCardNumber(), entity.getEndSumPay());
        } catch (CardsException | SiebelServiceException e) {
            log.error("Error canceling payment", e);
            beepError("SiebelPaymentsPlugin: error canceling payment - " + e.getMessage());
        }
    }

    /**
     * Получили номер карты, запросим максимальную сумму для списания или покажем ошибку (карта не активна, неверный номер, ...).
     *
     * @param insertType  - способ ввода карты
     * @param textStrings - набор строк содержащих номер карты
     * @throws CardsException
     * @throws SiebelServiceException
     */
    public void processCardInput(InsertType insertType, String... textStrings) throws CardsException, SiebelServiceException {
        String cardNumber = Stream.of(textStrings)
                .filter(str -> !Strings.isNullOrEmpty(str))
                .findFirst()
                .orElseThrow(() -> {
                    String message = "Siebel: Cannot find " + (insertType == InsertType.MSR ? "not empty MSR track" : "barcode read by " + insertType);
                    log.error(message);
                    beepError(message);
                    return new SiebelServiceException(ResBundlePaymentSiebelGiftCard.getString("WRONG_CARD"));
                });

        PurchaseEntity purchase = getPurchase();
        SiebelGiftCardResult result = service.determinePaymentSum(purchase, cardNumber);

        if (result.getAmount() == null || result.getAmount() <= 0) {
            log.warn("Incorrect card amount received.");
            throw new SiebelServiceException(StringUtils.isNotEmpty(result.getCashierMessage())
                    ? SiebelUtils.getFormattedCashierMessage(result.getCashierMessage())
                    : ResBundlePaymentSiebelGiftCard.getString("GIFTCARD_PAYMENT_UNAVAILABLE")
            );
        }

        // По одной и той же карте можно только один раз расплатиться в одном чеке
        boolean cardAlreadyApplied = CollectionUtils.isNotEmpty(purchase.getPayments()) &&
                purchase.getPayments().stream()
                        .anyMatch(p -> p instanceof SiebelGiftCardPaymentEntity &&
                                result.getCardNumber().equals(((SiebelGiftCardPaymentEntity) p).getCardNumber()));

        if (cardAlreadyApplied) {
            log.warn("Card with number {} is already applied in purchase.", result.getCardNumber());
            throw new SiebelServiceException(ResBundlePaymentSiebelGiftCard.getString("CARD_ALREADY_APPLIED"));
        }

        // Покажем доступную сумму с учетом совершенных оплат. По сути костыль из-за кривого расчета в Siebel.
        Long paidSum = CollectionUtils.isEmpty(purchase.getPayments()) ? 0L :
                purchase.getPayments().stream()
                        .filter(p -> !(p instanceof SiebelGiftCardPaymentEntity))
                        .mapToLong(PaymentEntity::getEndSumPay)
                        .sum();

        getModel().getPayment().setSumPay(result.getAmount() - paidSum);
        ((SiebelGiftCardPaymentEntity) getModel().getPayment()).setCardNumber(result.getCardNumber());
        ((SiebelGiftCardPaymentInfo) getModel().getInfo()).setSiebelGiftCardResult(result);

        if (result.getVerificationCode() != null) {
            getModel().setState(SiebelGiftCardPaymentState.VERIFICATION);
        } else if (StringUtils.isNotBlank(result.getCashierMessage())) {
            getModel().setState(SiebelGiftCardPaymentState.CASHIER_MESSAGE);
        } else {
            getModel().setState(SiebelGiftCardPaymentState.PAYMENT);
        }
    }

    /**
     * Получили код подтверждения из SMS - проверим его.
     *
     * @param code - код подтверждения из SMS
     * @throws SiebelServiceException
     */
    public void processVerificationCodeInput(String code) throws SiebelServiceException {
        SiebelGiftCardResult result = ((SiebelGiftCardPaymentInfo) getModel().getInfo()).getSiebelGiftCardResult();
        if (StringUtils.isNumeric(code) && Objects.equals(new BigDecimal(code), result.getVerificationCode())) {
            if (StringUtils.isNotBlank(result.getCashierMessage())) {
                getModel().setState(SiebelGiftCardPaymentState.CASHIER_MESSAGE);
            } else {
                getModel().setState(SiebelGiftCardPaymentState.PAYMENT);
            }
            return;
        }
        if (result.getVerificationAttempts().incrementAndGet() == MAX_ATTEMPTS) {
            getModel().setState(SiebelGiftCardPaymentState.ENTER_CARD_NUMBER);
        }
        log.error("Incorrect code to verify gift card: {}", code);
        throw new SiebelServiceException(ResBundleVisualization.getString("INCORRECT_CODE"));
    }

    /**
     * Все готово к процессингу оплаты, на форме подтверждения введена и проверена сумма оплаты.
     * Запускаем процесс оплаты - если будет ошибка - вернемся по эксепшену на вью, если все ок
     * то запускаем processPayment(), который выведет нас из плагина.
     * <p/>
     * Номер карты запоминаем с предыдущей операции проверки.
     *
     * @param amount
     * @throws CardsException
     * @throws SiebelServiceException
     */
    public void processPayWithGiftCard(BigDecimal amount) throws CardsException, SiebelServiceException {
        long amountLong = BigDecimalConverter.convertMoney(amount);
        String cardNumber = ((SiebelGiftCardPaymentEntity) getModel().getPayment()).getCardNumber();
        SiebelGiftCardResult result = service.payByGiftCard(getPurchase(), cardNumber, amountLong);

        SiebelGiftCardPaymentEntity payment = (SiebelGiftCardPaymentEntity) getAdapter().getPayment();

        payment.setCardNumber(result.getCardNumber());
        getAdapter().setCallDone(true);
        getAdapter().setSum(amountLong);
        getAdapter().processPayment();
    }

    /**
     * Сообщение кассиру от Siebel прочитано, переход к оплате.
     */
    public void processCashierMessageRead() {
        getModel().setState(SiebelGiftCardPaymentState.PAYMENT);
    }

    private PurchaseEntity getPurchase() throws SiebelServiceException {
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();
        if (purchase == null) {
            log.error("Current purchase is null");
            throw new SiebelServiceException(ResBundlePaymentSiebelGiftCard.getString("ERROR"));
        }
        return purchase;
    }
}
