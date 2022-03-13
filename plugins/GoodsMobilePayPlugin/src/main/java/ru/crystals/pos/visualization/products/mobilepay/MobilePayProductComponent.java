package ru.crystals.pos.visualization.products.mobilepay;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.mobilepay.MobilePayProductContainer.MobilePayState;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.JLabel;


public class MobilePayProductComponent extends VisualPanel {

    private static final long serialVersionUID = 1L;
    private MobilePayProductPhonePanel phonePanel = null;
    private MobilePayProductPaymentPanel paymentPanel = null;
    private JLabel jExpand0 = null;
    private JLabel jTitle = null;

    public MobilePayProductComponent() {
        initialize();
    }

    private void initialize() {
        phonePanel = new MobilePayProductPhonePanel();
        paymentPanel = new MobilePayProductPaymentPanel();
        jExpand0 = new Empty(600, 8);
        jExpand0.setVisible(false);
        jTitle = new JLabel();
        Style.setProductTitleStyle(jTitle);
        jTitle.setVisible(false);
        this.add(jExpand0);
        this.add(jTitle);
        this.add(phonePanel);
        this.add(paymentPanel);
        phonePanel.setPhoneNumber("");
        paymentPanel.setVisible(false);
        this.setPreferredSize(Size.middlePanel);
    }

    public void setProduct(ProductEntity product) {
        // Nothing to see here
    }


    public void setPhoneNumber(String phone) {
        phonePanel.setPhoneNumber(phone);
        paymentPanel.setPhoneNumber(phone);
    }

    public void setState(MobilePayState state) {
        switch (state) {
            case ENTER_PAYMENT:
                phonePanel.setVisible(false);
                paymentPanel.setVisible(true);
                validate();
                paymentPanel.validate();
                break;
            case ENTER_PHONE:
                phonePanel.setVisible(true);
                paymentPanel.setVisible(false);
                validate();
                phonePanel.validate();
                break;
            default:
                break;
        }
    }


    public void setPayment(Double price) {
        paymentPanel.setSumma(price);
    }

    public void setItem(String item) {
        phonePanel.setItem(item);
        paymentPanel.setItem(item);
    }

    public void setName(String name) {
        phonePanel.setName(name);
        paymentPanel.setName(name);
    }

    public void collapse() {
        jExpand0.setVisible(false);
        jTitle.setVisible(false);
        paymentPanel.collapse();
        this.setPreferredSize(Size.middlePanel);
    }

    public void expand() {
        jExpand0.setVisible(true);
        jTitle.setVisible(true);
        paymentPanel.expand();
        this.setPreferredSize(Size.mainPanel);
    }

    public void setTitle(String title) {
        jTitle.setText(title);
    }

    public void setProductState(ProductState state) {
        switch (state) {
            case ADD:
            case REFUND:
                collapse();
                break;
            case EDIT:
                setTitle(ResBundleGoodsMobilePay.getString("QUANTITY_CHANGING"));
                setState(MobilePayState.ENTER_PAYMENT);
                expand();
                paymentPanel.setSumma(100.0);
                break;
            case VIEW:
                setTitle(ResBundleGoodsMobilePay.getString("PRODUCT_INFORMATION"));
                setState(MobilePayState.ENTER_PAYMENT);
                paymentPanel.getInputPanel().setDisabled(true);
                expand();
                paymentPanel.setSumma(100.0);
                break;
        }
    }

    public void setNotEditing(boolean b) {
        paymentPanel.setNotEditing(b);
    }

}
