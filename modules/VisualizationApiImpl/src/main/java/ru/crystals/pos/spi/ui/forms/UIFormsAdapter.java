package ru.crystals.pos.spi.ui.forms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.api.adapters.APILogger;
import ru.crystals.pos.api.ui.listener.CancelListener;
import ru.crystals.pos.api.ui.listener.ConfirmListener;
import ru.crystals.pos.api.ui.listener.DialogListener;
import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.api.ui.listener.InputScanNumberFormListener;
import ru.crystals.pos.api.ui.listener.ScanFormListener;
import ru.crystals.pos.api.ui.listener.SumToPayFormListener;
import ru.crystals.pos.api.ui.listener.TimeoutListener;
import ru.crystals.pos.spi.IncorrectStateException;
import ru.crystals.pos.spi.ui.DialogFormParameters;
import ru.crystals.pos.spi.ui.UIForms;
import ru.crystals.pos.spi.ui.forms.payment.ExtSumToPayFormParameters;
import ru.crystals.pos.spi.ui.forms.payment.SumToPayForm;
import ru.crystals.pos.spi.ui.input.UIInputForms;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;
import ru.crystals.pos.spi.ui.payment.UIPaymentForms;
import ru.crystals.pos.spi.ui.utils.UIAdapterUtils;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualizationtouch.components.inputfield.MaskFormatter;
import ru.crystals.pos.visualizationtouch.components.inputfield.NumberFormatter;

public class UIFormsAdapter implements UIForms {

    private static final Logger LOG = LoggerFactory.getLogger(UIFormsAdapter.class);

    private JPanel mainPanel;
    private final Map<Class, Object> formsLib = new ConcurrentHashMap<>();

    private XOperationType xOperationType;
    private BigDecimal maxSum;

    private UIPaymentForms paymentForms = new UIPaymentForms() {
        @Override
        public void showSumToPayForm(SumToPayFormParameters params, SumToPayFormListener sumToPayFormListener) throws IncorrectStateException {
            APILogger.LOG.info("showSumToPayForm caption={}, defaultSum={}", params.getCaption(), params.getDefaultSum());
            SumToPayForm paymentForm = getForm(SumToPayForm.class);
            ExtSumToPayFormParameters model = new ExtSumToPayFormParameters(params);
            switch (getxOperationType()) {
                case SALE:
                    model.setSale(true);
                    UIAdapterUtils.correctSumToPayParameters(params, maxSum);
                    model.setSum(params.getReceipt().getSurchargeSum());
                    model.setPaid(params.getReceipt().getSumWithDiscount().subtract(params.getReceipt().getSurchargeSum()));
                    break;
                case RETURN:
                    model.setSale(false);
                    UIAdapterUtils.correctSumToPayParameters(params, maxSum);
                    model.setSum(maxSum);
                    model.setPaid(params.getReceipt().getSumWithDiscount().subtract(params.getReceipt().getSurchargeSum()));
                    break;
                case CANCEL:
                    break;
            }
            paymentForm.setModel(model);
            paymentForm.setListener(sumToPayFormListener);
            show(paymentForm);
        }
    };

