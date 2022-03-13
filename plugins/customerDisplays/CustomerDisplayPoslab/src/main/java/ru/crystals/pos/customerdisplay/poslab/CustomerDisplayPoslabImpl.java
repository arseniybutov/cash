package ru.crystals.pos.customerdisplay.poslab;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

/**
 * Плагин "Дисплей покупателя POSLAB".
 *
 * @author k.danilov
 * @see <a href="http://www.poslab.com.tw/downloadfiles/Driver/POS/CustomerDisplay/PL-100F-VFD_UserManual.pdf">VFD PL-100F User Manual</a>
 */
public class CustomerDisplayPoslabImpl extends TextCustomerDisplayPluginAbstract {

    private static final int MAX_CHAR_PER_LINE = 20;
    private static final int ESC = 0x1B;
    private static final int CLR = 0x0C;

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        if (column < 0 || column > 20) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. column=" + column);
        } else if (row < 0 || row > 1) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. row=" + row);
        } else if (text == null) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. text=null");
        }

        // Move cursor to specified position 1 ≦ x(column) ≦ 20 ; 1 ≦ y(row) ≦ 2
        byte[] hex = {0x1F, 0x24, (byte) (column + 1), (byte) (row + 1)};
        executeCommand(new String(hex) + text);
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        executeCommand(new String(new byte[]{ESC, 0x40})); // Initialize display
        executeCommand(new String(new byte[]{ESC, 0x52, 0x0B})); // international font set - RUSSIA
        executeCommand(new String(new byte[]{ESC, 0x74, 0x09})); // character code table - (CP-866: Cyrillic(Russia)
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        executeCommand(new String(new byte[]{CLR}));
    }

    @Override
    public void verifyDevice() {
        // not used
    }
}
