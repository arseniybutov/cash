package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма "Введите количество"
 * @author myaichnikov
 */
public class CiggyEnterQuantityForm extends CommonProductForm<ProductCiggyEntity, PositionEntity, CommonDefaultProductHeaderPanel, CommonProductUnitPricePanel, CommonProductSummPanel, CommonProductQuantityPanel> {
    //признак того, что количестко взято из баркода товара и его нельзя менять

    public CiggyEnterQuantityForm(XListener outerListener) {
        super(outerListener);
        this.setName(CiggyEnterQuantityForm.class.getName());
    }

    @Override
    public void showForm(ProductCiggyEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        setScannedByBarcode(false);

        //анализируем пришедший баркод, если есть у него цена, то выставляем количество из него
        //этот код будет дублироваться во многих формах, его бы перенести повыше в иерархии
        boolean foundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null;
        if (foundByBarcode) {
            BarcodeEntity barcode = product.getBarCode();
            if (barcode.getCount() == 0) {
                footerPanel.setQuantity(null);
            } else {
                footerPanel.setQuantity(barcode.getCountBigDecimal());
                setScannedByBarcode(barcode.getCount() != 1000);
            }
        } else {
            footerPanel.reset();
        }
        allowUserInput &= !isScannedByBarcode();
        boolean isSoftCheckOrGetByBarcode = (!isScannedByBarcode() && !foundByBarcode) || position.isSoftCheckPosition();

        if (position != null) {
            if (position.getQnty() != null && isSoftCheckOrGetByBarcode) {
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        } else if (!isScannedByBarcode() && (product.getBarCode() == null || product.getBarCode().getCount() != 0)) {
            footerPanel.setQuantity(BigDecimal.ONE);
        }
        updateSumm();
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return new CommonProductQuantityPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    private void updateSumm() {
        BigDecimal summ = getSumm();
        if (controller.getModel().getState() == ProductState.QUICK_EDIT) {
            controller.updateSumm(getSummDiff());
        } else {
            controller.updateSumm(summ);
        }
        summPanel.updateSumm(summ);
    }

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
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
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) {
            return false;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                updateSumm();
                e.setKeyCode(KeyEvent.VK_KANA);
                return false;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            if (allowUserInput
                    && (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY) || Factory.getInstance().getProperties().getRequireRightForCancelPosition())
                    && !controller.currentProductIsMarked()) {
                footerPanel.keyPressedNew(e);
                updateSumm();
                e.setKeyCode(KeyEvent.VK_KANA);
                return false;
            } else if (controller != null) {
                controller.beepError("Ciggy. Change quantity not allowed.");
                controller.sendEventChangeDenied(getQuantity());
            }
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
    public void clear() {

    }
}
