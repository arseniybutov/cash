package ru.crystals.pos.visualization.payments.siebelbonuscard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.cards.CardEntity;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.external.ExternalLoyaltyServiceException;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.SiebelBonusCardPaymentEntity;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.pos.visualization.payments.siebelbonuscard.ResBundlePaymentSiebelBonusCard;
import ru.crystals.pos.visualization.payments.siebelbonuscard.model.SiebelBonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.siebelbonuscard.model.SiebelBonusCardPaymentState;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnBean({SiebelService.class})
public class SiebelBonusCardPaymentController extends AbstractPaymentController {
    private final SiebelService service;
    private boolean cardApplied = false;
    private TechProcessInterface techProcessInterface;

    @Autowired
    SiebelBonusCardPaymentController(SiebelService service) {
        this.service = service;
    }

    @Autowired
    void setTechProcessInterface(TechProcessInterface techProcessInterface) {
        this.techProcessInterface = techProcessInterface;
    }

    /**
     * Запускается процесс оплаты Siebel
     */
    @Override
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        try {
            getModel().setState(SiebelBonusCardPaymentState.PARSE_INTERNAL_CARD);
            if (checkContainsSiebelPayment()) {
                throw new ExternalLoyaltyServiceException(ResBundlePaymentSiebelBonusCard.getString("UNABLE_TO_PAY_TWICE"));
            }
            CardEntity current;
            if (Factory.getTechProcessImpl().getCheck().isReturn() && Factory.getTechProcessImpl().getCheck().getSuperPurchase() != null) {
                // Будем пытаться найти карту в оригинальном чеке.
                current = service.getCurrentCardIfApplied(Factory.getTechProcessImpl().getCheck().getSuperPurchase());
            } else {
                current = service.getCurrentCardIfApplied(Factory.getTechProcessImpl().getCheck());
            }
            if (current == null) {
                getModel().setState(SiebelBonusCardPaymentState.ENTER_CARD_NUMBER);
                cardApplied = false;
            } else {
                if (!isRefund()) {
                    verifyCardBalance(current);
                    payment.setSumPay(current.getCardBonusBalance().getBalanceElementary());
                }
                ((SiebelBonusCardPaymentInfo) getModel().getInfo()).setCardNumber(current.getNumber());
                ((SiebelBonusCardPaymentEntity) payment).setCardNumber(current.getNumber());
                cardApplied = true;
                getModel().setState(SiebelBonusCardPaymentState.PAYMENT);
            }
        } catch (ExternalLoyaltyServiceException e) {
            ((SiebelBonusCardPaymentInfo) getModel().getInfo()).setExceptionText(e.getMessage());
            getModel().setState(SiebelBonusCardPaymentState.ERROR);
        } catch (Exception e) {
            getModel().setState(SiebelBonusCardPaymentState.ENTER_CARD_NUMBER);
        }
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        // Не может быть отмены оплаты. Списание совершается после фискализации.
    }

    /**
     * Мы получили номер карты, теперь запросим информацию о карте в процессинге и покажем форму подтверждения оплаты, либо покажем ошибку (карта не
     * активна, неверный номер, ...)
     */
    public void processGetCardInfo(CardType enterType, String... textStrings) throws Exception {
        String cardNumber;
        switch (enterType) {
            case BARCODE:
            case PAN:
                cardNumber = textStrings[0];
                break;
            default:
                //прокатали карту - имеем целый список дорожек
                int i = getFirstNotNull(textStrings);
                if (i < 0) {
                    beepError("Siebel: Cannot find not empty MSR track");
                    throw new ExternalLoyaltyServiceException(ResBundlePaymentSiebelBonusCard.getString("WRONG_CARD"));
                }
                cardNumber = textStrings[i];
        }
        CardEntity card = service.getCardStatus(cardNumber).getCardEntity();
        verifyCardBalance(card);
        getModel().getPayment().setSumPay(card.getCardBonusBalance().getBalanceElementary());
        getModel().setState(SiebelBonusCardPaymentState.PAYMENT);
        ((SiebelBonusCardPaymentEntity) getModel().getPayment()).setCardNumber(cardNumber);
    }

    /**
     * Все готово к процессингу оплаты, на форме подтверждения введена и проверена сумма оплаты. Запускаем процесс оплаты - если будет ошибка -
     * вернемся по эксепшену на вью, если все ок то запускаем processPayment(), который выведет нас из плагина. <p/> Номер карты и тип ввода
     * запоминаем с предыдущей операции проверки
     */
    public void processPayWithGiftCard(BigDecimal amount) throws Exception {
        long amountLong = BigDecimalConverter.convertMoney(amount);
        getAdapter().setCallDone(true);
        getAdapter().setSum(amountLong);
        getAdapter().processPayment();
    }

    private int getFirstNotNull(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null && !strings[i].equals("null")) {
                return i;
            }
        }
        return -1;
    }

    public boolean isCardApplied() {
        return cardApplied;
    }

    public void setCardApplied(boolean cardApplied) {
        this.cardApplied = cardApplied;
    }

    private boolean checkContainsSiebelPayment() {
        List<PaymentEntity> payments = techProcessInterface.getCheck().getPayments();
        for (PaymentEntity payment : payments) {
            if (payment instanceof SiebelBonusCardPaymentEntity) {
                return true;
            }
        }
        return false;
    }

    private void verifyCardBalance(CardEntity card) throws ExternalLoyaltyServiceException {
        if (card.getCardBonusBalance() == null) {
            throw new ExternalLoyaltyServiceException(ResBundlePaymentSiebelBonusCard.getString("NO_CONNECTION_TO_SIEBEL"));
        }
        if (card.getCardBonusBalance().getBalanceElementary().compareTo(0L) <= 0) {
            throw new ExternalLoyaltyServiceException(ResBundlePaymentSiebelBonusCard.getString("CARD_IS_EMPTY"));
        }
    }
}
