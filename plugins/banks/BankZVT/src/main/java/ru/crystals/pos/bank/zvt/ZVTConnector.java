package ru.crystals.pos.bank.zvt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZVTConnector {

    private static final Logger log = LoggerFactory.getLogger(ZVTConnector.class);

    private ZVTTerminalConfig terminalConfig;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ZVTConnector(ZVTTerminalConfig terminalConfig) {
        this.terminalConfig = terminalConfig;
    }

    public TCPPortAdapter open() throws IOException, BankException {
        TCPPortAdapter portAdapter = new TCPPortAdapter();
        final TerminalConfiguration baseConfig = terminalConfig.getBaseConfiguration();
        if (baseConfig.getConnectionType() != TerminalConfiguration.TerminalConnectionType.TCP) {
            log.error("Not supported connectionType={}", baseConfig.getConnectionType());
            throw new BankConfigException(ResBundleBankZVT.getString("CONFIGURATION_ERROR"));
        }
        portAdapter.setTcpPort(baseConfig.getTerminalTcpPort());
        portAdapter.setTcpAddress(baseConfig.getTerminalIp());
        log.info("Connecting to terminal {}:{}...", portAdapter.getTcpAddress(), portAdapter.getTcpPort());
        portAdapter.openPort();
        return portAdapter;
    }

    public ZVTSession newSession() throws BankException {
        try {
            final TCPPortAdapter tcp = open();
            return new ZVTSession(tcp, executorService, terminalConfig);
        } catch (IOException e) {
            throw new BankCommunicationException(ResBundleBankZVT.getString("TERMINAL_COMMUNICATION_ERROR"), e);
        }
    }
}
