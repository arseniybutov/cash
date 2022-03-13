package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.components.Container;
import ru.crystals.pos.visualization.eventlisteners.CloseProductComponentEventListener;
import ru.crystals.pos.visualization.products.ProductContainer;

import java.awt.BorderLayout;
import java.math.BigDecimal;

public class KitComponentContainer extends Container implements CloseProductComponentEventListener {

    private ComponentVisualPanel visualPanel;
    private Container currentContainer;

    private ProductEntity currentComponent;

    private KitComponentListener kitComponentListener;
    private boolean needShow = true;

    /**
     * Кол-во комплектов для добавления, для каждого компонента его кол-во должно быть умножено на это число
     */
    private BigDecimal kitQuantity;

    public KitComponentContainer(KitComponentListener kitComponentListener) {
        this.kitComponentListener = kitComponentListener;
    }

    @Override
    public ComponentVisualPanel getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new ComponentVisualPanel();
        }
        return visualPanel;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }


    public void addKitComponent(ProductEntity component) {
        needShow = true;
        currentComponent = component;
        BarcodeEntity componentBarcode = currentComponent.getBarCode();
        componentBarcode.setCount(kitQuantity.longValue() * componentBarcode.getCount());
        ProductContainer container = (ProductContainer) Factory.getInstance().getProductTypes().get(component.getDiscriminator()).getProductContainer();
        if (container != null && component.getProductConfig().isSaleAllowed()) {
            if (container.isAvailable()) {
                if (!container.getCloseListeners().contains(this)) {
                    container.addCloseListener(this);
                }
                container.setPositionInsertType(InsertType.SCANNER);
                container.setProductState(ProductContainer.ProductState.ADD);
                container.setProduct(currentComponent);
                container.setPosition(null);
            } else {
                Factory.getInstance().showMessage(ResBundleVisualization.getString("PRODUCT_SALE_NOT_POSSIBLE"));
            }
        } else { // продажа товара запрещена
            Factory.getTechProcessImpl().error(ResBundleVisualization.getString("PRODUCT_SALE_PROHIBITED"));
        }
        setCurrentContainer((Container) container, BorderLayout.CENTER);
    }

    public void setCurrentContainer(final Container container, String position) {
        if (currentContainer != null) {
            currentContainer.getVisualPanel().setVisible(false);
        }
        getVisualPanel().addVisualPanel(container.getVisualPanel(), position);
        currentContainer = container;
        currentContainer.getVisualPanel().setVisible(true);
        if (needShow) {
            kitComponentListener.showPluginView();
        }
    }

    @Override
    public boolean closeProductComponent(boolean positionAdded) {
        needShow = false;
        kitComponentListener.hidePluginView();
        kitComponentListener.addComponentResult(positionAdded);
        return false;
    }

    public void processComponent(ProductEntity component) {
        addKitComponent(component);
    }

    public void setKitQuantity(BigDecimal kitQuantity) {
        this.kitQuantity = kitQuantity;
    }
}
