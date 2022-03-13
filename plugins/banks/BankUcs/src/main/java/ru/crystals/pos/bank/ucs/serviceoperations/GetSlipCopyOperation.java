package ru.crystals.pos.bank.ucs.serviceoperations;

import ru.crystals.pos.bank.datastruct.ServiceBankOperationParameter;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;
import ru.crystals.pos.bank.ucs.messages.requests.GetTransactionDetailsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;

public class GetSlipCopyOperation extends UCSServiceOperation {
    public GetSlipCopyOperation() {
        setParameter(new ServiceBankOperationParameter(ResBundleBankUcs.getString("CHECK_NUMBER_PARAMETER_NAME"),
                ResBundleBankUcs.getString("CHECK_NUMBER_WELCOME_TEXT")));
    }

    @Override
    public Request createRequest() throws BankException {
        return new GetTransactionDetailsRequest(getParameter().getInputValue());
    }

    @Override
    public boolean needsInLastTransactionID() {
        return true;
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
    }

    @Override
    public String getCommandTitle() {
        return ResBundleBankUcs.getString("SLIP_COPY_COMMAND_TITLE");
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankUcs.getString("SLIP_COPY_FORM_TITLE");
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankUcs.getString("GET_SLIP_COPY_SPINNER_MESSAGE");
    }
}
