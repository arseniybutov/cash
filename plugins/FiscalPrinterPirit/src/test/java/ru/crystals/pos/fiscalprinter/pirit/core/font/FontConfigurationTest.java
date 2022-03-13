package ru.crystals.pos.fiscalprinter.pirit.core.font;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FontConfigurationTest {

    @Test
    public void pirit2FConfiguration() {
        final FontConfiguration config = FontConfiguration.PIRIT_2F;

        assertEquals(new ImmutableMap.Builder<Integer, Integer>()
                .put(0, 44)
                .put(1, 57)
                .put(2, 44)
                .put(3, 57)
                .put(4, 72)
                .put(5, 24)
                .put(6, 24)
                .put(7, 44)
                .put(8, 57)
                .build(), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertTrue(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(7));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(9));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(10));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(12));
    }

    @Test
    public void pirit2RBConfiguration() {
        final FontConfiguration config = FontConfiguration.PIRIT_RB;

        assertEquals(new ImmutableMap.Builder<Integer, Integer>()
                .put(0, 44)
                .put(1, 57)
                .put(2, 44)
                .put(3, 57)
                .put(4, 72)
                .put(5, 24)
                .put(6, 24)
                .put(7, 44)
                .put(8, 57)
                .build(), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertFalse(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(7));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(9));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(10));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(12));
    }

    @Test
    public void pirit2FNarrowConfiguration() {
        final FontConfiguration config = FontConfiguration.PIRIT_2F_NARROW;

        assertEquals(new ImmutableMap.Builder<Integer, Integer>()
                .put(0, 30)
                .put(1, 40)
                .put(2, 30)
                .put(3, 40)
                .put(4, 50)
                .put(5, 16)
                .put(6, 16)
                .put(7, 30)
                .put(8, 40)
                .build(), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertTrue(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(7));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(9));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(10));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(12));
    }

    @Test
    public void pirit2RBNarrowConfiguration() {
        final FontConfiguration config = FontConfiguration.PIRIT_RB_NARROW;

        assertEquals(new ImmutableMap.Builder<Integer, Integer>()
                .put(0, 30)
                .put(1, 40)
                .put(2, 30)
                .put(3, 40)
                .put(4, 50)
                .put(5, 16)
                .put(6, 16)
                .put(7, 30)
                .put(8, 40)
                .build(), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertFalse(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(7));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(9));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(10));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(12));
    }

    @Test
    public void pirit1FConfiguration() {
        final FontConfiguration config = FontConfiguration.PIRIT_1F;

        assertEquals(of(
                0, 43,
                1, 55,
                8, 43
        ), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertFalse(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(2));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(4));
        assertEquals(1, (int) config.getDefaultNumberGetter().apply(3));
        assertEquals(1, (int) config.getDefaultNumberGetter().apply(5));
    }

    @Test
    public void vikiPrintConfiguration() {
        final FontConfiguration config = FontConfiguration.VIKIPRINT_80;

        assertEquals(of(
                0, 48,
                1, 64
        ), config.getDefaultFonts());

        assertEquals(0b0010_0000, config.getBoldNormalFontNumber());
        assertEquals(1 | 0b0010_0000, config.getBoldSmallFontNumber());

        assertTrue(config.isCanBeRequested());

        assertEquals(0, (int) config.getDefaultNumberGetter().apply(2));
        assertEquals(0, (int) config.getDefaultNumberGetter().apply(4));
        assertEquals(1, (int) config.getDefaultNumberGetter().apply(3));
        assertEquals(1, (int) config.getDefaultNumberGetter().apply(5));
    }
}