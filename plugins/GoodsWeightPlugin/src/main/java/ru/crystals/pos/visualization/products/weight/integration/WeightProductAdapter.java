package ru.crystals.pos.visualization.products.weight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.eventlisteners.WeightChangeEventListener;
import ru.crystals.pos.visualization.products.weight.controller.WeightProductController;
import ru.crystals.pos.visualization.products.weight.model.WeightProductPluginModel;
import ru.crystals.pos.visualization.products.weight.view.WeightProductView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Адаптер Весового товара
 * Тут новая реализация плагинов встраивается в текущую работу визуализации
 */
@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_WEIGHT_ENTITY, mainEntity = ProductWeightEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class WeightProductAdapter extends CommonAbstractPluginAdapter implements WeightChangeEventListener {
    private final WeightProductPluginModel model;
    private final WeightProductController controller;
    private final WeightProductView view;

    @Autowired
    WeightProductAdapter(Properties properties, WeightProductController controller) {
        this.controller = controller;
        model = new WeightProductPluginModel();
        view = new WeightProductView(properties);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected WeightProductView getView() {
        return view;
    }

    @Override
    protected WeightProductPluginModel getModel() {
        return model;
    }

    @Override
    protected WeightProductController getController() {
        return controller;
    }

    /**
     * Ловим событие изменения веса
     * Пробрасываем его в плагин
     *
     * @param weight
     */
    @Override
    public void weightChange(BigDecimal weight) {
        getController().weightChange(weight);
    }

    @Override
    public PositionEntity makeNewPosition(Class< ? extends PositionEntity> positionClass) {
        PositionEntity positionEntity = super.makeNewPosition(positionClass);
        ProductWeightEntity prod = (ProductWeightEntity) product;
        if (prod.getWeight() != null && prod.getWeight() > 0) {
            positionEntity.setQnty(prod.getWeight());
        } else {
            positionEntity.setQnty(0L);
        }
        return positionEntity;
    }

    @Override
    public boolean ableToAddPosition() {
        return super.ableToAddPosition() && isAllowSelling() && getController().checkWeight(product);
    }

    /**
     * Можно ли добавить в чек товар, вес которого меньше минимального
     * Если оповещение включено, то предполагается, что продавец в курсе и с сознанием дела подошел к этому
     * Если оповещение отключено, то будет осуществлена проверка веса, меньше ли вес минимального
     */
    private boolean isAllowSelling() {
        return (getController().isNotifyLowWeight() && (!getController().isWeightScanned() || getModel().isLowWeightConfirmed())) || !((WeightProductView) getVisualPanel()).isWeightLessThanMinValue();
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn, List<PositionEntity> validatedPositions) {
        Map<Long, PositionEntity> returnPositionsMap = new HashMap<>();

        for (PositionEntity p : checkReturnPositions) {
            returnPositionsMap.put(p.getNumberInOriginal(), p);
        }

        List<PositionEntity> result = new ArrayList<>();

        PositionEntity returnPosition;
        for (PositionEntity origPos : returnPurchase.getPositions()) {
            returnPosition = returnPositionsMap.get(origPos.getNumber());
            if (returnPosition != null) {
                if (origPos.getQnty().equals(returnPosition.getQnty())) {
                    result.add(returnPosition);
                }
            }
        }
        return result;
    }
}
