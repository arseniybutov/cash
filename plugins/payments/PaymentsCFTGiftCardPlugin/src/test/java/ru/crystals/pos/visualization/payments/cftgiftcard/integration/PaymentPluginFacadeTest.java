package ru.crystals.pos.visualization.payments.cftgiftcard.integration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: myaichnikov
 * Date: 9/30/13
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentPluginFacadeTest {
    @Test
    public void shouldinitMVCmodelOnCreation() {
        CFTGiftCardPaymentPluginAdapter adapter = new CFTGiftCardPaymentPluginAdapter();

        assertNotNull(adapter.getController());
        assertNotNull(adapter.getModel());
        assertNotNull(adapter.getView());
        assertTrue(adapter.getModel().getModelListeners().contains(adapter.getView()));
        assertEquals(adapter.getView().getController(), adapter.getController());
        assertEquals(adapter.getController().getAdapter(), adapter);
    }

    @Test
    public void assertViewParentExists() {
        CFTGiftCardPaymentPluginAdapter adapter = new CFTGiftCardPaymentPluginAdapter();
        assertNotNull(adapter.getView().getParent());
    }
}
