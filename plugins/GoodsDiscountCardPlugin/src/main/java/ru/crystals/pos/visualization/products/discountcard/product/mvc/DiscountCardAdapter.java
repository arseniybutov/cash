package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardSearchResult;
import ru.crystals.pos.CashException;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.ProductContainer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Через эту {@link ProductContainer херь} получаем конструктор для разворачивания MVC для сценария "Продажа ДК (Дисконтной Карты)".
 *
 * @author aperevozchikov
 */
//NOTE: name обязан совпадасть с class.getSimpleName() сущности - иначе ProductTypeLoader этот плагин не "найдет"
@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_DISCOUNT_CARD_ENTITY, mainEntity = ProductDiscountCardEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class DiscountCardAdapter extends CommonAbstractPluginAdapter {
    private CheckService checkService;
    private CardsService cardsService;
    private final DiscountCardModel model;
    private final DiscountCardController controller;
    private final DiscountCardView view;

    @Autowired
    public DiscountCardAdapter(DiscountCardController controller) {
        this.controller = controller;
        model = new DiscountCardModel();
        view = new DiscountCardView();

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Autowired
    void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }

    @Autowired
    void setCardsService(CardsService cardsService) {
        this.cardsService = cardsService;
    }

    @Override
    protected DiscountCardModel getModel() {
        return model;
    }

    @Override
    protected DiscountCardController getController() {
        return controller;
    }

    @Override
    protected DiscountCardView getView() {
        return view;
    }

    @Override
    public boolean canUseAdminCommand(PositionEntity position) {
        return false;
    }

    @Override
    public boolean ableToAddPosition() {
        if (getView().showsMessage() && !getController().isOnFinishLine()) {
            // идет отображение сообщения об ошибке. либо крутится спиннер. и при этом не вызван метод finish контроллера.
            //  нельзя позицию добавить в чек
            return false;
        } else {
            return super.ableToAddPosition();
        }
    }

    @Override
    public void postRepeatProcess(PositionEntity position) throws CashException {
        PositionDiscountCardEntity positionDiscountCard = (PositionDiscountCardEntity) position;
        if (positionDiscountCard.isInstantApplicable()) {
            CardSearchResult cardSearchResult;
            String cardNumber = positionDiscountCard.getCardNumber();
            try {
                cardSearchResult = cardsService.getCardType(cardNumber);
            } catch (Exception e) {
                LOG.error("Cannot add card to check, not found card: {}", cardNumber);
                return;
            }
            if (cardSearchResult != null && cardSearchResult.getCard() != null) {
                PurchaseEntity purchase = checkService.getCheck(checkService.getCurrentCheckNum());
                if (purchase != null && !purchase.containsCardsByType(cardSearchResult.getCard().getCardTypeEnumValue()).isEmpty()) {
                    LOG.trace("The card {} will not be applied 'cause some other discount card was applied already", cardNumber);
                    return;
                }
                checkService.addCard(cardNumber, cardSearchResult.getCard(), checkService.getCurrentCheckNum(), null);
                getFactory().showCardIcon(cardSearchResult.getCard());
            }
        }
    }

    @Override
    public boolean fastAddPosition() {
        DiscountCardController controller = getController();
        if (controller.addPosition(controller.getModel().getProduct(), BigDecimal.ONE, controller.getModel().getProduct().getPrice().getPriceBigDecimal(), true)) {
            controller.getAdapter().dispatchCloseEvent(true);
            Factory.getInstance().getMainWindow().getCheckContainer().closeProductComponent(true);
        }

        return true;
    }

    @Override
    public boolean positionAddedAgain() {
        return getModel().areStepsPassed();
    }

    @Override
    public void onCheckFiscalized(PurchaseEntity purchase, boolean isCanceled) {
        if ((purchase.getOperationType() == PurchaseEntity.OPERATION_TYPE_SALE && isCanceled)
                || (purchase.getOperationType() == PurchaseEntity.OPERATION_TYPE_RETURN && !isCanceled)) {
            getController().deactivateCards(purchase);
        }
        // Забудем добавленные ДК после фискализации
        getModel().forgetProductsThatPassedSteps();
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase,
                                                   List<PositionEntity> checkReturnPositions,
                                                   boolean fullReturn,
                                                   List<PositionEntity> validatedPositions) {

        if (Factory.getTechProcessImpl().getProductConfig(ProductDiscountCardEntity.class.getSimpleName()).isReturnGiftCardCheckNeed()) {
            CheckContainer checkContainer = Factory.getInstance().getMainWindow().getCheckContainer();
            Object installedContainer = Factory.getInstance().getMainWindow().getInstalledContainer();
            if (installedContainer == checkContainer) {
                return new DiscountCardValidationView().validatePositions(checkReturnPositions, validatedPositions);
            } else {
                try {
                    Factory.getInstance().getMainWindow().installContainer(checkContainer);
                    return new DiscountCardValidationView().validatePositions(checkReturnPositions, validatedPositions);
                } finally {
                    Factory.getInstance().getMainWindow().installContainer(installedContainer);
                }
            }
        } else {
            return checkReturnPositions;
        }
    }

}
