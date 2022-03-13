package ru.crystals.pos.bank.bpc.serviceoperations;

import ru.crystals.pos.bank.bpc.Request;
import ru.crystals.pos.bank.bpc.RequestFactory;
import ru.crystals.pos.bank.bpc.ResBundleBankBPC;

public class TestHostOperation extends BPCServiceOperation {
    @Override
    public Request createRequest(String ecr, String ern) {
        return RequestFactory.createServiceOperationRequest(ecr, 0x04);
    }

    @Override
    public String getCommandTitle() {
        return ResBundleBankBPC.getString("HOST_TEST");
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankBPC.getString("HOST_TEST");
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankBPC.getString("HOST_TEST");
    }
}
