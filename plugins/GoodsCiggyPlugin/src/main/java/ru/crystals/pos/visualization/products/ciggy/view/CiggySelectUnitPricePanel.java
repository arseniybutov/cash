package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.visualization.check.PositionTable;
import ru.crystals.pos.visualization.check.PositionTableScroll;
import ru.crystals.pos.visualization.check.PositionTableSelectionListener;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.products.ciggy.PriceTableModel;
import ru.crystals.pos.visualization.search.CommonCellRenderer;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;


public class CiggySelectUnitPricePanel extends AbstractProductUnitPriceComponent implements XKeyListener {

    private static final long serialVersionUID = 1L;
    private JLabel codeLabel;
    private JLabel nameLabel;
    private JLabel infoLabel = null;
    private JPanel centerPanel;
    private PositionTableScroll jScrollPanel;
    private PriceTableModel priceTableModel = new PriceTableModel();
    private PositionTable positionTable = new PositionTable(false);

    public CiggySelectUnitPricePanel(String info, boolean view, PositionTableSelectionListener positionTableSelectionListener) {

        this.setLayout(new BorderLayout(16, 16));
        this.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        this.setBackground(ru.crystals.pos.visualization.styles.Color.greyBackground);

        positionTable.setModel(priceTableModel);
        positionTable.getTableHeader().setDefaultRenderer(new CommonCellRenderer());
        if (positionTableSelectionListener != null) {
            positionTable.setPositionTableSelectionListener(positionTableSelectionListener);
        }

        nameLabel = new JLabel();
        Style.setNameLabelStyle(nameLabel);
        codeLabel = new JLabel();
        Style.setCodeLabelStyle(codeLabel);

        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2, 16, 16));

        jScrollPanel = new PositionTableScroll(positionTable, true);
        centerPanel.add(jScrollPanel);
        centerPanel.setBackground(this.getBackground());

        if (view) {
            this.setPreferredSize(new ScalableDimension(240, 220));
        } else {
            this.setPreferredSize(Size.middlePanel);
            infoLabel = new JLabel(info);
            infoLabel.setFont(MyriadFont.getItalic(26F));
            infoLabel.setBackground(this.getBackground());
            centerPanel.add(infoLabel);

        }

        this.add(centerPanel, BorderLayout.CENTER);
    }

    public void up() {
        positionTable.scrollUp();
    }

    public void down() {
        positionTable.scrollDown();
    }

    public BigDecimal getSelectedPrice() {
        if(positionTable.getSelectedRow()<0){
            positionTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        return priceTableModel.getValueAt(positionTable.getSelectedRow(), 1);
    }

    public BigDecimal getSelectedMRP() {
        if (positionTable.getSelectedRow() < 0) {
            positionTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        return priceTableModel.getValueAt(positionTable.getSelectedRow(), 0);
    }


    @Override
    public void setProduct(ProductEntity product) {
        codeLabel.setText(product.getItem());
        nameLabel.setText(product.getName());
        priceTableModel.setData((ProductCiggyEntity) product);
        positionTable.selectFirst();

        if(((ProductCiggyEntity) product).getAdditionalPrices().size() == 0 ){
            jScrollPanel.setVisible(false);
        }else{
            jScrollPanel.setVisible(true);
        }
    }

    @Override
    public void setUnitPrice(BigDecimal price) {

    }

    @Override
    public BigDecimal getUnitPrice() {
        return getSelectedPrice();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down();
        }
    }
}
