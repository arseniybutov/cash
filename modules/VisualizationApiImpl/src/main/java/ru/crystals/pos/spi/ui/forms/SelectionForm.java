package ru.crystals.pos.spi.ui.forms;

import ru.crystals.pos.api.ui.listener.InputListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.controls.font.MyriadFont;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Форма выбора из нескольких значений.
 */
public class SelectionForm extends ContextPanelWithCaption {

    private final InputListener inputListener;
    private final JTable jTable;
    private final String[] keys;


    /**
     * @param items таблица выбора, где ключ произвольная строка, а значения отображаются в виде списка для выбора
     * @param inputListener слушатель событий формы, в качестве введенного значения возвращает ключ из таблицы выбора
     */
    public SelectionForm(Map<String, List<String>> items, InputListener inputListener) {
        this.inputListener = Objects.requireNonNull(inputListener);

        keys = items.keySet().toArray(new String[0]);

        int columns = items.values().stream().mapToInt(List::size).max().orElse(1);
        Vector<String> columnNames = IntStream.range(0, columns).mapToObj(x -> "").collect(Collectors.toCollection(Vector::new));

        Vector<Vector<String>> rowData = new Vector<>();
        for (List<String> line: items.values()) {
            Vector<String> row = new Vector<>();
            for (String str: line) {
                row.addElement(str);
            }
            rowData.addElement(row);
        }

        jTable = newProperlySizedTable(columnNames, rowData);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable.setTableHeader(null);
        jTable.setShowHorizontalLines(false);
        jTable.setShowVerticalLines(false);
        jTable.setSelectionBackground(Color.tableSelectionBackGround);
        jTable.setSelectionForeground(Color.tableSelectionForeGround);
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable.setShowGrid(false);
        jTable.setForeground(Color.tableForeGround);
        jTable.setFont(MyriadFont.getRegular(18F));
        jTable.setRowHeight(Scale.getY(22));
        jTable.setIntercellSpacing(new ScaleDimension(0, 0));
        jTable.setRowSelectionInterval(0, 0);

        JScrollPane jViewport = new JScrollPane(jTable);
        jViewport.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jViewport.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jViewport.setBorder(BorderFactory.createLineBorder(Color.tableBorder, 1));
        jViewport.getViewport().setBackground(Color.tableBackGround);

        this.add(jViewport, new ScaleRectangle(10, HEIGHT_DEFAULT - 10 - 185, WIDTH_DEFAULT - 20, 185));
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                setSelectedRow(jTable.getSelectedRow() - 1);
                return true;
            case KeyEvent.VK_DOWN:
                setSelectedRow(jTable.getSelectedRow() + 1);
                return true;
            case KeyEvent.VK_ESCAPE:
                inputListener.eventCanceled();
                return true;
            case KeyEvent.VK_ENTER:
                inputListener.eventInputComplete(keys[jTable.getSelectedRow()]);
                return true;
            default:
                return false;
        }
    }

    private JTable newProperlySizedTable(Vector<String> columnNames, Vector<Vector<String>> rowData) {
        return new JTable(rowData, columnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                if (super.getColumnCount() == 1) {
                    tableColumn.setPreferredWidth(tableColumn.getMaxWidth());
                } else if (column < super.getColumnCount() - 1) {
                    tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width + 10, tableColumn.getPreferredWidth()));
                } else {
                    tableColumn.setMinWidth(super.getWidth());
                }
                return component;
            }
        };
    }

    private void setSelectedRow(int index) {
        if (index > -1 && index < jTable.getRowCount()) {
            jTable.setRowSelectionInterval(index, index);
        }
    }

}
