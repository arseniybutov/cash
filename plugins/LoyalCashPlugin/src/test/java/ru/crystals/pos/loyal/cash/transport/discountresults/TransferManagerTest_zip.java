package ru.crystals.pos.loyal.cash.transport.discountresults;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.httpclient.HttpFileTransport;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.registry.Registry;
import ru.crystals.pos.techprocess.TechProcessEvents;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransferManagerTest_zip {

    @Spy
    @InjectMocks
    private TransferManager transferManager;

    @Mock
    private LoyTxDao loyTxDao;

    @Mock
    private HttpFileTransport httpFileTransport;

    @Mock
    private Loyal loyalService;

    @Mock
    private Registry registry;

    @Mock
    private PropertiesManager propertiesManager;

    @Mock
    private TechProcessEvents techProcessEvents;
  
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(loyalService).isLoyTransactionComplete(Mockito.any(LoyTransactionEntity.class));
    }

    @Test
    public void shouldnt_send_zip_for_two_txns() throws IOException {
        //given

        List<LoyTransactionEntity> transactions = Arrays.asList(generatTxn(), generatTxn());

        when(loyTxDao.getLoyTxesByStatus(Arrays.asList(new SentToServerStatus[] {
                        SentToServerStatus.NO_SENT, SentToServerStatus.WAIT_ACKNOWLEDGEMENT, SentToServerStatus.SENT_ERROR}), 100))
        .thenReturn(transactions);

        //when
        transferManager. new RepeatSender().run();

        //then
        ArgumentCaptor<String> fileName = ArgumentCaptor.forClass(String.class);
        verify(httpFileTransport, times(2)).getServerURL(fileName.capture());
        assertTrue(fileName.getValue().endsWith(".ser"));
    }
    @Test
    public void should_send_ser_for_one_txns() throws IOException {
        //given

        List<LoyTransactionEntity> transactions = Arrays.asList(generatTxn());

        when(loyTxDao.getLoyTxesByStatus(Arrays.asList(new SentToServerStatus[] {
                        SentToServerStatus.NO_SENT, SentToServerStatus.WAIT_ACKNOWLEDGEMENT, SentToServerStatus.SENT_ERROR}), 100))
        .thenReturn(transactions);

        //when
        transferManager. new RepeatSender().run();

        //then
        ArgumentCaptor<String> fileName = ArgumentCaptor.forClass(String.class);
        verify(httpFileTransport, times(1)).getServerURL(fileName.capture());
        assertTrue(fileName.getValue().endsWith(".ser"));
    }

    private LoyTransactionEntity generatTxn() {
        LoyTransactionEntity transactionEntity = new LoyTransactionEntity();
        transactionEntity.setSaleTime(new Date());
        transactionEntity.setTransactionTime(new Date());
        transactionEntity.setSentToServerStatus(SentToServerStatus.NO_SENT);
        return transactionEntity;
    }
}
