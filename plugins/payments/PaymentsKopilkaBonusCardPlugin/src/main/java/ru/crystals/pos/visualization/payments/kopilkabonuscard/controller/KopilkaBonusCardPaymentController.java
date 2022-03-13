package ru.crystals.pos.visualization.payments.kopilkabonuscard.controller;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.external.ExternalLoyaltyServiceException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.PurchaseExtDataKey;
import ru.crystals.pos.kopilka.KopilkaService;
import ru.crystals.pos.payments.KopilkaPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.ResBundlePaymentKopilkaBonusCard;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.model.KopilkaBonusCardPaymentModel;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.model.KopilkaBonusCardPaymentState;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Контроль процесса оплаты картой Копилка
 */
@Component
@ConditionalOnBean({KopilkaService.class})
public class KopilkaBonusCardPaymentController extends AbstractPaymentController {
    private final KopilkaService service;

    @Autowired
    KopilkaBonusCardPaymentController(KopilkaService service) {
        this.service = service;
    }

    /**
     * Запускается процесс оплаты
     */
    @Override
    public void processPayment(PaymentEntity payment) {
        KopilkaBonusCardPaymentModel kopilkaModel = getKopilkaModel();
        kopilkaModel.setPayment(payment);
        try {
            kopilkaModel.setState(KopilkaBonusCardPaymentState.PARSE_CARD);
            PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
            Optional<CardTypeEntity> first = check.getCardsWithType(CardTypeEntity.class)
                    .stream().filter(card -> KopilkaService.PROVIDER_NAME.equals(card.getProcessingName()) && !card.getCards().isEmpty()).findFirst();
            if (first.isPresent()) {
                CardTypeEntity externalCardsEntity = first.get();
                CardEntity current = externalCardsEntity.getCards().iterator().next();
                if (current.getCardNumberHash() != null) {
                    processGetCardInfo(InsertType.MSR, current.getCardNumberHash());
                } else {
                    kopilkaModel.setState(KopilkaBonusCardPaymentState.ENTER_CARD_NUMBER);
                }
            } else {
                kopilkaModel.setState(KopilkaBonusCardPaymentState.ENTER_CARD_NUMBER);
            }
        } catch (ExternalLoyaltyServiceException e) {
            kopilkaModel.getInfo().setExceptionText(e.getMessage());
            kopilkaModel.setState(KopilkaBonusCardPaymentState.ERROR);
        } catch (Exception e) {
            kopilkaModel.setState(KopilkaBonusCardPaymentState.ENTER_CARD_NUMBER);
        }
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        KopilkaPaymentEntity kopilkaPayment = (KopilkaPaymentEntity) payment;

        String cardNumberHash = kopilkaPayment.getCardNumberHash();
        String authCode = kopilkaPayment.getAuthCode();

        try {
            service.cancelHold(cardNumberHash, authCode);
        } catch (Exception e) {
            (getKopilkaModel()).getInfo().setExceptionText(e.getMessage());
            (getKopilkaModel()).setState(KopilkaBonusCardPaymentState.ERROR);
        }
    }

    /**
     * Все готово к процессингу оплаты, на форме подтверждения введена и проверена сумма оплаты. Запускаем процесс оплаты - если будет ошибка -
     * вернемся по эксепшену на вью, если все ок то запускаем processPayment(), который выведет нас из плагина. <p/> Номер карты и тип ввода
     * запоминаем с предыдущей операции проверки
     */
    public void processPayWithCard(BigDecimal amount) throws Exception {
        long amountLong = BigDecimalConverter.convertMoney(amount);
        try {
            KopilkaPaymentEntity modelPayment = (KopilkaPaymentEntity) getModel().getPayment();
            String hash = modelPayment.getCardNumberHash();
            String uuid = service.hold(hash, amount);
            KopilkaPaymentEntity payment = (KopilkaPaymentEntity) getAdapter().getPayment();
            payment.setAuthCode(uuid);
            payment.setCardNumberHash(hash);
            payment.setCardNumber(modelPayment.getCardNumber());

            fillExtData(getCurrentBalanceExcludePaymentSum() - amountLong);

            getAdapter().setCallDone(true);
            getAdapter().setSum(amountLong);
            getAdapter().processPayment();
            //Сохраним баланс с вычетом оплаты

        } catch (Exception e) {
            getKopilkaModel().getInfo().setExceptionText(e.getMessage());
            (getKopilkaModel()).setState(KopilkaBonusCardPaymentState.ERROR);
        }
    }

