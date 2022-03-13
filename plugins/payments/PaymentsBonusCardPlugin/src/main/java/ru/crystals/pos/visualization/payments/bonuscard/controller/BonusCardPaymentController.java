package ru.crystals.pos.visualization.payments.bonuscard.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import ru.crystals.cards.internalcards.InternalCards;
import ru.crystals.pos.cards.informix.InformixService;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.payments.BonusesConverter;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.integration.BonusCardPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentModel;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;
import ru.crystals.wsclient.cards.internal.BonusAccountVO;
import ru.crystals.wsclient.cards.internal.InternalCardInformationVO;
import ru.crystalservice.setv6.discounts.plugins.CompositeRowUtils;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


public class BonusCardPaymentController extends AbstractPaymentController {

    @Override
    public void processPayment(PaymentEntity payment) {
        preparePayment(payment);
        if (getAdapter().isRefund()) {
            getModel().setState(BonusCardPaymentState.PAYMENT);
            return;
        }

        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();
        long surcharge = CurrencyUtil.convertMoney(getModel().getInfo().getSurcharge());
        if (CollectionUtils.isEmpty(payment.getPaymentSettings().getProductRows())) {
            getModel().getInfo().setPaymentTypeSurcharge(surcharge);
        } else {
            long payTypePaidSum = purchase.getPayments().stream()
                    .filter(p -> Objects.equals(p.getPaymentType(), payment.getPaymentType())).mapToLong(PaymentEntity::getSumPay).sum();
            long payTypeMaxSum = purchase.getPositions().stream()
                    .filter(pos -> isGoodAffected(pos.getItem(), payment.getPaymentSettings())).mapToLong(PositionEntity::getSum).sum();
            if (payTypeMaxSum <= 0) {
                getModel().getInfo().setExceptionText(ResBundlePaymentBonusCard.getString("NO_GOODS_AFFECTED"));
                getModel().setState(BonusCardPaymentState.ERROR);
                return;
            }
            long payTypeSurcharge = payTypeMaxSum - payTypePaidSum;
            if (payTypeSurcharge <= 0) {
                getModel().getInfo().setExceptionText(ResBundlePaymentBonusCard.getString("BONUS_PAYMENT_COMPLETE"));
                getModel().setState(BonusCardPaymentState.ERROR);
                return;
            }
            getModel().getInfo().setPaymentTypeSurcharge(Math.min(payTypeSurcharge, surcharge));
        }

        getModel().getInfo().setPurchase(purchase);

        BonusCardPaymentInfo info = getModel().getInfo();
        purchase.getCards().stream().findFirst()
                .map(PurchaseCardsEntity::getNumber).ifPresent(info::setCardNumber);

        // если некая карта есть в чеке, сразу ищем именно ее
        if (StringUtils.isEmpty(getModel().getInfo().getCardNumber())) {
            getModel().setState(BonusCardPaymentState.ENTER_CARD_NUMBER);
            return;
        }

        try {
            getAccountInformation();
        } catch (Exception e) {
            getModel().getInfo().setExceptionText(e.getMessage());
            getModel().setState(BonusCardPaymentState.ERROR);
        }
    }

    private boolean isGoodAffected(String item, PaymentType paymentType) {
        return CompositeRowUtils.isGoodAffected(LoyalProductsConverter.getLoyalProductByItem(item), null, paymentType.getProductRows(), false);
    }

