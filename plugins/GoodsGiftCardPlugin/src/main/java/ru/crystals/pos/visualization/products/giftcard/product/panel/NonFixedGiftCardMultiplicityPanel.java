package ru.crystals.pos.visualization.products.giftcard.product.panel;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class NonFixedGiftCardMultiplicityPanel extends AbstractProductUnitPriceComponent {
    private JLabel jMultiplicityLabel;

    public NonFixedGiftCardMultiplicityPanel() {
        JLabel jMultiplicityNameLabel = new JLabel();
        Style.setPaid60LabelStyle(jMultiplicityNameLabel);
        jMultiplicityNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jMultiplicityNameLabel.setText(ResBundleGoodsGiftCard.getString("MULTIPLICITY"));

        jMultiplicityLabel = new JLabel();
        Style.setPaid40Style(jMultiplicityLabel);
        jMultiplicityLabel.setFont(MyriadFont.getRegular(34F));

        this.setBackground(Color.greyBackground);
        this.setPreferredSize(new ScaleDimension(175, 80));
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = 0;
        this.add(jMultiplicityNameLabel, c);

        c.gridy = 1;
        this.add(jMultiplicityLabel, c);
        this.setBorder(BorderFactory.createEmptyBorder(0, Scale.getX(20), 0, 0));
    }

    public void setMultiplicity(Long multiplicity) {
        jMultiplicityLabel.setText(CurrencyUtil.formatSum(BigDecimalConverter.convertMoney(multiplicity)));
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
