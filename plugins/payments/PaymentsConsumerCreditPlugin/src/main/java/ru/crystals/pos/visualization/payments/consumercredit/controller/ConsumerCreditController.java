package ru.crystals.pos.visualization.payments.consumercredit.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import ru.crystals.discounts.transport.SetV5AdvertiseActionType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.integration.ConsumerCreditAdapter;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditBarcodeData;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditInfo;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditModel;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * "Мозг" плаигна оплат потребительским кредитом
 * Тут описана вся логика работы плагина
 */
public class ConsumerCreditController extends AbstractPaymentController<ConsumerCreditAdapter, ConsumerCreditModel> {
    private static final String BANK_PRODUCT_CODE_WITHOUT_DISCOUNTS_PREFIX = "@";

    /**
     * Точка входа в плагин при оплате
     *
     * @param payment
     */
    @Override
    public void processPayment(PaymentEntity payment) {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        ConsumerCreditPaymentEntity currentPayment = (ConsumerCreditPaymentEntity) payment;
        currentInfo.setShowOnscreenKeyboard(getAdapter().isShowOnscreenKeyboard());
        getModel().setPayment(payment);
        fillInforFromEntity(currentInfo, currentPayment);
        getModel().setState(ConsumerCreditState.ENTER_SUM);
    }

    /**
     * Заполняем PaymentInfo в модли из пришедшей сущности
     *
     * @param currentInfo    PaymentInfo
     * @param currentPayment сущность оплаты
     */
    private void fillInforFromEntity(ConsumerCreditInfo currentInfo, ConsumerCreditPaymentEntity currentPayment) {
        currentInfo.setContractNumber(currentPayment.getContractNum());
        currentInfo.setFIO(currentPayment.getFio());
        currentInfo.setSelectedBank(currentPayment.getBank());
        currentInfo.setSelectedProduct(currentPayment.getBankProduct());
    }

    /**
     * Точка входа в плагин при возврате
     *
     * @param payment
     */
    @Override
    public void processCancelPayment(PaymentEntity payment) {

    }

    public PaymentType getCashPaymentType() {
        return getAdapter().getCashPaymentType();
    }

    /**
     * Пользователь отменил выбор банка
     */
    public void processCancelChooseBank() {
        getModel().getPayment().setSumPay(null);
        getModel().setState(ConsumerCreditState.ENTER_SUM);
    }

    /**
     * Пользователь ввел сумму к оплате/возврату
     */
    public void processEnterSum(BigDecimal sum) {
        getModel().getPayment().setSumPay(CurrencyUtil.convertMoney(sum));

        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setBanks(new ArrayList<>(getCashPaymentType().getBankProducts().keySet()));

        if (getAdapter().isRefund()) {
            getModel().setState(ConsumerCreditState.CONFIRM_PAYMENT);
        } else {
            getModel().setState(ConsumerCreditState.CHOOSE_BANK);
        }
    }

    /**
     * Пользователь выбрал банк
     *
     * @param selectedValue выбранное значение
     */
    public void processChooseBank(String selectedValue) {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setSelectedBank(selectedValue);
        currentInfo.setBankProducts(getCashPaymentType().getBankProducts().get(selectedValue));

        getModel().setState(ConsumerCreditState.CHOOSE_BANK_PRODUCT);
    }

    /**
     * Пользователь выбрал банковский продукт (кредит)
     *
     * @param selectedValue выбранное значение
     */
    public void processChooseProduct(String selectedValue) {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setSelectedProduct(selectedValue);

        getModel().setState(ConsumerCreditState.ENTER_FIO);
    }

    /**
     * Пользователь отменил выбор банковского продукта (кредита)
     */
    public void processCancelChooseProduct() {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setSelectedProduct(null);
        getModel().setState(ConsumerCreditState.CHOOSE_BANK);
    }

    /**
     * Пользователь ввел ФИО
     *
     * @param textValue
     */
    public void processFilledFIO(String textValue) {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setFIO(textValue);
        getModel().setState(ConsumerCreditState.ENTER_CONTRACT_NUMBER);
    }

    /**
     * Пользователь отменил ввод ФИО
     */
    public void processCancelFillFIO() {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setFIO(null);
        getModel().setState(ConsumerCreditState.CHOOSE_BANK_PRODUCT);
    }

    /**
     * Пользователь ввел номер контракта
     *
     * @param textValue
     */
    public void processFilledContract(String textValue) {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setContractNumber(textValue);
        getModel().setState(ConsumerCreditState.CONFIRM_PAYMENT);
    }

    /**
     * Пользователь отменил ввод номера контракта
     */
    public void processCancelFillContract() {
        ConsumerCreditInfo currentInfo = (ConsumerCreditInfo) getModel().getInfo();
        currentInfo.setContractNumber(null);
        getModel().setState(ConsumerCreditState.ENTER_FIO);
    }

