package ru.crystals.pos.visualization.products.jewel;

import java.awt.Component;
import java.awt.Dimension;
import java.math.BigDecimal;

import javax.swing.JLabel;


import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.jewel.view.JewelAddPositionScanExciseForm;
import ru.crystals.pos.visualization.products.jewel.view.JewelDeletePositionScanExciseForm;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;


public class JewelProductComponent extends VisualPanel {
    private static final long serialVersionUID = 2L;

    private static final ScaleDimension jewelryMainPanelScale = new ScaleDimension(640, 426);
    private static final ScaleDimension jewelryMiddlePanelScale = new ScaleDimension(640, 236);

    private JewelProductPanel productPanel = null;

    private VisualPanel editOrDeleteButtonsPanel = null;
    private JLabel deleteButton;
    private JLabel editButton;

    private ProductState state = ProductState.ADD;
    private ProductEntity product;

    private JewelAddPositionScanExciseForm addPositionScanExciseForm;
    private JewelDeletePositionScanExciseForm deletePositionScanExciseForm;
    private CommonDeletePositionConfirmForm deletePositionForm;
    private String currentFormName = null;

    public JewelProductComponent() {
        initialize();
    }

    public void setAddPositionScanExciseForm(JewelAddPositionScanExciseForm addPositionScanExciseForm) {
        this.addPositionScanExciseForm = addPositionScanExciseForm;
    }

    public void setDeletePositionScanExciseForm(JewelDeletePositionScanExciseForm deletePositionScanExciseForm) {
        this.deletePositionScanExciseForm = deletePositionScanExciseForm;
    }

    public void setDeletePositionForm(CommonDeletePositionConfirmForm deletePositionForm) {
        this.deletePositionForm = deletePositionForm;
    }

    protected void initialize() {
        productPanel = new JewelProductPanel();

        editOrDeleteButtonsPanel = new VisualPanel();
        editOrDeleteButtonsPanel.setPreferredSize(Size.inputPanel);

        deleteButton = new JLabel();
        deleteButton.setText(ResBundleVisualization.getString("BUTTON_DELETE")); //$NON-NLS-1$
        Style.setButtonStyle(deleteButton);
        deleteButton.setFont(MyriadFont.getRegular(28f));
        deleteButton.setPreferredSize(new ScaleDimension(300, 42));
        editButton = new JLabel();
        editButton.setText(ResBundleGoodsJewel.getString("BUTTON_EDIT")); //$NON-NLS-1$
        Style.setButtonStyle(editButton);
        editButton.setFont(MyriadFont.getRegular(28f));
        editButton.setPreferredSize(new ScaleDimension(300, 42));

        editOrDeleteButtonsPanel.add(new Empty(640, 18));
        editOrDeleteButtonsPanel.add(deleteButton);
        editOrDeleteButtonsPanel.add(new Empty(10, 80));
        editOrDeleteButtonsPanel.add(editButton);

        this.setLayout(flowLayout);
        this.add(productPanel, null);
        this.add(new Empty(640, 22), null);
        this.setPreferredSize(Size.mainPanel);
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

    public void setProduct(ProductEntity product) {
        this.product = product;
        productPanel.setProduct(product);
    }

    public ProductState getState() {
        return state;
    }


    public void setSumma(BigDecimal summa) {
        productPanel.setSumma(summa);
    }

    public void setProductState(ProductState state) {

        this.state = state;
        switch (state) {
            case ADD:
            case REFUND:
                collapse();
                if (this.equals(editOrDeleteButtonsPanel.getParent())) {
                    this.remove(editOrDeleteButtonsPanel);
                }
                break;
            case EDIT:
                productPanel.setTitle(ResBundleGoodsJewel.getString("QUANTITY_CHANGING")); //$NON-NLS-1$
                this.remove(editOrDeleteButtonsPanel);
                expand();
                repaint();
                break;
            case EDIT_OR_DELETE:
                productPanel.setTitle(ResBundleGoodsJewel.getString("ITEM_EDIT")); //$NON-NLS-1$
                this.add(editOrDeleteButtonsPanel);
                expand();
                break;
            case EDIT_REFUND:
                productPanel.setTitle(ResBundleGoodsJewel.getString("REFUND_QUANTITY")); //$NON-NLS-1$
                this.remove(editOrDeleteButtonsPanel);
                expand();
                break;
            case VIEW:
                productPanel.setTitle(ResBundleGoodsJewel.getString("PRODUCT_INFORMATION")); //$NON-NLS-1$
                this.remove(editOrDeleteButtonsPanel);
                expand();
                break;
        }
    }

    private void collapse() {
        this.setPreferredSize(Size.middlePanel);
        productPanel.collapse();
    }

    private void expand() {
        this.setPreferredSize(Size.mainPanel);
        productPanel.expand();
    }

    public void showAddPositionScanExciseForm(PositionEntity position) {
        this.add(addPositionScanExciseForm);
        productPanel.setVisible(false);
        addPositionScanExciseForm.showForm(product, position);
        addPositionScanExciseForm.setVisible(true);
        addPositionScanExciseForm.setPreferredSize(Size.middlePanel);
        currentFormName = addPositionScanExciseForm.getClass().getName();
    }

    public void showDeletePositionScanExciseForm(PositionEntity position) {
        this.add(deletePositionScanExciseForm);
        deletePositionForm.setVisible(false);
        productPanel.setVisible(false);
        deletePositionScanExciseForm.showForm(product, position);
        deletePositionScanExciseForm.setVisible(true);
        deletePositionScanExciseForm.setPreferredSize(getDeletePanelSize());
        currentFormName = deletePositionScanExciseForm.getClass().getName();
    }

    public void showDeletePositionForm(PositionEntity position) {
        this.remove(editOrDeleteButtonsPanel);
        this.add(deletePositionForm);
        productPanel.setVisible(false);
        deletePositionForm.showForm(product, position);
        deletePositionForm.setVisible(true);
        deletePositionForm.setPreferredSize(getDeletePanelSize());
        currentFormName = deletePositionForm.getClass().getName();
    }

    public boolean isValidExcise() {
        return addPositionScanExciseForm.isValidExcise();
    }

    public void clear() {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        addPositionScanExciseForm.clear();
        this.remove(addPositionScanExciseForm);
        this.remove(deletePositionScanExciseForm);
        this.remove(deletePositionForm);
        productPanel.setVisible(true);
        currentFormName = productPanel.getClass().getName();
        repaint();
    }

    public boolean isCurrentForm(Component component) {
        if (component == null) {
            return false;
        }

        return component.getClass().getName().equals(currentFormName);
    }

    private Dimension getDeletePanelSize() {
        switch (getState()) {
            case DELETE:
                return jewelryMainPanelScale;
            case QUICK_DELETE:
                return jewelryMiddlePanelScale;
            default:
                return Size.middlePanel;
        }
    }
}
