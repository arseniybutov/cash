package ru.crystals.pos.visualization.products.giftcard.product.integration;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.product.controller.GiftCardPluginController;
import ru.crystals.pos.visualization.products.giftcard.ret.ValidateReturnlGiftCardForm;

import java.util.ArrayList;

public class GiftCardPluginAdapterTest {
    /**
     * Проверяем, что произвольный возврат ПК запрещен
     */
    @Test
    public void commonReturnGiftCardPossibleTest() {
        Factory instance = Factory.getInstance();
        instance.setCards(new ArrayList<>());
        GiftCardPluginAdapter adapter = new GiftCardPluginAdapter(Mockito.mock(ValidateReturnlGiftCardForm.class), Mockito.mock(GiftCardPluginController.class));
        Assert.assertFalse(adapter.isReturnPossible(null));
    }
}
