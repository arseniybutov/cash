package ru.crystals.pos.visualization.egais;

import ru.crystals.pos.egais.EgaisNotValidItem;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.configurator.view.util.Scaling;
import ru.crystals.pos.visualization.search.CommonCellRenderer;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by achibisov on 23.06.16.
 */
public class PositionNameAndBarcodeTable extends JTable {

    private List<EgaisNotValidItem> data;


    public PositionNameAndBarcodeTable() {
        this.data = new ArrayList<>();
        this.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return rowIndex + 1;
                    case 1:
                        return data.get(rowIndex).getName().trim();
                    case 2:
                        return data.get(rowIndex).getBarcode().trim();
                    default:
                        return null;
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }


            @Override
            public String getColumnName(int column) {
                switch (column) {
                    case 1:
                        return ResBundleVisualization.getString("NAME");
                    case 2:
                        return ResBundleVisualization.getString("BARCODE");
                    default:
                        return null;
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setRowHeight(Scaling.scaleY(30));

        getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());
        getColumnModel().getColumn(0).setPreferredWidth(Scaling.scaleX(20));

        final NameAndBarcodeTableCellRenderer CELL_RENDERER = new NameAndBarcodeTableCellRenderer();
        getColumnModel().getColumn(1).setCellRenderer(CELL_RENDERER);
        getColumnModel().getColumn(1).setPreferredWidth(Scaling.scaleX(450));
        getColumnModel().getColumn(2).setCellRenderer(CELL_RENDERER);
        getColumnModel().getColumn(2).setPreferredWidth(Scaling.scaleX(130));

        this.setTableHeader(new JTableHeader(getColumnModel()));
        getTableHeader().setDefaultRenderer(new HeaderRenderer());
        getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        getTableHeader().setBounds(1, 1, 1, 1);
        getTableHeader().setPreferredSize(new ScalableDimension(610, 30));
        setBounds(1, 1, 1, 1);
        setBorder(BorderFactory.createEmptyBorder());
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
    }

    public void setData(List<EgaisNotValidItem> data) {
        this.data = data;
        updateUI();
    }

    public void selectRowByBottle(EgaisNotValidItem bottle){
        int i = data.indexOf(bottle);
        setRowSelectionInterval(i, i);

    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (data.get(row).isDeleted()){
            comp.setForeground(Color.greyText);
        }
        return comp;
    }
}

class HeaderRenderer extends CommonCellRenderer{
    public HeaderRenderer() {
        super();
        this.setVerticalAlignment(SwingConstants.BOTTOM);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Style.setGrey18WhiteLabelStyle(comp);
        setSelectedStyle(comp, isSelected);
        return comp;
    }

    @Override
    protected void setSelectedStyle(Component comp, boolean isSelected) {
        if (isSelected) {
            comp.setBackground(Color.tableSelectionBackGround);
            comp.setForeground(Color.greyText);
        } else {
            comp.setBackground(Color.tableBackGround);
            comp.setForeground(Color.greyText);
        }
    }
}

class FirstColumnRenderer extends HeaderRenderer{
    public FirstColumnRenderer() {
        super();
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }
}

class NameAndBarcodeTableCellRenderer extends CommonCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Style.setBlack18LabelStyle(comp);
        setSelectedStyle(comp, isSelected);
        return comp;
    }


}


