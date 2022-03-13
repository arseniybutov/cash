package ru.crystals.pos.visualization.products.cftcard.controller;

import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.PositionCFTGiftCardEntity;
import ru.crystals.pos.model.cft.CFTGiftCardsModel;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

import java.math.BigDecimal;

@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_CFTGIFT_CARD_ENTITY)
public class CFTGiftCardPluginController extends AbstractProductController<CommonProductPluginModel> {
    private CFTGiftCardsModel cftModel;

    @Override
    public boolean barcodeScanned(String barcode) {
        return super.barcodeScanned(barcode);
    }

    public void activateCFTCard(PositionCFTGiftCardEntity position, String enteredNumber, CardType type) throws CFTException {
        position.setClientID(enteredNumber);
        position.setAmount(position.getPriceStart());
        cftModel.activate(position, type);
        getModel().setPosition(position);
        if (getAdapter().doPositionAdd(getModel().getPosition())){
            getAdapter().dispatchCloseEvent(true);
        }
    }

    public String isPossibleToAddPosition() {
        PositionCFTGiftCardEntity entity = new PositionCFTGiftCardEntity();
        fillDefaultPosition(BigDecimal.ONE, getModel().getProduct().getPrice().getPriceBigDecimal(), getModel().getProduct(), entity);
        return getAdapter().isPossibleToAddPosition(entity);
    }

    public void setCftModel(CFTGiftCardsModel cftModel) {
        this.cftModel = cftModel;
    }
}
