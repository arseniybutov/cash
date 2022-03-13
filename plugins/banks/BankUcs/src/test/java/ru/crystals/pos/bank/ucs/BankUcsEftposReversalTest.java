package ru.crystals.pos.bank.ucs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.ucs.exceptions.NoPreviousTransactionWithSuchRefNumberException;
import ru.crystals.pos.bank.ucs.messages.requests.CreditRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.requests.ReversalRequest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankUcsEftposReversalTest {
    @Mock
    private RequestManager requestManager;
    @Mock
    private ReversalData reversalData;
    @Mock
    private LastOperation lastOperation;
    @InjectMocks
    private BankUcsEftpos bank = new BankUcsEftpos();

    @Before
    public void setUp() throws Exception {
        when(reversalData.getAmount()).thenReturn(10000L);
        when(reversalData.getCashTransId()).thenReturn(123L);
        when(reversalData.getCurrencyCode()).thenReturn("RUB");
        when(reversalData.getOriginalSaleTransactionAmount()).thenReturn(10000L);
        when(reversalData.getRefNumber()).thenReturn("12345");
    }

    @Test
    public void testReversal() throws Exception {

        //given
        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setStatus(true);
        when(requestManager.makeTransaction(any(Request.class))).thenReturn(authorizationData);

        //when
        AuthorizationData resultAuthorizationData = bank.reversal(reversalData);

        //then
        verify(requestManager).makeTransaction(new ReversalRequest(reversalData));
        assertThat(resultAuthorizationData.getOperationType()).isEqualTo(BankOperationType.REVERSAL);
        assertThat(resultAuthorizationData.getCashTransId()).isEqualTo(reversalData.getCashTransId());
    }

    @Test
    public void testReversalWithNoPreviousTransactionWithSuchRefNumber() throws Exception {
        //given
        when(requestManager.makeTransaction(any(Request.class))).thenThrow(new NoPreviousTransactionWithSuchRefNumberException())
            .thenReturn(new AuthorizationData());

        //when
        AuthorizationData resultAuthorizationData = bank.reversal(reversalData);

        //then
        verify(requestManager).makeTransaction(new ReversalRequest(reversalData));
        verify(requestManager).makeTransaction(new CreditRequest(reversalData));
        assertThat(resultAuthorizationData.getOperationType()).isEqualTo(BankOperationType.REFUND);
        assertThat(resultAuthorizationData.getCashTransId()).isEqualTo(reversalData.getCashTransId());
    }

    @Test
    public void testPartialReversal() throws Exception {

        //given
        when(reversalData.getAmount()).thenReturn(1000L);
        when(reversalData.isPartial()).thenReturn(true);
        when(requestManager.makeTransaction(any(Request.class))).thenReturn(new AuthorizationData());

        //when
        AuthorizationData authorizationData = bank.reversal(reversalData);

        //then
        verify(requestManager).makeTransaction(new CreditRequest(reversalData));
        assertThat(authorizationData.getOperationType()).isEqualTo(BankOperationType.REFUND);
        assertThat(authorizationData.getCashTransId()).isEqualTo(reversalData.getCashTransId());
    }
}
