package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Копия класса для работы с нефискальным принтером чеков Axiom и Пирит. В копии убрана проверка isDSR,
 * т.к. она не работает при подключении по USB
 */
@PrototypedComponent
public class DocumentPrinterNFDAxiohm extends DocumentPrinterAxiohm {

    @Override
    protected byte[] getTextBytes(String text) {
        return text.getBytes(encoding);
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        feedAll();

        //Препринт заголовка для экономии ч.л.
        setFont(Font.NORMAL);
        printHeaders();
        cut();
        try {
            getStatus();
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinter.getString("ERROR_PRINTER"), CashErrorType.FISCAL_ERROR);
        }
    }

    protected void feedAll() throws FiscalPrinterException {
        feed();
        feed();
        feed();
    }

    @Override
    protected void setQRCodeSizeCommand(BarCode code) {
        super.setQRCodeSizeCommand(config.getQrSize().orElse(3));
    }


}
