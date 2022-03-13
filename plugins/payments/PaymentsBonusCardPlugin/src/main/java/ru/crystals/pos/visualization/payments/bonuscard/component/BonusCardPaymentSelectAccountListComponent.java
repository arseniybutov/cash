package ru.crystals.pos.visualization.payments.bonuscard.component;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.payments.BonusesConverter;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.PositionTable;
import ru.crystals.pos.visualization.check.PositionTableScroll;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.wsclient.cards.internal.BonusAccountVO;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BonusCardPaymentSelectAccountListComponent extends CommonPaymentPaidPanel implements XKeyListener {

    private static final long serialVersionUID = 1L;
    private PositionTable bonusAccountTable = null;
    private BonusAccountsTableModel bonusAccountTableModel = new BonusAccountsTableModel();

    public BonusCardPaymentSelectAccountListComponent() {
        this.removeAll();
        JLabel selectBonusAccount = new JLabel();
        selectBonusAccount.setText(ResBundleVisualization.getString("bonus.setretail.select.bonus.account"));
        Style.setPaymentTypeLabelStyle(selectBonusAccount);
        selectBonusAccount.setFont(MyriadFont.getItalic(20F));
        selectBonusAccount.setPreferredSize(new ScaleDimension(395, 25));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.ipady = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 5, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        this.setPreferredSize(new ScaleDimension(400, 260));
        this.setSize(new ScaleDimension(400, 260));
        this.add(selectBonusAccount, c);
        c.gridy = 1;
        this.add(getScrollPaneWithBonusAccountTable(), c);
    }


    private JPanel getScrollPaneWithBonusAccountTable() {
        bonusAccountTableModel = new BonusAccountsTableModel();

        bonusAccountTable = new PositionTable(Scale.getY(38));
        bonusAccountTable.setModel(bonusAccountTableModel);

        bonusAccountTable.setIntercellSpacing(new ScaleDimension(0, 0));
        bonusAccountTable.setFont(MyriadFont.getRegular(18F));

        DefaultTableCellRenderer renderer;

        //Колонка 1 - отступ
        bonusAccountTable.getColumnModel().getColumn(0).setPreferredWidth(Scale.getX(5));

        //Колонка 2 - название бонусного счета
        bonusAccountTable.getColumnModel().getColumn(1).setPreferredWidth(Scale.getX(200));

        //Колонка 3 - баланс в бонусах
        renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        bonusAccountTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        bonusAccountTable.getColumnModel().getColumn(2).setPreferredWidth(Scale.getX(100));

        //Колонка 3 - баланс в рублях
        renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        bonusAccountTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
        bonusAccountTable.getColumnModel().getColumn(3).setPreferredWidth(Scale.getX(100));

        //Колонка 5 - отступ
        bonusAccountTable.getColumnModel().getColumn(4).setPreferredWidth(Scale.getX(5));

        JPanel jScrollPane = new PositionTableScroll(bonusAccountTable, true);
        jScrollPane.setPreferredSize(new ScaleDimension(395, 175));
        jScrollPane.setBackground(Color.tableBackGround);

        return jScrollPane;
    }

    public void fillTable(List<BonusAccountVO> accounts) {
        bonusAccountTableModel.setData(accounts);
        bonusAccountTable.selectFirst();
    }

    public int getSelectedRow() {
        return bonusAccountTable.getSelectedRow();
    }

    public BonusAccountVO getSelectedAccount() {
        return bonusAccountTableModel.getSelectedAccount(bonusAccountTable.getSelectedRow());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            bonusAccountTable.scrollUp();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            bonusAccountTable.scrollDown();
        }
    }

    private static class BonusAccountsTableModel extends AbstractTableModel {

        private static final String SUMM_IN_BONUSES = " " + ResBundleVisualization.getString("bonus.in.bonuses");
        private static final String SUMM_IN_CURRENCY = " " + ResBundleVisualization.getString("bonus.in.rubles");

        private List<BonusAccountVO> accounts = new ArrayList<>();

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return accounts == null ? 0 : accounts.size();
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0 || columnIndex == 4) {
                return "";
            } else if (columnIndex == 1) {
                return getSelectedAccount(rowIndex).getBonusAccountsTypeVO().getBonusAccountsTypeName();
            } else if (columnIndex == 2) {
                return getSelectedAccount(rowIndex).getBalance() + SUMM_IN_BONUSES;
            } else if (columnIndex == 3) {
                return getSelectedAccount(rowIndex).getBalance() != null ?
                        CurrencyUtil.formatSum(BonusesConverter.convertBonuses(getSelectedAccount(rowIndex))) + SUMM_IN_CURRENCY : "0" + SUMM_IN_CURRENCY;
            }
            return null;
        }

        public BonusAccountVO getSelectedAccount(int rowIndex) {
            return accounts.get(rowIndex);
        }

        public void setData(List<BonusAccountVO> accounts) {
            this.accounts = accounts;
            fireTableDataChanged();
        }
    }

    public void setPaid(PaymentInfo info) {
    }

    public void setRefund(boolean refund) {
    }
}
