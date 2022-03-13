package ru.crystals.pos.visualization.products.piece.controller;

import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.MarkType;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractCommonMarkedProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.piece.model.PiecePluginModel;

import java.awt.event.KeyEvent;


/**
 * Контроллер штучных товаров
 * Энергетики теперь проверяют возраст покупателя, если есть ограничение в БД
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_PIECE_ENTITY)
public class PiecePluginController extends AbstractCommonMarkedProductController<PiecePluginModel> {

    @Override
    public void processProductAdd(ProductEntity product) {
        getModel().setProduct(product);

        setAgeRestriction(product);

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode()
                && product.getBarCode() != null
                && product.getBarCode().getCount() != 1000L;

        setScannedByBarcode(isFoundByBarcode);
        PositionEntity posQuick = new PositionEntity();
        posQuick.setProduct(product);
        posQuick.setPriceStart(product.getPrice().getPrice());
        posQuick.setPriceEnd(product.getPrice().getPrice());
        if(isFoundByBarcode) {
            if (product.getBarCode().getCount() == 0) {
                posQuick.setQnty(null);
                posQuick.setCanChangeQnty(true);
            } else {
                posQuick.setQnty(product.getBarCode().getCount());
                posQuick.setCanChangeQnty(false);
            }
        } else {
            posQuick.setQnty(1000L);
        }
        fillDefaultPosition(posQuick.getQntyBigDecimal(), product.getPrice().getPriceBigDecimal(), product, posQuick);

        if (posQuick.isCanChangeQnty()) {
            // Для позиций подлежащих "мягкой" маркировке изменять количество нельзя
            posQuick.setCanChangeQnty(!softMarkedEnabledForCurrentPosition());
        }

        getModel().setPosition(posQuick);
        getModel().setState(ProductContainer.ProductState.ADD);
        getModel().setNeedScanMark(needCheckExciseForAddMarkedProduct());
        getModel().setCanSkipScanMarkForm(canSkipScanMarkForCurrentPosition());
        getModel().changed();
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

    /**
     * Признак. Нужно ли для маркированного товара дополнительно сканировать (и проверять) марку при добавлении в чек.
     * Добавление в чек - либо при продаже, либо при произвольном возврате.
     */
    @Override
    public boolean needCheckExciseForAddMarkedProduct() {
        boolean softMarkedEnabled = softMarkedEnabledForCurrentPosition();
        if (!isRefund()) {
            return currentProductIsMarked() || softMarkedEnabled;
        }
        //если в настройках отключено (false) сканирование (и/или проверка) марки маркированного товара при произвольном возврате,
        //то это условие распространяется только в случае, если этот товар не был добавлен по марке (т.е. если товар добавлен по марке,
        //то возвращаем как маркированный)
        MarkType currentProductMarkType = getProduct() != null && getProduct().getMarkType() != null ? getProduct().getMarkType() : MarkType.UNKNOWN;
        boolean needCheckExciseOnAnyRefund = needCheckMarkedProductExciseOnAnyRefund(currentProductMarkType);
        return (currentProductIsMarked() || softMarkedEnabled) && (needCheckExciseOnAnyRefund || currentProductHasScannedExcise());
    }
}
