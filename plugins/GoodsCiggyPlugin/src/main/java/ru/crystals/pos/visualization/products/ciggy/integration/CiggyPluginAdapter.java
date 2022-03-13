package ru.crystals.pos.visualization.products.ciggy.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductCiggyController;
import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.ProductCiggyPriceEntity;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;
import ru.crystals.pos.visualization.products.ciggy.model.CiggyProductModel;
import ru.crystals.pos.visualization.products.ciggy.ret.ValidateReturnCiggyForm;
import ru.crystals.pos.visualization.products.ciggy.view.CiggyPluginView;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Адаптер для плагина товаров.
 * Стыкует новую реализацию плагинов товаров со старой схемой визуализации.
 */
@ProductCashPluginComponent(typeName = CiggyPluginAdapter.PRODUCT_TYPE, mainEntity = ProductCiggyEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class CiggyPluginAdapter extends CommonAbstractPluginAdapter {
    public final static String PRODUCT_TYPE = ProductDiscriminators.PRODUCT_CIGGY_ENTITY;
    private final CiggyProductModel model;
    private final CiggyPluginView view;
    private final CiggyPluginController controller;
    private final ValidateReturnCiggyForm returnCiggyForm;

    @Autowired
    CiggyPluginAdapter(Properties properties, ValidateReturnCiggyForm returnCiggyForm, CiggyPluginController controller) {
        this.returnCiggyForm = returnCiggyForm;
        this.controller = controller;
        model = new CiggyProductModel();
        view = new CiggyPluginView(properties);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected CiggyPluginView getView() {
        return view;
    }

    @Override
    protected CiggyProductModel getModel() {
        return model;
    }

    @Override
    protected CiggyPluginController getController() {
        return controller;
    }

    @Override
    public int getMinAgeCheckBeforeSubTotal(PositionEntity position) {
        ProductEntity product = position.getProduct();
        return getProductConfig(PRODUCT_TYPE).isCheckAge() ? product.getProductConfig().calculateMinAge(product) : 0;
    }

    @Override
    public boolean ableToAddPosition() {
        if (!getController().isPriceSelected() && !checkForErrorStateOfAddingPosition()) {
            getView().showSelectPriceForm();
            return false;
        }
        return getModel().ableToAddPosition();
    }

    @Override
    public void setProduct(ProductEntity product) {
        getView().reset();
        super.setProduct(product);
    }

    @Override
    public PositionEntity makeNewPosition(Class<? extends PositionEntity> positionClass) {
        PositionEntity newPosition = super.makeNewPosition(positionClass);
        Set<ProductCiggyPriceEntity> additionalPrices = getAdditionalPrices();
        if (newPosition != null && product != null) {
            if (!CollectionUtils.isEmpty(additionalPrices)) {
                // Выбираем максимальную цену
                newPosition.setPriceStart(getMaxAddPrice(additionalPrices));
            }
            // SRTB-4661 Если пришли с маркой, и вытащили оттуда мрц, то должны учитывать в адаптере,
            // чтобы в призму и ДП всё уходило корректно
            if (product instanceof ProductCiggyEntity) {
                ProductCiggyController productController = (ProductCiggyController) product.getProductConfig();
                Optional<BigDecimal> priceFromMarkOp = productController.getPriceFromExcise((ProductCiggyEntity) product);
                priceFromMarkOp.ifPresent(newPosition::setPriceStartBigDecimal);
            }
            newPosition.setSum(CurrencyUtil.getPositionSum(newPosition.getPriceStart(), newPosition.getQnty()));
        }
        return newPosition;
    }

    /**
     * Выбираем максимальную цену из списка
     *
     * @param prices
     * @return
     */
    private Long getMaxAddPrice(Set<ProductCiggyPriceEntity> prices) {
        ProductCiggyPriceEntity maxPrice = Collections.max(prices);
        if (maxPrice.getSalePrice() != null && maxPrice.getSalePrice() > 0) {
            return maxPrice.getSalePrice();
        } else {
            return maxPrice.getPrice();
        }
    }

    private Set<ProductCiggyPriceEntity> getAdditionalPrices() {
        return ((ProductCiggyEntity) product).getAdditionalPrices();
    }

    @Override
    public List<PositionEntity> getReturnPositions(
            PurchaseEntity returnPurchase,
            List<PositionEntity> checkReturnPositions,
            boolean fullReturn,
            List<PositionEntity> validatedPositions
    ) {
        return returnCiggyForm.validatePositions(returnPurchase, checkReturnPositions, fullReturn, validatedPositions);
    }

    /**
     * Проверка на наличие ошибочного состояния для текущего процесса добавления позиции в чек
     * Проверка на наличие позиций с таким же АМ
     */
    private boolean checkForErrorStateOfAddingPosition() {
        return getController().checkNextStateOfAddingPosition(true).equals(CiggyPluginController.State.ERROR)
                || getController().checkForDuplicationExcise();
    }
}