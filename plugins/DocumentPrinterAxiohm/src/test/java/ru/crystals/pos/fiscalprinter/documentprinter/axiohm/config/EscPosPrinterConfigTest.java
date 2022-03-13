package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import org.junit.Test;
import ru.crystals.pos.configurator.core.ConfigDeserializer;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;

import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

public class EscPosPrinterConfigTest {

    @Test
    public void testConfig() throws Exception {
        final URL res = this.getClass().getClassLoader().getResource("test-printer-config.xml");

        final EscPosPrinterConfig config = new ConfigDeserializer().convert(res, EscPosPrinterConfig.class);

        assertThat(config.getMaxCharRowMap())
                .hasSize(2)
                .includes(
                        entry(Font.NORMAL, 44),
                        entry(Font.SMALL, 77)
                );

        assertThat(config.getCommands())
                .hasSize(4)
                .includes(
                        entry(PrinterCommandType.STATUS, ByteSequence.of(0x1D, (byte) 0xD3, 0x4F)),
                        entry(PrinterCommandType.FULL_CUT, ByteSequence.of(0x1A)),
                        entry(PrinterCommandType.DRAWER_STATUS, ByteSequence.of(0x11, (byte) 0xDD)),
                        entry(PrinterCommandType.CUT, ByteSequence.of(0x19))
                );

        assertThat(config.getPrinterStatusMap())
                .hasSize(3)
                .includes(
                        entry(ByteSequence.of(0x1A, 0x21), StatusFP.Status.OPEN_COVER),
                        entry(ByteSequence.of(0x2F, 0x22), StatusFP.Status.END_PAPER),
                        entry(ByteSequence.of(0x3E, 0x23), StatusFP.Status.FATAL)
                );

        assertThat(config.getDrawerStatusMap())
                .hasSize(1)
                .includes(entry(false, ByteSequence.of(0x04)));

        assertThat(config.getMissingSymbolsReplacement())
                .hasSize(2)
                .includes(
                        entry('«', '"'),
                        entry('»', '"')
                );
    }

}