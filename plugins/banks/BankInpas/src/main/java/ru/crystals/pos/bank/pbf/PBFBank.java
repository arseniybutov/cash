package ru.crystals.pos.bank.pbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.InpasConstants;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.inpas.smartsale.FieldCollection;
import ru.crystals.pos.bank.inpas.smartsale.InpasBankServiceImpl;
import ru.crystals.pos.bank.inpas.smartsale.InpasConnector;
import ru.crystals.pos.bank.inpas.smartsale.ResBundleBankInpas;

import java.util.ArrayList;
import java.util.List;

public class PBFBank extends InpasBankServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(PBFBank.class);

    protected TerminalConnector terminalConnector;
    protected final PBFTerminalConfig terminalConfig = new PBFTerminalConfig();

    @Override
    public void start() {
        terminalConfig.setBaseConfiguration(getTerminalConfiguration());
        terminalConnector = new TerminalConnector(terminalConfig);
    }

    @Override
    public synchronized boolean requestTerminalStateIfOffline() {
        try {
            createNewData();
            try (InpasConnector connector = terminalConnector.newSession()) {
                executeCommand(InpasConstants.CHECK_STATE, connector);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected List<List<String>> generateBankSlip(FieldCollection result) {
        List<List<String>> slip = new ArrayList<>();
        InpasBankServiceImpl.generateNotInnerSlip(result, slip);

        return slip;
    }

    protected AuthorizationData processBankOperation(long command) throws BankCommunicationException {
        try (InpasConnector connector = terminalConnector.newSession()) {
            return processBankOperationInner(command, connector);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"));
        }
    }

    protected FieldCollection processTerminalResponse(FieldCollection fc) {
        return fc;
    }

    protected String formatRefNumber(String refNumber) {
        return refNumber;
    }

    @Override
    public synchronized DailyLogData dailyLog(Long cashTransId) throws BankException {
        DailyLogData result = new DailyLogData();
        try (InpasConnector connector = terminalConnector.newSession()) {
            dailyLogInner(cashTransId, result, connector);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"));
        }
        return result;
    }

    public void setTimeoutT1(long timeoutT1) {
        terminalConfig.setOverallTimeOut(timeoutT1);
    }

    public void setTimeoutT2(long timeoutT2) {
        terminalConfig.setReadByteTimeOut(timeoutT2);
    }

}
