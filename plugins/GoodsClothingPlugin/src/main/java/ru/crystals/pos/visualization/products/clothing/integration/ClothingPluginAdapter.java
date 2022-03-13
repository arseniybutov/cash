package ru.crystals.pos.visualization.products.clothing.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductClothingEntity;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.clothing.controller.ClothingProductController;
import ru.crystals.pos.visualization.products.clothing.model.ClothingProductModel;
import ru.crystals.pos.visualization.products.clothing.ret.ValidateReturnlClothingForm;
import ru.crystals.pos.visualization.products.clothing.view.ClothingPluginView;

import java.util.List;

/**
 * Адаптер для плагина товаров.
 * Стыкует новую реализацию плагинов товаров со старой схемой визуализации.
 *
 * @author Tatarinov Eduard
 */
@ProductCashPluginComponent(typeName = ClothingPluginAdapter.PRODUCT_TYPE, mainEntity = ProductClothingEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class ClothingPluginAdapter extends CommonAbstractPluginAdapter {

    public final static String PRODUCT_TYPE = ProductDiscriminators.PRODUCT_CLOTHING_ENTITY;

    private final ValidateReturnlClothingForm returnlClothingForm;
    private final ClothingProductModel model;
    private ClothingProductController controller;
    private final ClothingPluginView view;

    @Autowired
    public ClothingPluginAdapter(ValidateReturnlClothingForm returnlClothingForm, ClothingProductController controller) {
        this.returnlClothingForm = returnlClothingForm;
        this.controller = controller;

        model = new ClothingProductModel();
        view = new ClothingPluginView();
        this.controller = controller;

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected ClothingProductModel getModel() {
        return model;
    }

    @Override
    protected ClothingProductController getController() {
        return controller;
    }

    @Override
    protected ClothingPluginView getView() {
        return view;
    }

    @Override
    public int getMinAgeCheckBeforeSubTotal(PositionEntity position) {
        return 0;
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn,
                                                   List<PositionEntity> validatedPositions) {
        return returnlClothingForm.validatePositions(returnPurchase, checkReturnPositions, fullReturn);
    }
}
