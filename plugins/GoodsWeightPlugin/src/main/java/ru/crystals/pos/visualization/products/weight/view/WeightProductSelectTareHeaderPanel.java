package ru.crystals.pos.visualization.products.weight.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

public class WeightProductSelectTareHeaderPanel extends JPanel{
    private final JLabel header = new JLabel();

    public WeightProductSelectTareHeaderPanel() {
        setBackground(Color.greyBackground);
        header.setFont(MyriadFont.getRegular(28F));
        header.setForeground(Color.blackText);
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setVerticalAlignment(SwingConstants.BOTTOM);
        header.setPreferredSize(new ScaleDimension(600, 30));
        header.setName("headerLabel");
        add(header);
    }

    public WeightProductSelectTareHeaderPanel(String headerText) {
        this();
        header.setText(headerText);
    }
}
