package ru.crystals.pos.visualization.products.piece.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPieceController;
import ru.crystals.pos.catalog.ProductPieceEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.piece.controller.PiecePluginController;
import ru.crystals.pos.visualization.products.piece.model.PiecePluginModel;
import ru.crystals.pos.visualization.products.piece.ret.ValidateReturnPieceForm;
import ru.crystals.pos.visualization.products.piece.view.PiecePluginView;

import java.util.List;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_PIECE_ENTITY, mainEntity = ProductPieceEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class PiecePluginAdapter extends CommonAbstractPluginAdapter {
    private final PiecePluginModel model;
    private final PiecePluginController controller;
    private final PiecePluginView view;
    private final ValidateReturnPieceForm returnPieceForm;

    @Autowired
    PiecePluginAdapter(Properties properties, ValidateReturnPieceForm returnPieceForm, ProductPieceController productPieceController,
                       PiecePluginController controller) {
        this.returnPieceForm = returnPieceForm;
        this.controller = controller;
        model = new PiecePluginModel();
        view = new PiecePluginView(properties, productPieceController);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected PiecePluginView getView() {
        return view;
    }

    @Override
    protected PiecePluginModel getModel() {
        return model;
    }

    @Override
    protected PiecePluginController getController() {
        return controller;
    }

    @Override
    public void setProduct(ProductEntity product) {
        getView().clear();
        super.setProduct(product);
    }

    @Override
    public int getMinAgeCheckBeforeSubTotal(PositionEntity position) {
        ProductEntity product = position.getProduct();
        return product.getProductConfig().calculateMinAge(product);
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn, List<PositionEntity> validatedPositions) {
        return returnPieceForm.validatePositions(returnPurchase, checkReturnPositions, fullReturn, validatedPositions);
    }
}
