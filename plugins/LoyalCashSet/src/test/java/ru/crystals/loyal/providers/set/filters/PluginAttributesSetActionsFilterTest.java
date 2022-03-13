package ru.crystals.loyal.providers.set.filters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.enums.ActionPluginAttributes;
import ru.crystals.discounts.interfaces.ActionPluginAttributable;
import ru.crystals.discounts.interfaces.IActionPlugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Тест фильтра РА, что содержат плагин результата с указанным атрибутом
 */
public class PluginAttributesSetActionsFilterTest {

    private PluginAttributesSetActionsFilter filter = new PluginAttributesSetActionsFilter();

    @Before
    public void before() {
        filter.setAttribute(ActionPluginAttributes.SALES_TAX_DISCOUNT);
    }

    @Test
    public void applyTest_null() {
        Assert.assertFalse(filter.apply(null));
    }

    @Test
    public void applyTest() {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        IActionPlugin plugin1 = mock(IActionPlugin.class);
        action.getDeserializedPlugins().add(plugin1);
        IActionPlugin plugin2 = mock(IActionPlugin.class, withSettings().extraInterfaces(ActionPluginAttributable.class));
        when(((ActionPluginAttributable) plugin2).getPluginAttributes())
                .thenReturn(new ActionPluginAttributes[] {ActionPluginAttributes.SALES_TAX_DISCOUNT});
        action.getDeserializedPlugins().add(plugin2);
        Assert.assertTrue(filter.apply(action));
    }

    @Test
    public void applyTest_false() {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        IActionPlugin plugin1 = mock(IActionPlugin.class);
        action.getDeserializedPlugins().add(plugin1);
        IActionPlugin plugin2 = mock(IActionPlugin.class, withSettings().extraInterfaces(ActionPluginAttributable.class));
        when(((ActionPluginAttributable) plugin2).getPluginAttributes())
                .thenReturn(new ActionPluginAttributes[] {ActionPluginAttributes.PERCENT});
        action.getDeserializedPlugins().add(plugin2);
        Assert.assertFalse(filter.apply(action));
    }
}
