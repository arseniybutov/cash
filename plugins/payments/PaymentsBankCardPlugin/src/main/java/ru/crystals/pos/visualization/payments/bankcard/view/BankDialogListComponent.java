package ru.crystals.pos.visualization.payments.bankcard.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.visualization.check.PositionTable;
import ru.crystals.pos.visualization.check.PositionTableScroll;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

/**
 * Created by agaydenger on 18.11.16.
 */
public class BankDialogListComponent extends
        AbstractPaymentForm<BankCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
                CommonProductInputPanel, BankCardPaymentController> {

    private JTextPane messageLabel;
    private PositionTable positionTable;
    private BankDialog dialog;
    private DefaultTableModel dataModel;

    public BankDialogListComponent(XListener outerListener) {
        super(outerListener);
        messageLabel = new JTextPane();
        messageLabel.setEditable(false);
        Style.setactionStatusTextPaneStyle(messageLabel);
        messageLabel.setPreferredSize(new ScalableDimension(600, 100));

        this.setLayout(new FlowLayout());
        this.add(messageLabel, BorderLayout.NORTH);

        positionTable = new PositionTable();
        positionTable.setName("bankDialogListTable");
        PositionTableScroll scroll = new PositionTableScroll(positionTable, false, new ScalableDimension(600, 100));
        scroll.setVisible(true);
        JPanel listSelectPanel = new JPanel();
        listSelectPanel.setPreferredSize(new ScalableDimension(600, 150));
        listSelectPanel.setBackground(Color.greyBackground);
        listSelectPanel.setLayout(new GridLayoutManager(1, 1));
        listSelectPanel.add(scroll,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.FILL_NONE,
                        GridConstraints.FILL_NONE, null, null, null, 0, false)
        );
        this.add(listSelectPanel, BorderLayout.SOUTH);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return null;
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        return null;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return null;
    }


    @Override
    public void showForm(BankCardPaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        dialog = ((BankCardPaymentInfo) info).getDialog();
        messageLabel.setText(dialog.getTitle());
        dataModel = new DefaultTableModel();
        positionTable.setModel(dataModel);
        dataModel.setColumnCount(1);
        for (String rowTitle : dialog.getValues()) {
            dataModel.addRow(new String[]{rowTitle});
        }
        dataModel.fireTableDataChanged();
        positionTable.selectFirst();
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            positionTable.scrollUp();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            positionTable.scrollDown();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            getController().sendBankOperationResponse(dialog, String.valueOf(positionTable.getSelectedRow()));
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getController().closeBankDialog();
        }
        return true;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return true;
    }

}
