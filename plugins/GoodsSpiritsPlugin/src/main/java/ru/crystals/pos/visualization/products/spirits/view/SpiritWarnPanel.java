package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.Swing;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by nbogdanov on 23.07.2015.
 */
public class SpiritWarnPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel jWarningLabel = null;

    public SpiritWarnPanel() {
        jWarningLabel = newJLabelWithName("jWarningLabel");
        jWarningLabel.setText(ResBundleVisualization.getString("DENIED_PURCHASE"));
        Style.setWarningLabelStyle(jWarningLabel);
        jWarningLabel.setPreferredSize(new ScaleDimension(600, 180));
        jWarningLabel.setVisible(true);
        this.add(jWarningLabel);
        setBackground(Color.greyBackground);
        setPreferredSize(new ScaleDimension(640, 180));
    }

    public void setWarningText(String message) {
        Swing.wrapLabelTextUsingSeparators(jWarningLabel, message);
    }

    protected JLabel newJLabelWithName(String name) {
        JLabel label = new JLabel();
        label.setName(name);
        return label;
    }

}
