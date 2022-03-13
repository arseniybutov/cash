package ru.crystals.pos.fiscalprinter.documentprinter.citizen;

import com.google.common.collect.ImmutableMap;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.DocumentPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;

@PrototypedComponent
public class DocumentPrinterCitizen extends DocumentPrinterAxiohm {

    /**
     * Общая команда статуса принтера и статуса ДЯ
     */
    private static final ByteSequence STATUS_COMMAND = ByteSequence.of(0x10, 0x04, 0x01);

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setFeedLength(2);

        defaultConfig.setMaxCharRowMap(ImmutableMap.of(
                Font.NORMAL, 48,
                Font.SMALL, 64
        ));

        defaultConfig
                .addCommand(PrinterCommandType.CUT, ByteSequence.of(0x1B, 0x69))
                .addCommand(PrinterCommandType.STATUS, STATUS_COMMAND)
                .addCommand(PrinterCommandType.DRAWER_STATUS, STATUS_COMMAND)
                .addCommand(PrinterCommandType.FEED, ByteSequence.of(0x1B, 0x64));


        defaultConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(0b0000_1000), StatusFP.Status.END_PAPER
        ));

        defaultConfig.setDrawerStatusMap(ImmutableMap.of(
                false, ByteSequence.of(0b0000_0100)
        ));
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_CITIZEN");
    }
}
