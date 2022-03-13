package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.styles.Size;

import java.awt.BorderLayout;
import java.awt.Component;

public class ComponentVisualPanel extends VisualPanel {
    private VisualPanel bottomPanel;

    public ComponentVisualPanel() {
        this.setPreferredSize(Size.mainPanel);
        bottomPanel = new VisualPanel();
        bottomPanel.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.setName(ComponentVisualPanel.class.getSimpleName());
    }

    public void addVisualPanel(Component visualPanel, String position) {
        if (BorderLayout.NORTH.equals(position)) {
            add(visualPanel, BorderLayout.NORTH);
        } else {
            if (bottomPanel.getParent() == null) {
                add(bottomPanel, BorderLayout.CENTER);
            }
            for (Component c :bottomPanel.getComponents()){
                if (visualPanel.getClass().equals(c.getClass())){
                    bottomPanel.remove(c);
                } else {
                    c.setVisible(false);
                }
            }
            bottomPanel.add(visualPanel, BorderLayout.CENTER);
            visualPanel.setVisible(true);
        }
    }
}
