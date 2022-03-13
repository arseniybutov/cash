package ru.crystals.pos.fiscalprinter.pirit.core.font;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextStyle;
import ru.crystals.pos.fiscalprinter.pirit.core.PiritPluginConfig;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConfig;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PiritFontManagerTest {

    private static final int FONT_0 = 0;
    private static final int FONT_0_DH = 0b0001_0000;
    private static final int FONT_0_DW = 0b0010_0000;
    private static final int FONT_0_UNDERLINE = 0b1000_0000;

    private static final int FONT_1_SMALL = 1;
    private static final int FONT_2_BOLD = 2;
    private static final int FONT_3_BOLD_SMALL = 3;

    private static final int DEFAULT_0 = 55;
    private static final int DEFAULT_0_DH = 53;
    private static final int DEFAULT_0_DW = 50;
    private static final FontConfiguration BASIC_FONT_CONF = new FontConfiguration(true, of(FONT_0, DEFAULT_0), FONT_0, FONT_0, input -> FONT_0);
    private static final int REQUESTED_0 = 65;
    private static final int REQUESTED_0_DH = 63;
    private static final int REQUESTED_0_DW = 60;
    private static final int CONFIGURED_0 = 75;
    private static final int CONFIGURED_0_DH = 73;
    private static final int CONFIGURED_0_DW = 70;

    private static final int DEFAULT_1 = 58;
    private static final int DEFAULT_2 = 46;
    private static final int DEFAULT_3 = 36;
    private static final int CONFIGURED_1 = 80;


    private static final int CUSTOM_DESIGN_LOWEST_NUMBER = 16;
    private static final int STANDARD_DESIGN_MAX_NUMBER = CUSTOM_DESIGN_LOWEST_NUMBER - 1;

    @Mock
    private PiritConfig piritConfig;

    private final PiritPluginConfig config = new PiritPluginConfig();

    private final PiritFontManager manager = new PiritFontManager();

    @Before
    public void setUp() {
        when(piritConfig.getMaxCharCount(anyInt())).thenReturn(Optional.empty());
    }

    @Test
    public void shouldUseDefaultWhenNoConfiguredAndCustomDesignUsed() {
        configure(BASIC_FONT_CONF, CUSTOM_DESIGN_LOWEST_NUMBER);

        verifyDefaultUsed();
        verify(piritConfig, never()).getMaxCharCount(FONT_0);
    }

    @Test
    public void shouldUseDefaultWhenNoConfiguredAndCannotBeRequested() {
        configure(new FontConfiguration(false, of(FONT_0, DEFAULT_0), FONT_0, FONT_0, input -> FONT_0));

        verifyDefaultUsed();
        verify(piritConfig, never()).getMaxCharCount(FONT_0);
    }

    @Test
    public void shouldUseDefaultWhenNoConfiguredAndEmptyRequested() {
        configure();

        verifyDefaultUsed();
        verify(piritConfig, times(1)).getMaxCharCount(FONT_0);
    }

    private void verifyDefaultUsed() {
        assertEquals(DEFAULT_0, manager.getMaxCharRow(FONT_0));
    }

    @Test
    public void shouldUseConfigured() {
        config.setMaxCharRowMap(of(FONT_0, CONFIGURED_0));
        configure();

        final int actual = manager.getMaxCharRow(FONT_0);

        assertEquals(CONFIGURED_0, actual);
        verify(piritConfig, never()).getMaxCharCount(FONT_0);
    }

    @Test
    public void shouldUseRequested() {
        when(piritConfig.getMaxCharCount(FONT_0)).thenReturn(Optional.of(REQUESTED_0));
        configure();

        final int actual = manager.getMaxCharRow(FONT_0);

        assertEquals(REQUESTED_0, actual);
        verify(piritConfig, times(1)).getMaxCharCount(FONT_0);
    }

    @Test
    public void shouldUseRequestedForAttributes() {
        when(piritConfig.getMaxCharCount(FONT_0_DH)).thenReturn(Optional.of(REQUESTED_0_DH));
        when(piritConfig.getMaxCharCount(FONT_0_DW)).thenReturn(Optional.of(REQUESTED_0_DW));
        configure();

        assertEquals(REQUESTED_0_DH, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(REQUESTED_0_DW, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void shouldUseConfiguredForAttributes() {
        config.setMaxCharRowMap(of(
                FONT_0_DW, CONFIGURED_0_DW,
                FONT_0_DH, CONFIGURED_0_DH
        ));
        configure();

        assertEquals(CONFIGURED_0_DH, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(CONFIGURED_0_DW, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void shouldUseDefaultForAttributes() {
        configure(new FontConfiguration(false, of(
                FONT_0, DEFAULT_0,
                FONT_0_DH, DEFAULT_0_DH,
                FONT_0_DW, DEFAULT_0_DW
        ), FONT_0, FONT_0, input -> FONT_0));

        assertEquals(DEFAULT_0_DH, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(DEFAULT_0_DW, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void shouldUseCalculatedFromDefaultBase() {
        configure();

        assertEquals(DEFAULT_0, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(DEFAULT_0 / 2, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void shouldUseCalculatedFromConfiguredBase() {
        config.setMaxCharRowMap(of(
                FONT_0, CONFIGURED_0
        ));

        when(piritConfig.getMaxCharCount(FONT_0_DH)).thenReturn(Optional.of(REQUESTED_0_DH));
        when(piritConfig.getMaxCharCount(FONT_0_DW)).thenReturn(Optional.of(REQUESTED_0_DW));

        configure();
        assertEquals(CONFIGURED_0, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(CONFIGURED_0 / 2, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void shouldUseCalculatedFromRequestedBase() {
        when(piritConfig.getMaxCharCount(FONT_0)).thenReturn(Optional.of(REQUESTED_0));

        configure();
        assertEquals(REQUESTED_0, manager.getMaxCharRow(FONT_0_DH));
        assertEquals(REQUESTED_0 / 2, manager.getMaxCharRow(FONT_0_DW));
    }

    @Test
    public void getMaxCharByFont() {
        config.setMaxCharRowMap(of(
                FONT_0, CONFIGURED_0,
                FONT_1_SMALL, CONFIGURED_1
        ));

        configure();

        final Map<Font, Integer> expected = of(
                Font.NORMAL, CONFIGURED_0,
                Font.SMALL, CONFIGURED_1,
                Font.DOUBLEHEIGHT, CONFIGURED_0,
                Font.DOUBLEWIDTH, CONFIGURED_0 / 2,
                Font.UNDERLINE, CONFIGURED_0
        );

        final Map<Font, Integer> actual = Stream.of(Font.values())
                .collect(Collectors.toMap(Function.identity(), manager::getMaxCharRow, (a, b) -> a));

        assertEquals(expected, actual);
    }

    @Test
    public void getFontAttributeTest() {
        configure();

        assertEquals(FONT_0, getFontAttribute(Font.NORMAL));
        assertEquals(FONT_1_SMALL, getFontAttribute(Font.SMALL));
        assertEquals(FONT_0_DH, getFontAttribute(Font.DOUBLEHEIGHT));
        assertEquals(FONT_0_DW, getFontAttribute(Font.DOUBLEWIDTH));
        assertEquals(FONT_0_UNDERLINE, getFontAttribute(Font.UNDERLINE));
        assertEquals(0, getFontAttribute(0));
        assertEquals(2, getFontAttribute(2));
        assertEquals(40, getFontAttribute(40));
    }

    @Test
    public void getTextAttributes() {
        configure(new FontConfiguration(false, of(
                FONT_0, DEFAULT_0,
                FONT_1_SMALL, DEFAULT_1
        ), FONT_2_BOLD, FONT_3_BOLD_SMALL, input -> input));

        assertEquals(FONT_0, getTextAttributes(TextSize.NORMAL, TextStyle.NORMAL));
        assertEquals(FONT_2_BOLD, getTextAttributes(TextSize.NORMAL, TextStyle.BOLD));
        assertEquals(FONT_0_UNDERLINE, getTextAttributes(TextSize.NORMAL, TextStyle.LINE_TEXT));
        assertEquals(FONT_0, getTextAttributes(TextSize.NORMAL, TextStyle.ITALIC));

        assertEquals(FONT_1_SMALL, getTextAttributes(TextSize.SMALL, TextStyle.NORMAL));
        assertEquals(FONT_3_BOLD_SMALL, getTextAttributes(TextSize.SMALL, TextStyle.BOLD));
        assertEquals(FONT_1_SMALL | FONT_0_UNDERLINE, getTextAttributes(TextSize.SMALL, TextStyle.LINE_TEXT));
        assertEquals(FONT_1_SMALL, getTextAttributes(TextSize.SMALL, TextStyle.ITALIC));

        assertEquals(FONT_0_DH, getTextAttributes(TextSize.DOUBLE_HEIGHT, TextStyle.NORMAL));
        assertEquals(FONT_0_DH | FONT_2_BOLD, getTextAttributes(TextSize.DOUBLE_HEIGHT, TextStyle.BOLD));
        assertEquals(FONT_0_DH | FONT_0_UNDERLINE, getTextAttributes(TextSize.DOUBLE_HEIGHT, TextStyle.LINE_TEXT));
        assertEquals(FONT_0_DH, getTextAttributes(TextSize.DOUBLE_HEIGHT, TextStyle.ITALIC));

        assertEquals(FONT_0_DW, getTextAttributes(TextSize.DOUBLE_WIDTH, TextStyle.NORMAL));
        assertEquals(FONT_0_DW | FONT_2_BOLD, getTextAttributes(TextSize.DOUBLE_WIDTH, TextStyle.BOLD));
        assertEquals(FONT_0_DW | FONT_0_UNDERLINE, getTextAttributes(TextSize.DOUBLE_WIDTH, TextStyle.LINE_TEXT));
        assertEquals(FONT_0_DW, getTextAttributes(TextSize.DOUBLE_WIDTH, TextStyle.ITALIC));

        assertEquals(FONT_0_DH | FONT_0_DW, getTextAttributes(TextSize.FULL_DOUBLE, TextStyle.NORMAL));
        assertEquals(FONT_0_DH | FONT_0_DW | FONT_2_BOLD, getTextAttributes(TextSize.FULL_DOUBLE, TextStyle.BOLD));
        assertEquals(FONT_0_DH | FONT_0_DW | FONT_0_UNDERLINE, getTextAttributes(TextSize.FULL_DOUBLE, TextStyle.LINE_TEXT));
        assertEquals(FONT_0_DH | FONT_0_DW, getTextAttributes(TextSize.FULL_DOUBLE, TextStyle.ITALIC));
    }

    @Test
    public void getTextAttributesConcreteFont() {
        configure();

        final Text text = new Text("", TextSize.NORMAL, TextStyle.NORMAL);
        text.setConcreteFont(40);

        assertEquals(40, manager.getTextAttributes(text));
    }

    @Test
    public void getTextAttributesBoldNoSmallBold() {
        configure(new FontConfiguration(false, of(
                FONT_0, DEFAULT_0,
                FONT_1_SMALL, DEFAULT_1,
                FONT_2_BOLD, DEFAULT_2,
                FONT_3_BOLD_SMALL, DEFAULT_3
        ), FONT_2_BOLD, FONT_3_BOLD_SMALL, input -> input));


        assertEquals(FONT_2_BOLD, getTextAttributes(TextSize.NORMAL, TextStyle.BOLD));
    }

    @Test
    public void getMaxCharRowByTextRow() {
        configure(new FontConfiguration(false, of(
                FONT_0, DEFAULT_0,
                FONT_1_SMALL, DEFAULT_1,
                FONT_2_BOLD, DEFAULT_2,
                FONT_3_BOLD_SMALL, DEFAULT_3
        ), FONT_2_BOLD, FONT_3_BOLD_SMALL, input -> input));

        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.NORMAL, TextStyle.NORMAL));
        assertEquals(DEFAULT_2, getMaxCharRow(TextSize.NORMAL, TextStyle.BOLD));
        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.NORMAL, TextStyle.LINE_TEXT));
        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.NORMAL, TextStyle.ITALIC));

        assertEquals(DEFAULT_1, getMaxCharRow(TextSize.SMALL, TextStyle.NORMAL));
        assertEquals(DEFAULT_3, getMaxCharRow(TextSize.SMALL, TextStyle.BOLD));
        assertEquals(DEFAULT_1, getMaxCharRow(TextSize.SMALL, TextStyle.LINE_TEXT));
        assertEquals(DEFAULT_1, getMaxCharRow(TextSize.SMALL, TextStyle.ITALIC));

        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.DOUBLE_HEIGHT, TextStyle.NORMAL));
        assertEquals(DEFAULT_2, getMaxCharRow(TextSize.DOUBLE_HEIGHT, TextStyle.BOLD));
        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.DOUBLE_HEIGHT, TextStyle.LINE_TEXT));
        assertEquals(DEFAULT_0, getMaxCharRow(TextSize.DOUBLE_HEIGHT, TextStyle.ITALIC));

        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.DOUBLE_WIDTH, TextStyle.NORMAL));
        assertEquals(DEFAULT_2 / 2, getMaxCharRow(TextSize.DOUBLE_WIDTH, TextStyle.BOLD));
        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.DOUBLE_WIDTH, TextStyle.LINE_TEXT));
        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.DOUBLE_WIDTH, TextStyle.ITALIC));

        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.FULL_DOUBLE, TextStyle.NORMAL));
        assertEquals(DEFAULT_2 / 2, getMaxCharRow(TextSize.FULL_DOUBLE, TextStyle.BOLD));
        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.FULL_DOUBLE, TextStyle.LINE_TEXT));
        assertEquals(DEFAULT_0 / 2, getMaxCharRow(TextSize.FULL_DOUBLE, TextStyle.ITALIC));
    }

    private int getMaxCharRow(TextSize size, TextStyle style) {
        return manager.getMaxCharRow(new Text("", size, style));
    }

    private int getTextAttributes(TextSize size, TextStyle style) {
        return manager.getTextAttributes(new Text("", size, style));
    }

    private int getFontAttribute(Font font) {
        return manager.getFontAttribute(new FontLine("", font, null));
    }

    private int getFontAttribute(int concreteFont) {
        return manager.getFontAttribute(new FontLine("", null, concreteFont));
    }

    private void configure(FontConfiguration fontConfiguration, int designNumber) {
        manager.configure(piritConfig, config, fontConfiguration, designNumber);
    }

    private void configure(FontConfiguration fontConfiguration) {
        manager.configure(piritConfig, config, fontConfiguration, STANDARD_DESIGN_MAX_NUMBER);
    }

    private void configure() {
        manager.configure(piritConfig, config, BASIC_FONT_CONF, STANDARD_DESIGN_MAX_NUMBER);
    }

}