package ru.crystals.pos.fiscalprinter.nonfiscalmode.withoutprinter;

import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;

public class EmptyPrinter implements ReceiptPrinter {

    @Override
    public void printLine(String text) {

    }

    @Override
    public void skipAndCut() {

    }

}
