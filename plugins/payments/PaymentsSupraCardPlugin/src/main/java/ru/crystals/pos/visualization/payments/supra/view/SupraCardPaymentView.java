package ru.crystals.pos.visualization.payments.supra.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;
import ru.crystals.pos.visualization.payments.supra.controller.SupraCardPaymentController;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentInfo;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentState;
import ru.crystals.supra.exception.SupraException;
import ru.crystals.supra.exception.SupraResultException;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;

public class SupraCardPaymentView extends AbstractPaymentView<SupraCardPaymentController> {
    private static final Logger logger = LoggerFactory.getLogger(SupraCardPaymentView.class);

    private final SupraCardPaymentEnterCardNumberForm enterCardNumberForm;
    private final SupraCardPaymentEnterLastDigitsForm enterLastDigitsForm;
    private final SupraCardPaymentEnterSumForm enterSumForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonMessageForm messageForm;

    private JPanel beforeMessage = null;

    public SupraCardPaymentView() {
        enterCardNumberForm = new SupraCardPaymentEnterCardNumberForm(this);
        enterLastDigitsForm = new SupraCardPaymentEnterLastDigitsForm(this);
        enterSumForm = new SupraCardPaymentEnterSumForm(this);

        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentSupraCard.getString("CHECK_DATA_WITH_SERVER"));
        messageForm = new CommonMessageForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(enterLastDigitsForm, enterLastDigitsForm.getClass().getName());
        this.add(enterSumForm, enterSumForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        this.add(enterSumForm, enterSumForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (SupraCardPaymentController) controller;

        enterCardNumberForm.setController(controller);
        enterLastDigitsForm.setController(controller);
        enterSumForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((SupraCardPaymentState) lastEvent.getState()) {
            case NOT_SET:
            case ENTER_CARD:
                getController().clearInfo();
                setCurrentForm(enterCardNumberForm);
                break;
            case ENTER_AMOUNT:
                setCurrentForm(enterSumForm);
                break;
            case ENTER_VERIFY:
                setCurrentForm(enterLastDigitsForm);
                break;
            case ERROR:
                String message = ((SupraCardPaymentInfo) event.getInfo()).getErrorMessage();

                if (message == null) {
                    message = ResBundlePaymentSupraCard.getString("ERROR");
                }

                messageForm.setMessage(message);
                controller.beepError(message);
                setCurrentForm(messageForm);
                break;
            default:
                logger.warn("Unknown state {}", lastEvent.getState());
                break;
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return onEsc();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return onEnter();
        }
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return true;
        }
        //CardNumberForm записали считанное значение куда надо, и теперь просто сменим форму
        if (currentForm == enterCardNumberForm) {
            setCurrentForm(enterLastDigitsForm);
        }
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return true;
        }
        //CardNumberForm записали считанное значение куда надо, и теперь просто сменим форму
        if (currentForm == enterCardNumberForm) {
            setCurrentForm(enterLastDigitsForm);
        }
        return true;
    }

    private boolean onEnter() {
        if (currentForm == enterCardNumberForm) {
            //Если показана форма ввода номера карты, то затем нужно ввести последние 4 цфиры
            setCurrentForm(enterLastDigitsForm);
            return true;
        } else if (currentForm == enterLastDigitsForm) {
            try {
                //Ввели 4 цифры, теперь верифицируем карту
                setCurrentForm(spinnerForm);
                getController().verifyCard(enterLastDigitsForm.getLastDigits());
            } catch (SupraException e) {
                if (!(e instanceof SupraResultException)) {
                    //Выводить рутиную херню в логи не надо
                    logger.error(e.getMessage(), e);
                }
                messageForm.setMessage(e.getMessage());
                setCurrentForm(messageForm);
                return false;
            }
            //Сюда доходим, если карта прошла верификацию и
            setCurrentForm(enterSumForm);
            return true;
        } else if (currentForm == enterSumForm) {
            if (enterSumForm.getSumToPay().signum() == 0) {
                messageForm.setMessage(ResBundlePaymentSupraCard.getString("RESTRICT_ZERO_CHARGE_OFF"));
                setCurrentForm(messageForm);
                return true;
            }

            setCurrentForm(spinnerForm);
            getController().processOperation(enterSumForm.getSumToPay());
            return true;
        }
        return false;
    }


    private boolean onEsc() {
        if (currentForm == enterLastDigitsForm) {
            setCurrentForm(enterCardNumberForm);
            return true;
        } else if (currentForm == enterSumForm) {
            setCurrentForm(enterLastDigitsForm);
            return true;
        } else if (currentForm == messageForm) {
            if (beforeMessage != null) {
                setCurrentForm(beforeMessage);
            } else {
                setCurrentForm(enterLastDigitsForm);
            }
            return true;
        }
        return false;
    }

    @Override
    public SupraCardPaymentController getController() {
        return (SupraCardPaymentController) super.getController();
    }

    @Override
    protected void setCurrentForm(JPanel current) {
        if (current == messageForm && currentForm != messageForm && currentForm != spinnerForm) {
            beforeMessage = currentForm;
        }
        super.setCurrentForm(current);
    }

}

