package ru.crystals.pos.fiscalprinter.pirit.core;

import org.fest.assertions.Assertions;
import org.fest.assertions.MapAssert;
import org.junit.Test;
import ru.crystals.pos.configurator.core.ConfigDeserializer;

import java.net.URL;

public class PiritPluginConfigTest {

    @Test
    public void testTimeouts() throws Exception {
        final URL res = this.getClass().getClassLoader().getResource("fiscalPrinter-pirittest-config.xml");

        final PiritPluginConfig config = new ConfigDeserializer().convert(res, PiritPluginConfig.class);

        Assertions.assertThat(config.getCommandsReadTimeout())
                .isNotEmpty()
                .hasSize(3)
                .includes(
                        MapAssert.entry(0x00, 20000L),
                        MapAssert.entry(0x21, 30000L),
                        MapAssert.entry(0x22, 40000L)
                );
    }
}