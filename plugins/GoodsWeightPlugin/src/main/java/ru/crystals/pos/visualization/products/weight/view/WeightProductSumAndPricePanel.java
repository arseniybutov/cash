package ru.crystals.pos.visualization.products.weight.view;

import java.awt.FlowLayout;
import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.products.weight.ResBundleGoodsWeight;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

public class WeightProductSumAndPricePanel extends JPanel {
    private static final long serialVersionUID = 5247344377791989494L;
    private JLabel weightLabel;
    private JLabel priceLabel;

    public WeightProductSumAndPricePanel() {
        setBackground(Color.greyBackground);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setPreferredSize(new ScalableDimension(170, 200));
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        infoPanel.setPreferredSize(new ScaleDimension(150, 200));
        infoPanel.setBackground(getBackground());
        JLabel weightHeaderLabel = new JLabel(ResBundleGoodsWeight.getString("WEIGHT_WITHOUT_TARE"));
        Style.setPaidLabelStyle(weightHeaderLabel);
        infoPanel.add(weightHeaderLabel);
        weightLabel = new JLabel();
        weightLabel.setName("weightLabel");
        Style.setPaidStyle(weightLabel);
        infoPanel.add(weightLabel);
        infoPanel.add(new Empty(150, 30));
        JLabel priceHeaderLabel = new JLabel(ResBundleGoodsWeight.getString("PRICE"));
        Style.setPaidLabelStyle(priceHeaderLabel);
        infoPanel.add(priceHeaderLabel);
        priceLabel = new JLabel();
        priceLabel.setName("priceLabel");
        Style.setPaidStyle(priceLabel);
        infoPanel.add(priceLabel);
        add(infoPanel);
    }

    public void updateSum(BigDecimal sum) {
        priceLabel.setText(CurrencyUtil.formatSum(CurrencyUtil.round(sum)));
    }

    public void updateWeight(BigDecimal weight) {
        weightLabel.setText(weight.toString());
    }
}
