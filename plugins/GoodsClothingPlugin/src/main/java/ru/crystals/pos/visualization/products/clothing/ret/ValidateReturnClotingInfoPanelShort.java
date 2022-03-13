package ru.crystals.pos.visualization.products.clothing.ret;

import javax.swing.JPanel;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.clothing.ResBundleGoodsClothing;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.ScaleLayout;
import ru.crystals.pos.visualizationtouch.components.XFont;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

/**
 *
 * @author Tatarinov Eduard
 */
public class ValidateReturnClotingInfoPanelShort extends JPanel {

    private Label warningLabel = new Label("");
    private boolean warning = false;

    public ValidateReturnClotingInfoPanelShort() {
        this.setLayout(new ScaleLayout());
        this.setBackground(Color.greyBackground);
        this.setOpaque(true);

        Style.setDialogLabelStyle(warningLabel);
        warningLabel.setFont(new XFont(MyriadFont.getItalic(37F), 1.0f));
        this.add(warningLabel, new ScaleRectangle(16, 16, 620, 50));
    }

    public void showWarning(String text) {
        if (text != null && text.length() > 0) {
            Factory.getTechProcessImpl().error(text);
            warningLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
            warningLabel.setText(text);
            warning = true;
        } else {
            warningLabel.setIcon(null);
            warningLabel.setText(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));
            warning = false;
        }
    }

    public boolean isWarning() {
        return warning;
    }
}
