package ru.crystals.pos.visualization.products.clothing.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.products.clothing.ResBundleGoodsClothing;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.Swing;

@SuppressWarnings("serial")
public class ClothingKisPanel extends JPanel{
    private JLabel scanExciseLabel = new JLabel();
    private boolean warning = false;

    public ClothingKisPanel() {
        FlowLayout fl = new FlowLayout();
        fl.setHgap(0);
        fl.setVgap(0);
        this.setLayout(fl);
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        this.setLayout(new BorderLayout());
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        this.setBorder(BorderFactory.createEmptyBorder(0, Scale.getX(10), 0, Scale.getX(10)));

        this.setPreferredSize(new ScalableDimension(640, 113));
        this.add(scanExciseLabel, BorderLayout.CENTER);
        
        scanExciseLabel.setText(ResBundleGoodsClothing.getString("SCAN_CIS_LABEL"));
        Style.setOperationLabelStyle(scanExciseLabel);
        scanExciseLabel.setPreferredSize(new ScaleDimension(600, 50));
        scanExciseLabel.setIcon(null);
        scanExciseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scanExciseLabel.setVerticalAlignment(SwingConstants.CENTER);
    }
    public void setWarning(boolean warn){
        this.warning = warn;
        if(warning){
            scanExciseLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        }else{
            scanExciseLabel.setIcon(null);
        }
    }

    public boolean isWarning() {
        return warning;
    }

    public void setMessage(String message) {
        Swing.wrapLabelTextUsingSeparators(scanExciseLabel,message);
    }
}
