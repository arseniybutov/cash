package ru.crystals.pos.cashdrawer.wincor.beetle20;

import ru.crystals.pos.CashException;
import ru.crystals.pos.cashdrawer.CashDrawer;
import ru.crystals.pos.cashdrawer.exception.CashDrawerException;

public class WincorBeetle20CashDrawerServiceImpl implements CashDrawer {

    private CashDrawerConnector connector = new CashDrawerConnector();

    @Override
    public void start() {
        //
    }

    @Override
    public void stop() {
        //
    }

    @Override
    public boolean openDrawer(CashDrawerOpenMode openMode) throws CashException {
        int n = connector.openDrawer(1);
        if (n < 0) {
            throw new CashDrawerException(getErrorString(n));
        }
        return true;
    }

    @Override
    public boolean isOpenDrawer() throws CashDrawerException {
        boolean isOpenDrawer = false;
        int n = connector.getDrawerOpened(1);
        if (n < 0) {
            throw new CashDrawerException(getErrorString(n));
        }
        if (n > 0) {
            isOpenDrawer = true;
        }
        return isOpenDrawer;
    }

    private String getErrorString(int errorNumber) {
        return "Cash drawer error " + errorNumber;
    }

}
