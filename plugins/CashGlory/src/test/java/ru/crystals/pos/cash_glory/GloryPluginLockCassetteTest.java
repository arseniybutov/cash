package ru.crystals.pos.cash_glory;

import static org.testng.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.cash_glory.constants.DeviceType;
import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.exceptions.CashMachineUnsupportedOperationException;

@RunWith(MockitoJUnitRunner.class)
public class GloryPluginLockCassetteTest {

    @Mock
    private CashGloryFacadeInterface facade;

    @InjectMocks
    private GloryPlugin plugin = new GloryPlugin();


    @Test
    public void testLockBanknoteCassette() throws Exception {
        Mockito.when(facade.lock(DeviceType.RBW)).thenReturn(Response.SUCCESS);

        plugin.lockBanknoteCassette();

        Mockito.verify(facade).lock(DeviceType.RBW);
    }

    @Test(expected = CashMachineUnsupportedOperationException.class)
    public void testLockBanknoteCassetteWithException() throws CashMachineUnsupportedOperationException {
        Mockito.when(facade.lock(DeviceType.RBW)).thenReturn(Response.EXCLUSIVE_ERROR);

        plugin.lockBanknoteCassette();
    }

    @Test
    public void testUnlockBanknoteCassette() throws Exception {
        Mockito.when(facade.unlock(DeviceType.RBW)).thenReturn(Response.SUCCESS);

        plugin.unlockBanknoteCassette();

        Mockito.verify(facade).unlock(DeviceType.RBW);
    }

    @Test(expected = CashMachineUnsupportedOperationException.class)
    public void testUnlockBanknoteCassetteWithException() throws CashMachineUnsupportedOperationException {
        Mockito.when(facade.unlock(DeviceType.RBW)).thenReturn(Response.EXCLUSIVE_ERROR);

        plugin.unlockBanknoteCassette();
    }

    @Test
    public void testLockCoinCassette() throws Exception {
        Mockito.when(facade.lock(DeviceType.RCW)).thenReturn(Response.SUCCESS);

        plugin.lockCoinCassette();

        Mockito.verify(facade).lock(DeviceType.RCW);
    }

    @Test(expected = CashMachineUnsupportedOperationException.class)
    public void testLockCoinCassetteWithException() throws CashMachineUnsupportedOperationException {
        Mockito.when(facade.lock(DeviceType.RCW)).thenReturn(Response.EXCLUSIVE_ERROR);

        plugin.lockCoinCassette();
    }

    @Test
    public void testUnlockCoinCassette() throws Exception {
        Mockito.when(facade.unlock(DeviceType.RCW)).thenReturn(Response.SUCCESS);

        plugin.unlockCoinCassette();

        Mockito.verify(facade).unlock(DeviceType.RCW);
    }

    @Test(expected = CashMachineUnsupportedOperationException.class)
    public void testUnlockCoinCassetteWithException() throws CashMachineUnsupportedOperationException {
        Mockito.when(facade.unlock(DeviceType.RCW)).thenReturn(Response.EXCLUSIVE_ERROR);

        plugin.unlockCoinCassette();
    }
}