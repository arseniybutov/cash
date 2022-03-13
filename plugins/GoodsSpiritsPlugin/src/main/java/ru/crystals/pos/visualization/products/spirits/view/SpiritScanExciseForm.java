package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.egais.EGAISUtils;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.mark.validation.events.ValidationBadMarkEvent;
import ru.crystals.pos.utils.CommonLogger;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма "сканируйте акцизную марку"
 * Включается в работу только при работающем процессинге ЕГАИС
 *
 * @author nbogdanov
 */
public class SpiritScanExciseForm extends CommonProductForm<ProductSpiritsEntity, PositionSpiritsEntity,
        CommonDefaultProductHeaderPanel, CommonProductUnitPricePanel, CommonProductSummPanel, SpiritExcisePanel>
        implements ScanFormUIConsumer {

    private final WaitComponent waitComponent;

    protected int quantity = 0;

    public SpiritScanExciseForm(XListener outerListener) {
        super(outerListener);
        this.setName(SpiritScanExciseForm.class.getName());
        this.waitComponent = new WaitComponent(ResBundleVisualization.getString("SCAN_EXCISE_WAIT"), new XListener() {
            @Override
            public boolean barcodeScanned(String barcode) {
                return false;
            }

            @Override
            public boolean keyPressedNew(XKeyEvent e) {
                return false;
            }

            @Override
            public boolean eventMSR(String track1, String track2, String track3, String track4) {
                return false;
            }
        });
        this.waitComponent.setVisible(false);
        this.add(waitComponent);
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionSpiritsEntity position) {
        super.showForm(product, position);
        ProductEntity p = product;
        if (p == null && position != null) {
            p = position.getProduct();
        }
        getModel().getProcessedBottles().clear();
        headerPanel.setHeaderInfo(p);
        unitPanel.setProduct(p);
        if (position != null && position.getPriceStartBigDecimal() != null) {
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        }
        footerPanel.setWarning(false);
        showScanExciseLabel();
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
    public SpiritExcisePanel createQuantityPanel() {
        return new SpiritExcisePanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public SpiritProductController getController() {
        return (SpiritProductController) super.getController();
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        // позволим обрабатывать клавиши меню
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isWarning()) {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                footerPanel.setWarning(false);
                showScanExciseLabel();
                return true;
            }
            return false;
        } else {
            return e.getMenuNumber() == null;
        }
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    /**
     * Обратно кол-во возвращаем через кол-во сосканированных акцизных марок
     */
    @Override
    public BigDecimal getQuantity() {
        //кол-во добавленных штук равно кол-ву бутылок на данной форме
        return BigDecimal.valueOf(getModel().getProcessedBottles() != null ? getModel().getProcessedBottles().size() : 0);
    }

    @Override
    public void showScanExciseLabel() {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        footerPanel.setWarning(false);
        if (product.isKit()) {
            if (getController().isWaitExciseForBottleMode()) {
                footerPanel.setMessage(ResBundleGoodsSpirits.getString("SCAN_EXCISE_LABEL"));
            } else {
                footerPanel.setMessage(ResBundleGoodsSpirits.getString("SCAN_EXCISE_LABEL_FOR_KIT"));
            }
        } else {
            footerPanel.setMessage(ResBundleGoodsSpirits.getString("SCAN_EXCISE_LABEL"));
        }
    }

    @Override
    protected boolean dispatchBarcodeEvent(String scannedBarcode) {
        if (footerPanel.isWarning()) {
            return true;
        }

        //  Независимо от настройки caseSensitive АМ алкоголя должны быть в верхнем регистре
        //  https://crystals.atlassian.net/browse/SRTB-2921
        if (scannedBarcode != null) {
            scannedBarcode = scannedBarcode.toUpperCase();
        }

        SpiritProductController spiritProductController = getController();
        if (spiritProductController.isExciseBarcode(scannedBarcode)) {
            String errorMessage = spiritProductController.checkExciseBeforeAdd(scannedBarcode, getModel().getProcessedBottles());
            if (spiritProductController.isProductRefund()) {
                try {
                    if (!spiritProductController.getCheckAlcoCode().alcocodeExist(scannedBarcode, getModel().getPosition()) && !product.isKit()) {
                        errorMessage = String.format(ResBundleVisualization.getString("ALCO_CODE_CANT_RETURN"), EGAISUtils.extractAlcoCodeFromExcise(scannedBarcode));
                    }
                } catch (Exception e) {
                    errorMessage = String.format(ResBundleVisualization.getString("ALCO_CODE_CANT_RETURN"), EGAISUtils.extractAlcoCodeFromExcise(scannedBarcode));
                }
            }
            if (errorMessage != null) {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                showMessage(errorMessage, true);
                return true;
            }
            ExciseValidationResult validationResult = egaisExciseValidation(scannedBarcode);
            getController().processValidationResults(validationResult);
            if (!validationResult.operationPossibility) {
                return true;
            }
        } else {
            Factory.getTechProcessImpl().getTechProcessEvents().publishEvent(new ValidationBadMarkEvent(this, scannedBarcode));
        }
        return getController().addBottleWithBarcode(scannedBarcode, this);
    }

    @Override
    public boolean updateQuantity() {
        int quant = footerPanel.getQuantity();
        quant++;
        if (getModel().getProduct().isKit()) {
            footerPanel.setQuantity(quant, getModel().getProduct().getExciseBottles().size());
        } else {
            footerPanel.setQuantity(quant);
        }
        return quant < quantity;
    }

    @Override
    public void showMessage(String message, boolean warning) {
        if (warning) {
            CommonLogger.getCommonLogger().error(message);
        }
        footerPanel.setWarning(warning);
        footerPanel.setMessage(message);
    }

    @Override
    public void clear() {

    }

    /**
     * Валидация АМ. Оборачивает вызов валидации в крутилку
     *
     * @param inputScannedBarcode - АМ
     * @return результат валидации
     */
    public ExciseValidationResult egaisExciseValidation(String inputScannedBarcode) {
        showWaitComponent();
        ExciseValidationResult validationResult = getController().egaisExciseValidation(inputScannedBarcode);
        hideWaitComponent();
        return validationResult;
    }

    public SpiritProductModel getModel() {
        return (SpiritProductModel) super.getModel();
    }

    public void setSumm(BigDecimal summ) {
        summPanel.updateSumm(summ);
    }

    public void setQuantity(BigDecimal quantity) {
        this.setQuantity(quantity.intValue());
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        footerPanel.setQuantity(0);
    }

    /**
     * Чтобы отобразить "Просканировано: 0 из {quantity}"
     */
    public void setKitQuantity(int quantity) {
        this.quantity = quantity;
        footerPanel.setQuantity(0, quantity);
    }

    public void showWaitComponent() {
        headerPanel.setVisible(false);
        unitPanel.setVisible(false);
        footerPanel.setVisible(false);
        summPanel.setVisible(false);
        createQuantityPanel().setVisible(false);
        createHeaderPanel().setVisible(false);
        createUnitPanel().setVisible(false);
        waitComponent.setVisible(true);
    }

    public void hideWaitComponent() {
        headerPanel.setVisible(true);
        unitPanel.setVisible(true);
        footerPanel.setVisible(true);
        summPanel.setVisible(true);
        createQuantityPanel().setVisible(true);
        createHeaderPanel().setVisible(true);
        createUnitPanel().setVisible(true);
        waitComponent.setVisible(false);
    }
}

