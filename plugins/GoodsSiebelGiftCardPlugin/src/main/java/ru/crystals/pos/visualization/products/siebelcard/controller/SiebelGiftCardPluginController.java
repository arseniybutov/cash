package ru.crystals.pos.visualization.products.siebelcard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.cards.siebel.SiebelGiftCardResult;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.cards.siebel.exception.SiebelServiceException;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSiebelGiftCardEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.siebelcard.ResBundleGoodsSiebelGiftCard;
import ru.crystals.pos.visualization.products.siebelcard.model.SiebelGiftCardModel;
import ru.crystals.pos.visualization.products.siebelcard.model.SiebelGiftCardState;
import ru.crystals.siebel.SiebelUtils;

import java.math.BigDecimal;

/**
 * @author s.pavlikhin
 */
@Component
@ConditionalOnBean({SiebelService.class})
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_SIEBEL_GIFT_CARD_ENTITY)
public class SiebelGiftCardPluginController extends AbstractProductController<SiebelGiftCardModel> {
    private final SiebelService service;

    @Autowired
    public SiebelGiftCardPluginController(SiebelService service) {
        this.service = service;
    }

    @Override
    public void processProductAdd(ProductEntity product) {
        getModel().setInternalState(SiebelGiftCardState.ADD_GIFT_CARD);
        super.processProductAdd(product);
    }

    /**
     * Находит информацию по конкретной ПК и добавляет эту информацию в модель.
     *
     * @throws SiebelServiceException - при отсутствии связи, при некорректном состоянии ПК, при некорректном заведении ПК.
     */
    public void findAndAddGiftCardInfo(String cardNumber) throws SiebelServiceException, PositionAddingException {
        SiebelGiftCardResult card = getSiebelCardStatus(cardNumber);

        PositionSiebelGiftCardEntity position = new PositionSiebelGiftCardEntity();
        fillDefaultPosition(BigDecimal.ONE, isFixedRateGiftCard(card) ? CurrencyUtil.convertMoney(card.getCardRate()) : BigDecimal.ZERO, getProduct(), position);
        position.setCardNumber(card.getCardNumber());
        position.setBankGiftCard(card.isBankCard());

        checkCanAddPosition(position);

        getModel().setCard(card);
        getModel().setPosition(position);

        if (isFixedRateGiftCard(card)) {
            getModel().setInternalState(SiebelGiftCardState.FIXED_RATE_GIFT_CARD);
        } else {
            getModel().setInternalState(SiebelGiftCardState.NOT_FIXED_RATE_GIFT_CARD);
        }
        getModel().changed();
    }

    /**
     * Запрос на продажу ПК в Siebel и в случае успеха добавление карты в чек.
     * @return Сообщение кассиру
     * @throws SiebelServiceException при отсутствии связи, при некорректном состоянии ПК.
     */
    public String saleGiftCard() throws SiebelServiceException {
        if (service == null) {
            throw new SiebelServiceException(ResBundleGoodsSiebelGiftCard.getString("NO_CONNECTION"));
        }

        PositionSiebelGiftCardEntity position = (PositionSiebelGiftCardEntity) getModel().getPosition();
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheckOrNextCheckStub(true);
        purchase.setShift(Factory.getTechProcessImpl().getShift());
        String cashierMessage = service.saleGiftCard(position.getCardNumber(), purchase, position.isBankGiftCard());

        if (getAdapter().doPositionAdd(getModel().getPosition())) {
            getAdapter().dispatchCloseEvent(true);
        }

        return SiebelUtils.getFormattedCashierMessage(cashierMessage);
    }

    /**
     * Получить статус карты Siebel.
     *
     * @param cardNumber
     * @return
     * @throws SiebelServiceException при отсутствии связи, при некорректном состоянии ПК, при некорректном заведении ПК.
     */
    private SiebelGiftCardResult getSiebelCardStatus(String cardNumber) throws SiebelServiceException, PositionAddingException {
        if (service == null) {
            throw new SiebelServiceException(ResBundleGoodsSiebelGiftCard.getString("NO_CONNECTION"));
        }
        SiebelGiftCardResult card = service.getGiftCardInfo(cardNumber, true);
        // Если статус не "Не продана"
        if (!CardStatus.Inactive.equals(card.getStatus())) {
            throw new PositionAddingException(SiebelUtils.getFormattedCashierMessage(card.getCashierMessage()));
        }
        return card;
    }

    /**
     * Проверяет на возможность добавления позиции в чек.
     */
    private void checkCanAddPosition(PositionEntity positionEntity) throws PositionAddingException {
        checkCanAddGiftCard(positionEntity);
        Factory.getTechProcessImpl().isPossibleToAddPosition(positionEntity);
    }

    /**
     * В чеке может быть или только одна банковская ПК или одна и больше обычных ПК.
     */
    private void checkCanAddGiftCard(PositionEntity positionEntity) throws PositionAddingException {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity != null && purchaseEntity.getPositions().stream()
                .anyMatch(p -> (p instanceof PositionSiebelGiftCardEntity) && ((PositionSiebelGiftCardEntity) p).isBankGiftCard())) {
            throw new PositionAddingException(ResBundleGoodsSiebelGiftCard.getString("ERROR_GOOD_TYPE"));
        }
        if (positionEntity != null && ((PositionSiebelGiftCardEntity) positionEntity).isBankGiftCard() &&
                purchaseEntity != null && purchaseEntity.getPositions().stream()
                .anyMatch(p -> (p instanceof PositionSiebelGiftCardEntity))) {
            throw new PositionAddingException(ResBundleGoodsSiebelGiftCard.getString("ERROR_GOOD_TYPE"));
        }
    }

    /**
     * Вернет <code>true</code> если у ПК фиксированный номинал.
     */
    private boolean isFixedRateGiftCard(SiebelGiftCardResult card) {
        return card != null && card.getCardRate() != null && card.getCardRate() > 0;
    }

}
