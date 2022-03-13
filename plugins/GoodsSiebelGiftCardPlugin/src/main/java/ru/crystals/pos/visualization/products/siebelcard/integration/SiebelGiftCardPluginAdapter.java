package ru.crystals.pos.visualization.products.siebelcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductSiebelGiftCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.siebelcard.controller.SiebelGiftCardPluginController;
import ru.crystals.pos.visualization.products.siebelcard.model.SiebelGiftCardModel;
import ru.crystals.pos.visualization.products.siebelcard.view.SiebelGiftCardPluginView;

import java.util.List;

/**
 * @author s.pavlikhin
 */
@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_SIEBEL_GIFT_CARD_ENTITY, mainEntity = ProductSiebelGiftCardEntity.class)
@CashPluginQualifier(PluginType.GOODS)
@ConditionalOnBean({SiebelService.class})
public class SiebelGiftCardPluginAdapter extends CommonAbstractPluginAdapter {
    private final SiebelGiftCardModel model;
    private final SiebelGiftCardPluginController controller;
    private final SiebelGiftCardPluginView view;

    @Autowired
    public SiebelGiftCardPluginAdapter(SiebelGiftCardPluginController controller) {
        this.controller = controller;
        model = new SiebelGiftCardModel();
        view = new SiebelGiftCardPluginView();

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected SiebelGiftCardPluginView getView() {
        return view;
    }

    @Override
    protected SiebelGiftCardModel getModel() {
        return model;
    }

    @Override
    protected SiebelGiftCardPluginController getController() {
        return controller;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean canDeferPosision() {
        return false;
    }

    @Override
    public boolean canRepeatPosition() {
        return false;
    }

    @Override
    public void preparePrintCheck(List<ServiceDocument> serviceDocuments, PurchaseEntity purchase) {
    }

    @Override
    public boolean isReturnPossible(PositionEntity position) {
        if (position == null) {//произвольный возврат
            return false;
        }
        return super.isReturnPossible(position);
    }

    @Override
    public void notifyAddPosition(boolean checkOperationType) {
        // nop
    }
}
