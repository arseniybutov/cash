package ru.crystals.pos.fiscalprinter.pirit.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgentFN100;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.user.UserEntity;

public class PiritCashierInn {

    @Test
    public void getNameWithINN() {
        PiritConnector pc = mock(PiritConnector.class);
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Tip");
        userEntity.setMiddleName("Tipov");
        userEntity.setLastName("Tipovich");
        userEntity.setInn("123456789123");
        PiritAgentFN100 pa = new PiritAgentFN100(pc);
        Cashier cashier = new Cashier(userEntity);
        String result = pa.getCashierName(cashier);
        Assert.assertEquals(result, "123456789123&Tipovich");
    }

    @Test
    public void getNameWithoutINN() {
        PiritConnector pc = mock(PiritConnector.class);
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Tip");
        userEntity.setMiddleName("Tipov");
        userEntity.setLastName("Tipovich");
        PiritAgentFN100 pa = new PiritAgentFN100(pc);
        Cashier cashier = new Cashier(userEntity);
        String result = pa.getCashierName(cashier);
        Assert.assertEquals(result, "Tipovich");
    }

    @Test
    public void getNameWithJobTitle() {
        PiritConnector pc = mock(PiritConnector.class);
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Tip");
        userEntity.setMiddleName("Tipov");
        userEntity.setLastName("Tipovich");
        userEntity.setJobTitle("Cashier");
        PiritAgentFN100 pa = new PiritAgentFN100(pc);
        Cashier cashier = new Cashier(userEntity);
        String result = pa.getCashierName(cashier);
        Assert.assertEquals(result, "Cashier Tipovich");
    }

}
