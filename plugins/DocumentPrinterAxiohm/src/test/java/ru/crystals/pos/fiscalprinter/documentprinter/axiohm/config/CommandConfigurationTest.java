package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandConfigurationTest {

    @Test
    public void defaultConfigurationTest() {
        EscPosPrinterConfig emptyConfig = new EscPosPrinterConfig();
        EscPosPrinterConfig defaultConfig = new EscPosPrinterConfig();
        defaultConfig.setPrinterEncoding("cp866");
        defaultConfig.setFeedLength(10);
        defaultConfig.setDrawerStatusMap(ImmutableMap.of(
                false, ByteSequence.of(0x1B, 0x1D)
        ));
        Map<PrinterCommandType, ByteSequence> commands = new HashMap<>();
        commands.put(PrinterCommandType.FULL_CUT, ByteSequence.of(0x19));
        commands.put(PrinterCommandType.CUT, ByteSequence.of(0x1A));
        commands.put(PrinterCommandType.STATUS, ByteSequence.of(0x1D, 0x72, 0x31));
        commands.put(PrinterCommandType.DRAWER_STATUS, ByteSequence.of(0x1D, 0x72, 0x32));
        commands.put(PrinterCommandType.FEED, ByteSequence.of(0x15));
        commands.put(PrinterCommandType.INIT_CHARSET, ByteSequence.of(0x1B, 0x52, 0x07));
        defaultConfig.setCommands(commands);

        defaultConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(0b0001_0000, 0b0001_0000), StatusFP.Status.END_PAPER
        ));
        defaultConfig.setMissingSymbolsReplacement(ImmutableMap.of(
                '«', '"',
                '»', '"'
        ));


        final CommandConfiguration cfg = new CommandConfiguration(emptyConfig, defaultConfig);

        assertEquals(Charset.forName("cp866"), cfg.getEncoding());
        assertEquals("\"quotes\"", cfg.replaceMissingSymbols("«quotes»"));

        final StatusCommand statusCommand = cfg.getStatusCommand();
        assertArrayEquals(new byte[] {0x1D, 0x72, 0x31}, statusCommand.getCommand());
        assertEquals(2, statusCommand.getResponseLength());
        assertEquals(StatusFP.Status.END_PAPER, statusCommand.parseResult(new byte[] {0b0001_0000, 0b0001_0000}));
        assertEquals(StatusFP.Status.NORMAL, statusCommand.parseResult(new byte[] {0b0000_0000, 0b0001_0000}));

        final DrawerStatusCommand drawerStatusCommand = cfg.getDrawerStatusCommand();
        assertArrayEquals(new byte[] {0x1D, 0x72, 0x32}, drawerStatusCommand.getCommand());
        assertEquals(2, drawerStatusCommand.getResponseLength());
        assertFalse(drawerStatusCommand.parseResult(new byte[] {0x1B, 0x1D}));
        assertTrue(drawerStatusCommand.parseResult(new byte[] {0x10, 0x1F}));

        assertArrayEquals(new byte[] {0x1A}, cfg.getCutCommand().getCommand());
        assertArrayEquals(new byte[] {0x19}, cfg.getFullCutCommand().getCommand());
        assertArrayEquals(new byte[] {0x1B, 0x52, 0x07}, cfg.getInitCharsetCommand().getCommand());
        assertArrayEquals(new byte[] {0x15, 0x0A}, cfg.getFeedCommand().getCommand());
    }

    @Test
    public void mergeConfigsTest() {
        EscPosPrinterConfig defaultConfig = new EscPosPrinterConfig();
        defaultConfig.setPrinterEncoding("cp866");
        defaultConfig.setFeedLength(10);
        defaultConfig.setDrawerStatusMap(ImmutableMap.of(
                false, ByteSequence.of(0x1B, 0x1D)
        ));
        Map<PrinterCommandType, ByteSequence> commands = new HashMap<>();
        commands.put(PrinterCommandType.CUT, ByteSequence.of(0x1A));
        commands.put(PrinterCommandType.STATUS, ByteSequence.of(0x1D, 0x72, 0x31));
        commands.put(PrinterCommandType.DRAWER_STATUS, ByteSequence.of(0x1D, 0x72, 0x32));
        commands.put(PrinterCommandType.FEED, ByteSequence.of(0x15));
        commands.put(PrinterCommandType.INIT_CHARSET, ByteSequence.of(0x1B, 0x52, 0x07));
        defaultConfig.setCommands(commands);

        defaultConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(0b0001_0000, 0b0001_0000), StatusFP.Status.END_PAPER
        ));
        defaultConfig.setMissingSymbolsReplacement(ImmutableMap.of(
                '«', '"',
                '»', '"'
        ));


        EscPosPrinterConfig customConfig = new EscPosPrinterConfig();
        customConfig.setPrinterEncoding("cp1251");
        customConfig.setFeedLength(5);
        customConfig.setCommands(ImmutableMap.of(PrinterCommandType.FEED, ByteSequence.of(0x16)));
        customConfig.setMissingSymbolsReplacement(ImmutableMap.of(
                '«', '*',
                '%', '$'
        ));
        customConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(0b0001_0000), StatusFP.Status.END_PAPER
        ));
        customConfig.setDrawerStatusMap(ImmutableMap.of(
                false, ByteSequence.of(0x01)
        ));


        final CommandConfiguration cfg = new CommandConfiguration(customConfig, defaultConfig);

        assertEquals(Charset.forName("cp1251"), cfg.getEncoding());
        assertEquals("*quo$tes\"", cfg.replaceMissingSymbols("«quo%tes»"));

        final StatusCommand statusCommand = cfg.getStatusCommand();
        assertArrayEquals(new byte[] {0x1D, 0x72, 0x31}, statusCommand.getCommand());
        assertEquals(1, statusCommand.getResponseLength());
        assertEquals(StatusFP.Status.END_PAPER, statusCommand.parseResult(new byte[] {0b0001_0000}));
        assertEquals(StatusFP.Status.NORMAL, statusCommand.parseResult(new byte[] {0b0000_0000}));

        final DrawerStatusCommand drawerStatusCommand = cfg.getDrawerStatusCommand();
        assertArrayEquals(new byte[] {0x1D, 0x72, 0x32}, drawerStatusCommand.getCommand());
        assertEquals(1, drawerStatusCommand.getResponseLength());
        assertFalse(drawerStatusCommand.parseResult(new byte[] {0x01}));
        assertTrue(drawerStatusCommand.parseResult(new byte[] {0x00}));

        assertArrayEquals(new byte[] {0x1A}, cfg.getCutCommand().getCommand());
        assertArrayEquals(new byte[] {0x1A}, cfg.getFullCutCommand().getCommand());
        assertArrayEquals(new byte[] {0x1B, 0x52, 0x07}, cfg.getInitCharsetCommand().getCommand());
        assertArrayEquals(new byte[] {0x16, 0x05}, cfg.getFeedCommand().getCommand());

    }
}