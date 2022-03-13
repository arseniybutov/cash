package ru.crystals.pos.visualization.products.ciggy.ret;

import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponent;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponentType;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.ElementFactory;
import ru.crystals.pos.visualizationtouch.components.XFont;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

import javax.swing.*;

public class CommitCancelCiggyComponent extends CommitCancelComponent {

    public CommitCancelCiggyComponent() {
        super(CommitCancelComponentType.YES_NO, ResBundleVisualization.getString("OK"), ResBundleVisualization.getString("CANCEL"));
        initPanel();
    }

    private void initPanel() {
        this.addNorthComponent(createConfirmLabel());
        this.setSelected(true);
        this.setBorder(BorderFactory.createEmptyBorder(1, 16, 16, 16));
        this.setOpaque(true);
        this.setBackground(Color.greyBackground);
    }

    private Label createConfirmLabel() {
        Label confirmLabel = new Label(ResBundleVisualization.getString("RETURN_POSITION_WILL_NOT_BE_ADDED"));
        confirmLabel.setFont(new XFont(MyriadFont.getItalic(37F), 1.0f));
        confirmLabel.setPreferredSize(new ScaleDimension(620, 190));
        confirmLabel.setAligmentY(ElementFactory.AligmentY.Y_ALIGMENT_CENTER);
        confirmLabel.setAligmentX(ElementFactory.AligmentX.X_ALIGMENT_CENTER);
        confirmLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        confirmLabel.setOpaque(true);
        confirmLabel.setForeground(Color.secondTitleForeGround);
        confirmLabel.setBackground(Color.greyBackground);

        return confirmLabel;
    }
}
