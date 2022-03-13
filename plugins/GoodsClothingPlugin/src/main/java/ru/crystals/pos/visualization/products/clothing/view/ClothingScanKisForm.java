package ru.crystals.pos.visualization.products.clothing.view;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import ru.crystals.pos.catalog.ProductClothingEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.cis.validation.CisValidationState;
import ru.crystals.pos.check.PositionClothingEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponent;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponentType;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.clothing.ResBundleGoodsClothing;
import ru.crystals.pos.visualization.products.clothing.controller.ClothingProductController;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.ElementFactory;
import ru.crystals.pos.visualizationtouch.components.XFont;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

/**
 * Форма сканирования КИЗ
 * @author Tatarinov Eduard
 */
public class ClothingScanKisForm extends CommonProductForm<ProductClothingEntity, PositionClothingEntity,
        CommonDefaultProductHeaderPanel, CommonProductUnitPricePanel, CommonProductSummPanel, ClothingKisPanel>{

    private CommitCancelComponent commitCancelComponent
            = new CommitCancelComponent(CommitCancelComponentType.YES_NO, ResBundleVisualization.getString("OK"), ResBundleVisualization.getString("CANCEL"));
    private WaitComponent waitComponent;
    private boolean isRefund;

    public ClothingScanKisForm(XListener outerListener) {
        super(outerListener);
        this.setName(ClothingScanKisForm.class.getName());
        this.waitComponent = new WaitComponent(ResBundleGoodsClothing.getString("SCAN_CIS_WAIT"), new XListener() {
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
        this.add(commitCancelComponent);

        Label confirmLabel = new Label(ResBundleGoodsClothing.getString("RETURN_SKIP_CIS_SCAN"));
        Style.setDialogTitleStyle(confirmLabel);
        confirmLabel.setFont(new XFont(MyriadFont.getItalic(37F), 1.0f));
        confirmLabel.setPreferredSize(new ScaleDimension(620, 190));
        confirmLabel.setAligmentY(ElementFactory.AligmentY.Y_ALIGMENT_CENTER);
        confirmLabel.setAligmentX(ElementFactory.AligmentX.X_ALIGMENT_CENTER);
        confirmLabel.setOpaque(false);
        commitCancelComponent.setBackground(Color.greyBackground);
        commitCancelComponent.addNorthComponent(confirmLabel);
        commitCancelComponent.setSelected(true);
        commitCancelComponent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        commitCancelComponent.setVisible(false);
    }
    
    @Override
    public void showForm(ProductClothingEntity product, PositionClothingEntity position) {
        super.showForm(product, position);
        ProductEntity p = product;
        if (p == null && position != null) {
            p = position.getProduct();
        }
        hideYesNoPanel();
        headerPanel.setHeaderInfo(p);
        unitPanel.setProduct(p);
        footerPanel.setWarning(false);
        footerPanel.setMessage(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));
    }
    
    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public ClothingKisPanel createQuantityPanel() {
        return new ClothingKisPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        isRefund = ((ClothingProductController) controller).getAdapter().getProductState() == ProductContainer.ProductState.REFUND;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isWarning()) {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                footerPanel.setWarning(false);
                footerPanel.setMessage(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));
                return true;
            } else if (isRefund){
                showYesNoPanel();
                return true;
            }
            return false;
        } else if (commitCancelComponent.isVisible()) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                commitCancelComponent.changeCommitSelection();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (commitCancelComponent.isCommit()) {
                    ((ClothingProductController) controller).setCis(null);
                    return false;
                } else {
                    hideYesNoPanel();
                    return true;
                }
            }
        } else if (e.getMenuNumber() != null) {// позволим обрабатывать клавиши меню
            return false;
        }
        return true;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        isRefund = ((ClothingProductController) controller).getAdapter().getProductState() == ProductContainer.ProductState.REFUND;
        if (footerPanel.isWarning()) {
            return true;
        }

        if (kisValidation(barcode, isRefund)) {
            footerPanel.setWarning(true);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
    }
    
    public void setSumm(BigDecimal summ) {
        summPanel.updateSumm(summ);
    }

    private boolean kisValidation(String barcode, boolean refund) {
        showWaitComponent();
        boolean result = false;
        CisValidationState state = ((ClothingProductController) controller).validation(barcode, refund);
        switch (state) {
            case IS_CAN_SALE:
            case IS_CAN_RETURN:
                ((ClothingProductController) controller).setCis(barcode);
                break;
            case CIS_IN_CHECK:
            case CIS_NOT_FOUND:
            case CIS_NO_CORRECT:    
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                footerPanel.setMessage(ResBundleGoodsClothing.getString(state.name()));
                ((ClothingProductController) controller).setCis(null);
                result = true;
                break;                
        }
        hideWaitComponent();
        return result;
    }
    
    public void showYesNoPanel() {
        hideProductPanels();
        commitCancelComponent.setVisible(true);
        
    }
    
    public void hideYesNoPanel() {
        showProductPanels();
        commitCancelComponent.setCommit(false);
        commitCancelComponent.setVisible(false);
    }

    public void showWaitComponent() {
        hideProductPanels();
        waitComponent.setVisible(true);
    }

    public void hideWaitComponent() {
        showProductPanels();
        waitComponent.setVisible(false);
    }
    
    private void showProductPanels(){
        headerPanel.setVisible(true);
        unitPanel.setVisible(true);
        footerPanel.setVisible(true);
        summPanel.setVisible(true);
    }
    private void hideProductPanels(){
        headerPanel.setVisible(false);
        unitPanel.setVisible(false);
        footerPanel.setVisible(false);
        summPanel.setVisible(false);
    }
}
