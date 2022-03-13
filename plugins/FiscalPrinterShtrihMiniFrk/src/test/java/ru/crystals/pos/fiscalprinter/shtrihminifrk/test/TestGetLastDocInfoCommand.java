package ru.crystals.pos.fiscalprinter.shtrihminifrk.test;

import org.junit.Test;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetLastDocInfoCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;

public class TestGetLastDocInfoCommand {

    @Test
    public void testGetLastDocument() throws FiscalPrinterException, PortAdapterException, ShtrihException, IOException {
        byte[] response = {2, 24, -1, 10, 0, 3, 0, 17, 12, 7, 17, 22, 44, 1, 0, 0, 60, -49, -87, 118, 1, -109, 78, 0, 0, 0, 46};
        GetLastDocInfoCommand command = new GetLastDocInfoCommand(100, 30);
        FiscalDocumentData fiscalDocumentData = command.decodeResponse(response);
        System.out.println(fiscalDocumentData);
        assert(fiscalDocumentData.getSum() == 20115
                && fiscalDocumentData.getType() == FiscalDocumentType.SALE
                && fiscalDocumentData.getNumFD() == 300);
    }

}
