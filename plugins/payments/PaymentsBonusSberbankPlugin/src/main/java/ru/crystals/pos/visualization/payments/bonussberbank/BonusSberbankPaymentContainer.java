package ru.crystals.pos.visualization.payments.bonussberbank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.CardInfoBankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankTypeEnum;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.cards.cft.CFTBonusCardInfo;
import ru.crystals.pos.cards.cft.CFTBridge;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.payments.BonusSberbankPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPlugin;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.admin.components.ReportComponent;
import ru.crystals.pos.visualization.admin.components.ReportComponentAdapter;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;
import ru.crystals.pos.visualization.utils.FormatHelper;
import ru.crystals.pos.visualization.utils.Runner;
import ru.crystals.pos.visualization.utils.Swing;

import javax.swing.SwingUtilities;

import static ru.crystals.pos.check.BigDecimalConverter.convertMoney;

@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.BONUS_SBERBANK_PAYMENT_ENTITY, mainEntity = BonusSberbankPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class BonusSberbankPaymentContainer extends PaymentContainer implements NumberEventListener, EscEventListener, EnterEventListener, DotEventListener,
        BankEvent, PaymentPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(BonusSberbankPaymentContainer.class);

    public enum State {
        ENTER_AMOUNT, DATA_EXCHANGE
    }

    private BonusSberbankPaymentComponent visualPanel = null;

    private String cardNumber = "";
    private State state = State.DATA_EXCHANGE;
    private boolean isPaymentProcessing;
    private boolean isManualSumInput = true;

    private long limit = 0;
    private long bonusesOnCard = 0;
    private long purchasePercent = 0;

    private InternalCashPoolExecutor cashPoolExecutor;

    private Bank bankModule;

    private static final long FULL_PURCHASE_PERCENT = 10000L;

    private String errorMessage = null;
    private CFTBonusCardInfo cardInfo = null;
    private CFTBridge cft;

    @Autowired
    void setCashPoolExecutor(InternalCashPoolExecutor cashPoolExecutor) {
        this.cashPoolExecutor = cashPoolExecutor;
    }

    @Autowired(required = false)
    void setBankModule(Bank bankModule) {
        this.bankModule = bankModule;
    }

    @Autowired(required = false)
    @Qualifier(Constants.CFTType.Names.CFT_PROCESSING_NAME)
    void setCft(CFTBridge cft) {
        this.cft = cft;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;

        getVisualPanel().setState(state);
    }

    @Override
    public void enter() {
        if (isPaymentProcessing) {
            return;
        }
        switch (state) {
            case ENTER_AMOUNT:
                if (isSumAvailable()) {
                    if (!isRefund()) {
                        if (getSum() <= limit) {
                            processPayment();
                        } else {
                            getFactory().getTechProcess().error();
                        }
                    }
                } else {
                    getFactory().getTechProcess().error();
                }
                break;
            case DATA_EXCHANGE:
                ReportComponent rc = getVisualPanel().getReportComponent();
                if (rc != null) {
                    rc.enter();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void esc() {
        if (isPaymentProcessing) {
            return;
        }
        if (CheckContainer.isPrinting()) {
            return;
        }
        if (getPayment().getSumPay() != null) {
            dispatchCloseEvent(true);
            return;
        }
        super.esc();
        switch (state) {
            case ENTER_AMOUNT:
                getVisualPanel().getInputField().clear();
                setReset(true);

                if (!isSummaReset() && isManualSumInput) {
                    isManualSumInput = false;

                    if (limit < getSurcharge()) {
                        setSum(limit);
                    }
                }
                break;

            case DATA_EXCHANGE:
                ReportComponent rc = getVisualPanel().getReportComponent();
                if (rc != null) {
                    rc.esc();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public BonusSberbankPaymentComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new BonusSberbankPaymentComponent();

        }
        return visualPanel;
    }

    private void fillBonusInfo() {

        getVisualPanel().setPaymentType(ResBundlePaymentBonusSberbank.getString("SBERBANK_BONUS_COUNT"));
        getVisualPanel().setBonus(bonusesOnCard);
        getVisualPanel().setBonusLabelText(ResBundlePaymentBonusSberbank.getString("BONUSES_ON_COUNT"));

        countLimit();

        getVisualPanel().setSummaLabel(
                ResBundlePaymentBonusSberbank.getString("SUMMA_TO_PAY") + " (" + ResBundlePaymentBonusSberbank.getString("NOT_MORE") + " "
                        + FormatHelper.formatSumma(limit / 100.0) + " )");

        if (limit < getSurcharge()) {
            setSum(limit);
        } else {
            setSum(getSurcharge());
        }
    }

    private void countLimit() {

        if (purchasePercent < FULL_PURCHASE_PERCENT) {

            long paymentPercent = purchasePercent;

            if (Factory.getTechProcessImpl().getCheck() != null
                    && Factory.getTechProcessImpl().getCheck().getPayments() != null
                    && !Factory.getTechProcessImpl().getCheck().getPayments().isEmpty()) {

                for (PaymentEntity bpayment : Factory.getTechProcessImpl().getCheck().getPayments()) {
                    if (bpayment instanceof BonusSberbankPaymentEntity) {
                        paymentPercent = (paymentPercent - Math.round(bpayment.getSumPay() * 100.0 / (getSurcharge() + getPaid()) * 100.0));
                    }
                }
            }

            limit = Math.min(bonusesOnCard, Math.round(paymentPercent * (getSurcharge() + getPaid()) * 0.01 * 0.01));
            limit = Math.min(limit, getSurcharge());
        } else {
            limit = Math.min(Math.round(bonusesOnCard), getSurcharge());
        }
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public void number(Byte num) {
        setReset(false);
        if (state == State.ENTER_AMOUNT) {
            getVisualPanel().getInputField().addChar(num.toString().charAt(0));
            enterAmount(num);
            isManualSumInput = true;
        }
    }

    @Override
    public void dot() {
        if (state == State.ENTER_AMOUNT) {
            getVisualPanel().getInputField().dot();
        }
    }

    @Override
    public void dispatchCloseEvent(boolean isCloseByKey) {
        super.dispatchCloseEvent(isCloseByKey);
    }

    @Override
    public void reset() {
        isPaymentProcessing = false;

        setReset(true);
        setChange(false);
        cardNumber = "";
        resetPanels();

    }

    @Override
    public String getPaymentType() {
        if (isRefund()) {
            return ResBundlePaymentBonusSberbank.getString("REFUND_CARD_PAYMENT");
        } else {
            return ResBundlePaymentBonusSberbank.getString("BANK_CARD");
        }
    }

    @Override
    public String getChargeName() {
        return ResBundlePaymentBonusSberbank.getString("TO_CHARGE");
    }

    @Override
    public String getPaymentString() {
        return ResBundlePaymentBonusSberbank.getString("BANK_CARD");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentBonusSberbank.getString("RETURN_PAYMENT_BONUSBANK_CARD");
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return visualPanel;
    }

    @Override
    public String getTitlePaymentType() {
        String title = null;

        if (isRefund()) {
            if (getPayment() != null) {

                String cardNumber = ((BonusSberbankPaymentEntity) getPayment()).getCardNumber();

                if (cardNumber.length() > 4) {
                    cardNumber = "..." + cardNumber.substring(cardNumber.length() - 4);
                }
                title = ResBundlePaymentBonusSberbank.getString("REFUND_CARD_PAYMENT") + " " + cardNumber;
            }

        } else if (isPositionsRefund()) {
            title = ResBundlePaymentBonusSberbank.getString("REFUND_CARD_PAYMENT");

        } else {
            title = ResBundlePaymentBonusSberbank.getString("CARD_PAYMENT");
        }
        return title;
    }

    @Override
    public void setPayment(PaymentEntity payment, PurchaseEntity purchase) {
        super.setPayment(payment, purchase);

        Runner bankRunner = new Runner() {
            @Override
            protected void onRun() {
                if (bankModule != null) {
                    try {
                        final BankCard bankCard = bankModule.getBankCardInfo();
                        if (bankCard != null && bankCard.getCardOperator() != null && bankCard.getCardOperator() == BankTypeEnum.SBERBANK) {

                            cardInfo = null;

                            cashPoolExecutor.execute(() -> {

                                if (cft != null) {
                                    try {
                                        cardInfo = cft.getCardInfo(bankCard.getCardNumberHash());
                                    } catch (CFTException e) {
                                        errorMessage = e.getMessage();
                                    }
                                }

                                SwingUtilities.invokeLater(() -> {

                                    if (cardInfo != null) {
                                        // если на счете есть бонусы
                                        if (cardInfo.getTotalBonusesAmount() > 0L) {

                                            bonusesOnCard = Math.round(cardInfo.getAvailableToPaymentBonusesAmount() * 100);
                                            purchasePercent = getPayment().getPaymentSettings().getMaxPercentForPayment();

                                            cardNumber = bankCard.getCardNumberHash();

                                            setState(State.ENTER_AMOUNT);
                                            fillBonusInfo();

                                        } else {
                                            // на счете нет бонусов
                                            Swing.invokeLater(new Runner() {
                                                @Override
                                                protected void onRun() {
                                                    ReportComponent erc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"),
                                                            ResBundlePaymentBonusSberbank.getString("INSUFFICIENT_FUNDS"), null, 0, false);
                                                    erc.addListener(new ReportComponentAdapter() {
                                                        @Override
                                                        public void onPressEnter(ReportComponent sender) {
                                                            isPaymentProcessing = false;
                                                            BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                                        }
                                                    });
                                                    visualPanel.setReportComponent(erc);
                                                }
                                            });
                                        }
                                    } else {
                                        Swing.invokeLater(new Runner() {
                                            @Override
                                            protected void onRun() {
                                                ReportComponent erc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"),
                                                        errorMessage, null, 0, false);
                                                erc.addListener(new ReportComponentAdapter() {
                                                    @Override
                                                    public void onPressEnter(ReportComponent sender) {
                                                        isPaymentProcessing = false;
                                                        BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                                    }
                                                });
                                                visualPanel.setReportComponent(erc);
                                            }
                                        });
                                    }
                                });

                            });


                        } else {
                            Swing.invokeLater(new Runner() {
                                @Override
                                protected void onRun() {
                                    ReportComponent erc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"),
                                            ResBundlePaymentBonusSberbank.getString("NOT_SBERBANK_CARD_MESSAGE"), null, 0, false);
                                    erc.addListener(new ReportComponentAdapter() {
                                        @Override
                                        public void onPressEnter(ReportComponent sender) {
                                            isPaymentProcessing = false;
                                            BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                        }
                                    });
                                    visualPanel.setReportComponent(erc);
                                }
                            });
                        }
                    } catch (final BankException ex) {
                        Swing.invokeLater(new Runner() {
                            @Override
                            protected void onRun() {
                                ReportComponent erc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"), ex.getLocalizedMessage(), null, 0, false);
                                erc.addListener(new ReportComponentAdapter() {
                                    @Override
                                    public void onPressEnter(ReportComponent sender) {
                                        isPaymentProcessing = false;
                                        BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                    }
                                });
                                visualPanel.setReportComponent(erc);
                            }
                        });
                    }
                } else {
                    Swing.invokeLater(new Runner() {
                        @Override
                        protected void onRun() {

                            ReportComponent erc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"),
                                    ResBundlePaymentBonusSberbank.getString("ERROR_BANK_MODULE_DISCONNECTED"), null, 0, false);
                            erc.addListener(new ReportComponentAdapter() {
                                @Override
                                public void onPressEnter(ReportComponent sender) {

                                    isPaymentProcessing = false;
                                    BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);
                                }
                            });
                            visualPanel.setReportComponent(erc);
                        }
                    });
                }
            }
        };

        ReportComponent rc = new ReportComponent(ResBundlePaymentBonusSberbank.getString("SBERBANK_PAYMENT"),
                ResBundlePaymentBonusSberbank.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"), bankRunner, 0, true);

        visualPanel.setReportComponent(rc);
        setState(State.DATA_EXCHANGE);
        rc.startAction();

    }

    @Override
    protected void doProcessPayment() {
        LOG.trace("BEGIN");

        isPaymentProcessing = true;
        ReportComponent rc =
                new ReportComponent(ResBundlePaymentBonusSberbank.getString("BONUS_PAYMENT"),
                        ResBundlePaymentBonusSberbank.getString("CHECK_DATA_WITH_SERVER"), new Runner() {
                    @Override
                    protected void onRun() {
                        getTechProcessEvents().eventAddCashlessPayment(Factory.getTechProcessImpl().getCheckWithNumber(), getPayment());
                        if (isRefund()) {
                            BonusSberbankPaymentContainer.super.doProcessPayment();
                        } else {
                            boolean isPaySuccess = true;

                            BonusSberbankPaymentEntity bspe = (BonusSberbankPaymentEntity) getPayment();
                            bspe.setSumPay(getSum());
                            bspe.setCurrency(getFactory().getTechProcess().getCurrency().getId());
                            bspe.setCardNumber(cardNumber);

                            try {
                                // списание бонусных баллов (запрос в ЦФТ)
                                cft.payBonuses(getPayment());
                            } catch (CFTException e) {
                                errorMessage = e.getMessage();
                                isPaySuccess = false;
                                isPaymentProcessing = false;
                            }

                            // если списание бонусных баллов прошла успешно
                            // добавляем позицию в чек
                            if (isPaySuccess) {
                                try {
                                    updatePayment();
                                    BonusSberbankPaymentContainer.super.doProcessPayment();
                                } catch (Exception ce) {
                                    errorMessage = ce.getMessage();
                                    isPaymentProcessing = false;

                                    Swing.invokeLater(new Runner() {
                                        @Override
                                        protected void onRun() {
                                            ReportComponent erc =
                                                    new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"), errorMessage, null, 0,
                                                            false);
                                            erc.addListener(new ReportComponentAdapter() {
                                                @Override
                                                public void onPressEnter(ReportComponent sender) {
                                                    isPaymentProcessing = false;
                                                    BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                                }
                                            });
                                            visualPanel.setReportComponent(erc);
                                        }
                                    });
                                }
                            } else {
                                Swing.invokeLater(new Runner() {
                                    @Override
                                    protected void onRun() {
                                        ReportComponent erc =
                                                new ReportComponent(ResBundlePaymentBonusSberbank.getString("ERROR"), errorMessage, null, 0,
                                                        false);
                                        erc.addListener(new ReportComponentAdapter() {
                                            @Override
                                            public void onPressEnter(ReportComponent sender) {
                                                isPaymentProcessing = false;
                                                BonusSberbankPaymentContainer.super.dispatchCloseEvent(false);

                                            }
                                        });
                                        visualPanel.setReportComponent(erc);
                                    }
                                });
                            }
                        }
                    }
                }, 0, true);
        state = State.DATA_EXCHANGE;
        visualPanel.setState(State.DATA_EXCHANGE);
        visualPanel.setReportComponent(rc);
        rc.startAction();

        LOG.trace("END");
    }

    private void updatePayment() {
        LOG.trace("BEGIN");

        BonusSberbankPaymentEntity bspe = (BonusSberbankPaymentEntity) getPayment();
        PurchaseEntity pe = Factory.getTechProcessImpl().getCheck();

        if (pe != null) {
            bspe.setPurchase(pe);
        }
        LOG.trace("END");

    }

    @Override
    public void eventPINEntry() {
        LOG.trace("BEGIN");
        LOG.trace("END");
    }

    @Override
    public void eventShowCustomPaymentProcessMessage(String message) {
        LOG.trace("BEGIN");
        LOG.trace("END");
    }

    @Override
    public void eventDailyLogComplete(BankPlugin plugin) {
        //
    }

    @Override
    public void showCustomProcessScreen(BankDialog dialog) {
        //
    }

    @Override
    public void eventOnlineRequest() {
        LOG.trace("BEGIN");
        LOG.trace("END");
    }

    @Override
    public void eventAuthorizationComplete(AuthorizationData auth) {
        LOG.trace("BEGIN");

        if (auth.isStatus()) {
            updatePayment(auth);
        }
        isPaymentProcessing = false;
        LOG.trace("END");
    }

    @Override
    public void eventBankModuleOnline() {
        //
    }

    @Override
    public void eventBankModuleOffline() {
        //
    }

    private void updatePayment(AuthorizationData auth) {
        LOG.trace("BEGIN");
        BonusSberbankPaymentEntity bspe = (BonusSberbankPaymentEntity) getPayment();
        PurchaseEntity pe = Factory.getTechProcessImpl().getCheck();

        if (pe != null) {
            bspe.setPurchase(pe);
        }
        LOG.trace("END");

    }

    @Override
    public void setPaymentFields() {
        //
    }

    @Override
    public void cancelPurchase(final PaymentEntity cancelPayment) {
        //
    }

    @Override
    public long getSum() {
        return convertMoney(getVisualPanel().getInputField().getValue());
    }

    @Override
    public void setSum(long sum) {
        getVisualPanel().getInputField().setValue(convertMoney(sum));
    }

    public boolean isSumAvailable() {
        return getSum() != 0;
    }

    @Override
    public String getPaymentTypeName() {
        return ResBundlePaymentBonusSberbank.getString("CARD_PAYMENT");
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return false;
    }

    @Override
    public boolean isChangeAvailable() {
        return false;
    }

    @Override
    public boolean isActivated() {
        return bankModule != null && cft != null && bankModule.isProviderAvailable(CardInfoBankPlugin.class);
    }

}
