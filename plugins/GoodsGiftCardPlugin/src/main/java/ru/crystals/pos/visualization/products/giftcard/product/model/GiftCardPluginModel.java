package ru.crystals.pos.visualization.products.giftcard.product.model;


import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;
import ru.crystals.pos.visualization.products.ProductContainer;

public class GiftCardPluginModel extends CommonProductPluginModel {

    private PresentCardInformationVO presentCard = null;
    private GiftCardPluginState internalState = null;

    public GiftCardPluginModel() {
        this.internalState = GiftCardPluginState.ENTER_CARD_NUMBER;
    }

    public PresentCardInformationVO getPresentCard() {
        return presentCard;
    }

    public void setPresentCard(PresentCardInformationVO presentCard) {
        this.presentCard = presentCard;
    }

    public GiftCardPluginState getInternalState() {
        return internalState;
    }

    public void setInternalState(GiftCardPluginState state) {
        this.internalState = state;
        processModelChangedEvent();
    }

    @Override
    public void setState(ProductContainer.ProductState state) {
        super.setState(state);
        if (state == ProductContainer.ProductState.ADD) {
            setInternalState(GiftCardPluginState.ENTER_CARD_NUMBER);
        }
    }
    
    @Override
    public boolean ableToAddPosition() {
        return getInternalState() != GiftCardPluginState.ENTER_CARD_NUMBER;
    }
}
