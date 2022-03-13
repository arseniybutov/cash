package ru.crystals.pos.bank.demir;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.demir.connector.DemirConnector;
import ru.crystals.pos.bank.demir.response.Response;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.property.Properties;
import ru.crystals.utils.time.DateConverters;

public class DemirBankServiceImpl extends AbstractBankPluginImpl {

    private DemirConnector connector;
    @Autowired
    private Properties properties;

    @Override
    public void start() throws CashException {
        connector = new DemirConnector(getTerminalConfiguration());
        connector.start();
    }

    @Override
    public void stop() {
        connector.close();
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        byte[] command = new Command()
                .setCashNumber(properties.getCashNumber())
                .setProcessType(ProcessType.SALES)
                .setAmount(saleData.getAmount())
                .toByteArray();

        Response response = Response.parse(connector.sendCommand(command));

        AuthorizationData authData = new AuthorizationData();
        authData.setOperationType(BankOperationType.SALE);
        authData.setAmount(saleData.getAmount());
        boolean isOk = response.isOk();
        authData.setStatus(isOk);
        if (!isOk) {
            String terminalMessage = response.getMessage().trim();
            String responseCode = response.getResponseCode();
            authData.setMessage(String.format("%s \"%s\", %s: %s", CoreResBundle.getStringCommon("ERROR"),
                    terminalMessage, ResBundleBank.getString("ERROR_CODE"), responseCode));
        }
        authData.setDate(DateConverters.toDate(response.getDateTime()));
        authData.setRefNumber(response.getRRN());
        authData.setAuthCode(response.getAuthCode());

        return authData;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        byte[] command = new Command()
                .setProcessType(ProcessType.END_OF_DAY)
                .setCashNumber(properties.getCashNumber())
                .toByteArray();

        connector.sendCommand(command);

        return new DailyLogData();
    }

    // unsupported

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        throw new UnsupportedOperationException();
    }
}
