package ru.crystals.pos.visualization.payments.giftcard;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.cards.PluginCard;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckStatus;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionGiftCardEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SlipsContainer;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TransactionData;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.GiftCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPlugin;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.products.ProductToPositionConverter;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.admin.components.ReportComponent;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.eventlisteners.LeftListener;
import ru.crystals.pos.visualization.eventlisteners.RightListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;
import ru.crystals.pos.visualization.utils.Runner;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static ru.crystals.cards.common.CardStatus.Active;
import static ru.crystals.cards.common.CardStatus.Blocked;
import static ru.crystals.cards.common.CardStatus.Create;
import static ru.crystals.cards.common.CardStatus.Used;

/**
 * Грязный коннтролер, манипулирует
 * {@link ru.crystals.pos.visualization.payments.giftcard.GiftCardPaymentComponent}
 */
@PaymentCashPluginComponent(typeName = GiftCardPaymentContainer.GIFT_CARD_PRODUCT_TYPE, mainEntity = GiftCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class GiftCardPaymentContainer extends PaymentContainer implements LeftListener, RightListener, PaymentPlugin, XListener {

    private static final Logger log = LoggerFactory.getLogger(GiftCardPaymentContainer.class);

    public final static String GIFT_CARD_PRODUCT_TYPE = PaymentsDiscriminators.GIFT_CARD_PAYMENT_ENTITY;

    enum GiftCardState {
        ENTER_NUMBER, ENTER_AMOUNT, DATA_EXCHANGE, SHOW_WARNING_BEEP, SHOW_WARNING
    }

    private GiftCardPaymentComponent visualPanel;

    private GiftCardState state = GiftCardState.ENTER_NUMBER;

    private long sum = 0;

    private PresentCardInformationVO presentCardInfoVO = null;

    private boolean isPaymentProcessing = false;
    private TechProcessInterface tp;
    private Properties properties;

    @PostConstruct
    private void localInit() {
        visualPanel = new GiftCardPaymentComponent();
        new XListenerAdapter(visualPanel, this);
    }

    @Autowired
    void setTp(TechProcessInterface tp) {
        this.tp = tp;
    }

    @Autowired
    void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        return isPaymentProcessing || state == GiftCardState.DATA_EXCHANGE;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        String cardNumber = Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4, PresentCards.MODULE_NAME);
        cardNumberEntered(cardNumber, true);
        return true;
    }

    @Override
    public boolean barcodeScanned(String cardNumber) {
        if (getPayment().getPaymentSettings().isAllowBarcodeScan()) {
            getCardNumberInputField().setValue(cardNumber);
            cardNumberEntered(cardNumber, false);
        } else {
            Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("BARCODE_SCANNING_NOT_ALLOWED"));
        }
        return true;
    }

    private void cardNumberEntered(String cardNumber, boolean msrInput) {
        try {
            //получаем сервис для работы с картами
            PresentCards service = getCardPluginService();

            if (service == null) {
                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("NO_CONNECTION"));
                return;
            }

            presentCardInfoVO = service.getCardData(cardNumber, msrInput);

            if (presentCardInfoVO != null) {
                log.trace("Received GiftCard card by:  " + presentCardInfoVO.toString());
                if (msrInput) {
                    getCardNumberInputField().setValue(StringUtils.isEmpty(presentCardInfoVO.getCardNumber())
                            ? cardNumber : presentCardInfoVO.getCardNumber());
                }
            } else {
                log.trace("Received null GiftCard card by: " + cardNumber);
                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("CARD_NOT_ACTIVE"));
                return;
            }

            //не возврат ли у нас часом ?
            if (isRefund()) {
                if (!presentCardInfoVO.getStatus().equals(Create)) {
                    Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("COULD_NOT_SALE_CARD"));
                    return;
                }
            } else {
                //не хотим ли мы использовать карту которую только-только вбили ?
                if (isUsedAlreadyAddedCards(cardNumber) || wantProcessNotActivedOrUsedCard()) {
                    return;
                }
            }

            //узнаем сколько денег на карта и ее испольнуем
            sum = presentCardInfoVO.getBalance();

            if (cardAmountMoreThanSumToPay()) {
                showWarning();
                return;
            }

            visualPanel.setPayment(sum);
            visualPanel.setPaymentEditable(presentCardInfoVO.getPartialPaymentEnabled());
        } catch (CardsException ce) {
            Factory.getInstance().showMessage(ce.getMessage());
            return;
        }

        state = GiftCardState.ENTER_AMOUNT;
        getVisualPanel().setState(state);
    }

    @Override
    public void enter() {
        switch (state) {
            case ENTER_NUMBER:
                if (getCardNumber().length() > 0) {
                    cardNumberEntered(getCardNumber(), false);
                }
                break;
            case SHOW_WARNING_BEEP:
            case SHOW_WARNING:
                // превышение номиналом карты суммы оплаты - что делать?
                if (getVisualPanel().isCardUsingConfirm()) {
                    if (presentCardInfoVO.getPartialPaymentEnabled()) {
                        // можно частично погасить карту
                        sum = calculateSurcharge();
                    } // else
                    // нас устраивает превышение

                    // далее просто оплачиваем - окно подтверждения оплаты не выводим
                } else {
                    // отказываемся от оплаты в случае превышения номиналом карты суммы оплаты и выходим в меню ввода номера карты
                    reset();
                    break;
                }
            case ENTER_AMOUNT:
                if (!isChange()) {
                    if (StringUtils.isEmpty(getPaymentExcessGoodMarking())) {
                        sum = getVisualPanel().getPaymentSum();
                    }
                    if (sum != 0) {
                        setCallDone(true);
                        processPayment();
                    } else {
                        Factory.getTechProcessImpl().error();
                    }
                } else {
                    dispatchCloseEvent(false);
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

    private PresentCards getCardPluginService() {
        PresentCards returnValue = null;
        for (PluginCard card : Factory.getInstance().getCards()) {
            if (card instanceof PresentCards) {
                returnValue = (PresentCards) card;
                break;
            }
        }

        return returnValue;
    }

    /**
     * Проверяем чтобы покупатель не жаждал расплатить картой которую мы в этом
     * же чеке пробили
     */
    private boolean isUsedAlreadyAddedCards(String cardNumber) {
        if (Factory.getTechProcessImpl().getCheck() != null) {
            for (PositionEntity p : Factory.getTechProcessImpl().getCheck().getPositions()) {
                if (p instanceof PositionGiftCardEntity) {
                    //номер подарочной карты может содержать символы
                    if (((PositionGiftCardEntity) p).getCardNumber().equalsIgnoreCase(cardNumber)) {
                        Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("CURRENT_CARD_WAS_ADDED"));
                        return true;
                    }
                }
            }
        }

        if (Factory.getTechProcessImpl().getCheck() != null && Factory.getTechProcessImpl().getCheck().getPayments() != null) {
            for (PaymentEntity p : Factory.getTechProcessImpl().getCheck().getPayments()) {
                if (p instanceof GiftCardPaymentEntity) {
                    //номер подарочной карты может содержать символы
                    if (((GiftCardPaymentEntity) p).getCardNumber().equalsIgnoreCase(cardNumber)) {
                        Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("CURRENT_CARD_WAS_ADDED"));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Превышение номинальной стоимости подарочной карты над необходимой суммой
     */
    private boolean cardAmountMoreThanSumToPay() {
        return calculateDifferent() > 0;
    }

    private PurchaseEntity getMainDividedPurchaseIfNeeded() {
        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
        if (check != null && check.isPurchaseDivided()) {
            for (PurchaseEntity dividedPurchase : Factory.getTechProcessImpl().getCheck().getDividedPurchases()) {
                if (Factory.getTechProcessImpl().isFirstFiscalPrinter(dividedPurchase.getInn())) {
                    return dividedPurchase;
                }
            }
            check = null;
        }
        return check;
    }

    /**
     * Отображает на экране кассы предупреждающее сообщение о том, что номинал карты больше, чем
     * сумма чека.
     */
    private void showWarning() {
        state = GiftCardState.SHOW_WARNING_BEEP;

        final String text;
        if (presentCardInfoVO.getPartialPaymentEnabled()) {
            // Будет остаток по карте, необходимо не забыть вернуть карту покупателю.
            text = String.format(ResBundlePaymentGiftCard.getString("CARD_USAGE_BALANCE"), CurrencyUtil.formatSum(calculateDifferent()));
        } else {
            // В этому случае карта расходуется неэффективно, поскольку с карты будет списан весь доступный объём, вне зависимости от суммы чека, о чем
            // и предупреждается кассир/покупатель этим сообщением.
            text = String.format(ResBundlePaymentGiftCard.getString("NOT_EFFECTIVE_CARD_USAGE_WARNING"),
                    CurrencyUtil.formatSum(sum), CurrencyUtil.formatSum(calculateDifferent()));
        }

        getVisualPanel().setWarnText(text);
        getVisualPanel().setState(state);
    }

    /**
     * Посчитать разницу между номиналом карты и необходимой суммой
     */
    private long calculateDifferent() {
        return sum - calculateSurcharge();
    }

    /**
     * @return требуемая доплата по чеку в копейках
     */
    private long calculateSurcharge() {
        return Factory.getTechProcessImpl().getSurchargeValue(getMainDividedPurchaseIfNeeded());
    }

    @Override
    protected Long recalcSurchargeIfPurchaseSplitted(PurchaseEntity check, Long surcharge) {
        Long subCheckSurcharge = Factory.getTechProcessImpl().getSurchargeValue(getMainDividedPurchaseIfNeeded());
        return subCheckSurcharge.compareTo(0L) > 0 ? subCheckSurcharge : surcharge;
    }

    private boolean wantProcessNotActivedOrUsedCard() {
        if (!presentCardInfoVO.getStatus().equals(Active)
                && !presentCardInfoVO.getStatus().equals(Blocked)) {

            String message = ResBundlePaymentGiftCard.getString("CARD_NOT_ACTIVE");

            if (presentCardInfoVO.getStatus() == Used) {
                message = ResBundlePaymentGiftCard.getString("CARD_HAS_BEEN_ALREADY_USED");
            }

            Factory.getInstance().showMessage(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setPayment(PaymentEntity payment, PurchaseEntity purchase) {
        super.setPayment(payment, purchase);

        getPayment().setPaymentInn(Factory.getTechProcessImpl().getPrimaryFiscalVO().getInnNum());

        isPaymentProcessing = false;

        if (((GiftCardPaymentEntity) payment).getAmount() != null) {
            visualPanel.setSumma((((GiftCardPaymentEntity) payment).getAmount() * 100));
        }
    }

    @Override
    protected void doProcessPayment() {
        ReportComponent rc = new ReportComponent(ResBundlePaymentGiftCard.getString("GIFT_PAYMENT"), ResBundlePaymentGiftCard.getString("CHECK_DATA_WITH_SERVER"),
                new Runner() {
                    @Override
                    protected void onRun() {
                        if (isRefund()) {

                            log.trace(String.format("Do refund GiftCardPayment: [number: %s]", presentCardInfoVO != null ? presentCardInfoVO.getCardNumber() : ""));

                            if (Factory.getInstance().getMainWindow().getCheckContainer().getPaymentTypesContainer().getRefundCheck().getPayments().size() > 1) {
                                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("NOT_ONE_GIFT_CARD"));
                                state = GiftCardState.ENTER_NUMBER;
                                visualPanel.setState(GiftCardState.ENTER_NUMBER);
                                return;
                            }

                            GiftCardPaymentEntity paymentTemp =
                                    (GiftCardPaymentEntity) Factory.getInstance().getMainWindow().getCheckContainer().getPaymentTypesContainer()
                                    .getRefundCheck().getPayments().get(0);

                            if (!paymentTemp.getAmount().equals(getSumInRoubles())) {
                                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("WRONG_NOMINAL"));
                                state = GiftCardState.ENTER_NUMBER;
                                visualPanel.setState(GiftCardState.ENTER_NUMBER);
                                return;
                            }

                            Long multiplicity = presentCardInfoVO.getMultiplicity();
                            Long maxAmount = presentCardInfoVO.getMaxAmount();

                            if (getSumInRoubles() % multiplicity != 0) {
                                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("CHECK_SUMMA_MULTIPLICITY") + " " + multiplicity);

                                state = GiftCardState.ENTER_NUMBER;
                                visualPanel.setState(GiftCardState.ENTER_NUMBER);
                                return;
                            }

                            if (getSumInRoubles() > maxAmount) {
                                Factory.getInstance().showMessage(ResBundlePaymentGiftCard.getString("MAXIMUM_SUMMA_FOR_CARD") + " " + maxAmount);

                                state = GiftCardState.ENTER_NUMBER;
                                visualPanel.setState(GiftCardState.ENTER_NUMBER);
                                return;
                            }

                            paymentTemp.setCardNumber(getCardNumber());
                            paymentTemp.setSumPay(sum);
                        } else {

                            log.trace(String.format("Do sale GiftCardPayment: [number: %s]", presentCardInfoVO != null ? presentCardInfoVO.getCardNumber() : ""));

                            try {
                                PresentCards service = getCardPluginService();
                                isPaymentProcessing = true;
                                if (service != null) {
                                    GiftCardPaymentEntity paymentEntity = ((GiftCardPaymentEntity) getPayment());

                                    paymentEntity.setCardNumber(getCardNumber());
                                    paymentEntity.setAmount(getInRoubles(presentCardInfoVO.getAmount()));
                                    paymentEntity.setBalance(presentCardInfoVO.getBalance() - sum);

                                    //Выводим текущее состояние оплаты
                                    log.trace(String.format("Before add GiftCardPayment: [number: %s, amount: %d]", paymentEntity.getCardNumber(),
                                            paymentEntity.getAmount()));

                                    addPaymentExcessGoodIfNeeded();

                                    //Выводим текущее состояние оплаты
                                    log.trace(String.format("Before sale GiftCard payment is: [number: %s, amount: %d]", paymentEntity.getCardNumber(),
                                            paymentEntity.getAmount()));
                                    service.payment(sum, getCardNumber(), Factory.getTechProcessImpl().getCheck(), Factory.getTechProcessImpl().getCurrentUser());

                                    getVisualPanel().setState(GiftCardState.DATA_EXCHANGE);
                                    GiftCardPaymentContainer.super.doProcessPayment();

                                    paymentEntity = ((GiftCardPaymentEntity) getPayment());
                                    log.trace(String.format("After payment by GiftCardPayment is: [number: %s, amount: %d]", paymentEntity.getCardNumber(),
                                            paymentEntity.getAmount()));
                                    getTechProcessEvents().eventAddCashlessPayment(Factory.getTechProcessImpl().getCheckWithNumber(), getPayment());
                                }
                            } catch (CardsException ce) {
                                Factory.getInstance().showMessage(ce.getMessage());

                                state = GiftCardState.ENTER_AMOUNT;
                                getVisualPanel().setState(state);
                            }
                            isPaymentProcessing = false;
                        }
                    }
                }, 0, true);

        visualPanel.setReportComponent(rc);
        setState(GiftCardState.DATA_EXCHANGE);
        rc.startAction();
    }

    private long getSumInRoubles() {
        return getInRoubles(sum);
    }

    private long getInRoubles(long value) {
        return CurrencyUtil.convertMoney(value).longValue();
    }

    private void setState(GiftCardState state) {
        this.state = state;
        getVisualPanel().setState(state);
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
            dispatchCloseEvent(false);
            return;
        }
        switch (state) {
            case ENTER_NUMBER:
                super.esc();
                reset();
                break;
            case ENTER_AMOUNT:
                super.esc();
                state = GiftCardState.ENTER_NUMBER;
                getVisualPanel().setState(state);
                break;
            case SHOW_WARNING_BEEP:
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                state = GiftCardState.SHOW_WARNING;
                getVisualPanel().setState(state);
                break;
            case SHOW_WARNING:
                reset();
                break;
            default:
                break;
        }
    }

    @Override
    public GiftCardPaymentComponent getVisualPanel() {
        return visualPanel;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public void number(Byte num) {
        if (isPaymentProcessing) {
            return;
        }
        setReset(false);

        switch (state) {
            case ENTER_NUMBER:
                if (isUserInputAllowed()) {
                    getCardNumberInputField().addChar(getChar(num));
                } else {
                    Factory.getTechProcessImpl().error();
                }
                break;
            case ENTER_AMOUNT:
                getVisualPanel().getPaymentInputField().addChar(getChar(num));
                break;
            default:
                break;
        }
    }

    @Override
    public void reset() {
        if (!isPaymentProcessing) {
            setReset(true);
            setChange(false);
            state = GiftCardState.ENTER_NUMBER;
            getCardNumberInputField().clear();
            resetPanels();
            getVisualPanel().setState(state);
        }
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return getVisualPanel();
    }

    @Override
    public String getChargeName() {
        return ResBundlePaymentGiftCard.getString("TO_CHARGE");
    }

    @Override
    public String getPaymentType() {
        if (isRefund()) {
            return ResBundlePaymentGiftCard.getString("REFUND_GIFT_PAYMENT");
        } else {
            return ResBundlePaymentGiftCard.getString("GIFT_CARD");
        }
    }

    @Override
    public String getTitlePaymentType() {
        if (isRefund()) {
            return ResBundlePaymentGiftCard.getString("REFUND_GIFT_PAYMENT");
        } else {
            return ResBundlePaymentGiftCard.getString("GIFT_PAYMENT");
        }
    }

    @Override
    public String getPaymentString() {
        return ResBundlePaymentGiftCard.getString("GIFT_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentGiftCard.getString("RETURN_PAYMENT_GIFT_CARD");
    }

    @Override
    public void setPaymentFields() {
        GiftCardPaymentEntity payment = (GiftCardPaymentEntity) getPayment();
        payment.setCardNumber(getCardNumber());
    }

    @Override
    public long getSum() {
        return sum;
    }

    @Override
    public void setSum(long sum) {
        this.sum = sum;
    }

    @Override
    public String getPaymentTypeName() {
        return ResBundlePaymentGiftCard.getString("GIFT_PAYMENT");
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return true;
    }

    @Override
    public boolean isChangeAvailable() {
        return false;
    }

    @Override
    protected Long getSumPay() {
        String excessGood = getPaymentExcessGoodMarking();
        if (StringUtils.isEmpty(excessGood)) {
            //товар не указан - настройка выключена
            return super.getSumPay();
        }
        return getSum();
    }

    /**
     * SR-1439 Проверка необходимости и добавление товара на разницу суммы чека и номинала гасимой ПК
     */
    private void addPaymentExcessGoodIfNeeded() throws CardsException {
        if (!cardAmountMoreThanSumToPay()) {
            return;
        }

        String excessGood = getPaymentExcessGoodMarking();
        if (StringUtils.isEmpty(excessGood)) {
            //товар не указан - настройка выключена
            log.trace("Payment-excess-good not specified");
            return;
        }

        try {
            ProductEntity product = Factory.getTechProcessImpl().searchProductWithoutCheckRestrictions(excessGood);
            if (product == null) {
                throw new PositionAddingException("Payment-excess-good not found");
            }

            long change = getSum() - getSurcharge();
            PositionEntity pos = new PositionEntity();
            pos.setAddedToCheckManually(false);
            pos.setCanChangeQnty(false);
            ProductToPositionConverter.fill(pos, product, BigDecimalConverter.convertQuantity(1000L), BigDecimalConverter.convertMoney(change), InsertType.DIRECTORY);
            tp.addPositionIgnoreState(pos, false, pos.getInsertType());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CardsException(String.format(ResBundlePaymentGiftCard.getString("PAYMENT_EXCESS_GOOD_NOT_ADDED"), excessGood));
        }

    }

    /**
     * Код товара для добавления на разницу суммы чека и номинала гасимой ПК
     */
    private String getPaymentExcessGoodMarking() {
        return (getPayment() != null && getPayment().getPaymentSettings() != null) ? getPayment().getPaymentSettings().getGiftCardPaymentExcessGoodMarking() : null;
    }

    public boolean isAdditionalInputAllowed() {
        return state == GiftCardState.ENTER_NUMBER;
    }

    public boolean isUserInputAllowed() {
        return (getPayment() != null) && getPayment().getPaymentSettings().isAllowUserInput();
    }

    public void setAdditionalInput(String inputData) {
        if (isAdditionalInputAllowed()) {
            getCardNumberInputField().setText(inputData);
            enter();
        }
    }

    private String getCardNumber() {
        return getCardNumberInputField().getValue().toString();
    }

    private InputFieldInterface getCardNumberInputField() {
        return getVisualPanel().getCardNumberInputField();
    }

    @Override
    public boolean isActivated() {
        PurchaseEntity purchase = getMainDividedPurchaseIfNeeded();
        return purchase == null || Factory.getTechProcessImpl().getSurchargeValue(purchase) > 0;
    }

    @Override
    public void left() {
        if (isPaymentProcessing) {
            return;
        }
        getVisualPanel().left(state);
    }

    @Override
    public void right() {
        if (isPaymentProcessing) {
            return;
        }
        getVisualPanel().right(state);
    }

    @Override
    public void preparePrintCheck(Check check, PaymentEntity p) {
        if (properties.getPrintCashlessSlipInCheck()) {
            return;
        }
        GiftCardPaymentEntity payment = (GiftCardPaymentEntity) p;
        TransactionData pt = new TransactionData(p.getDateCommit());
        pt.getSlips().add(String.format(ResBundlePaymentGiftCard.getString("GIFT_CARD_PAYMENT_SLIP"), payment.getCardNumber(),
                CurrencyUtil.convertMoney(payment.getRightAmount()).toString()));
        SlipsContainer sc = check.getCheckSlipsContainer("GiftCardPaymentEntity");
        if (sc == null) {
            sc = new SlipsContainer(null);
            check.setCheckSlipsContainer("GiftCardPaymentEntity", sc);
        }
        sc.add(pt);
    }

    @Override
    public void preparePrintCheck(List<ServiceDocument> serviceDocuments, PurchaseEntity purchase) {
        if (purchase.isSale() && purchase.getCheckStatus() != CheckStatus.Cancelled) {
            List<GiftCardPaymentEntity> giftCardPayments = new ArrayList<>();
            for (PaymentEntity p : purchase.getPayments()) {
                if (p instanceof GiftCardPaymentEntity) {
                    giftCardPayments.add((GiftCardPaymentEntity) p);
                }
            }

            if (!giftCardPayments.isEmpty()) {
                SimpleServiceDocument slip = new SimpleServiceDocument();
                slip.setPromo(true);
                List<String> rows = new ArrayList<>();
                rows.add(ResBundlePaymentGiftCard.getString("GIFT_CARD_PAYMENT_SLIP_HEADER"));
                rows.add("------------------------------------------");
                for (GiftCardPaymentEntity p : giftCardPayments) {
                    rows.add(String.format(ResBundlePaymentGiftCard.getString("GIFT_CARD_PAYMENT_SLIP"), p.getCardNumber(),
                            CurrencyUtil.convertMoney(p.getAmount() * 100).toString()));
                }
                rows.add("------------------------------------------");
                slip.addText(rows);
                serviceDocuments.add(slip);
            }
        }
    }

    @Override
    public boolean canUseAdminCommand(PaymentEntity payment) {
        return false;
    }
}
