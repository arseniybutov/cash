package ru.crystals.pos.visualization.products.jewel;

import java.awt.BorderLayout;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

public class JewelProductPanel  extends VisualPanel {

    private static final long serialVersionUID = 1L;
    private JLabel jBarcodeLabel = null;
    private Label jNameLabel = null;
    private JPanel jPerUnitPanel = null;
    private JPanel jSummaPanel = null;
    private JPanel jContainerPanel = null;
    private JLabel jUnitNameLabel = null;
    private JLabel jUnitPriceLabel = null;
    private JLabel jSummaLabel = null;
    private JLabel jExpand1 = null;
    private JLabel jExpand2 = null;
    private JLabel jTitle = null;
    private JPanel infoPanel = null;
    private JLabel jInfoItemLabel = null;

    private JLabel jInfoBarcodeLabel = null;
    private Label jInfoNameLabel = null;
    private JPanel jInfoPerUnitPanel = null;
    private JPanel jInfoSummaPanel = null;
    private JPanel jInfoContainerPanel = null;
    private JLabel jInfoUnitNameLabel = null;
    private JLabel jInfoUnitPriceLabel = null;
    private JLabel jInfoSummaLabel = null;
    private JLabel jInfoExpand3 = null;
    private JLabel jInfoTitle = null;

    private JLabel jWarningLabel = null;
    private JLabel jInfoWarningLabel = null;

    private enum ProductPanelState {EXPAND, COLLAPSE};
    private ProductPanelState state;

    public JewelProductPanel() {
        initialize();
    }

    protected void initialize() {
        jSummaLabel = newJLabelWithName("jSummaLabel");
        Style.setSummaLabelStyle(jSummaLabel);


        jNameLabel = new Label();
        jNameLabel.setName("jNameLabel");
        jNameLabel.setFont(MyriadFont.getRegular(30F));
        jNameLabel.setForeground(/*Color.blackText*/Color.secondTitleForeGround);
        jNameLabel.setBackground(Color.greyBackground);
        jNameLabel.setPreferredSize(new ScaleDimension(440, 146));
        jNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        jBarcodeLabel = newJLabelWithName("jBarcodeLabel");
        JPanel codeLabelPanel=new JPanel(new BorderLayout(0,0));
        codeLabelPanel.setBackground(Color.greyBackground);
        codeLabelPanel.setPreferredSize(new ScaleDimension(160, 146));
        jBarcodeLabel.setBorder(BorderFactory.createEmptyBorder(Scale.getY(20), 0, 0, 0));
        codeLabelPanel.add(jBarcodeLabel, BorderLayout.NORTH);

        jBarcodeLabel.setFont(MyriadFont.getRegular(22F));
        jBarcodeLabel.setForeground(Color.blackText);

        jBarcodeLabel.setPreferredSize(new ScaleDimension(120, 34));
        jBarcodeLabel.setVerticalTextPosition(JLabel.BOTTOM);
        jBarcodeLabel.setHorizontalTextPosition(JLabel.LEFT);

        jUnitPriceLabel = newJLabelWithName("jUnitPriceLabel");
        Style.setPaid40Style(jUnitPriceLabel);
        jUnitNameLabel = newJLabelWithName("jUnitNameLabel");
        Style.setPaid60LabelStyle(jUnitNameLabel);
        jPerUnitPanel = new JPanel();
        jPerUnitPanel.setBackground(Color.greyBackground);
        jPerUnitPanel.setPreferredSize(new ScaleDimension(160, 100));
        jPerUnitPanel.setLayout(this.flowLayout);
        jPerUnitPanel.add(jUnitNameLabel, null);
        jPerUnitPanel.add(jUnitPriceLabel, null);

        jSummaPanel = new JPanel();
        jSummaPanel.setBackground(Color.greyBackground);
        jSummaPanel.setPreferredSize(new ScaleDimension(450, 100));
        jSummaPanel.setLayout(this.flowLayout);
        jSummaPanel.add(new Empty(450,35));
        jSummaPanel.add(jSummaLabel);

        jWarningLabel = newJLabelWithName("jWarningLabel");
        jWarningLabel.setText(ResBundleVisualization.getString("DENIED_PURCHASE")); //$NON-NLS-1$
        Style.setWarningLabelStyle(jWarningLabel);
        jWarningLabel.setVisible(false);

        jExpand1 = new Empty(600, 20);
        jExpand1.setVisible(false);

        jExpand2 = new Empty(600, 60);
        jExpand2.setVisible(false);
        jTitle = newJLabelWithName("jTitle");
        Style.setProductTitleStyle(jTitle);
        jTitle.setVisible(false);
        jContainerPanel = new JPanel();
        jContainerPanel.setBackground(Color.greyBackground);
        jContainerPanel.setPreferredSize(new ScaleDimension(620, 100));
        jContainerPanel.setLayout(this.flowLayout);
        jContainerPanel.add(jPerUnitPanel);
        jContainerPanel.add(jSummaPanel);

        createInfoPanel();
        this.add(infoPanel);

        this.add(new Empty(640, 1), null);
        this.add(jTitle);

        this.add(codeLabelPanel, null);
        this.add(jNameLabel, null);

        this.add(jExpand1);
        this.add(jWarningLabel);

        this.add(jContainerPanel, null);
        this.add(jExpand2);
        this.add(new Empty(640, 1), null);

        this.setPreferredSize(Size.mainPanel);
    }

