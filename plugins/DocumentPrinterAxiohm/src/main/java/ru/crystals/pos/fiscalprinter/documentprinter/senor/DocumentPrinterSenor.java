package ru.crystals.pos.fiscalprinter.documentprinter.senor;

import com.google.common.collect.ImmutableMap;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.DocumentPrinterNFDAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.ArrayList;
import java.util.List;

@PrototypedComponent
public class DocumentPrinterSenor extends DocumentPrinterNFDAxiohm {

    /**
     * Бумага совсем закончилась или открыта крышка+низкий уровень бумаги
     * <p>
     * Для низкого уровня бумаги будет 0b0000_0011
     */
    private static final byte END_OF_PAPER = 0b0000_1111;

    /**
     * Открыта крышка принтера
     */
    private static final byte COVER_OPENED = 0b0000_1100;

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(END_OF_PAPER), StatusFP.Status.END_PAPER,
                ByteSequence.of(COVER_OPENED), StatusFP.Status.OPEN_COVER
        ));
        // Senor в малом размере шрифта вместо кириллической большой K печатает малую. Поэтому меняем на латиницу - с ней ок.
        defaultConfig.addMissingSymbolReplacement('К', 'K');
        defaultConfig.addCommand(PrinterCommandType.CUT, ByteSequence.of(0x1B, 0x69));
    }


    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_SENOR");
    }

    @Override
    protected void feedAll() throws FiscalPrinterException {
        printLine("  ");
    }

    @Override
    public List<FontLine> customizeFooter(List<FontLine> sectionList) {
        final ArrayList<FontLine> result = new ArrayList<>(sectionList);
        result.add(new FontLine("      "));
        result.add(new FontLine("      "));
        return result;
    }

    @Override
    public void printBeforeCutForServiceDoc() throws FiscalPrinterException {
        printLine("    ");
    }
}
