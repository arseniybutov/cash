package ru.crystals.pos.visualization.products.kit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashException;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductKitController;
import ru.crystals.pos.catalog.ProductKitEntity;
import ru.crystals.pos.catalog.ResBundleKit;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.kit.integration.KitPluginAdapter;
import ru.crystals.pos.visualization.products.kit.model.KitPluginModel;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProductTypeConfig(typeName = KitPluginAdapter.PRODUCT_TYPE)
public class KitPluginController extends AbstractProductController<KitPluginModel> {
    private static final Logger log = LoggerFactory.getLogger(KitPluginController.class);

    @Override
    public void processProductAdd(ProductEntity product) {
        fillModelByKit((ProductKitEntity) product);
        super.processProductAdd(product);
    }

    @Override
    public boolean productCheckBeforeAdd(ProductEntity product, BigDecimal quantity) {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        } else {
            log.error("Incorrect quantity: {}", quantity);
            getAdapter().beepError("Incorrect quantity: " + quantity);
            return false;
        }
    }

    @Override
    public boolean isPossibleToAddPosition(PositionEntity positionEntity) {
        return true;
    }

    @Override
    public void eventChangeQuantity(PositionEntity position, Long newQuantity) {
        ProductKitController productKitController = (ProductKitController) position.getProduct().getProductConfig();
        try {
            productKitController.changeQuantity(position, newQuantity);
        } catch (CashException ex) {
            log.error("Quantity change cash exception", ex);

            Factory.getInstance().showMessage(ex.getMessage());
        } catch (Exception ex) {
            log.error("Quantity change unexpected error", ex);

            Factory.getInstance().showMessage(ResBundleKit.getString("QUANTITY_CHANGE_ERROR"));
        }
    }

    /**
     * При отмене добавления комплекта в чек некоторые позиции могли быть уже добавлены.
     * Необходимо их удалить.
     *
     * @param kit             Комплект который добавлялся
     * @param addedComponents Позиции которые были добавлены
     */
    public void deleteAlreadyAddedComponents(ProductEntity kit, List<ProductEntity> addedComponents) {
        ProductKitController productKitController = (ProductKitController) kit.getProductConfig();

        try {
            productKitController.deleteAlreadyAddedComponents(kit, addedComponents);
        } catch (CashException ex) {
            log.error("Delete components cash exception", ex);

            showMessage(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete components unexpected error", ex);

            showMessage(ResBundleKit.getString("COMPONENTS_DELETE_ERROR"));
        }
    }

    private void fillModelByKit(ProductKitEntity kit) {
        ProductKitController productKitController = (ProductKitController) kit.getProductConfig();

        try {
            List<ProductEntity> components = productKitController.getComponents(kit);
            getModel().setComponents(components);
            getModel().setPrice(CurrencyUtil.convertMoney(productKitController.calculatePrice(kit)));

        } catch (CashException ex) {
            log.error("Adding kit cash exception", ex);

            showMessage(ex.getMessage());
        } catch (Exception ex) {
            log.error("Adding kit unexpected error", ex);

            showMessage(ResBundleKit.getString("KIT_ADDING_ERROR"));
        }

    }

    @Override
    public void processPositionEdit(PositionEntity position) {
        try {
            PositionEntity pos = position.cloneLight();
            getModel().setPosition(pos);
            ProductEntity kit = pos.getProduct();
            getModel().setProduct(kit);
            fillModelByKit((ProductKitEntity) kit);
            getModel().setState(ProductContainer.ProductState.QUICK_EDIT);
        } catch (CloneNotSupportedException cnse) {
            log.error("Cannot clone position before edit", cnse);
        }
        getModel().changed();
    }

    @Override
    public void processProductView(ProductEntity product) {
        fillModelByKit((ProductKitEntity) product);
        super.processProductView(product);
    }

    private void showMessage(String message) {
        getModel().setMessage(message);
        getModel().setState(ProductContainer.ProductState.SHOW_MESSAGE);
        getModel().changed();
    }
}
