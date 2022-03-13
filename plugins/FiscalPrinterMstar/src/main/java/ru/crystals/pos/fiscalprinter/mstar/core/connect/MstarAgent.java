package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.fn.ShiftParameters;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.fn.StateFN;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.fn.StatusOFD;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

import java.util.Date;

public class MstarAgent {
    private static final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    private MstarConnector mstarConnector;
    private String loggedInn = null;

    public MstarAgent(MstarConnector mstarConnector) {
        this.mstarConnector = mstarConnector;
    }

    /**
     * Запрос параметров последнего фискального документа
     *
     * @return Номер фискального документа
     */
    public long getLastKpk() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_LAST_FISCAL_DOC_PARAMETERS);
            return dp.getLongValue(0);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ShiftParameters getShiftParameters() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_PARAMETER_CURRENT_SHIFT);
            return new ShiftParameters(dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getLoggedInn() {
        return loggedInn;
    }

    public String getINN() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(3L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            loggedInn = dp.getStringValue(1);
            mstarConnector.setInn(loggedInn);
            return loggedInn;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер последней закрытой
     */
    public long getShiftNumber() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(1L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            long result = dp.getLongValue(1);
            LOG.info("ShiftNumber = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вернуть регистрационный номер ККТ
     *
     * @return регистрационный номер
     */
    public String getRegistrationNumber() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(4L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            return dp.getStringValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получить дату регистрации/перерегистрации
     */
    public Date getRegistrationDate() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(6L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            return dp.getDateValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вернуть номер следующего документа
     */
    public String getNextDocNum() throws Exception {
        DataPacket dp = new DataPacket();
        dp.putLongValue(8L);
        dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
        return dp.getStringValue(1);
    }

    public long openShiftInFN(Cashier cashier) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(cashier.getNullSafeName());
            mstarConnector.sendRequest(MstarCommand.OPEN_SHIFT_IN_FN, dp);
            return getShiftNumber();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Запрос состояния фискального накопителя
     *
     * @return StateFN
     */
    public StateFN getStatusFN() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_FN_STATE);
            return new StateFN(dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getStatusFN", e);
        }
    }

    /**
     * Запрос параметров информационного обмена с оператором фискальных данных
     *
     * @return StatusOFD
     */
    public StatusOFD getStatusOFD() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_EXCHANGE_PARAM_OFD);
            return new StatusOFD(dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getStatusOFD", e);
        }
    }
}
