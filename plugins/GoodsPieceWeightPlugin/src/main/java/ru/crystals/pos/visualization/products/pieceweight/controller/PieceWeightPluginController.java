package ru.crystals.pos.visualization.products.pieceweight.controller;

import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractAgeRestrictedProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.pieceweight.ResBundleGoodsPieceWeight;
import ru.crystals.pos.visualization.products.pieceweight.model.PieceWeightPluginModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Created by alexey on 17.07.15.
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_PIECE_WEIGHT_ENTITY)
public class PieceWeightPluginController extends AbstractAgeRestrictedProductController<PieceWeightPluginModel> {
    private static final String MIN_QUANTITY_MESSAGE = ResBundleGoodsPieceWeight.getString("QUANTITY_IS_LESS_THAN_MINIMUM");

    @Override
    protected boolean productCheckBeforeAdd(ProductEntity product, BigDecimal quantity) {
        Long minQuantity = getModel().getMinQuantity();
        if (minQuantity != null && quantity.longValue() < minQuantity) {
            String message = String.format(MIN_QUANTITY_MESSAGE, minQuantity);
            beepError(message);
            getModel().setState(ProductContainer.ProductState.SHOW_MESSAGE);
            getModel().setMessage(message);
            getModel().changed();
            getModel().setPosition(null);
            return false;
        }

        return true;
    }

    @Override
    public void checkInputDataBeforeAdd(ProductEntity product, BigDecimal quantity, BigDecimal price) throws Exception {
        super.checkInputDataBeforeAdd(product, quantity, price);
        Long minQuantity = getModel().getMinQuantity();
        if (minQuantity != null && quantity.longValue() < minQuantity ) {
            throw new Exception("Unable to add piece-weight product: quantity is smaller as minimal possible");
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        // если по ESC или по ENTER на отмене проверки возраста вышли сюда - значит нужно завершить работу плагина
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            processEscPressEvent();
            return true;
        }
        return false;
    }
}
