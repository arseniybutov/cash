package ru.crystals.pos.visualization.products.weight.view;

import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.JPanel;

import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.visualization.check.PositionTable;
import ru.crystals.pos.visualization.check.PositionTableScroll;
import ru.crystals.pos.visualization.products.weight.TareTableModel;
import ru.crystals.pos.visualization.products.weight.tare.Tare;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.utils.ScaleDimension;

public class WeightProductSelectTarePanel extends JPanel implements XKeyListener {
    private static final long serialVersionUID = 1L;
    private TareTableModel tareTableModel = new TareTableModel();
    private PositionTable positionTable = new PositionTable();

    public WeightProductSelectTarePanel() {
        LayoutManager flowLayout = new FlowLayout(FlowLayout.LEADING, 20, 0);
        setLayout(flowLayout);
        setBackground(Color.greyBackground);
        setSize(Size.middlePanel);

        positionTable.setModel(tareTableModel);
        PositionTableScroll jScrollPanel = new PositionTableScroll(positionTable, true, new ScaleDimension(430, 178));
        jScrollPanel.setName("scrollPanel");
        add(jScrollPanel);
    }

    public void clear() {
        positionTable.selectFirst();
    }

    public void up() {
        positionTable.scrollUp();
    }

    public void down() {
        positionTable.scrollDown();
    }

    public Tare getSelectedTare() {
        if (positionTable.getSelectedRow() < 0) {
            Tare tare = new Tare();
            tare.setWeight(0L);
            return tare;
        }
        return tareTableModel.getValueAt(positionTable.getSelectedRow(), 0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down();
        }
    }

    public void refreshTare(ProductWeightEntity product) {
        tareTableModel.refreshTare(product);
        positionTable.selectFirst();
    }

    public boolean isTareAvailable() {
        return tareTableModel.isTareAvailable();
    }
}
