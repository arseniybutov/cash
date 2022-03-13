package ru.crystals.pos.visualization.products.jewel.controller;

import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.MarkType;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractCommonMarkedProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.jewel.model.JewelProductModel;

@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_JEWEL_ENTITY)
public class JewelPluginController extends AbstractCommonMarkedProductController<JewelProductModel> {

    public void update(ProductEntity product, PositionEntity pos) {
        getModel().setProduct(product);
        getModel().setPosition(pos);
    }

    @Override
    public boolean isRefund() {
        return getContainer().getProductState() == ProductContainer.ProductState.REFUND;
    }

    @Override
    public boolean needCheckExciseForAddMarkedProduct() {
        boolean softMarkedEnabled = softMarkedEnabledForCurrentPosition();
        if (!isRefund()) {
            return currentProductIsMarked() || softMarkedEnabled;
        }
        MarkType currentProductMarkType = getProduct() != null && getProduct().getMarkType() != null ? getProduct().getMarkType() : MarkType.UNKNOWN;
        boolean needCheckExciseOnAnyRefund = needCheckMarkedProductExciseOnAnyRefund(currentProductMarkType);
        return (currentProductIsMarked() || softMarkedEnabled) && needCheckExciseOnAnyRefund;
    }
}
