package ru.crystals.pos.card.replacement.visualization.model;

import java.util.concurrent.CopyOnWriteArrayList;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.card.replacement.MLReplaceCardResult;
import ru.crystals.pos.card.replacement.visualization.listener.ModelChangedListener;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.ml.MLService;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardModel {
    private ProductEntity product;
    private ReplaceCardModelInfo modelInfo;
    private CopyOnWriteArrayList<ModelChangedListener> listeners;
    private ReplaceCardState state = ReplaceCardState.APPROACH_TO_REPLACE;
    private String messageText;
    private MLReplaceCardResult result;
    private boolean needToEnterOldCard = false;

    public ReplaceCardModel(ProductEntity product) {
        this.product = product;
    }

    public ReplaceCardModel(ProductEntity product, String oldCardNumber) {
        this.product = product;
        modelInfo = new ReplaceCardModelInfo(oldCardNumber);
        if(oldCardNumber != null) {
            state = ReplaceCardState.GO_TO_REPLACE;
        } else {
            needToEnterOldCard = true;
        }
        listeners = new CopyOnWriteArrayList<>();
    }

    public void addListener(ModelChangedListener listener) {
        if(listener != null) {
            listeners.add(listener);
        }
    }

    public void setModelInfo(ReplaceCardModelInfo modelInfo) {
        this.modelInfo = modelInfo;

    }

    public void fireModelChanged() {
        for (ModelChangedListener listener : listeners) {
            listener.modelChanged(new ReplaceCardModelChangedEvent(modelInfo, state, messageText));
        }
    }

    public ProductEntity getProduct() {
        return product;
    }

    public PositionEntity getPosition() {
        PositionDiscountCardEntity result = new PositionDiscountCardEntity();
        result.setProduct(product);
        result.setCardNumber(modelInfo.getNewCardNumber());
        result.setHolderId(modelInfo.getContractId());
        result.setQnty(1000L);
        result.setPriceStart(product.getPrice().getPrice());
        result.setReplaced(true);
        result.setInsertType(InsertType.HAND);
        return result;
    }

    public ReplaceCardModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setState(ReplaceCardState state) {
        this.state = state;
        fireModelChanged();
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public ReplaceCardState getState() {
        return state;
    }

    public MLReplaceCardResult getResult() {
        return result;
    }

    public void fillResult(boolean status) {
        if(status) {
            //Карта успешно добавилась
            result = new MLReplaceCardResult(modelInfo.getNewCardNumber(), CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME), getPosition());
        } else {
            //Отказались на этапе GO_TO_REPLACE вернем старую карту
            result = new MLReplaceCardResult(modelInfo.getOldCardNumber(), CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME), null);
        }
    }

    public boolean isNeedToEnterOldCard() {
        return needToEnterOldCard;
    }
}
