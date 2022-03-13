package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import com.google.common.collect.ImmutableMap;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;

/**
 * Реализация для работы с принтером чеков SEWOO SLK-TS400, отличается от Axiohm отсутствием некоторых однобайтовых команд
 * и размером среднего шрифта в 42 символа
 */
@PrototypedComponent
public class DocumentPrinterSewoo extends DocumentPrinterAxiohm {

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setMaxCharRowMap(ImmutableMap.of(
                Font.NORMAL, 42,
                Font.SMALL, 56,
                // Странно, что 22 (скорее всего не тестили), но оставляю как было для эквивалентности рефакторинга
                Font.DOUBLEWIDTH, 22
        ));

        defaultConfig.getCommands().put(PrinterCommandType.CUT, ByteSequence.of(0x6D));
        defaultConfig.getCommands().put(PrinterCommandType.FULL_CUT, ByteSequence.of(0x69));
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_SEWOO");
    }
}
