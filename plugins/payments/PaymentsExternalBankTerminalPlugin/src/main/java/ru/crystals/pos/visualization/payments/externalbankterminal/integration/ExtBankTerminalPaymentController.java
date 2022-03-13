package ru.crystals.pos.visualization.payments.externalbankterminal.integration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.payments.ExternalBankTerminalPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.utils.UnboxingUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class ExtBankTerminalPaymentController extends AbstractPaymentController<ExtBankTerminalPaymentPluginAdapter, ExtBankTerminalPaymentModel> {

    @Override
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        ExtBankTerminalPaymentInfo modelInfo = (ExtBankTerminalPaymentInfo) getModel().getInfo();
        PurchaseEntity pe = Factory.getTechProcessImpl().getCheck();
        // если были данные об оплате сохранены ранее (например, получены из МЧ), используем их и не запрашиваем у кассира
        modelInfo.setPrePayment(pe != null && CollectionUtils.isEmpty(pe.getPayments()) && pe.getPrePayment() != null
                && ExternalBankTerminalPaymentEntity.class.getSimpleName().equalsIgnoreCase(pe.getPrePayment().getPaymentType())
                ? pe.getPrePayment() : null);
        if (modelInfo.getPrePayment() != null && UnboxingUtils.valueOf(modelInfo.getPrePayment().getSumPay()) != 0) {
            processSumToPayEntered(CurrencyUtil.convertMoney(modelInfo.getPrePayment().getSumPay()));
        } else {
            getModel().setState(ExtBankTerminalPaymentState.ENTER_SUM);
        }
    }

    public void processSumToPayEntered(BigDecimal amount) {
        getModel().getPayment().setSumPay(CurrencyUtil.convertMoney(amount));
        getModel().getPayment().setSumPayBaseCurrency(getModel().getPayment().getSumPay());
        //Проверим кратность введенной суммы. Если проверка не пройдена - сразу пробуем оплатить.. и вернемся обратно с ошибкой
        if (CurrencyUtil.checkPaymentRatio(getModel().getPayment().getSumPay())) {
            String lastDigits = getPrePaymentProperties().get(ExternalBankTerminalPaymentEntity.PLUGIN_PROPERTY_CARD_NUMBER);
            if (lastDigits != null) {
                processLastDigitsEntered(lastDigits);
            } else {
                getModel().setState(ExtBankTerminalPaymentState.ENTER_LAST_4_DIGITS);
            }
        } else {
            processPayWithExtBankTerminal(new ExtBankTerminalPaymentInfo());
        }
    }

    public void processLastDigitsEntered(String text) {
        ((ExtBankTerminalPaymentInfo) getModel().getInfo()).setLastDigits(text);
        String authCode = getPrePaymentProperties().get(ExternalBankTerminalPaymentEntity.PLUGIN_PROPERTY_AUTH_CODE);
        if (authCode != null) {
            processAuthCodeEntered(authCode);
        } else {
            getModel().setState(ExtBankTerminalPaymentState.ENTER_AUTH_CODE);
        }
    }

    public void processAuthCodeEntered(String text) {
        ((ExtBankTerminalPaymentInfo) getModel().getInfo()).setAuthCode(text);
        String checkNum = getPrePaymentProperties().get(ExternalBankTerminalPaymentEntity.PLUGIN_PROPERTY_PURCHASE_NUMBER);
        if (StringUtils.isNumeric(checkNum)) {
            processReceiptNumberEntered(checkNum);
        } else {
            getModel().setState(ExtBankTerminalPaymentState.ENTER_RECEIPT_NUMBER);
        }
    }

    public void processReceiptNumberEntered(String number) {
        if (StringUtils.isNumeric(number)) {
            ((ExtBankTerminalPaymentInfo) getModel().getInfo()).setCheckNumber(Long.valueOf(number));
            getAdapter().getThreadPool().execute(() -> processPayWithExtBankTerminal());
        }
    }

    public void processReceiptNumberCanceled() {
        String authCode = getPrePaymentProperties().get(ExternalBankTerminalPaymentEntity.PLUGIN_PROPERTY_AUTH_CODE);
        if (authCode != null) {
            processAuthCodeCanceled();
        } else {
            getModel().setState(ExtBankTerminalPaymentState.ENTER_AUTH_CODE);
        }
    }

    public void processAuthCodeCanceled() {
        String lastDigits = getPrePaymentProperties().get(ExternalBankTerminalPaymentEntity.PLUGIN_PROPERTY_CARD_NUMBER);
        if (lastDigits != null) {
            processLastDigitsCanceled();
        } else {
            getModel().setState(ExtBankTerminalPaymentState.ENTER_LAST_4_DIGITS);
        }
    }

    public void processLastDigitsCanceled() {
        if (getPrePayment() != null && UnboxingUtils.valueOf(getPrePayment().getSumPay()) != 0) {
            processSumToPayEntered(CurrencyUtil.convertMoney(getPrePayment().getSumPay()));
        } else {
            getModel().getPayment().setSumPay(null);
            getModel().setState(ExtBankTerminalPaymentState.ENTER_SUM);
        }
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        getModel().setState(ExtBankTerminalPaymentState.ENTER_SUM);
    }

    public void processPayWithExtBankTerminal() {
        processPayWithExtBankTerminal((ExtBankTerminalPaymentInfo) getModel().getInfo());
    }

    public void processPayWithExtBankTerminal(ExtBankTerminalPaymentInfo info) {
        ((ExternalBankTerminalPaymentEntity) getAdapter().getPayment()).setAuthCode(info.getAuthCode());
        ((ExternalBankTerminalPaymentEntity) getAdapter().getPayment()).setCardNum(info.getLastDigits());
        ((ExternalBankTerminalPaymentEntity) getAdapter().getPayment()).setCheckNumber(info.getCheckNumber());
        getAdapter().setCallDone(true);
        getAdapter().setSum(getModel().getPayment().getSumPay());
        getAdapter().processPayment();

    }

    public boolean validateDigits(String digits) {
        return StringUtils.trimToEmpty(digits).length() == 4;
    }

    private Map<String, String> getPrePaymentProperties() {
        PaymentEntity prePayment = getPrePayment();
        return prePayment != null ? prePayment.getPluginPropertiesMap() : Collections.emptyMap();
    }

    private PaymentEntity getPrePayment() {
        return ((ExtBankTerminalPaymentInfo) getModel().getInfo()).getPrePayment();
    }
}
