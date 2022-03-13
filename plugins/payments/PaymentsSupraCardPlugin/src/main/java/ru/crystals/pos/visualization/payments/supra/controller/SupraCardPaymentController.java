package ru.crystals.pos.visualization.payments.supra.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnModule;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.SupraPaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentModel;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentState;
import ru.crystals.supra.SupraBridge;
import ru.crystals.supra.exception.SupraException;
import ru.crystals.supra.exception.SupraResultException;
import ru.crystals.supra.ru.crystals.supra.api.SupraResponse;

import java.math.BigDecimal;

/**
 * Created by s.pavlikhin on 08.06.2017.
 */
@Component
@ConditionalOnModule(Constants.SUPRA_CARD_PROCESSING_NAME)
public class SupraCardPaymentController extends AbstractPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(SupraCardPaymentController.class);
    private final SupraBridge supraBridge;

    @Autowired
    SupraCardPaymentController(SupraBridge supraBridge) {
        this.supraBridge = supraBridge;
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        getModel().getInfo().setPosNumber(Math.toIntExact(Factory.getTechProcessImpl().getShift().getCashNum()));
    }

    /**
     * Очищает результаты предыдущей оплаты
     */
    public void clearInfo() {
        getModel().getInfo().setLastFour(null);
        getModel().getInfo().setErrorMessage(null);
        getModel().getInfo().setCardNumber(null);
    }

    /**
     * Перед оплатой, карту необходимо проверить
     *
     * @param lastFour
     * @throws SupraException
     */
    public void verifyCard(String lastFour) throws SupraException {
        if (StringUtils.isEmpty(lastFour)) {
            throw new SupraResultException(ResBundlePaymentSupraCard.getString("RESTRICT_EMPTY_LAST_FOUR"));
        }
        int posNumber = getModel().getInfo().getPosNumber();
        String cardNumber = getModel().getInfo().getCardNumber();

        //Проверка ResultCode внутри
        SupraResponse response = supraBridge.checkCard(posNumber, cardNumber, lastFour);

        Long currentAmount = response.getAmount();

        if (currentAmount <= 0) {
            throw new SupraResultException(ResBundlePaymentSupraCard.getString("NOT_ENOUGH_BONUSES"));
        }

        getModel().getInfo().setAmount(currentAmount);
        getModel().setState(SupraCardPaymentState.ENTER_AMOUNT);


    }

    public void processOperation(BigDecimal sumToPay) {
        SupraPaymentEntity payment = (SupraPaymentEntity) getAdapter().getPayment();
        long amountLong = BigDecimalConverter.convertMoney(sumToPay);

        try {
            int posNumber = getModel().getInfo().getPosNumber();
            String cardNumber = getModel().getInfo().getCardNumber();
            String lastFour = getModel().getInfo().getLastFour();

            //Проверка ResultCode внутри
            SupraResponse response = supraBridge.makePayment(posNumber, cardNumber, lastFour, amountLong);

            payment.setCardNumber(cardNumber);
            payment.setSumPay(amountLong);
            payment.setVerificationNumber(lastFour);
            payment.setAuthCode(response.getAuthCode());

            getAdapter().setCallDone(true);
            getAdapter().setSum(amountLong);
            getAdapter().processPayment();
            getModel().setState(SupraCardPaymentState.NOT_SET);

        } catch (Exception e) {
            logger.error("Error while process payment", e);
            String errorMsg = ResBundlePaymentSupraCard.getString("ERROR");
            if (e instanceof SupraResultException) {
                errorMsg = e.getMessage();
            }
            getModel().getInfo().setErrorMessage(errorMsg);
            getModel().setState(SupraCardPaymentState.ERROR);
        } finally {
            clearInfo();
        }
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        SupraPaymentEntity supraPayment = (SupraPaymentEntity) payment;

        int posNumber = Math.toIntExact(Factory.getTechProcessImpl().getShift().getCashNum());
        String cardNumber = supraPayment.getCardNumber();
        String lastFour = supraPayment.getVerificationNumber();

        Long authCode = Long.parseLong(supraPayment.getAuthCode());

        try {
            supraBridge.refund(posNumber, cardNumber, lastFour, authCode, payment.getSumPay());
            getModel().setState(SupraCardPaymentState.NOT_SET);

        } catch (Exception e) {
            logger.error("Error while cancel payment", e);
            String errorMsg = ResBundlePaymentSupraCard.getString("ERROR");
            if (e instanceof SupraResultException) {
                errorMsg = e.getMessage();
            }
            getModel().getInfo().setErrorMessage(errorMsg);
            getModel().setState(SupraCardPaymentState.ERROR);
        } finally {
            clearInfo();
        }
    }

    @Override
    public SupraCardPaymentModel getModel() {
        return (SupraCardPaymentModel) super.getModel();
    }
}
