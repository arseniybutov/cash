package ru.crystals.pos.card.replacement.visualization;

import ru.crystals.pos.visualization.commonplugin.view.panel.CommonInputPanelExt;
import ru.crystals.pos.visualization.styles.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Причин существования этого класса две:
 * 1. Предоставлять имя класса, отличное от JPanel.
 * 2. Уместиться в окне ввода номера карты.
 *
 * @since 10.2.83.0
 */
class CardInputFormDecorator extends JPanel {

    public CardInputFormDecorator(CommonInputPanelExt input) {
        this.setLayout(new BorderLayout());
        this.add(input, BorderLayout.SOUTH);
        this.setBackground(Color.greyBackground);
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}
