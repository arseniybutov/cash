package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.fn.StatusFN;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.fn.StatusOFD;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.fn.VersionFN;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.utils.time.DateConverters;

import java.util.Date;
public class PiritAgent {

    protected static final int CASHIER_NAME_MAX_LENGTH = 26;

    protected PiritConnector pc;

    protected final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    public PiritAgent(PiritConnector pc) {
        this.pc = pc;
    }

    public long getLastKpk() throws FiscalPrinterException {
        try {
            return pc.sendRequest(ExtendedCommand.GET_FN_INFO_LAST_FD).getLongValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ShiftCounters getCountOfSalesAndReturns() throws FiscalPrinterException {
        try {
            ShiftCounters sc = new ShiftCounters();
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);
            sc.setCountSale(dp.getDoubleToRoundLong(1));
            sc.setCountReturn(dp.getDoubleToRoundLong(2));

            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_SHIFT_NUMBER);
            sc.setShiftNum(dp.getLongValue(1));

            return sc;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String loggedInn = null;

    public String getLoggedInn() {
        return loggedInn;
    }

    public String getINN() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_INN);
            loggedInn = dp.getStringValue(1);
            pc.setInn(loggedInn);
            return loggedInn;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getShiftNumber() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_SHIFT_NUMBER);
            long result = dp.getLongValue(1);
            LOG.info("ShiftNumber = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getEklzNum() throws FiscalPrinterException {
        try {
            return pc.sendRequest(ExtendedCommand.GET_FN_INFO_NUMBER).getStringValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Date getEKLZActivizationDate() throws FiscalPrinterException {
        try{
            return DateConverters.toDate(pc.sendRequest(ExtendedCommand.GET_FN_INFO_REG_DATE).getOptionalDateValue(1)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid date")));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void printCurrentFNReport(Cashier cashier) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(cashier.getNullSafeName());
            pc.sendRequest(PiritCommand.PRINT_CURRENT_FN_REPORT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long openShiftInFN(Cashier cashier) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(cashier));
            pc.sendRequest(PiritCommand.OPEN_SHIFT_IN_FN, dp);
            return getShiftNumber();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getCashierName(Cashier cashier) {
        return StringUtils.left(formatCashierName(cashier), CASHIER_NAME_MAX_LENGTH);
    }

    private String formatCashierName(Cashier cashier) {
        return StringUtils.leftPad(String.valueOf(cashier.getTabNumLong()), 2, "0") + cashier.getCashierStringForOFDTag1021();
    }

    /**
     * Получить всю сервисную информацию с принтера
     */
    public ServiceInfo getPrinterServiceInfo() {
        ServiceInfo serviceInfo = new ServiceInfo();
        try {
            serviceInfo.setVoltage(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_VOLTAGE));
            serviceInfo.setBatteryVoltage(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_BATTERY_VOLTAGE));
            serviceInfo.setTemperature(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_TEMP));
            serviceInfo.setCutsCount(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_CUT_COUNTS));
            serviceInfo.setThermoHeadResource(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_HEAD_RES));
            serviceInfo.setCutsCountTotal(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_CUT_COUNTS_TOTAL));
            serviceInfo.setThermoHeadResourceTotal(getServiceInfo(ExtendedCommand.GET_SERVICE_INFO_HEAD_RES_TOTAL));
        } catch (FiscalPrinterException e) {
            LOG.info("Error get service info", e);
            return null;
        }
        return serviceInfo;
    }

    private long getServiceInfo(ExtendedCommand paramValue) throws FiscalPrinterException {
        try {
            return pc.sendRequest(paramValue).getLongValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StatusFN getStatusFN() throws FiscalPrinterException{
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_FN_INFO_STATUS);
            return new StatusFN(dp);
        } catch (FiscalPrinterException fpe){
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getStatusFN", e);
        }
    }

    public StatusOFD getStatusOFD() throws FiscalPrinterException{
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_FN_INFO_OFD_STATUS);
            return new StatusOFD(dp);
        } catch (FiscalPrinterException fpe){
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getStatusOFD", e);
        }
    }

    public String getUrlOFD() throws FiscalPrinterException{
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(77L);
            dp.putLongValue(0L);
            dp = pc.sendRequest(PiritCommand.GET_CONFIGURATION_TABLE, dp);
            return dp.getStringValueNull(0);
        } catch (FiscalPrinterException fpe){
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getUrlOFD", e);
        }
    }

    public VersionFN getVersionFN() throws FiscalPrinterException{
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_EKLZ_INFO_FW);
            return new VersionFN(dp);
        } catch (FiscalPrinterException fpe){
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException("getVersionFN", e);
        }
    }
}
