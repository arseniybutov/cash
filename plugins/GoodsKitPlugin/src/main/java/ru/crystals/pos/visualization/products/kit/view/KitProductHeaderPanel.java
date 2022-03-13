package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.ProductKitEntity;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.FlowLayout;

public class KitProductHeaderPanel extends JPanel {
    private JLabel barcodeLabel;
    private JLabel nameLabel;

    public KitProductHeaderPanel() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ScaleDimension size = new ScaleDimension(530, 63);
        this.setSize(size);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        this.setMinimumSize(size);
        this.setBackground(Color.greyBackground);

        barcodeLabel = new JLabel();
        Style.setXNewCodeLabelHalfStyle(barcodeLabel);
        barcodeLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        nameLabel = new JLabel();
        Style.setXNewViewNameLabelStyle(nameLabel);

        Box verticalBox = Box.createVerticalBox();
        verticalBox.setPreferredSize(size);
        verticalBox.add(barcodeLabel);
        verticalBox.add(nameLabel);

        this.add(verticalBox);
    }

    public void setHeaderInfo(ProductKitEntity product) {
        barcodeLabel.setText(product.getItem());
        nameLabel.setText(product.getName());
    }
}