    public void processConfirmPayment() {
        ConsumerCreditInfo info = (ConsumerCreditInfo) getModel().getInfo();
        ((ConsumerCreditPaymentEntity) getAdapter().getPayment()).setBank(info.getSelectedBank());
        ((ConsumerCreditPaymentEntity) getAdapter().getPayment()).setBankProduct(info.getSelectedProduct());
        ((ConsumerCreditPaymentEntity) getAdapter().getPayment()).setContractNum(info.getContractNumber());
        ((ConsumerCreditPaymentEntity) getAdapter().getPayment()).setFio(info.getFIO());
        getAdapter().setCallDone(true);
        getAdapter().setSum(getModel().getPayment().getSumPay());
        getAdapter().processPayment();
    }

    public void processCancelConfirmPayment() {
        if (getAdapter().isRefund()) {
            getModel().setState(ConsumerCreditState.ENTER_SUM);
        } else {
            getModel().setState(ConsumerCreditState.ENTER_CONTRACT_NUMBER);
        }
    }

    public void processDataFromBarcode(String barcode) {
        if (!getAdapter().isRefund()) {
            ConsumerCreditBarcodeData barcodeData = ConsumerCreditBarcodeData.getEntity(barcode);
            if (verifyEntity(barcodeData)) {
                fillInfoFromBarcodeData(barcodeData);
                getModel().setState(ConsumerCreditState.CONFIRM_PAYMENT);
            }
        }
    }

    private void fillInfoFromBarcodeData(ConsumerCreditBarcodeData barcodeData) {
        ConsumerCreditInfo info = (ConsumerCreditInfo) getModel().getInfo();
        info.setContractNumber(barcodeData.getContractNumber());
        info.setFIO(barcodeData.getFIO());
        info.setSelectedBank(barcodeData.getBankCode());
        info.setSelectedProduct(barcodeData.getProductCode());
        getModel().getPayment().setSumPay(barcodeData.getCreditSum());
    }

    private boolean verifyEntity(ConsumerCreditBarcodeData barcode) {
        if (barcode == null) {
            showException(ResBundlePaymentConsumerCredit.getString("WRONG_BARCODE_FORMAT"));
            return false;
        }

        long creditSum = barcode.getCreditSum();
        if (creditSum <= 0) {
            showException(ResBundlePaymentConsumerCredit.getString("WRONG_AMOUNT_VALUE_ZERO_CREDIT_SUM"));
            return false;
        }

        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();

        if (!StringUtils.startsWith(barcode.getProductCode(), BANK_PRODUCT_CODE_WITHOUT_DISCOUNTS_PREFIX) && MapUtils.isNotEmpty(getCashPaymentType().getBankProducts())
                && CollectionUtils.isNotEmpty(getCashPaymentType().getBankProducts().get(barcode.getBankCode())) && getCashPaymentType().getBankProducts().get(barcode.getBankCode()).contains(BANK_PRODUCT_CODE_WITHOUT_DISCOUNTS_PREFIX + barcode.getProductCode())) {
            barcode.setProductCode(BANK_PRODUCT_CODE_WITHOUT_DISCOUNTS_PREFIX + barcode.getProductCode());
        }
        discardDiscountsIfNeeded(barcode, check);

        if (check.getToPaySumm().compareTo(creditSum) == -1) {
            showException(ResBundlePaymentConsumerCredit.getString("WRONG_AMOUNT_VALUE"));
            return false;
        }

        for (PaymentEntity payment : check.getPayments()) {
            if (payment instanceof ConsumerCreditPaymentEntity) {
                ConsumerCreditPaymentEntity creditPayment = (ConsumerCreditPaymentEntity) payment;
                if (StringUtils.equals(creditPayment.getContractNum(), barcode.getContractNumber()) &&
                        StringUtils.equals(creditPayment.getBank(), barcode.getBankCode()) &&
                        StringUtils.equals(creditPayment.getBankProduct(), barcode.getProductCode())) {
                    showException(String.format(ResBundlePaymentConsumerCredit.getString("CREDIT_CONTRACT_ALREADY_APPLIED"),
                            barcode.getContractNumber()));
                    return false;
                }
            }
        }
        return true;
    }

    private void showException(String text) {
        ((ConsumerCreditInfo) getModel().getInfo()).setErrorMessage(text);
        getModel().setState(ConsumerCreditState.ERROR);
    }

    public void discardDiscountsIfNeeded(ConsumerCreditBarcodeData barcode, PurchaseEntity check) {
        if (check.getAlreadyPaidSumm().equals(0L) && (barcode.getProductCode().startsWith(BANK_PRODUCT_CODE_WITHOUT_DISCOUNTS_PREFIX))) {
            check.setAllowedDiscountTypesForPurchase(Arrays.asList(new String[]{String.valueOf(SetV5AdvertiseActionType.SECOND_PRICE.getValue())}));
            Factory.getInstance().getMainWindow().getCheckContainer().reSubtotalFromPayment(getAdapter());
        }
    }
}
