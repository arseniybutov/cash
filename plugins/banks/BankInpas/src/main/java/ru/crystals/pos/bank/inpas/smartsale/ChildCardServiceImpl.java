package ru.crystals.pos.bank.inpas.smartsale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.ChildCardBankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;

public class ChildCardServiceImpl extends InpasBankServiceImpl implements ChildCardBankPlugin {
    public static final Logger logger = LoggerFactory.getLogger(ChildCardServiceImpl.class);
    private static final long BALANCE_REQUEST_COMMAND = 13L;

    @Override
    public synchronized AuthorizationData getBankCardBalance() throws BankException {
        try {
            logger.info("Balance request");
            createNewData();
            data.setCurrencyCode("RUB");
            return processTerminalResponseAuthorizationData(executeCommand(BALANCE_REQUEST_COMMAND));
        } catch (Exception e) {
            logger.error("", e);
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"), e);
        }
    }
}
