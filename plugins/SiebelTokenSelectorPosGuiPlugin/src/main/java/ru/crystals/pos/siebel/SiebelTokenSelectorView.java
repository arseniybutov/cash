package ru.crystals.pos.siebel;

import ru.crystals.pos.cards.siebel.results.SiebelTokenThreshold;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;

public class SiebelTokenSelectorView extends VisualPanel implements XListener {
    private JTextPane headerTextPane = new JTextPane();
    private SiebelTokenTable table;
    private JLabel tokenAmountLabel = new JLabel("");
    private SiebelTokenSelectorController controller;
    private CardLayout layout = new CardLayout();

    public SiebelTokenSelectorView() {
        this.setLayout(layout);
        this.add(createTokenSelectorPanel(), "tokenSelector");

        new XListenerAdapter(this);
    }

    private JPanel createTokenSelectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 10, 10, 10));
        panel.setBackground(Color.greyBackground);

        setComponentPosStyle(headerTextPane);
        this.headerTextPane.setText(Strings.TOKEN_SELECTOR_HEADER.get());

        table = new SiebelTokenTable();
        table.setBorder(new EmptyBorder(0, 0, 0, 10));

        panel.add(headerTextPane, BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(labelPanel, BoxLayout.Y_AXIS);
        labelPanel.setBackground(Color.greyBackground);
        labelPanel.setLayout(boxLayout);
        labelPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel balanceText = new JLabel(Strings.TOKEN_SELECTOR_LABEL_TOKEN_BALANCE.get());
        labelPanel.add(setComponentPosStyle(balanceText));
        tokenAmountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelPanel.add(setComponentPosStyle(tokenAmountLabel));

        panel.add(labelPanel, BorderLayout.EAST);

        return panel;
    }

    public void reset() {
        layout.show(this, "tokenSelector");
        this.getComponent(0).validate();
    }

    public void showTokens(List<SiebelTokenThreshold> tokens) {
        table.populate(tokens);
        if (!tokens.isEmpty()) {
            // Сброс всякого выделения из списка токенов.
            table.setSelectedRow(-1);
            // Выставление выделения по умолчанию на "Лучший выбор".
            table.setSelectedRow(0);
        }
    }

    public void setTokenAmount(BigDecimal amount) {
        this.tokenAmountLabel.setText(amount.toPlainString());
    }

    public void setController(SiebelTokenSelectorController controller) {
        this.controller = controller;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return false;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        switch (e.getKeyCode()) {
            case XKeyEvent.VK_UP:
                table.up();
                return true;
            case XKeyEvent.VK_DOWN:
                table.down();
                return true;
            case XKeyEvent.VK_ESCAPE:
                controller.next();
                return true;
            case XKeyEvent.VK_ENTER:
                if (table.getSelectedRow() == 0) {
                    controller.selectBestOffer();
                    return true;
                }
                controller.next(table.getSelected());
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return false;
    }

    private Component setComponentPosStyle(Component c) {
        // Таки это проще, чем продираться сквозь style
        c.setFont(MyriadFont.getItalic(30.0f));
        c.setForeground(Color.greyText);
        c.setBackground(Color.greyBackground);
        return c;
    }
}