    public void setWarningInterval(String interval){
        jWarningLabel.setText(" " + ResBundleVisualization.getString("DENIED_PURCHASE") + " " + interval); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        jInfoWarningLabel.setText(" " + ResBundleVisualization.getString("DENIED_PURCHASE") + " " + interval); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void setWarningMessage(String message){
        jWarningLabel.setText(" " + message); //$NON-NLS-1$
        jInfoWarningLabel.setText(" " +message); //$NON-NLS-1$
    }

    public void setWarningVisible(boolean visible){
        if (visible){
            jInfoWarningLabel.setVisible(true);
            jInfoExpand3.setVisible(false);

            jWarningLabel.setVisible(true);
        }
        else{
            jInfoWarningLabel.setVisible(false);
            jInfoExpand3.setVisible(true);

            jWarningLabel.setVisible(false);
        }

    }

    public JLabel getWarningLabel(){
        return jWarningLabel;
    }

    public JLabel getInfoWarningLabel(){
        return jInfoWarningLabel;
    }

    private void createInfoPanel(){

        jInfoSummaLabel = newJLabelWithName("jInfoSummaLabel");
        Style.setSummaLabelStyle(jInfoSummaLabel);

        jInfoNameLabel = new Label();
        jInfoNameLabel.setName("jInfoNameLabel");
        jInfoNameLabel.setFont(MyriadFont.getRegular(30F));
        jInfoNameLabel.setForeground(/*Color.blackText*/Color.secondTitleForeGround);
        jInfoNameLabel.setBackground(Color.greyBackground);
        jInfoNameLabel.setPreferredSize(new ScaleDimension(440, 146));

        jInfoBarcodeLabel = newJLabelWithName("jInfoBarcodeLabel");
        Style.setCodeLabelStyle(jInfoBarcodeLabel);
        jInfoBarcodeLabel.setPreferredSize(new ScaleDimension(300, 30));

        jInfoUnitPriceLabel = newJLabelWithName("jInfoUnitPriceLabel");
        Style.setPaid40Style(jInfoUnitPriceLabel);
        jInfoUnitNameLabel = newJLabelWithName("jInfoUnitNameLabel");
        Style.setPaid60LabelStyle(jInfoUnitNameLabel);
        jInfoUnitNameLabel.setPreferredSize(new ScaleDimension(100, 40));
        jInfoPerUnitPanel = new JPanel();
        jInfoPerUnitPanel.setBackground(Color.greyBackground);
        jInfoPerUnitPanel.setPreferredSize(new ScaleDimension(160, 100));
        jInfoPerUnitPanel.setLayout(this.flowLayout);
        jInfoPerUnitPanel.add(jInfoUnitNameLabel, BorderLayout.EAST);
        jInfoPerUnitPanel.add(jInfoUnitPriceLabel, BorderLayout.EAST);

        jInfoSummaPanel = new JPanel();
        jInfoSummaPanel.setBackground(Color.greyBackground);
        jInfoSummaPanel.setPreferredSize(new ScaleDimension(450, 60));
        jInfoSummaPanel.setLayout(this.flowLayout);
        jInfoSummaPanel.add(jInfoSummaLabel);
        jInfoExpand3 = new Empty(600, 40);
        jInfoExpand3.setVisible(true);
        jInfoTitle = newJLabelWithName("jInfoTitle");
        Style.setProductTitleStyle(jInfoTitle);
        jInfoContainerPanel = new JPanel();
        jInfoContainerPanel.setBackground(Color.greyBackground);
        jInfoContainerPanel.setLayout(this.flowLayout);
        jInfoContainerPanel.add(jInfoPerUnitPanel);
        jInfoContainerPanel.add(jInfoSummaPanel);
        jInfoContainerPanel.setPreferredSize(new ScaleDimension(620, 100));
        jInfoContainerPanel.setVisible(true);


        jInfoItemLabel = newJLabelWithName("jInfoItemLabel");
        Style.setCodeLabelStyle(jInfoItemLabel);
        jInfoItemLabel.setPreferredSize(new ScaleDimension(300, 30));

        jInfoWarningLabel = newJLabelWithName("jInfoWarningLabel");
        jInfoWarningLabel.setText(ResBundleVisualization.getString("DENIED_PURCHASE")); //$NON-NLS-1$
        Style.setWarningLabelStyle(jInfoWarningLabel);
        jInfoWarningLabel.setVisible(false);

        infoPanel = new JPanel();
        infoPanel.setLayout(this.flowLayout);
        infoPanel.setBackground(Color.greyBackground);
        infoPanel.add(new Empty(640,1));
        infoPanel.add(jInfoTitle);
        infoPanel.add(jInfoNameLabel);
        infoPanel.add(jInfoBarcodeLabel);

        infoPanel.add(jInfoItemLabel);

        infoPanel.add(new Empty(600,5));
        infoPanel.add(jInfoWarningLabel);
        infoPanel.add(jInfoContainerPanel);
        infoPanel.add(new Empty(640, 1));

        infoPanel.setPreferredSize(Size.mainPanel);
    }


    public void setProduct(ProductEntity product){
        setBarcode(product.getBarCode()==null?"":product.getBarCode().getBarCode());
        setProductName(product.getName());
        setPrice(product.getPrice().getPriceBigDecimal());
        setMeasure(product.getMeasure().getName());
        setItem(product.getItem());
    }

    private void setBarcode(String barcode){
        jBarcodeLabel.setText(barcode);
        jInfoBarcodeLabel.setText(ResBundleVisualization.getString("GOODS_BARCODE")+ " "+ barcode); //$NON-NLS-1$
    }

    private void setProductName(String name){
        jNameLabel.setText(name);
        jInfoNameLabel.setText(name);
    }

    private void setPrice(BigDecimal price){
        jUnitPriceLabel.setText(price.toString());
        jInfoUnitPriceLabel.setText(price.toString());
    }

    public void setSumma(BigDecimal summa){
        jSummaLabel.setText(summa.toString());
        jInfoSummaLabel.setText(summa.toString());
    }

    private void setMeasure(String measure){
        jUnitNameLabel.setText(String.format(this.getLocale(), ResBundleVisualization.getString("PER"), measure)); //$NON-NLS-1$
        jInfoUnitNameLabel.setText(String.format(this.getLocale(), ResBundleVisualization.getString("PER"), measure)); //$NON-NLS-1$
    }

    private void setItem(String item){
        jInfoItemLabel.setText(ResBundleVisualization.getString("GOODS_CODE")+" " + item); //$NON-NLS-1$
    }

    public void collapse() {
        setState(ProductPanelState.COLLAPSE);
    }

    public void expand() {
        setState(ProductPanelState.EXPAND);
    }

    public void setTitle(String title){
        jTitle.setText(title);
        jInfoTitle.setText(title);
    }

    public void setState(ProductPanelState state) {
        this.state = state;

        switch (state){
            case EXPAND:
                infoPanel.setVisible(true);
                this.setPreferredSize(Size.productPanelEx);
                break;
            case COLLAPSE:
                infoPanel.setVisible(false);
                jTitle.setVisible(false);
                this.setPreferredSize(Size.productPanelEx);
                break;
        }

    }

    public ProductPanelState getState() {
        return state;
    }
}
