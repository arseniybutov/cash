package ru.crystals.pos.visualization.payments.consumercredit.view;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditInfo;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditState;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditConfirmForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditContractInputForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditFIOInputForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditOnscreenContractInputForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditOnscreenFIOInputForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditPaymentChooseBankForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditPaymentChooseProductForm;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.ConsumerCreditPaymentEnterSum;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;

/**
 * Created by myaichnikov on 19.11.2014.
 */
public class ConsumerCreditView extends AbstractPaymentView<ConsumerCreditController> {
    private final ConsumerCreditPaymentChooseBankForm chooseBankForm;

    private final ConsumerCreditPaymentEnterSum enterSumForm;
    private final ConsumerCreditPaymentChooseProductForm chooseProductForm;
    private final ConsumerCreditOnscreenFIOInputForm onscreenKbFioForm;
    private final ConsumerCreditOnscreenContractInputForm onscreenKbContractForm;
    private final ConsumerCreditFIOInputForm fioForm;
    private final ConsumerCreditContractInputForm contractForm;
    private final ConsumerCreditConfirmForm confirmForm;
    private final CommonMessageForm messageForm;

    public ConsumerCreditView() {
        this.enterSumForm = new ConsumerCreditPaymentEnterSum(this);
        this.chooseBankForm = new ConsumerCreditPaymentChooseBankForm(this);
        this.chooseProductForm = new ConsumerCreditPaymentChooseProductForm(this);
        this.onscreenKbFioForm = new ConsumerCreditOnscreenFIOInputForm(this, ResBundlePaymentConsumerCredit.getString("FIO").toUpperCase(),
                ResBundlePaymentConsumerCredit.getString("ENTER_FIO").toLowerCase());
        this.onscreenKbContractForm = new ConsumerCreditOnscreenContractInputForm(this, ResBundlePaymentConsumerCredit.getString("CONTRACT_NUMBER_NAME").toLowerCase(),
                ResBundlePaymentConsumerCredit.getString("ENTER_CONTRACT_NUMBER").toLowerCase());
        this.fioForm = new ConsumerCreditFIOInputForm(this, ResBundlePaymentConsumerCredit.getString("FIO").toUpperCase(), ResBundlePaymentConsumerCredit.getString(
                "ENTER_FIO").toLowerCase());
        this.contractForm = new ConsumerCreditContractInputForm(this, ResBundlePaymentConsumerCredit.getString("CONTRACT_NUMBER_NAME").toLowerCase(),
                ResBundlePaymentConsumerCredit.getString("ENTER_CONTRACT_NUMBER").toLowerCase());
        this.confirmForm = new ConsumerCreditConfirmForm(this);
        this.messageForm = new CommonMessageForm(this);

        this.add(enterSumForm, enterSumForm.getClass().getName());
        this.add(chooseBankForm, chooseBankForm.getClass().getName());
        this.add(chooseProductForm, chooseProductForm.getClass().getName());
        this.add(onscreenKbFioForm, onscreenKbFioForm.getClass().getName());
        this.add(fioForm, fioForm.getClass().getName());
        this.add(contractForm, contractForm.getClass().getName());
        this.add(onscreenKbContractForm, onscreenKbContractForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (ConsumerCreditController) controller;

        enterSumForm.setController(controller);
        chooseBankForm.setController(controller);
        chooseProductForm.setController(controller);
        onscreenKbFioForm.setController(controller);
        fioForm.setController(controller);
        onscreenKbContractForm.setController(controller);
        contractForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        ConsumerCreditInfo info = (ConsumerCreditInfo) event.getInfo();
        switch ((ConsumerCreditState) lastEvent.getState()) {
            case NOT_SET:
                break;
            case ENTER_SUM:
                setCurrentForm(enterSumForm);
                break;
            case CHOOSE_BANK:
                chooseBankForm.setBanks(info.getBanks());
                setCurrentForm(chooseBankForm);
                break;
            case CHOOSE_BANK_PRODUCT:
                chooseProductForm.setBankProducts(info.getBankProducts());
                setCurrentForm(chooseProductForm);
                break;
            case ENTER_FIO:
                setCurrentForm(getFioForm(info));
                break;
            case ENTER_CONTRACT_NUMBER:
                setCurrentForm(getContractForm(info));
                break;
            case CONFIRM_PAYMENT:
                confirmForm.setBank(info.getSelectedBank());
                confirmForm.setBankProduct(info.getSelectedProduct());
                confirmForm.setContractNumber(info.getContractNumber());
                confirmForm.setCreditAmount(CurrencyUtil.formatSum(event.getPayment().getSumPay()));
                confirmForm.setFIO(info.getFIO());
                setCurrentForm(confirmForm);
                break;
            case ERROR:
                messageForm.setMessage(((ConsumerCreditInfo) event.getInfo()).getErrorMessage());
                setCurrentForm(messageForm);
            default:
                break;
        }
    }

    private JPanel getContractForm(ConsumerCreditInfo info) {
        return info.isShowOnscreenKeyboard() ? onscreenKbContractForm : contractForm;
    }

    private JPanel getFioForm(ConsumerCreditInfo info) {
        return info.isShowOnscreenKeyboard() ? onscreenKbFioForm : fioForm;
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (currentForm == messageForm && (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER)) {
            setCurrentForm(enterSumForm);
            return true;
        }
        return false;
    }
}
