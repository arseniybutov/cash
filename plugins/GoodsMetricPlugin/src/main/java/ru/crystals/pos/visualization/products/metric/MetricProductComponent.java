package ru.crystals.pos.visualization.products.metric;

import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductMetricEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.components.inputfield.InputFieldFlat;
import ru.crystals.pos.visualization.productinfo.ProductInfoContainer;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.ProductPanel;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.inputfield.BigDecimalFormatter;
import ru.crystals.pos.visualizationtouch.components.inputfield.CurrencyFormatter;

public class MetricProductComponent extends VisualPanel {
    
    public enum METRIC_COMPONENT_MODE {
        VALUE, PRICE
    }
    private static final long serialVersionUID = 1L;
    private ProductPanel productPanel;
    private VisualPanel editOrDeleteButtonsPanel;
    private JLabel deleteButton;
    private JLabel editButton;
    private ProductState state = ProductState.ADD;
    private METRIC_COMPONENT_MODE weightComponentMode = METRIC_COMPONENT_MODE.VALUE;
    private ProductMetricEntity product;
    private BigDecimalFormatter valueFormatter = new BigDecimalFormatter(3, false);
    private CurrencyFormatter priceFormatter = new CurrencyFormatter(2);
    private InputFieldFlat<BigDecimal> inputField = new InputFieldFlat<BigDecimal>(valueFormatter);
    private JPanel inputPanel;
    private JLabel jLabel = null;
    private PositionEntity position;
    private long presetQuantity = 0;
    

    public MetricProductComponent() {
        super();
        productPanel = new ProductPanel();
        Style.setInputPanelStyle(inputField);
        inputField.addInputFieldListener((target, data) -> setSumma(priceFormatter.getValue().multiply(valueFormatter.getValue()).setScale(2, RoundingMode.HALF_EVEN)));

        editOrDeleteButtonsPanel = new VisualPanel();
        editOrDeleteButtonsPanel.setPreferredSize(Size.inputPanel);

        deleteButton = new JLabel();
        deleteButton.setText(ResBundleVisualization.getString("BUTTON_DELETE")); //$NON-NLS-1$
        Style.setButtonStyle(deleteButton);
        deleteButton.setFont(MyriadFont.getRegular(28f));
        deleteButton.setPreferredSize(new ScaleDimension(300, 42));
        editButton = new JLabel();
        editButton.setText(ResBundleGoodsMetric.getString("BUTTON_EDIT")); //$NON-NLS-1$
        Style.setButtonStyle(editButton);
        editButton.setFont(MyriadFont.getRegular(28f));
        editButton.setPreferredSize(new ScaleDimension(300, 42));

        editOrDeleteButtonsPanel.add(new Empty(640, 18));
        editOrDeleteButtonsPanel.add(deleteButton);
        editOrDeleteButtonsPanel.add(new Empty(10, 80));
        editOrDeleteButtonsPanel.add(editButton);

        this.setPreferredSize(Size.middlePanel);
        this.add(productPanel, null);
        this.add(new Empty(640, 22), null);

        initInputPanel();
    }

    private void initInputPanel() {
        inputPanel = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setHgap(0);
        fl.setVgap(0);
        inputPanel.setLayout(fl);
        inputPanel.setBackground(Color.greyBackground);
        inputPanel.setBorder(BorderFactory.createEmptyBorder());


        jLabel = new JLabel();
        Style.setLabelStyle(jLabel);
        inputPanel.setPreferredSize(Size.inputPanel);
        inputPanel.add(jLabel, null);

        jLabel.setText(ResBundleGoodsMetric.getString("QUANTITY"));
        inputPanel.add((JPanel) inputField);

        this.add(inputPanel, null);
    }

    public BigDecimal getPrice() {
        return priceFormatter.getValue();
    }

    public BigDecimal getValue() {
        return valueFormatter.getValue();
    }
    
    public void setValue(BigDecimal value) {
        inputField.setValue(value);
    }

    public PositionEntity getPosition() {
        return position;
    }

    public void setPosition(PositionEntity position) {
        this.position = position;
    }

    public BigDecimal getSumm() {
        return priceFormatter.getValue().multiply(valueFormatter.getValue()).setScale(2, RoundingMode.HALF_EVEN);
    }

    public METRIC_COMPONENT_MODE getMetricComponentMode() {
        return this.weightComponentMode;
    }

    public void setMetricComponentMode(METRIC_COMPONENT_MODE weightComponentMode) {
        this.weightComponentMode = weightComponentMode;
        switch (weightComponentMode) {
            case VALUE:
                inputField.setTextFormatter(valueFormatter);
                break;
            case PRICE:
                inputField.setTextFormatter(priceFormatter);
                break;
        }
    }

