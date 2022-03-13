package ru.crystals.pos.visualization.products.pieceweight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductPieceWeightController;
import ru.crystals.pos.catalog.ProductPieceWeightEntity;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.pieceweight.controller.PieceWeightPluginController;
import ru.crystals.pos.visualization.products.pieceweight.model.PieceWeightPluginModel;
import ru.crystals.pos.visualization.products.pieceweight.view.PieceWeightPluginView;

/**
 * Created by alexey on 17.07.15.
 */
@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_PIECE_WEIGHT_ENTITY, mainEntity = ProductPieceWeightEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class PieceWeightPluginAdapter extends CommonAbstractPluginAdapter {
    private final PieceWeightPluginModel model;
    private final PieceWeightPluginController controller;
    private final PieceWeightPluginView view;

    @Autowired
    PieceWeightPluginAdapter(Properties properties, ProductPieceWeightController productPieceWeightController, PieceWeightPluginController controller) {
        this.controller = controller;
        //Для штучно-весового в минимальном весе передается минимальное количество.
        model = new PieceWeightPluginModel(productPieceWeightController.getMinWeight());
        view = new PieceWeightPluginView(properties);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected PieceWeightPluginView getView() {
        return view;
    }

    @Override
    protected PieceWeightPluginModel getModel() {
        return model;
    }

    @Override
    protected PieceWeightPluginController getController() {
        return controller;
    }
}
