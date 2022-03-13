package ru.crystals.pos.siebel;

import ru.crystals.pos.cards.siebel.results.SiebelTokenThreshold;
import ru.crystals.pos.visualization.components.TableComponent;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.List;
import java.util.Vector;

// Дряная затея использовать TableComponent, проще было с нуля всё написать.
public class SiebelTokenTable extends TableComponent {
    private CustomTableModel model = new CustomTableModel(new String[]{
            Strings.TOKEN_SELECTOR_TABLE_COLUMN_TOKEN_AMOUNT.get(),
            Strings.TOKEN_SELECTOR_TABLE_COLUMN_PRICE.get()
    });
    private JTable actualTable;

    public SiebelTokenTable() {
        super();
        actualTable = getCommonTable(model);

        actualTable.setOpaque(false);
        JScrollPane tableScroll = new JScrollPane(actualTable);
        tableScroll.setBackground(actualTable.getBackground());
        tableScroll.getViewport().setOpaque(false);
        this.add(tableScroll);
        this.setCheckHeaders(new int[]{SwingConstants.CENTER, SwingConstants.CENTER});
        this.setCheckColumns(new int[]{SwingConstants.CENTER, SwingConstants.CENTER}, new int[]{SwingConstants.CENTER, SwingConstants.CENTER});
    }

    public void populate(List<SiebelTokenThreshold> thresholds) {
        model.getDataVector().clear();
        // Хак для BestOffer
        model.addRow(new Object[]{""});

        for (SiebelTokenThreshold threshold : thresholds) {
            model.addRow(new Object[]{threshold});
        }
        actualTable.repaint();
    }

    public SiebelTokenThreshold getSelected() {
        if (actualTable.getSelectedRow() == -1) {
            return null;
        }
        return (SiebelTokenThreshold) ((Vector) model.getDataVector().get(actualTable.getSelectedRow())).get(0);
    }

    @Override
    protected void setCheckColumns(int[] columnsAlignment, int[] columnsWidth) {
        for (int i = 0; i < productTableModel.getColumnCount(); i++) {
            DefaultTableCellRenderer cellRenderer = new SiebelTokenCellRenderer();
            cellRenderer.setHorizontalAlignment(columnsAlignment[i]);
            if (columnsWidth != null) {
                commonTable.getColumnModel().getColumn(i).setPreferredWidth(columnsWidth[i]);
            }
            commonTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        commonTable.setIntercellSpacing(new ScaleDimension(0, 0));
    }

    private class SiebelTokenCellRenderer extends CustomCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row == 0) {
                // Таки это BestOffer пытается отрендериться, он всегда первый.
                if (column == 0) {
                    ((JLabel) component).setText(Strings.TOKEN_TABLE_ROW_BEST_OFFER.get());
                    return component;
                }
                ((JLabel) component).setText("");
                return component;
            }
            switch (column) {
                case 0:
                    ((JLabel) component).setText(((SiebelTokenThreshold) value).getTokens().toPlainString());
                    break;
                case 1:
                    // Лучше бы с нуля писал, что-то совсем неудобно TableComponent пользоваться
                    ((JLabel) component).setText(((SiebelTokenThreshold) model.getValueAt(row, 0)).getCost().toPlainString());
                    break;
                default:
                    break;
            }
            return component;
        }
    }
}
