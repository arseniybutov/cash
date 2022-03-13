package ru.crystals.pos.bank.pbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.inpas.smartsale.InpasConnector;
import ru.crystals.pos.bank.inpas.smartsale.ResBundleBankInpas;
import ru.crystals.pos.bank.inpas.smartsale.serial.TcpPortConnector;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;

public class TerminalConnector {

    private static final Logger log = LoggerFactory.getLogger(TerminalConnector.class);

    private PBFTerminalConfig terminalConfig;

    public TerminalConnector(PBFTerminalConfig terminalConfig) {
        this.terminalConfig = terminalConfig;
    }

    public TcpPortConnector open() throws IOException, PortAdapterException {
        final TerminalConfiguration baseConfig = terminalConfig.getBaseConfiguration();
        TcpPortConnector portAdapter = new TcpPortConnector(baseConfig.getTerminalIp(), baseConfig.getTerminalTcpPort());
        log.info("Connecting to terminal {}:{}...", baseConfig.getTerminalIp(), baseConfig.getTerminalTcpPort());
        portAdapter.openPort();
        return portAdapter;
    }

    public InpasConnector newSession() throws BankException {
        try {
            final TcpPortConnector tcp = open();
            final InpasConnector connector = new InpasConnector(tcp);
            connector.setOverallTimeOut(terminalConfig.getOverallTimeOut());
            connector.setReadByteTimeOut(terminalConfig.getReadByteTimeOut());
            return connector;
        } catch (IOException | PortAdapterException e) {
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_SEND_DATA"), e);
        }
    }
}
