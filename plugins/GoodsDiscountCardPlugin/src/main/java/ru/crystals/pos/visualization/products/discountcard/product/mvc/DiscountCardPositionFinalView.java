package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import java.math.BigDecimal;

import javax.swing.JPanel;

import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;

//@formatter:off
public class DiscountCardPositionFinalView extends CommonProductForm<ProductDiscountCardEntity, 
                                                                         PositionDiscountCardEntity, 
                                                                         CommonDefaultProductHeaderPanel, 
                                                                         AbstractProductUnitPriceComponent, 
                                                                         CommonProductSummPanel, 
                                                                         JPanel> {
 // @formatter:on
    private static final long serialVersionUID = 1L;

    public DiscountCardPositionFinalView(XListener outerListener) {
        super(outerListener);
        DiscountCardPositionFinalView.this.setName("ru.crystals.pos.visualization.products.discountcard.product.mvc.DiscountCardPositionFinalView");
    }
    
    @Override
    public void showForm(ProductDiscountCardEntity product, PositionDiscountCardEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        summPanel.updateSumm(unitPanel.getUnitPrice());
    }
    
    @Override
    public BigDecimal getPrice(){
        return unitPanel.getUnitPrice();
    }
    
    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public JPanel createQuantityPanel() {
        return null;
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public AbstractProductUnitPriceComponent createUnitPanel() {
        return new CommonProductUnitPricePanel(false);
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
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
