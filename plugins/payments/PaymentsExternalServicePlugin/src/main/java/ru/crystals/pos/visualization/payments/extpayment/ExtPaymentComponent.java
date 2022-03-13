package ru.crystals.pos.visualization.payments.extpayment;

import ru.crystals.pos.visualization.payments.PaymentComponent;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * @author dalex
 */
public class ExtPaymentComponent extends JPanel implements PaymentComponent {

    private Component currentComponent;

    public ExtPaymentComponent() {
        super(new BorderLayout());
    }

    @Override
    public Component add(Component comp) {
        super.removeAll();
        this.currentComponent = comp;
        return super.add(comp);
    }

    public Component getCurrentComponent() {
        return currentComponent;
    }

    private void log(String text) {
        System.out.println("PANEL LOG: " + text);
    }

    @Override
    public void setPaid(Long paid) {
        log("setPaid(Long) " + paid);
    }

    @Override
    public void setPaid(String paid) {
        log("setPaid(String) " + paid);
    }

    @Override
    public void setPayment(Long payment) {
        log("setPayment " + payment);
    }

    @Override
    public void setChange(boolean change) {
        log("setChange " + change);
    }

    @Override
    public void setSumma(Long summa) {
        log("setSumma " + summa);
    }

    @Override
    public void setPaymentType(String paymentType) {
        log("setPaymentType " + paymentType);
    }

    @Override
    public void setOperation(String operation) {
        log("setOperation " + operation);
    }

    @Override
    public void setSummaLabel(String label) {
        log("setSummaLabel " + label);
    }

    @Override
    public void setCurrency(String symbol, String name) {
        log("setCurrency " + symbol + " " + name);
    }
}