    public UIFormsAdapter(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    private void show(JComponent panel) {
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            mainPanel.add(panel);
            mainPanel.updateUI();
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getForm(Class<T> type) {
        Object form = formsLib.get(type);
        if(form != null) {
            return (T)form;
        }
        synchronized (formsLib) {
            form = formsLib.get(type);
            if (form == null) {
                LOG.debug("Make new form impl: {}", type);
                try {
                    form = type.newInstance();
                    formsLib.put(type, form);
                } catch (Exception ex) {
                    LOG.error("Cannot create panel " + type, ex);
                }
            }
        }
        return (T) form;
    }

    @Override
    public UIInputForms getInputForms() {
        return new UIInputForms() {
            @Override
            public void showScanForm(String caption, String text, ScanFormListener listener) throws IncorrectStateException {
                APILogger.LOG.info("showScanForm caption={}, text={}", caption, text);
                ScanForm scanForm = getForm(ScanForm.class);
                scanForm.setCaption(caption);
                scanForm.setText(text);
                scanForm.setScanFormListener(listener);
                show(scanForm);
            }

            @Override
            public void showInputNumberForm(String caption, String text, String inputFieldHint, int maxLength, InputListener listener) throws IncorrectStateException {
                APILogger.LOG.info("showInputNumberForm caption={}, text={}", caption, text);
                InputForm  inputForm = getInputForm(caption, text, inputFieldHint, maxLength, listener);
                inputForm.setPermitScanning(false);
                inputForm.setPermitMSR(false);
                show(inputForm);
            }

            @Override
            public void showInputScanNumberForm(String caption, String text, String inputFieldHint, int maxLength, InputScanNumberFormListener listener) throws IncorrectStateException {
                APILogger.LOG.info("showInputScanNumberForm caption={}, text={}", caption, text);
                InputForm  inputForm = getInputForm(caption, text, inputFieldHint, maxLength, listener);
                inputForm.setPermitScanning(true);
                inputForm.setPermitMSR(true);
                inputForm.setScanListener(listener);
                show(inputForm);
            }

            @Override
            public void showPatternInputForm(String caption, String text, String defaultValue, String inputHint, String pattern, InputListener listener) throws IncorrectStateException {
                APILogger.LOG.info("showPatternInputForm(caption=\"{}\", text=\"{}\", defaultValue=\"{}\", inputHint=\"{}\", pattern=\"{}\", listener={}",
                        caption, text, defaultValue, inputHint, pattern, listener);
                InputForm  inputForm = new InputForm();
                inputForm.setCaption(caption);
                inputForm.setText(text);
                inputForm.setInputFieldHint(inputHint);
                inputForm.setFormatter(new MaskFormatter(pattern));
                if (defaultValue != null) {
                    inputForm.getFormatter().setValue(defaultValue);
                }
                inputForm.setInputListener(listener);
                inputForm.setPermitScanning(false);
                show(inputForm);
            }

            private InputForm getInputForm(String caption, String text, String inputFieldHint, int maxLength, InputListener listener) {
                InputForm inputForm = getForm(InputForm.class);
                inputForm.setPermitScanning(false);
                inputForm.setCaption(caption);
                inputForm.setText(text);
                inputForm.setInputFieldHint(inputFieldHint);
                inputForm.setMaxInputLength(maxLength);
                inputForm.setFormatter(new NumberFormatter());
                inputForm.setInputListener(listener);
                inputForm.setScanListener(null);
                return inputForm;
            }

            @Override
            public void showSelectionForm(String caption, Map<String, List<String>> items, InputListener listener) throws IncorrectStateException {
                SelectionForm selectionForm = new SelectionForm(items, listener);
                selectionForm.setCaption(caption);
                show(selectionForm);
            }
        };
    }

    @Override
    public UIPaymentForms getPaymentForms() {
        return paymentForms;
    }

    @Override
    public void showTimingOutForm(String message, int timeoutMs, TimeoutListener timeoutListener) {
        APILogger.LOG.info("showTimingOutForm(\"{}\", {}, {})", message, timeoutMs, timeoutListener);
        TimingOutForm countdown = getForm(TimingOutForm.class);
        countdown.setMessage(message);
        countdown.setTimeout(timeoutMs);
        countdown.setTimeoutListener(timeoutListener);
        show(countdown);
        countdown.start();
    }

    @Override
    public void showSpinnerForm(String message) throws IncorrectStateException {
        APILogger.LOG.info("showSpinnerForm message={}", message);
        MessageForm messageForm = getForm(MessageForm.class);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.LOADING_BIG));
        messageForm.setMessage(message);
        messageForm.setCancelListener(null);
        messageForm.setConfirmListener(null);
        messageForm.setModal(true);
        show(messageForm);
    }

    @Override
    public void showSpinnerFormWithCancel(String message, CancelListener canceledFormListener) throws IncorrectStateException {
        APILogger.LOG.info("showSpinnerFormWithCancel message={}", message);
        MessageForm messageForm = getForm(MessageForm.class);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.LOADING_BIG));
        messageForm.setMessage(message);
        messageForm.setConfirmListener(() -> canceledFormListener.eventCanceled());
        messageForm.setCancelListener(canceledFormListener);
        messageForm.setModal(true);
        show(messageForm);
    }

    @Override
    public void showErrorForm(String errorText, ConfirmListener confirmListener) throws IncorrectStateException {
        APILogger.LOG.info("showErrorForm text={}", errorText);
        MessageForm messageForm = getForm(MessageForm.class);
        messageForm.setMessage(errorText);
        messageForm.setModal(false);
        messageForm.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        messageForm.setConfirmListener(confirmListener);
        messageForm.setCancelListener(() -> confirmListener.eventConfirmed());
        show(messageForm);
    }

    @Override
    public void showMessageForm(String message, ConfirmListener confirmListener) throws IncorrectStateException {
        APILogger.LOG.info("Showing message form with text \"{}\"", message);
        MessageForm messageForm = getForm(MessageForm.class);
        messageForm.setMessage(message);
        messageForm.setIcon(null);
        messageForm.setModal(false);
        messageForm.setConfirmListener(confirmListener);
        messageForm.setCancelListener(() -> confirmListener.eventConfirmed());
        show(messageForm);
    }

    @Override
    public void showDialogForm(DialogFormParameters dialogFormParameters, DialogListener dialogListener) throws IncorrectStateException {
        APILogger.LOG.info("showDialogForm text1={}, text2={}", dialogFormParameters.getButton1Text(), dialogFormParameters.getButton2Text());
        DialogForm dialog = getForm(DialogForm.class);
        dialog.setModel(dialogFormParameters);
        dialog.setDialogListener(dialogListener);
        show(dialog);
    }

    public XOperationType getxOperationType() {
        return xOperationType;
    }

    public void setxOperationType(XOperationType xOperationType) {
        this.xOperationType = xOperationType;
    }

    public BigDecimal getMaxSum() {
        return maxSum;
    }

    public void setMaxSum(BigDecimal maxSum) {
        this.maxSum = maxSum;
    }

}
