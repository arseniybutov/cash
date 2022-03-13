package ru.crystals.pos.bank.inpas.smartsale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChildCardServiceImplTest {
    private static final long BALANCE_REQUEST_COMMAND = 13L;
    @Spy @InjectMocks
    private ChildCardServiceImpl service = new ChildCardServiceImpl();

    @Test
    public void testGetBankCardBalance() throws Exception {
        FieldCollection fc = new FieldCollection();
        fc.setAmount(25L);
        AuthorizationData data = fc.toAuthorizationData();

        doReturn(fc).when(service).executeCommand(BALANCE_REQUEST_COMMAND);
        doReturn(data).when(service).processTerminalResponseAuthorizationData(fc);


        AuthorizationData res = service.getBankCardBalance();

        verify(service).createNewData();
        verify(service).executeCommand(BALANCE_REQUEST_COMMAND);
        verify(service).processTerminalResponseAuthorizationData(fc);
        assertThat(data).isEqualTo(res);
    }

    @Test(expected = BankCommunicationException.class)
    public void testGetBankCardBalanceException() throws Exception {
        doThrow(new BankException()).when(service).executeCommand(anyLong());

        service.getBankCardBalance();
    }
}
