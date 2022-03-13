package ru.crystals.pos.visualization.products.kit.integration;

import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductKitEntity;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.kit.controller.KitPluginController;
import ru.crystals.pos.visualization.products.kit.model.KitPluginModel;
import ru.crystals.pos.visualization.products.kit.view.KitPluginView;

@ProductCashPluginComponent(typeName = KitPluginAdapter.PRODUCT_TYPE, mainEntity = ProductKitEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class KitPluginAdapter extends CommonAbstractPluginAdapter {
    public final static String PRODUCT_TYPE = ProductDiscriminators.PRODUCT_KIT_ENTITY;
    private final KitPluginModel model;
    private final KitPluginController controller;
    private final KitPluginView view;

    public KitPluginAdapter(KitPluginController controller) {
        this.controller = controller;
        model = new KitPluginModel();
        view = new KitPluginView();

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected KitPluginView getView() {
        return view;
    }

    @Override
    protected KitPluginModel getModel() {
        return model;
    }

    @Override
    protected KitPluginController getController() {
        return controller;
    }
}
