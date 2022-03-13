package ru.crystals.pos.visualization.products.clothing.ret;

import java.awt.BorderLayout;
import ru.crystals.pos.check.PositionClothingEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.products.clothing.ResBundleGoodsClothing;
import ru.crystals.pos.visualization.products.clothing.view.ClothingKisPanel;

/**
 *
 * @author Tatarinov Eduard
 */
public class ValidateReturnClothingInfoPanel extends VisualPanel{

    private CommonDefaultProductHeaderPanel headerPanel;
    private CommonProductUnitPricePanel unitPanel;
    private CommonProductSummPanel summPanel;
    private ClothingKisPanel footerPanel;

    public ValidateReturnClothingInfoPanel() {
        this.setLayout(new BorderLayout());
        headerPanel = createHeaderPanel();
        unitPanel = createUnitPanel();
        summPanel = createSummPanel();
        footerPanel = createQuantityPanel();

        if (headerPanel != null) {
            this.add(headerPanel, BorderLayout.NORTH);
        }
        if (unitPanel != null) {
            this.add(unitPanel, BorderLayout.WEST);
        }
        if (summPanel != null) {
            this.add(summPanel, BorderLayout.EAST);
        }
        if (footerPanel != null) {
            this.add(footerPanel, BorderLayout.SOUTH);
        }

    }
    
    public void fillData(PositionClothingEntity position){
        headerPanel.setHeaderInfo(position);
        unitPanel.setPosition(position);
        unitPanel.setUnitPrice(position.getPriceEndBigDecimal());
        footerPanel.setWarning(false);
        footerPanel.setMessage(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));        
    }

    public void showWarning(String text) {
        if (text != null && text.length() > 0) {
            Factory.getTechProcessImpl().error(text);
            footerPanel.setWarning(true);
            footerPanel.setMessage(text);
        } else {
            footerPanel.setWarning(false);
            footerPanel.setMessage(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));
        }
    }
    
    public boolean isWarning(){
        return footerPanel.isWarning();
    }

    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    public ClothingKisPanel createQuantityPanel() {
        return new ClothingKisPanel();
    }

    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }
}
