package ru.crystals.pos.visualization.products.giftcard.product.panel;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class NonFixedGiftCardMaxAmountPanel extends AbstractProductUnitPriceComponent {
    private JLabel jMaxAmountLabel;

    public NonFixedGiftCardMaxAmountPanel() {
        JLabel jMaxAmountNameLabel = new JLabel();
        Style.setPaid60LabelStyle(jMaxAmountNameLabel);
        jMaxAmountNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        jMaxAmountNameLabel.setPreferredSize(new ScaleDimension(250, 60));
        jMaxAmountNameLabel.setText(ResBundleGoodsGiftCard.getString("MAXIMUM_NOMINAL"));

        jMaxAmountLabel = new JLabel();
        Style.setPaid40Style(jMaxAmountLabel);

        this.setBackground(Color.greyBackground);
        this.setPreferredSize(new ScaleDimension(250, 80));
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = 0;
        this.add(jMaxAmountNameLabel, c);

        c.gridy = 1;
        this.add(jMaxAmountLabel, c);
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Scale.getX(20)));
    }

    public void setMaxAmount(Long maxAmount) {
        jMaxAmountLabel.setText(CurrencyUtil.formatSum(BigDecimalConverter.convertMoney(maxAmount)));
    }

    @Override
    public void setProduct(ProductEntity entity) {
    }

    @Override
    public void setUnitPrice(BigDecimal price) {
    }

    @Override
    public BigDecimal getUnitPrice() {
        return BigDecimal.ZERO;
    }
}
