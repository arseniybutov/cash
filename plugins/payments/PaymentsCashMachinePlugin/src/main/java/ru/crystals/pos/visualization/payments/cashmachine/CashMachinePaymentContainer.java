package ru.crystals.pos.visualization.payments.cashmachine;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.cash_machine.CashMachineInterface;
import ru.crystals.pos.catalog.CurrencyEntity;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;
import ru.crystals.pos.payments.CashMachinePaymentController;
import ru.crystals.pos.payments.CashMachinePaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.eventlisteners.BarcodeEventListener;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.DownEventListener;
import ru.crystals.pos.visualization.eventlisteners.UpEventListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;
import ru.crystals.pos.visualization.payments.cash.CashPaymentComponent;
import ru.crystals.pos.visualization.payments.cash.CashPaymentContainer.VisualState;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import java.math.BigDecimal;

import static ru.crystals.pos.check.BigDecimalConverter.convertMoney;

/**
 * Фиктивный контейнер для загрузки типа оплаты.
 * основная реализация визуальной части в TOUCH2
 */
@PaymentCashPluginComponent(typeName = CashMachinePaymentController.TYPE_NAME, mainEntity = CashMachinePaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class CashMachinePaymentContainer extends PaymentContainer
        implements DotEventListener, UpEventListener, DownEventListener, BarcodeEventListener {

    private CashPaymentComponent visualPanel = null;
    private static int selectedRow = 0;
    private boolean enableChangeBehavior = true;
    private CashMachineInterface cm;

    public CashMachinePaymentContainer() {
        super();
    }

    @Autowired(required = false)
    void setCm(CashMachineInterface cm) {
        this.cm = cm;
    }

    public void setState(VisualState state) {
        if (state == VisualState.SELECT_CURRENCY) {
            fillCurrencies();
        }

        getVisualPanel().setState(state);
    }

    public VisualState getState() {
        return getVisualPanel().getState();
    }

    public boolean mustSelectCurrency() {
        return Factory.getInstance().getCurrencies().length > 1;
    }

    public void fillCurrencies() {
        String[] currencies = new String[Factory.getInstance().getCurrencies().length];
        int i = 0;
        for (CurrencyEntity currency : Factory.getInstance().getCurrencies()) {
            currencies[i] = currency.getId();
            if (!currency.getId().equalsIgnoreCase(Factory.getInstance().getCurrency().getId())) {
                if (Factory.getTechProcessImpl().getCourse(currency) != null) {
                    currencies[i] += " " + String.format(ResBundleVisualization.getString("RATE"), Factory.getTechProcessImpl().getCourse(currency));
                } else {
                    currencies[i] += " " + ResBundleVisualization.getString("RATE_NOT_SET");
                }
            }
            i++;
        }
        getVisualPanel().fillTable(currencies, selectedRow);
    }

    @Override
    public void enter() {
        switch (getState()) {
            case SELECT_CURRENCY:
                CurrencyEntity currency = Factory.getInstance().getCurrencies()[getVisualPanel().getSelectedRow()];
                selectedRow = getVisualPanel().getSelectedRow();
                if (Factory.getTechProcessImpl().getCourse(currency) != null) {
                    setCurrency(currency);
                    setState(VisualState.ENTER_AMOUNT);
                    resetPanels();
                    getInputField().clear();
                } else {
                    Factory.getTechProcessImpl().error("Currency course is null");
                    selectedRow = 0;
                }
                break;
            case ENTER_AMOUNT:

                if (isFiscalizeError()) {
                    Factory.getInstance().getMainWindow().getCheckContainer().paymentComplete(getPayment(),
                            Factory.getInstance().getMainWindow().getCheckContainer().getChange());
                    break;
                }

                if (!isChange()) {
                    if (isSumAvailable()) {
                        getPayment().setCurrency(getCurrency().getId());
                        processPayment();
                    } else {
                        Factory.getTechProcessImpl().error("Cash payment plugin on enter(): sum is not available");
                    }
                } else {
                    if (!CheckContainer.isPrinting()) {
                        dispatchCloseEvent(false);
                    }
                }
                break;
            case SHOW_ERROR:
                getVisualPanel().getInputField().clear();
                setState(VisualState.ENTER_AMOUNT);
                break;
            default:
                break;
        }
    }

    @Override
    public void barcode(String barcode) {
        dispatchCloseEvent(false);
    }

    private boolean isFiscalizeError() {
        CheckContainer c = Factory.getInstance().getMainWindow().getCheckContainer();
        return c.getState() == CheckState.ADD_PAYMENT && c.getPaymentFiscalizeError() != FiscalPrinterExceptionType.NONE;
    }

    @Override
    public void esc() {
        if (isFiscalizeError()) {
            return;
        }

        if (CheckContainer.isPrinting()) {
            return;
        }

        if (getPayment() != null && getPayment().getSumPay() != null && !isRefund()) {
            dispatchCloseEvent(true);
            return;
        }
        switch (getState()) {
            case SELECT_CURRENCY:
                super.esc();
                break;
            case ENTER_AMOUNT:
                if (isSumAvailable()) {
                    resetPanels();
                    getInputField().clear();
                } else {
                    reset();
                    super.esc();
                }
                break;
            case SHOW_ERROR:
                getVisualPanel().getInputField().clear();
                setState(VisualState.ENTER_AMOUNT);
                break;
            default:
                break;
        }
    }

    @Override
    public void number(Byte num) {
        if (getState() == VisualState.ENTER_AMOUNT) {
            getInputField().addChar(num.toString().charAt(0));
            if (!isChange()) {
                enterAmount(num);
            }
        }
    }

    @Override
    public void dot() {
        if (getState() == VisualState.ENTER_AMOUNT) {
            getInputField().dot();
        }
    }

    @Override
    public void up() {
        if (getState() == VisualState.SELECT_CURRENCY) {
            getVisualPanel().moveUp();
        }
    }

    @Override
    public void down() {
        if (getState() == VisualState.SELECT_CURRENCY) {
            getVisualPanel().moveDown();
        }
    }

    @Override
    public void reset() {
        setChange(false);
        setReset(true);
        setState(VisualState.ENTER_AMOUNT);
        if (getCurrency() == null) {
            if (selectedRow != 0) {
                setCurrency(Factory.getInstance().getCurrencies()[selectedRow]);
            } else {
                setCurrency(Factory.getInstance().getCurrency());
            }
        }
        resetPanels();

        if (isRefund()) {
            getVisualPanel().getPaymentInputPanel().setPresetMaxValue(convertMoney(Math.min(getSurcharge(), getPayment().getSumPay())));
        } else {
            getVisualPanel().getPaymentInputPanel().setMaxValue(null);
        }

    }

    @Override
    public CashPaymentComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new CashPaymentComponent();
        }
        return visualPanel;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public String getChargeName() {
        if (isRefund() || isPositionsRefund()) {
            return ResBundleVisualization.getString("REFUND_DISPLAY");
        } else {
            return ResBundleVisualization.getString("CHECK_AMOUNT");
        }
    }

    @Override
    public String getPaymentType() {
        if (isRefund() || (isPositionsRefund())) {
            return ResBundleVisualization.getString("REFUND_CASH_PAYMENT") + " (купюроприемник)";
        } else {
            return ResBundleVisualization.getString("CASH_PAYMENT") + " (купюроприемник)";
        }
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return getVisualPanel();
    }

    @Override
    public String getTitlePaymentType() {
        if (isRefund() || (isPositionsRefund())) {
            return ResBundleVisualization.getString("REFUND_CASH_PAYMENT") + " (купюроприемник)";
        } else {
            return ResBundleVisualization.getString("CASH_PAYMENT") + " (купюроприемник)";
        }
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundleVisualization.getString("RETURN_PAYMENT_CASH") + " (купюроприемник)";
    }

    @Override
    public String getPaymentString() {
        return ResBundleVisualization.getString("CASH_PAYMENT") + " (купюроприемник)";
    }


    @Override
    public void setPaymentFields() {
        //
    }

    public void setEnableChangeBehavior(boolean enableChangeBehavior) {
        this.enableChangeBehavior = enableChangeBehavior;
    }

    public boolean isEnableChangeBehavior() {
        return enableChangeBehavior;
    }

    private InputFieldInterface<BigDecimal> getInputField() {
        return getVisualPanel().getInputField();
    }

    @Override
    public long getSum() {
        return convertMoney(getInputField().getValue());
    }

    @Override
    public void setSum(long sum) {
        getInputField().setValue(convertMoney(sum));
    }

    public boolean isSumAvailable() {
        return getSum() != 0;
    }

    @Override
    public String getPaymentTypeName() {
        return ResBundleVisualization.getString("CASH_PAYMENT") + " (купюроприемник)";
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return true;
    }

    @Override
    public boolean isChangeAvailable() {
        return true;
    }

    @Override
    public boolean isActivated() {
        return cm != null && cm.isOnline();
    }

    @Override
    protected Long recalcSurchargeIfPurchaseSplitted(PurchaseEntity check, Long surcharge) {
        //Для оплаты наличными нет ограничения
        return surcharge;
    }

    @Override
    public void resetPanels() {
        super.resetPanels();
        getInputField().clear();
    }

    @Override
    public String getSummaLabel() {
        return ResBundleVisualization.getString("CASH");
    }

    @Override
    protected void doProcessPayment() {
        super.doProcessPayment();
    }
}
