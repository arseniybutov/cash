package ru.crystals.pos.loyal.cash.transport.discountresults;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.discounts.CashTransportBeanRemote;
import ru.crystals.httpclient.HttpFileTransport;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.registry.Registry;
import ru.crystals.pos.transport.ExtendedHttpClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты транспорта транзакций лояльности
 */
@RunWith(MockitoJUnitRunner.class)
public class TransferManagerTest {

    @Mock
    private Registry registry;

    @Mock
    private ExtendedHttpClient httpConnect;

    @Mock
    private ExtendedHttpClient centrumHttpConnect;

    @Mock
    private HttpFileTransport httpFileTransport;

    @Mock
    private InternalCashPoolExecutor executor;

    @Spy
    @InjectMocks
    private TransferManager transferManager;

    @Test
    public void test() throws Exception {
        // given
        CashTransportBeanRemote expectedServerBean = mock(CashTransportBeanRemote.class);
        when(httpConnect.find(eq(CashTransportBeanRemote.class), eq(CashTransportBeanRemote.JNDI_NAME))).thenReturn(expectedServerBean);
        // when
        transferManager.start();
        // then
        Assert.assertSame(expectedServerBean, Whitebox.getInternalState(transferManager, "cashTransportManager"));
        verify(executor, times(2)).scheduleWithFixedDelay(any(Runnable.class), anyInt(), anyInt(), any(TimeUnit.class));
    }

}
