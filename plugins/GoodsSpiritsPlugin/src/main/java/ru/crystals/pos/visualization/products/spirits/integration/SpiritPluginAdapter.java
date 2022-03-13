package ru.crystals.pos.visualization.products.spirits.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.catalog.limits.ProductLimitsService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.utils.CheckAgeController;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;
import ru.crystals.pos.visualization.products.spirits.view.SpiritsPluginView;

import java.util.Optional;

/**
 * Адаптер для плагина товаров.
 * Стыкует новую реализацию плагинов товаров со старой схемой визуализации.
 *
 * @author nbogdanov
 */
@ProductCashPluginComponent(typeName = SpiritPluginAdapter.PRODUCT_TYPE, mainEntity = ProductSpiritsEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class SpiritPluginAdapter extends CommonAbstractPluginAdapter {
    public final static String PRODUCT_TYPE = ProductDiscriminators.PRODUCT_SPIRITS_ENTITY;
    private ProductLimitsService alcoholLimitService;
    private final SpiritProductModel model;
    private final SpiritProductController controller;
    private final SpiritsPluginView view;

    @Autowired
    SpiritPluginAdapter(Properties properties, ExciseValidation egaisExciseCheckValidation, SpiritProductController controller) {
        this.controller = controller;
        model = new SpiritProductModel();
        view = new SpiritsPluginView(properties, egaisExciseCheckValidation);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Autowired
    void setAlcoholLimitService(ProductLimitsService alcoholLimitService) {
        this.alcoholLimitService = alcoholLimitService;
    }

    @Override
    protected SpiritsPluginView getView() {
        return view;
    }

    @Override
    protected SpiritProductModel getModel() {
        return model;
    }

    @Override
    protected SpiritProductController getController() {
        return controller;
    }

    @Override
    public int getMinAgeCheckBeforeSubTotal(PositionEntity position) {
        int ageAlcoLimit = Optional.ofNullable(position.getProduct().getAgeLimit()).orElse(0);

        if (getProductConfig(PRODUCT_TYPE).isCheckAge()) {
            ageAlcoLimit = Math.max(ageAlcoLimit, alcoholLimitService.checkAgeAlcoLimit());
            if (ageAlcoLimit == 0) {
                // в старых версиях не было настройки возраста, поэтому, по умолчанию 18 лет
                ageAlcoLimit = CheckAgeController.DEFAULT_MIN_AGE;
            }
        }

        return ageAlcoLimit;
    }
}