    public KopilkaBonusCardPaymentModel getKopilkaModel() {
        return (KopilkaBonusCardPaymentModel) getModel();
    }

    /**
     * Мы получили номер карты, теперь запросим информацию о карте в процессинге и покажем форму подтверждения оплаты, либо покажем ошибку (карта не
     * активна, неверный номер, ...)
     *
     * @param insertType  - способ ввода карты
     * @param textStrings - набор строк содержащих номер карты
     * @throws Exception - вслучае если что-то пошло не так :)
     */
    public void processGetCardInfo(InsertType insertType, String... textStrings) throws Exception {
        //прокатали карту - имеем целый список дорожек
        String cardHash = Stream.of(textStrings)
                .filter(str -> !Strings.isNullOrEmpty(str))
                .findFirst()
                .orElseThrow(() -> {
                    beepError("Kopilka: Cannot find " + (insertType == InsertType.MSR ? "not empty MSR track" : "barcode read by " + insertType));
                    return new ExternalLoyaltyServiceException(ResBundlePaymentKopilkaBonusCard.getString("WRONG_CARD"));
                });
        if (InsertType.SCANNER.equals(insertType) && !service.isElectronicCard(cardHash)) {
            throw new ExternalLoyaltyServiceException(ResBundlePaymentKopilkaBonusCard.getString("CARD_NOT_FOUND"));
        }
        CardBonusBalance bonusBalance = service.getBalance(cardHash);
        String cardUId = bonusBalance.getCardNumberUId();
        String cardNumber = bonusBalance.getCardNumber();

        verifyCardBalance(bonusBalance);
        PaymentEntity payment = getModel().getPayment();
        payment.setSumPay(bonusBalance.getBalanceElementary());
        ((KopilkaPaymentEntity) payment).setCardNumber(cardNumber);
        ((KopilkaPaymentEntity) payment).setCardNumberHash(cardUId);

        (getKopilkaModel()).setState(KopilkaBonusCardPaymentState.PAYMENT);
        (getKopilkaModel()).getInfo().setTotalBalance(bonusBalance.getBalanceElementary());
    }

    private void fillExtData(Long value) {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        purchaseEntity.addExtData(PurchaseExtDataKey.KOPILKA_TOTAL_BONUSES, String.valueOf(value));
    }

    private Long getCurrentBalanceExcludePaymentSum() {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        String currentTotalBonusesString = purchaseEntity.getExtData(PurchaseExtDataKey.KOPILKA_TOTAL_BONUSES);

        if (StringUtils.isNotBlank(currentTotalBonusesString)) {
            return Long.parseLong(currentTotalBonusesString);
        }
        return getKopilkaModel().getInfo().getTotalBalance();
    }

    private void verifyCardBalance(CardBonusBalance bonusBalance) throws ExternalLoyaltyServiceException {
        if (bonusBalance == null) {
            throw new ExternalLoyaltyServiceException(ResBundlePaymentKopilkaBonusCard.getString("NO_CONNECTION_TO_KOPILKA"));
        }
        if (bonusBalance.getBalanceElementary().compareTo(0L) <= 0) {
            throw new ExternalLoyaltyServiceException(ResBundlePaymentKopilkaBonusCard.getString("CARD_IS_EMPTY"));
        }
    }
}
