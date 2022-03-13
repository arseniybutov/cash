package ru.crystals.pos.fiscalprinter.documentprinter.epson;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.documentprinter.system.DocumentPrinterSystem;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.utils.NativeExecutor;

/**
 *
 * @author dalex
 */
@PrototypedComponent
public class DocumentPrinterEpson extends DocumentPrinterSystem {

    @Override
    public void openCashDrawer() throws FiscalPrinterException {
        LOG.info("Print cash drawer");
        try {
            // запуск аналога комманды "echo -e -n \"\\x1b\\x70\\x30\\x40\\x50\" | lpr -o raw",
            NativeExecutor.processCommandWithOutput(
                    "lpr -o raw",
                    null, null, false,
                    // cash drawer open command
                    new byte[]{
                        0x1B, 0x70, 0x30, 0x40, 0x50
                    });
        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }
}
