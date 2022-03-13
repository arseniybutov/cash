package ru.crystals.pos.visualization.products.jewel.ret;

import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonScanExcisePanel;
import ru.crystals.pos.visualization.components.VisualPanel;

import java.awt.BorderLayout;

public class ValidateReturnJewelInfoPanel extends VisualPanel {
    private final CommonDefaultProductHeaderPanel headerPanel;
    private final CommonProductUnitPricePanel unitPanel;
    private final CommonProductSummPanel summPanel;
    private final CommonScanExcisePanel footerPanel;

    public ValidateReturnJewelInfoPanel() {
        this.setLayout(new BorderLayout());
        headerPanel = new CommonDefaultProductHeaderPanel();
        unitPanel = new CommonProductUnitPricePanel(true);
        summPanel = new CommonProductSummPanel();
        footerPanel = new CommonScanExcisePanel();

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(unitPanel, BorderLayout.WEST);
        this.add(summPanel, BorderLayout.EAST);
        this.add(footerPanel, BorderLayout.SOUTH);
    }

    public void fillData(PositionEntity position) {
        headerPanel.setHeaderInfo(position);
        unitPanel.setPosition(position);
        unitPanel.setUnitPrice(position.getPriceEndBigDecimal());
        summPanel.updateSumm(position.getPriceEndBigDecimal());
        footerPanel.restoreDefaultState();
    }

    public void showWarning(String text) {
        if (text != null && text.length() > 0) {
            Factory.getTechProcessImpl().error(text);
            footerPanel.setWarning(true);
            footerPanel.setMessage(text);
        } else {
            footerPanel.restoreDefaultState();
        }
    }

    public boolean isWarning() {
        return footerPanel.isWarning();
    }
}