    private void preparePayment(PaymentEntity payment) {
        getModel().getInfo().clearCustomData();
        getModel().setState(BonusCardPaymentState.NOT_SET);
        getModel().setPayment(payment);
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {

    }

    public void unblockBonusesIfNeeded() {
        InternalCards service = getService();
        if (service instanceof InformixService) {
            ((InformixService) service).unblockBonuses();
        }
    }

    public void getAccountInformation() throws Exception {
        InternalCardInformationVO cardInformationVO = getCardInfo(getModel().getInfo().getCardNumber());
        getModel().getInfo().setCardInfo(cardInformationVO);
        prepareDataForAccountList(cardInformationVO);
    }

    private InternalCards getService() {
        return ((BonusCardPaymentPluginAdapter) getAdapter()).getPaymentService();
    }

    private InternalCardInformationVO getCardInfo(String cardNumber) throws Exception {
        return getService().getCardData(cardNumber);
    }

    public void processPaymentBonuses(BigDecimal sumToPay) {
        long amountLong = BigDecimalConverter.convertMoney(sumToPay);
        BonusCardPaymentEntity payment = (BonusCardPaymentEntity) getAdapter().getPayment();
        if (isRefund()) {
            try {
                payment.setSumPay(amountLong);
                if (getService() instanceof InformixService) {
                    payment.setAuthCode(((InformixService) getService()).refundBonuses(payment));
                } else {
                    getService().stornoBonusAccount(payment.getCardNumber(), payment.getAccountId(), payment.getAuthCode(), payment.getSumPay(),
                            Factory.getTechProcessImpl().getCheck(), Factory.getTechProcessImpl().getShift());
                }
                getAdapter().setCallDone(true);
                getAdapter().setSum(amountLong);
                getAdapter().processPayment();
                getModel().setState(BonusCardPaymentState.NOT_SET);
            } catch (Exception ce) {
                getModel().getInfo().setExceptionText(ce.getMessage());
                getModel().setState(BonusCardPaymentState.ERROR);
            }
        } else {
            PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
            try {
                BonusAccountVO selectedCount = getModel().getInfo().getBonusAccount();
                payment.setAuthCode(getService().writeOffFromBonusAccount(getModel().getInfo().getCardNumber(),
                        selectedCount.getBonusAccountsTypeVO().getBonusAccountTypeCode(), amountLong, check,
                        Factory.getTechProcessImpl().getShift()));

                payment.setAccountId(selectedCount.getBonusAccountsTypeVO().getBonusAccountTypeCode());
                payment.setAccountType(BonusCardPaymentEntity.BONUS_TYPE.BONUS);
                payment.setCardNumber(getModel().getInfo().getCardNumber());

                // количество потраченных бонусов - прогноз (так считается на сервере):
                payment.setCancelBonuses(BonusesConverter.getBonusesSpent(amountLong, selectedCount));
                getAdapter().setCallDone(true);
                getAdapter().setSum(amountLong);
                getAdapter().processPayment();
                getModel().setState(BonusCardPaymentState.NOT_SET);
            } catch (Exception ce) {
                getModel().getInfo().setExceptionText(ce.getMessage());
                getModel().setState(BonusCardPaymentState.ERROR);
            }
        }
    }

    public boolean isInformixProcessing() {
        return getService() instanceof InformixService;
    }

    public void setSelectedAccount(int number) {
        BonusAccountVO bonusAccountVO = getModel().getInfo().getCardInfo().getBonusAccounts().get(number);
        getModel().getInfo().setBonusAccount(bonusAccountVO);

        long availableBalance = BonusesConverter.countLimit(bonusAccountVO,
                getModel().getInfo().getPurchase().getCheckSumEnd(), getModel().getInfo().getPaymentTypeSurcharge());
        if (availableBalance <= 0) {
            getModel().getInfo().setExceptionText(ResBundlePaymentBonusCard.getString("BALANCE_0"));
            getModel().setState(BonusCardPaymentState.ERROR);
            return;
        }
        getModel().getInfo().setAvailableBalance(BigDecimalConverter.convertMoney(availableBalance));
        getModel().setState(BonusCardPaymentState.PAYMENT);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getModel().setState(BonusCardPaymentState.NOT_SET);
        }
        return super.keyPressedNew(e);
    }

    public boolean verifySum(BigDecimal sumToPay) {
        getModel().getPayment().setSumPay(sumToPay.movePointRight(2).longValue());
        return !(isWriteOffAllAmount() && getModel().getInfo().getAvailableBalance().compareTo(sumToPay) > 0);
    }

    public boolean isWriteOffAllAmount() {
        return getModel().getInfo().getBonusAccount() != null && getModel().getInfo().getBonusAccount().getBonusAccountsTypeVO().isWriteOffAllAmountAtOnce();
    }

    @Override
    public BonusCardPaymentModel getModel() {
        return (BonusCardPaymentModel) super.getModel();
    }

    private void prepareDataForAccountList(InternalCardInformationVO internalCard) {
        List<BonusAccountVO> bonuses = internalCard.getBonusAccounts();
        if (bonuses.isEmpty()) {
            getModel().getInfo().setExceptionText(ResBundlePaymentBonusCard.getString("NO_COUNTS_ON_CARD"));
            getModel().setState(BonusCardPaymentState.ERROR);
        } else if (bonuses.size() == 1) {
            setSelectedAccount(0);
        } else {
            getModel().setState(BonusCardPaymentState.CHOOSE_ACCOUNT);
        }
    }
}
