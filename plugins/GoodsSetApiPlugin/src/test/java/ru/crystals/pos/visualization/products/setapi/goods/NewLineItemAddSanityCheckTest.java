package ru.crystals.pos.visualization.products.setapi.goods;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import org.junit.Test;
import ru.crystals.pos.api.plugin.goods.NewLineItem;

public class NewLineItemAddSanityCheckTest {

    @Test
    public void testIsValid() {
        assertFalse(NewLineItemAddSanityChecker.isValid(null));
        NewLineItem item = new NewLineItem("marking", "name", BigDecimal.valueOf(4.25), 1000, 0.f);
        assertTrue(NewLineItemAddSanityChecker.isValid(item));

        item.setPrice(null);
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
        item.setPrice(BigDecimal.valueOf(-100));
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
        item.setPrice(BigDecimal.ZERO);
        assertTrue(NewLineItemAddSanityChecker.isValid(item));
        item.setPrice(BigDecimal.valueOf(4.25));

        item.setName("");
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
        item.setName("Сок куриный с мякотью");

        item.setMarking("");
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
        item.setMarking("4815162342");

        item.setQuantity(0);
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
        item.setQuantity(-1);
        assertFalse(NewLineItemAddSanityChecker.isValid(item));
    }

}