    public void number(Byte num) {
            inputField.addChar(num.toString().charAt(0));
        
    }

    public void dot() {
            inputField.dot();
    }

    public void changeSelectedButton(boolean edit) {
        if (edit) {
            Style.setSelectedButtonStyle(editButton);
            Style.setButtonStyle(deleteButton);
        } else {
            Style.setSelectedButtonStyle(deleteButton);
            Style.setButtonStyle(editButton);
        }
        editButton.setFont(MyriadFont.getRegular(28f));
        deleteButton.setFont(MyriadFont.getRegular(28f));
    }

    public void disableEditButton() {
        editButton.setForeground(Color.disabledButtonForeGround);
        editButton.setBackground(Color.disabledButtonBackGround);
        editButton.setFont(MyriadFont.getRegular(28f));
    }

    public void disableDeleteButton() {
        deleteButton.setForeground(Color.disabledButtonForeGround);
        deleteButton.setBackground(Color.disabledButtonBackGround);
        deleteButton.setFont(MyriadFont.getRegular(28f));
    }

    public ProductState getState() {
        return state;
    }
    
    public long getPresetValue() {
        return presetQuantity;
    }

    public void setProduct(ProductEntity product) {
        this.product = (ProductMetricEntity)product;
        productPanel.setProduct(product);
        priceFormatter.setValue(product.getPrice().getPriceBigDecimal());
        Long presetValue = this.product.getValue();
        inputField.setEnabled(presetValue==null);
        if (presetValue == null) {
            presetValue = 0L;
            presetQuantity = presetValue;
        }
        inputField.setPresetValue(BigDecimal.valueOf(presetValue, 3));
    }

    public void setSumma(BigDecimal summa) {
        productPanel.setSumma(summa);
    }

    public void enterSumma() {
        jLabel.setText(ResBundleGoodsMetric.getString("SUMM"));
    }

    public void enterQuantity() {
        jLabel.setText(ResBundleGoodsMetric.getString("QUANTITY"));
        if (!(Factory.getInstance().getMainWindow().getCurrentContainer() instanceof ProductInfoContainer)) {
            inputField.setWelcomeText(ResBundleGoodsMetric.getString("ENTER_QUANTITY"));
        }
    }

    public void setInputFieldEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
    }

    public void setProductState(ProductState state) {
        this.state = state;
        switch (state) {
            case ADD:
                collapse();
                inputPanel.setEnabled(true);
                if (this.equals(editOrDeleteButtonsPanel.getParent())) {
                    this.remove(editOrDeleteButtonsPanel);
                    this.add(inputPanel);
                }
                break;
            case REFUND:
                collapse();
                inputField.setEnabled(true);
                if (this.equals(editOrDeleteButtonsPanel.getParent())) {
                    this.remove(editOrDeleteButtonsPanel);
                    this.add(inputPanel);
                }
                break;
            case EDIT:
                inputPanel.setEnabled(true);
                if (this.equals(editOrDeleteButtonsPanel.getParent())) {
                    this.remove(editOrDeleteButtonsPanel);
                    this.add(inputPanel);
                }
                expand();
                break;
                
            case EDIT_REFUND:
                inputPanel.setEnabled(true);
                valueFormatter.setMaxValue(position.getQntyBigDecimal());
                if (this.equals(editOrDeleteButtonsPanel.getParent())) {
                    this.remove(editOrDeleteButtonsPanel);
                    this.add(inputPanel);
                }
                expand();
                break;
            case EDIT_OR_DELETE:
                productPanel.setTitle(ResBundleGoodsMetric.getString("ITEM_EDIT")); //$NON-NLS-1$
                this.remove(inputPanel);
                this.add(editOrDeleteButtonsPanel);
                expand();
                break;
            case VIEW:
                productPanel.setTitle(ResBundleGoodsMetric.getString("PRODUCT_INFORMATION")); //$NON-NLS-1$
                inputPanel.setEnabled(false);
                expand();
                break;
        }
    }

    private void collapse() {
        this.setPreferredSize(Size.middlePanel);
        productPanel.collapse();
        this.updateUI();
    }

    private void expand() {
        this.setPreferredSize(Size.mainPanel);
        productPanel.expand();
        this.updateUI();
    }

    public void setSum(BigDecimal summa) {
        inputField.setValue(summa);
    }

    public void reset() {
        inputField.setEnabled(true);
        inputField.clear();
        inputField.setWelcomeText(ResBundleGoodsMetric.getString("ENTER_QUANTITY")); //$NON-NLS-1$
        priceFormatter.setValue(product.getPrice().getPriceBigDecimal());
    }
}
