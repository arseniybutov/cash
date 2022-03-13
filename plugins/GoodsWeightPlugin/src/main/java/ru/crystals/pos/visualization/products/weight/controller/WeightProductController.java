package ru.crystals.pos.visualization.products.weight.controller;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductWeightController;
import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.exception.PositionalCouponException;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.scale.Scale;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.techprocess.StatePurchase;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.utils.CommonLogger;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractAgeRestrictedProductController;
import ru.crystals.pos.visualization.eventlisteners.WeightChangeEventListener;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.weight.ResBundleGoodsWeight;
import ru.crystals.pos.visualization.products.weight.model.WeightProductPluginModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Контроллер для весового товара
 * Выполняет логику плагина.
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_WEIGHT_ENTITY)
public class WeightProductController extends AbstractAgeRestrictedProductController<WeightProductPluginModel> implements WeightChangeEventListener {
    private static final String MIN_WEIGHT_MESSAGE = ResBundleGoodsWeight.getString("WEIGHT_LESS_THAN_MININUM");
    private static final String CHECK_WEIGHT_LESS_THAN_MININUM = ResBundleGoodsWeight.getString("CHECK_WEIGHT_LESS_THAN_MININUM");
    private static final String GET_SCALES_WEIGHT_ERROR = ResBundleGoodsWeight.getString("GET_SCALES_WEIGHT_ERROR");
    private static final String PUT_GOODS_ON_SCALES = ResBundleGoodsWeight.getString("PUT_GOODS_ON_SCALES");
    private static final String WEIGHT_FROM_SCALES_LESS_LABEL = ResBundleGoodsWeight.getString("WEIGHT_FROM_SCALES_LESS_LABEL");
    private static final String WEIGHT_FROM_SCALES_MORE_LABEL = ResBundleGoodsWeight.getString("WEIGHT_FROM_SCALES_MORE_LABEL");
    private final Set<WeightChangeEventListener> listeners = new HashSet<>(2);
    private boolean weightScanned;
    private TechProcessServiceAsync techProcessServiceAsync;

    @Autowired(required = false)
    void setTechProcessServiceAsync(TechProcessServiceAsync techProcessServiceAsync) {
        this.techProcessServiceAsync = techProcessServiceAsync;
    }

    /**
     * Вызывается, когда на весах изменился вес товара
     * пробросим внутрь плагина
     */
    @Override
    public void weightChange(BigDecimal weight) {
        for (WeightChangeEventListener listener : listeners) {
            listener.weightChange(weight);
        }
    }

