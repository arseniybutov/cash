package ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels;

import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.visualization.check.PositionTable;
import ru.crystals.pos.visualization.check.PositionTableScroll;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.AbstractUnitPanel;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Контрол со списком для выбора для использования в плагинах оплат
 */
public class CommonSelectUnitPanel extends AbstractUnitPanel implements XKeyListener {

    private static final long serialVersionUID = 1L;
    private List<String> values;
    private JLabel codeLabel = null;
    private JLabel nameLabel = null;
    private JLabel infoLabel = null;
    private JPanel centerPanel;
    private PositionTableScroll jScrollPanel = null;
    private TableModel tableModel;
    private PositionTable positionTable = new PositionTable(false);

    public CommonSelectUnitPanel() {
        positionTable.setTableHeader(null);
        this.setLayout(new BorderLayout(16, 16));
        this.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);

        nameLabel = new JLabel();
        Style.setNameLabelStyle(nameLabel);
        codeLabel = new JLabel();
        Style.setCodeLabelStyle(codeLabel);

        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2, 16, 16));

        jScrollPanel = new PositionTableScroll(positionTable, true);
        centerPanel.add(jScrollPanel);
        centerPanel.setBackground(this.getBackground());

        this.setPreferredSize(Size.middlePanel);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    public void up() {
        positionTable.scrollUp();
    }

    public void down() {
        positionTable.scrollDown();
    }

    public String getSelectedValue() {
        if (positionTable.getSelectedRow() < 0) {
            positionTable.getSelectionModel().setSelectionInterval(0, 0);
        }

        return (String) tableModel.getValueAt(positionTable.getSelectedRow(), 0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down();
        }
    }

    @Override
    public void setPaid(PaymentInfo info) {
    }

    @Override
    public void setRefund(boolean refund) {
    }

    public void setValues(List<String> values) {
        final List<String> vals = values;
        tableModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return vals.size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return vals.get(rowIndex);
            }
        };
        positionTable.setModel(tableModel);
        if (tableModel.getRowCount() != 0) {
            positionTable.getSelectionModel().setSelectionInterval(0, 0);
        }

    }
}
