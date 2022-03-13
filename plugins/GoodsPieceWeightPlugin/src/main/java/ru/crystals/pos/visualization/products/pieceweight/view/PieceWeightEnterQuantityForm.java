package ru.crystals.pos.visualization.products.pieceweight.view;

import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductPieceWeightEntity;
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
import java.util.Date;
import java.util.Optional;

/**
 * Created by alexey on 17.07.15.
 * <p/>
 * Форма ввода количества товара (используется при продаже и возрвате)
 */
public class PieceWeightEnterQuantityForm extends
        CommonProductForm<ProductPieceWeightEntity, PositionEntity, CommonDefaultProductHeaderPanel, AbstractProductUnitPriceComponent, CommonProductSummPanel, CommonProductQuantityPanel> {

    public PieceWeightEnterQuantityForm(XListener outerListener) {
        super(outerListener);
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
            return true;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (footerPanel.getCurrentQuantity() == null || footerPanel.getCurrentQuantity().toString().isEmpty()) {
                    if (controller != null) {
                        controller.beepError("Quantity is null or empty");
                    }
                    return true;
                }
                return false;

            case KeyEvent.VK_ESCAPE:
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
            default:
                if (XKeyEvent.isCommaOrDigitOrBackspace(e)) {
                    if (allowUserInput && (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY)
                            || Factory.getInstance().getProperties().getRequireRightForCancelPosition())) {
                        footerPanel.keyPressedNew(e);
                        updateSumm();

                        if (controller.getModel().getState() == ProductState.QUICK_EDIT) {
                            controller.updateSumm(getSummDiff());
                        } else {
                            controller.updateSumm(getSumm());
                        }
                    } else if (controller != null) {
                        controller.beepError("PieceWeight. Change quantity not allowed.");
                        controller.sendEventChangeDenied(getQuantity());
                    }
                    return true;
                }
                return false;
        }
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
        if (product != null) {
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
    public void showForm(ProductPieceWeightEntity product, PositionEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);

        setScannedByBarcode(false);
        BarcodeEntity barcode = product.getBarCode();
        Date now = new Date();

        if (product.isFoundByBarcode()) {
            //Если в баркоде есть действующая цена или если цены нет и количество не единица, то ставим количество из баркода, его менять нельзя
            if ((barcode.getPrice() != null &&
                    (barcode.getBeginDate() == null || now.after(barcode.getBeginDate())) &&
                    (barcode.getEndDate() == null || now.before(barcode.getEndDate()))) ||(
                    barcode.getPrice() == null && barcode.getCount() != 1000)) {
                footerPanel.setQuantity(barcode.getCount() == 0 ? null : barcode.getCountBigDecimal());
                setScannedByBarcode(true);
            //Еcли нет цены и количество равно единице, то менять его можно
            } else if (barcode.getCount() == 1000) {
                footerPanel.setQuantity(BigDecimal.ONE);
            }

        } else {
            footerPanel.setQuantity(BigDecimal.ONE);
        }

        //Если колчество зашито в баркод с префиксом, и его вытащил баркодпроцессор, то выставляем это количество
        //в этом случае нельзя менять количество руками
        if (product.getAmount() != null) {
            footerPanel.setQuantity(BigDecimal.valueOf(product.getAmount()));
            setScannedByBarcode(true);
        }

        //если отсканировали марку товара с информацией о его весе, то
        //добавим эту информацию
        ProductPositionData productPositionData = product.getProductPositionData();
        if (productPositionData != null && product.useWeightFromPositionData()) {
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

        allowUserInput &= (!isScannedByBarcode() || footerPanel.getCurrentQuantity().longValue() == 0 || !product.isConsignment());
        updateSumm();
    }

}
