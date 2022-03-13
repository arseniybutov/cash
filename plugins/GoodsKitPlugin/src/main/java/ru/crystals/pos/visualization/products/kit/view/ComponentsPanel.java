package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.visualization.products.kit.model.KitPluginModel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.RobotoFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;

public class ComponentsPanel extends JPanel {

    private static final Font FONT = RobotoFont.getMedium(14F, false);
    private KitPluginModel model;
    private int labelHeight = getFontMetrics(FONT).getHeight();

    private DefaultListModel<ProductEntity> componentsListModel;

    public ComponentsPanel() {
        this.setPreferredSize(new ScaleDimension(360, 80));
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.setBackground(Color.greyBackground);
        createComponentsInfoList();
    }

    private void createComponentsInfoList() {
        componentsListModel = new DefaultListModel<>();
        JList<ProductEntity> componentsInfoList = new JList<>(componentsListModel);
        componentsInfoList.setFixedCellHeight(labelHeight + 1);
        componentsInfoList.setCellRenderer(new ComponentsInfoCellRenderer());

        this.add(componentsInfoList);
    }

    public void showComponents() {
        setVisible(false);
        componentsListModel.clear();

        model.getComponents().forEach(productEntity -> componentsListModel.addElement(productEntity));
        setVisible(true);
    }


    public void setModel(KitPluginModel model) {
        this.model = model;
    }

    public void changeQuantity(BigDecimal currentQuantity) {

    }

    private class ComponentsInfoCellRenderer extends JPanel implements ListCellRenderer<ProductEntity> {

        private JLabel name;
        private JLabel quantity;

        public ComponentsInfoCellRenderer() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.setBackground(Color.greyBackground);
            name = new JLabel();
            setStyle(name);
            ScaleDimension size = new ScaleDimension(300, labelHeight);
            name.setSize(size);
            name.setPreferredSize(size);
            name.setMaximumSize(size);
            name.setBorder(new EmptyBorder(0, 20, 0, 10));
            quantity = new JLabel();
            setStyle(quantity);
            quantity.setMaximumSize(new ScaleDimension(60, labelHeight));
            quantity.setHorizontalTextPosition(JLabel.LEFT);

            Box horizontalBox = Box.createHorizontalBox();
            horizontalBox.setPreferredSize(new ScaleDimension(360, labelHeight));
            horizontalBox.add(name);
            horizontalBox.add(quantity);

            add(horizontalBox);
        }

        private void setStyle(JLabel label) {
            name.setMinimumSize(new ScaleDimension(0, labelHeight));
            label.setVerticalAlignment(JLabel.TOP);
            label.setVerticalTextPosition(JLabel.TOP);
            label.setFont(FONT);
            label.setForeground(Color.secondTitleForeGround);
            label.setBackground(Color.greyBackground);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ProductEntity> list, ProductEntity value, int index, boolean isSelected, boolean cellHasFocus) {
            name.setText(value.getName());
            long quantity = value.getBarCode().getCount();
            this.quantity.setText(" - " + value.toStringQuantity(quantity));
            return this;
        }
    }
}
