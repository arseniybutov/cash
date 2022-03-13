package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
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
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма "Введите количество"
 *
 * @author nbogdanov
 */
public class SpiritEnterQuantityForm extends CommonProductForm<ProductSpiritsEntity, PositionSpiritsEntity, CommonDefaultProductHeaderPanel, CommonProductUnitPricePanel,
        CommonProductSummPanel, CommonProductQuantityPanel> {
    private SpiritProductModel model;

    public SpiritEnterQuantityForm(XListener outerListener) {
        super(outerListener);
        this.setName(SpiritEnterQuantityForm.class.getName());
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionSpiritsEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);

        setScannedByBarcode(false);

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null && product.getBarCode().getCount() != 1000L;

        setScannedByBarcode(isFoundByBarcode);

        //Флаг запрещает изменять количество
        allowUserInput &= !isFoundByBarcode;

        if (position != null) {
            if (position.getQnty() != null && !isScannedByBarcode()) {
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
            if (position.getPriceStartBigDecimal() != null) {
                unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
            }
            // SRL-848 если товар является набором, для него всегда количество=1 и запрет менять кол-во
            if (!model.isAllowChangeQuantityForKit()) {
                allowUserInput = false;
                footerPanel.setQuantity(BigDecimal.ONE);
            } else if (isFoundByBarcode) {
                allowUserInput &= position.isCanChangeQnty();
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
        } else if (isFoundByBarcode) {
            footerPanel.setQuantity(product.getBarCode().getCountBigDecimal());
        } else {
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

    private boolean changeQuantityDenied() {
        return footerPanel.getDefaultQuantity() == null || product.getProductConfig().getChangeExciseQnty()
                || !isExciseProductSpirits(product);
    }

    private boolean isExciseProductSpirits(ProductSpiritsEntity product) {
        return (product.isExcise() || ((product.isKit() && !product.getExciseBottles().isEmpty())));
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
            return processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscKey(e);
        } else if (XKeyEvent.isCommaOrDigitOrBackspace(e)) {
            return processCommaOrDigitOrBackspaceKey(e);
        }
        return false;
    }

    private boolean processCommaOrDigitOrBackspaceKey(XKeyEvent e) {
        if (allowUserInput
                && (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY) || Factory.getInstance().getProperties().getRequireRightForCancelPosition())
                && !controller.currentProductIsMarked()) {
            footerPanel.keyPressedNew(e);
            updateSumm();
            e.setKeyCode(KeyEvent.VK_KANA);
        } else if (controller != null) {
            controller.beepError("Spirits. Change quantity not allowed.");
            controller.sendEventChangeDenied(getQuantity());
        }
        return false;
    }

    private boolean processEscKey(XKeyEvent e) {
        if (!footerPanel.isReset()) {
            footerPanel.reset();
            updateSumm();
            e.setKeyCode(KeyEvent.VK_KANA);
        }
        return false;
    }

    private boolean processEnterKey() {
        if (getQuantity().compareTo(BigDecimal.ZERO) > 0 && changeQuantityDenied()) {
            return !controller.isPossibleToChangePositionQuantity(footerPanel.getDefaultQuantity(), getQuantity());
        }

        if (((SpiritProductController) controller).compareExcisePositionQuantity(footerPanel.getDefaultQuantity(),
                footerPanel.getInputFieldValue(), controller.getModel().getState().equals(ProductState.QUICK_EDIT))) {
            return false;
        }

        setDefaultQuantity();
        controller.beepError("Spirit: zero quantity not allowed!");
        return true;
    }

    private void setDefaultQuantity() {
        footerPanel.setQuantity(footerPanel.getDefaultQuantity());
        updateSumm();
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        if (((SpiritProductController) controller).isExciseBarcode(barcode) && model.isScanExciseLabelsMode()) {
            return processEnterKey();
        }

        return false;
    }

    @Override
    public void clear() {
        //
    }

    public void setCurrentModel(SpiritProductModel model) {
        this.model = model;
    }
}
