package ru.crystals.pos.visualization.products.piece.view;

import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductPieceEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Форма ввода количества штучного товара
 */
public class PieceEnterQuantityForm extends
        CommonProductForm<ProductPieceEntity, PositionEntity, CommonDefaultProductHeaderPanel,
                AbstractProductUnitPriceComponent, CommonProductSummPanel, CommonProductQuantityPanel> {
    //признак того, что количестко взято из баркода товара и его нельзя менять

    public PieceEnterQuantityForm(XListener outerListener) {
        super(outerListener);
        PieceEnterQuantityForm.this.setName("ru.crystals.pos.visualization.products.piece.PieceEnterQuantityForm");
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return new CommonProductQuantityPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public AbstractProductUnitPriceComponent createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) {
            return false;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.getCurrentQuantity() == null || footerPanel.getCurrentQuantity().toString().isEmpty()) {
                if (controller != null) {
                    controller.beepError("Quantity is null or empty");
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (isScannedByBarcode()) {    //если сосканированный товар - не надо делать ресет- просто выйти
                return false;
            }
            if (footerPanel.isReset()) {       //если данных не вводили - выйти
                return false;
            } else {
                footerPanel.reset();          //если в поле ввода что-то вводили - сделать очистку
                updateSumm();
                controller.updateSumm(getSumm());
                return true;
            }
        } else if (XKeyEvent.isCommaOrDigitOrBackspace(e)) {
            if (allowUserInput
                    && (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY) || Factory.getInstance().getProperties().getRequireRightForCancelPosition())
                    && !controller.currentProductIsMarked()) {
                footerPanel.keyPressedNew(e);
                updateSumm();

                if (controller.getModel().getState() == ProductState.QUICK_EDIT) {
                    controller.updateSumm(getSummDiff());
                } else {
                    controller.updateSumm(getSumm());
                }
            } else if (controller != null) {
                controller.beepError("Piece. Change quantity not allowed.");
                controller.sendEventChangeDenied(getQuantity());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return footerPanel.getCurrentQuantity();
    }

    @Override
    public BigDecimal getQuantityDiff() {
        return footerPanel.getQuantityDiff();
    }

    @Override
    public BigDecimal getPrice() {
        if (position != null && position.getSoftCheckNumber() != null) {
            return position.getPriceStartBigDecimal();
        } else if (product != null) {
            return product.getPrice().getPriceBigDecimal();
        }
        return BigDecimal.ONE;
    }

    @Override
    public void clear() {
        if (footerPanel != null) {
            footerPanel.clear();
        }
    }

    private void updateSumm() {
        summPanel.updateSumm(getSumm());
    }

    @Override
    public void showForm(ProductPieceEntity product, PositionEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(position);
        unitPanel.setProduct(product);
        setScannedByBarcode(false);
        //анализируем пришедший баркод, если есть у него цена, то выставляем количество из него
        //этот код будет дублироваться во многих формах, его бы перенести повыше в иерархии
        if (product.isFoundByBarcode() && product.getBarCode() != null) {
            BarcodeEntity barcode = product.getBarCode();
            if (barcode.getCount() == 0){
                footerPanel.setQuantity(null);
            } else {
                footerPanel.setQuantity(barcode.getCountBigDecimal());
                setScannedByBarcode(barcode.getCount() != 1000);
            }
        } else {
            footerPanel.setQuantity(BigDecimal.ONE);
        }

        //если отсканировали марку товара с информацией о его весе, то
        //добавим эту информацию
        ProductPositionData productPositionData = product.getProductPositionData();
        //вес из GS1 игнорируем для партионных товаров
        if (productPositionData != null && !product.isConsignment()) {
            long quantity = Optional.ofNullable(productPositionData.getWeight()).orElse(0L);
            if (quantity > 0) {
                footerPanel.setQuantity(BigDecimal.valueOf(quantity, Optional.ofNullable(productPositionData.getWeightScale()).orElse(0)));
                setScannedByBarcode(true);
            }
        }

        // для быстрого редактирования чека
        if (position != null) {
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
            if (position.getQnty() != null) {
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
        }
        allowUserInput &= !isScannedByBarcode() || !product.isConsignment();
        updateSumm();
    }
}
