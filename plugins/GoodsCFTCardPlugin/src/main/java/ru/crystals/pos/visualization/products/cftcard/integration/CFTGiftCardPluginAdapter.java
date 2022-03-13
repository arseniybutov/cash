package ru.crystals.pos.visualization.products.cftcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.cards.cft.CFTBridge;
import ru.crystals.pos.catalog.ProductCFTGiftCardEntity;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.check.exception.ProductNotAllowedForthisCheckException;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.model.cft.CFTGiftCardsModel;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;
import ru.crystals.pos.visualization.products.cftcard.ResBundleGoodsCFTGiftCard;
import ru.crystals.pos.visualization.products.cftcard.controller.CFTGiftCardPluginController;
import ru.crystals.pos.visualization.products.cftcard.view.CFTGiftCardPluginView;

import javax.annotation.PostConstruct;
import java.util.List;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_CFTGIFT_CARD_ENTITY, mainEntity = ProductCFTGiftCardEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class CFTGiftCardPluginAdapter extends CommonAbstractPluginAdapter {

    @Autowired(required = false)
    @Qualifier(Constants.CFTType.Names.CFT_GIFTCARDS_PROCESSING_NAME)
    private CFTBridge cft;

    @Autowired(required = false)
    private CheckService checkService;

    private final CommonProductPluginModel model;
    private final CFTGiftCardPluginController controller;
    private final CFTGiftCardPluginView view;

    @Autowired
    public CFTGiftCardPluginAdapter(CFTGiftCardPluginController controller) {
        this.controller = controller;
        model = new CommonProductPluginModel();
        view = new CFTGiftCardPluginView();

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @PostConstruct
    private void localInit() {
        controller.setCftModel(new CFTGiftCardsModel(cft));
    }

    @Override
    protected CFTGiftCardPluginView getView() {
        return view;
    }

    @Override
    protected CommonProductPluginModel getModel() {
        return model;
    }

    @Override
    protected CFTGiftCardPluginController getController() {
        return controller;
    }

    @Override
    public boolean isAvailable() {
        return cft != null;
    }

    @Override
    public String isPossibleToAddPosition(PositionEntity entity) {
        if (entity != null) {
            if (checkService != null) {
                try {
                    checkService.isPossibleToAddPosition(entity, Factory.getTechProcessImpl().getCheck());
                    return null;
                } catch (ProductNotAllowedForthisCheckException pnafce) {
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                    return ResBundleGoodsCFTGiftCard.getString("CARD_CAN_NOT_BE_SALED_WITH_OTHER_PRODUCTS");
                } catch (PositionAddingException e) {
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                    return e.getMessage();
                }
            }
        }
        return null;
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
        //произвольный возврат
        if (position == null) {
            return false;
        }
        return super.isReturnPossible(position);
    }

    @Override
    public void notifyAddPosition(boolean checkOperationType) {
        // nop
    }
}
