package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.posiflex.pd2600.encoding.PosiflexPD320CodeTable;

import java.io.UnsupportedEncodingException;

public class CustomerDisplayPosiflexPD320Impl extends CustomerDisplayPosiflexSerialImpl {

    /**
     * В Posiflex PD-320U используется своя кодировка кириллицы на основе CP866
     */
    private static final String ENCODING = "cp866";

    @Override
    public synchronized void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        try {
            executeCommand(PosiflexPD320CodeTable.convertForPD320(text.getBytes(ENCODING)));
        } catch (UnsupportedEncodingException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setOverwriteMode();
        hideCursor();
        clearText();
    }
}
