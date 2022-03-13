package ru.crystals.pos.fiscalprinter.documentprinter.fec;

import com.google.common.collect.ImmutableMap;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;
import ru.crystals.pos.fiscalprinter.documentprinter.citizen.DocumentPrinterCitizen;

@PrototypedComponent
public class DocumentPrinterFec extends DocumentPrinterCitizen {

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.addCommand(PrinterCommandType.INIT_CHARSET, ByteSequence.of(0x1B, 0x74, 0x08));
        defaultConfig.setMaxCharRowMap(ImmutableMap.of(
                Font.NORMAL, 42,
                Font.SMALL, 56
        ));
    }
}