    public void addWeightChangeListener(WeightChangeEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Проверяем есть ли прикассовые весы для взвешивания весового товара
     */
    public boolean isCashScaleUsed() {
        return getAdapter().getFactory().getTechProcess().checkScaleModuleState();
    }

    public Scale getScales() {
        return techProcessServiceAsync == null ? null : techProcessServiceAsync.getScaleModule();
    }

    public StatePurchase getPurchaseState() {
        return getAdapter().getFactory().getTechProcess().getPurchaseState();
    }

    @Override
    protected boolean productCheckBeforeAdd(ProductEntity product, BigDecimal quantity) {
        if (!isNotifyLowWeight(product) && isLessThatMinValue(quantity) && !getModel().isLowWeightConfirmed()) {
            toWarningState(product.getProductConfig().getMinWeight(), MIN_WEIGHT_MESSAGE, ProductContainer.ProductState.SHOW_MESSAGE);
            return false;
        }
        return checkWeight(product);
    }

    /**
     * Проверка и оповещение, если вес меньше минимального допустимого веса
     * @param product продукт, вес которого проверяется
     * @param weight вес продукта
     */
    public boolean checkAndNotifyForLowWeight(ProductEntity product, Long weight) {
        BigDecimal quantity = BigDecimal.valueOf(weight * product.getPrecision());
        if (isNotifyLowWeight(product) && isLessThatMinValue(quantity) && !getModel().isLowWeightConfirmed()) {
            toWarningState(product.getProductConfig().getMinWeight(), CHECK_WEIGHT_LESS_THAN_MININUM, ProductContainer.ProductState.SHOW_QUESTION);
            return false;
        }
        return true;
    }

    /**
     * Переход в состояние оповещения
     * @param weight вес, для отображения
     * @param textMessage сообщение, для отображения
     * @param state состояние оповещения
     */
    private void toWarningState(Long weight, String textMessage, ProductContainer.ProductState state) {
        String message = String.format(textMessage, weight);
        getModel().setState(state);
        beepError(message);
        getModel().setMessage(message);
        getModel().changed();
    }

    /**
     * Уведомлять ли о маленьком весе товара
     */
    public boolean isNotifyLowWeight() {
        return isNotifyLowWeight(getModel().getProduct());
    }

    /**
     * Уведомлять ли о маленьком весе товара
     * @param productEntity продукт, из которого будет получен продуктовый контроллер
     */
    public boolean isNotifyLowWeight(ProductEntity productEntity) {
        ProductWeightController productController = (ProductWeightController) productEntity.getProductConfig();
        return productController.isNotifyLowWeight();
    }

    /**
     * SRTB-980
     * Проверить сканированный вес используя прикассовые весы на минимальное отклонение (при включенном параметре в префиксе и подключенных весах).
     * @param product добавляемый товар
     * @return true - всё хорошо, false - вес не соответствует, необходимо вывести сообщение
     */
    public boolean checkWeight(ProductEntity product) {
        boolean result = true;
        ProductWeightEntity productWeight = (ProductWeightEntity) product;
        Long weightFromProduct = productWeight.getWeight();
        ProductWeightController productController = (ProductWeightController) product.getProductConfig();
        if (productController.isUseWeightCheckOnScales() && productWeight.isWeightCheckOnScales() && weightFromProduct != null && weightFromProduct > 0 && getScales() != null) {
            Scale scales = getScales();
            if(scales == null) {
                return true;
            }
            try {
                int weightFromScales = scales.getWeight();
                if (weightFromScales == 0) {
                    getModel().setState(ProductContainer.ProductState.SHOW_MESSAGE);
                    getModel().setMessage(PUT_GOODS_ON_SCALES);
                    getModel().changed();
                    CommonLogger.getCommonLogger().error(PUT_GOODS_ON_SCALES);
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                    return false;
                }
                long weightDifference = weightFromScales - weightFromProduct;
                result = Math.abs(weightDifference) <= productController.getWeightPermissibleVariation();
                if (!result) {
                    String msg;
                    if (weightDifference < 0) {
                        msg = WEIGHT_FROM_SCALES_LESS_LABEL;
                    } else {
                        msg = WEIGHT_FROM_SCALES_MORE_LABEL;
                    }
                    msg = String.format(msg, Math.abs(weightDifference));
                    getModel().setState(ProductContainer.ProductState.SHOW_MESSAGE);
                    getModel().setMessage(msg);
                    getModel().changed();
                    CommonLogger.getCommonLogger().error(msg);
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                }
            } catch (ScaleException e) {
                getModel().setState(ProductContainer.ProductState.SHOW_MESSAGE);
                getModel().setMessage(GET_SCALES_WEIGHT_ERROR);
                getModel().changed();
                CommonLogger.getCommonLogger().error(GET_SCALES_WEIGHT_ERROR);
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                return false;
            }
        }
        return result;
    }

    public boolean isLessThatMinValue(BigDecimal quantity) {
        return ((ProductWeightController) getModel().getProduct().getProductConfig()).isLessThatMinValue(quantity);
    }

    public void sendChangeWeightEvent(BigDecimal weight) {
        try {
            if (getModel().getPosition() != null) {
                PositionEntity positionForEvent = getModel().getPosition().cloneLight();
                positionForEvent.setQntyBigDecimal(weight);
                Factory.getTechProcessImpl().getTechProcessEvents().eventUpdateProductOnDisplay(
                        Factory.getTechProcessImpl().getCheckOrNextCheckStub(true), positionForEvent);
            }
        }catch(CloneNotSupportedException e){
            // NOP
        }
    }

    @Override
    protected void checkPositionRestrictionForCoupon(CardTypeEntity cardTypeEntity) throws PositionalCouponException {
        Collection<CardTypeEntity> couponsToApply = getAdapter().getPositionalCoupons();
        if (CollectionUtils.isNotEmpty(couponsToApply)) {
            throw new PositionalCouponException(ResBundleVisualization.getString("ONLY_ONE_POSITIONAL_COUPON"),
                    cardTypeEntity.getCardTypeEnumValue());
        }
    }

    @Override
    protected void checkCouponQuantityRestriction(BigDecimal quantity) throws Exception {

    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        // если по ESC или по ENTER вышли сюда - значит нужно завершить работу плагина
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            processEscPressEvent();
            return true;
        }
        return false;
    }

    public boolean isWeightScanned() {
        return weightScanned;
    }

    public void setWeightScanned(boolean weightScanned) {
        this.weightScanned = weightScanned;
    }

    public void lowWeightConfirmed() {
        getModel().setLowWeightConfirmed(true);
        getModel().setState(ProductContainer.ProductState.ADD);
        getModel().changed();
    }
}