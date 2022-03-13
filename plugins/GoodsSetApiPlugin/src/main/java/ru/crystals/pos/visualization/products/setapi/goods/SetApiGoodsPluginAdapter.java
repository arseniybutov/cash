package ru.crystals.pos.visualization.products.setapi.goods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.service.SetAPIProductPluginInitMark;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;

/**
 * Кассовый плагин для манипулирования в чеке товарами, использующими плагины товаров Set API.
 */
@ProductCashPluginComponent(typeName = "SetApiGoodsPlugin", mainEntity = ProductEntity.class)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetApiGoodsPluginAdapter extends CommonAbstractPluginAdapter implements SetAPIProductPluginInitMark {
    private final SetApiGoodsPluginModel model;
    private final SetApiGoodsPluginController controller;
    private final SetApiGoodsPluginView view;

    @Autowired
    SetApiGoodsPluginAdapter(Properties properties, SetApiGoodsPluginController controller) {
        this.controller = controller;
        model = new SetApiGoodsPluginModel();
        view = new SetApiGoodsPluginView(properties);

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected SetApiGoodsPluginModel getModel() {
        return model;
    }

    @Override
    protected SetApiGoodsPluginController getController() {
        return controller;
    }

    @Override
    protected SetApiGoodsPluginView getView() {
        return view;
    }

    @Override
    public boolean fastAddPosition() {
        return false;
    }

    @Override
    public boolean isReturnPossible(PositionEntity position) {
        return super.isReturnPossible(position);
    }

    @Override
    public boolean canDeferPosision() {
        return false;
    }
}