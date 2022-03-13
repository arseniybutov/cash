package ru.crystals.pos.visualization.products.spirits.view;


import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.Swing;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;


/**
 * панель с количеством и текстом
 * Помогает кассиру сканировать акцизные марки
 * Предупреждает, если что-то пошло не так
 * Находится в подвале плагина
 *
 * @author nbogdanov
 */
@SuppressWarnings("serial")
public class SpiritExcisePanel extends JPanel {
    private JLabel jLabel;
    private JLabel scanExciseLabel = new JLabel();
    private boolean warning = false;
    private int quantity = 0;

    public SpiritExcisePanel() {
        FlowLayout fl = new FlowLayout();
        fl.setHgap(0);
        fl.setVgap(0);
        this.setLayout(fl);
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        this.setLayout(new BorderLayout());
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);
        this.setBorder(BorderFactory.createEmptyBorder(0, Scale.getX(10), 0, Scale.getX(10)));

        jLabel = new JLabel();
        Style.setLabelStyle(jLabel);
        this.setPreferredSize(new ScalableDimension(640, 113));
        this.add(jLabel, BorderLayout.NORTH);
        this.add(scanExciseLabel, BorderLayout.CENTER);

        jLabel.setText(CoreResBundle.getStringCommon("QUANTITY") + ":");
        jLabel.setPreferredSize(new ScaleDimension(600, 40));
        scanExciseLabel.setText(ResBundleGoodsSpirits.getString("SCAN_EXCISE_LABEL"));
        Style.setOperationLabelStyle(scanExciseLabel);
        scanExciseLabel.setPreferredSize(new ScaleDimension(600, 50));
        scanExciseLabel.setIcon(null);
        scanExciseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scanExciseLabel.setVerticalAlignment(SwingConstants.CENTER);
    }

    public void setQuantity(int quantity, int maxQuantity) {
        this.quantity = quantity;
        jLabel.setText(String.format(CoreResBundle.getStringCommon("SCANNED_QNTY_FROM"), quantity, maxQuantity));
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        jLabel.setText(CoreResBundle.getStringCommon("QUANTITY") + ":" + quantity);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setWarning(boolean warn) {
        this.warning = warn;
        if (warning) {
            scanExciseLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        } else {
            scanExciseLabel.setIcon(null);
        }
    }

    public boolean isWarning() {
        return warning;
    }

    public void setMessage(String message) {
        Swing.wrapLabelTextUsingSeparators(scanExciseLabel, message);
    }
}
