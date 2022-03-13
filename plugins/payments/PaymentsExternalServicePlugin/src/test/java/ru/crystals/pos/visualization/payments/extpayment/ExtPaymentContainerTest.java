package ru.crystals.pos.visualization.payments.extpayment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.api.adapters.ExtPaymentController;
import ru.crystals.api.loader.payments.PluginDescription;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.api.plugin.PaymentPlugin;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.payments.CashPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessImpl;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.components.MainWindow;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

@PowerMockIgnore("javax.swing.*") // https://stackoverflow.com/questions/11943703/trying-to-mock-static-system-class-with-powermock-gives-verifyerror
@RunWith(PowerMockRunner.class)
@PrepareForTest({ExtPaymentContainer.class, Factory.class, CurrencyUtil.class})
public class ExtPaymentContainerTest {
    private volatile boolean testRoutineCalled = false;

    /**
     * Этот тест проверяет, что в слипе поддерживаются ньюлайны.<br>
     * Мотивация - проверить выполнение задачи <a href="https://crystals.atlassian.net/browse/CORE-224">CORE-224</a>
     */
    @Test
    public void paymentCancelCompletedCORE224Test() throws Exception {
        testRoutineCalled = false;
        Payment payment = new Payment();
        payment.setSlip("Это первая строка.\nЭто вторая строка.\nЭто третья строка, разве двух не достаточно?");

        TechProcessInterface techProcess = mock(TechProcessInterface.class);
        Mockito.doAnswer(invocationOnMock -> {
            PaymentTransactionEntity transaction = (PaymentTransactionEntity)invocationOnMock.getArguments()[0];
            testRoutineCalled = true;
            assertEquals(1, transaction.getSlips().size());
            assertEquals(3, transaction.getSlips().get(0).getSlip().size());
            assertEquals("Это первая строка.", transaction.getSlips().get(0).getSlip().get(0));
            assertEquals("Это вторая строка.", transaction.getSlips().get(0).getSlip().get(1));
            assertEquals("Это третья строка, разве двух не достаточно?", transaction.getSlips().get(0).getSlip().get(2));
            return null;
        }).when(techProcess).addPaymentTransaction(Mockito.anyObject());

        PowerMockito.mockStatic(Factory.class);
        PowerMockito.mockStatic(CurrencyUtil.class);
        PowerMockito.when(Factory.getTechProcessImpl()).thenReturn(techProcess);
        PowerMockito.when(Factory.getInstance()).thenReturn(null);
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setDateCreate(new Date());

        ExtPaymentContainer container = new ExtPaymentContainer();
        Whitebox.setInternalState(container, "paymentEntity", paymentEntity);

        container.paymentCancelCompleted(payment);
        if(!testRoutineCalled) {
            fail("Test routine has not been called");
        }
    }

    /**
     * Этот тест проверяет, что в чеке продажи ньюлайны поддерживаются.<br>
     * Мотивация - проверить выполнение задачи <a href="https://crystals.atlassian.net/browse/CORE-224">CORE-224</a>
     */
    @Test
    public void testPaymentSaleCompletedCORE224Test() throws Exception {
        testRoutineCalled = false;
        Payment payment = new Payment();
        payment.setSlip("Это первая строка.\nЭто вторая строка.\nЭто третья строка, разве двух не достаточно?");

        TechProcessInterface techProcess = mock(TechProcessInterface.class);
        Mockito.doAnswer(invocationOnMock -> {
            PaymentEntity pe = (PaymentEntity)invocationOnMock.getArguments()[0];
            testRoutineCalled = true;
            assertEquals(1, pe.getTransactions().size());
            assertEquals(1, pe.getTransactions().get(0).getSlips().size());
            assertEquals("Это первая строка.", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(0));
            assertEquals("Это вторая строка.", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(1));
            assertEquals("Это третья строка, разве двух не достаточно?", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(2));
            return null;
        }).when(techProcess).addPayment(Mockito.anyObject());
        TechProcessEvents tpe = mock(TechProcessEvents.class);
        doReturn(tpe).when(techProcess).getTechProcessEvents();

        PowerMockito.mockStatic(Factory.class);
        PowerMockito.mockStatic(CurrencyUtil.class);
        PowerMockito.when(Factory.getTechProcessImpl()).thenReturn(techProcess);
        PowerMockito.when(Factory.getInstance()).thenReturn(null);
        PowerMockito.when(CurrencyUtil.convertMoney(any(BigDecimal.class))).thenReturn(1210L);
        PowerMockito.when(CurrencyUtil.checkPaymentRatio(Mockito.anyLong())).thenReturn(true);
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setDateCreate(new Date());

        ExtPaymentContainer container = new ExtPaymentContainer();
        Whitebox.setInternalState(container, "paymentEntity", paymentEntity);

        container.paymentSaleCompleted(payment);
        if(!testRoutineCalled) {
            fail("Test routine has not been called");
        }
    }

    /**
     * Этот тест проверяет, что в чеке возврата ньюлайны поддерживаются.<br>
     * Мотивация - проверить выполнение задачи <a href="https://crystals.atlassian.net/browse/CORE-224">CORE-224</a>
     */
    @Test
    public void testPaymentReturnCompletedCORE224Test() throws Exception {
        testRoutineCalled = false;
        Payment payment = new Payment();
        payment.setSum(BigDecimal.valueOf(12.10));
        payment.setSlip("Это первая строка.\nЭто вторая строка.\nЭто третья строка, разве двух не достаточно?");

        TechProcessInterface techProcess = PowerMockito.mock(TechProcessInterface.class);
        PowerMockito.doAnswer(invocationOnMock -> {
            PaymentEntity pe = (PaymentEntity)invocationOnMock.getArguments()[0];
            testRoutineCalled = true;
            assertEquals(1, pe.getTransactions().size());
            assertEquals(1, pe.getTransactions().get(0).getSlips().size());
            assertEquals("Это первая строка.", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(0));
            assertEquals("Это вторая строка.", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(1));
            assertEquals("Это третья строка, разве двух не достаточно?", pe.getTransactions().get(0).getSlips().get(0).getSlip().get(2));
            return null;
        }).when(techProcess).addPayment(Mockito.anyObject());
        TechProcessEvents tpe = mock(TechProcessEvents.class);
        doReturn(tpe).when(techProcess).getTechProcessEvents();

        PowerMockito.mockStatic(Factory.class);
        PowerMockito.mockStatic(CurrencyUtil.class);
        PowerMockito.when(Factory.getTechProcessImpl()).thenReturn(techProcess);
        PowerMockito.when(Factory.getInstance()).thenReturn(null);
        PowerMockito.when(CurrencyUtil.convertMoney(any(BigDecimal.class))).thenReturn(1210L);
        PowerMockito.when(CurrencyUtil.checkPaymentRatio(Mockito.anyLong())).thenReturn(true);
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setDateCreate(new Date());

        ExtPaymentContainer container = new ExtPaymentContainer();
        Whitebox.setInternalState(container, "paymentEntity", paymentEntity);

        container.paymentReturnCompleted(payment);
        if(!testRoutineCalled) {
            fail("Test routine has not been called");
        }
    }

    @Test
    public void testPaymentTransactionsSlip() throws Exception {
        PaymentEntity pe = new PaymentEntity();
        TechProcessImpl tp = mock(TechProcessImpl.class);
        TechProcessEvents tpe = mock(TechProcessEvents.class);
        doReturn(tpe).when(tp).getTechProcessEvents(); // NOSONAR
        Mockito.when(tp.getCheckWithNumber()).thenReturn(new PurchaseEntity());

        pe.setDateCreate(new Date());
        pe.setSumPay(1546L);
        ExtPaymentContainer container = mock(ExtPaymentContainer.class);
        Mockito.doCallRealMethod().when(container).paymentCancelCompleted(any());
        Mockito.doCallRealMethod().when(container).paymentSaleCompleted(any());
        Mockito.doCallRealMethod().when(container).paymentReturnCompleted(any());
        PowerMockito.mockStatic(Factory.class);
        PowerMockito.when(Factory.getTechProcessImpl()).thenReturn(tp);
        PowerMockito.mockStatic(CurrencyUtil.class);
        PowerMockito.when(CurrencyUtil.convertMoney(any(BigDecimal.class))).thenReturn(1210L); // Нам нет дела до правильности расчетов здесь, проверяем слипы.
        PowerMockito.when(CurrencyUtil.checkPaymentRatio(Mockito.anyLong())).thenReturn(true);
        Whitebox.setInternalState(container, "paymentEntity", pe);
        Payment singleSlipPayment = new Payment();
        singleSlipPayment.getSlips().addAll(Arrays.asList("Slip1String1", "Slip1String2"));
        container.paymentCancelCompleted(singleSlipPayment);
        assertNotNull(pe.getTransactions());
        assertEquals(1, pe.getTransactions().size());
        assertEquals(2, pe.getTransactions().get(0).getSlips().size());


        Mockito.doAnswer(args -> {
            PaymentEntity pee = (PaymentEntity)args.getArguments()[0];
            assertNotNull(pee.getTransactions());
            assertEquals(1, pee.getTransactions().size());
            assertEquals(2, pee.getTransactions().get(0).getSlips().size());
            return null;
        }).when(tp).addPayment(any());

        pe.getTransactions().clear();
        container.paymentSaleCompleted(singleSlipPayment);

        pe.getTransactions().clear();
        container.paymentReturnCompleted(singleSlipPayment);
    }

    @Test
    public void setPaymentTest() {
        ExtPaymentContainer container = new ExtPaymentContainer();
        container.setPositionsRefund(true);
        container.setRefund(false);

        String paymentId = "paymentId";
        TechProcessInterface tp = mock(TechProcessInterface.class);
        Whitebox.setInternalState(container, "tp", tp);
        InternalCashPoolExecutor threadPool = mock(InternalCashPoolExecutor.class);
        Whitebox.setInternalState(container, "threadPool", threadPool);

        ExtPaymentController pt = mock(ExtPaymentController.class);
        PluginDescription dscr = new PluginDescription();
        PaymentPlugin plugin = mock(PaymentPlugin.class);
        dscr.setPlugin(plugin);
        doReturn(dscr).when(pt).getExternalPluginDescription();
        doReturn(pt).when(tp).getPaymentType(eq(paymentId));
        container.setPaymentId(paymentId);
        doReturn(2011L).when(tp).getSurchargeValue();

        PaymentEntity payment = mock(PaymentEntity.class);
        container.setPayment(payment, null);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(threadPool).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        ArgumentCaptor<RefundRequest> reqCaptor = ArgumentCaptor.forClass(RefundRequest.class);
        verify(plugin).doRefund(reqCaptor.capture());
        assertEquals(CurrencyUtil.convertMoney(2011L), reqCaptor.getValue().getSumToRefund());
    }

    /**
     * Проверим, что если во время возврата оплаты плагин вернул результат "запрос не выполнен" и флаг возврата наличными,
     * то данный плагинный тип оплаты/возврата заменяется на наличные.
     */
    @Test
    public void testRefundPaymentAsCash() {
        PluginDescription pluginDescription = mock(PluginDescription.class);
        Mockito.when(pluginDescription.getPlugin()).thenReturn(mock(PaymentPlugin.class));

        ExtPaymentController controller = Mockito.mock(ExtPaymentController.class);
        Mockito.when(controller.getExternalPluginDescription()).thenReturn(pluginDescription);
        Mockito.doCallRealMethod().when(controller).getReturnPayments(any());

        TechProcessInterface tp = mock(TechProcessInterface.class);
        when(tp.getPaymentType(anyString())).thenReturn(controller);

        Factory factory = mock(Factory.class);
        when(factory.getMainWindow()).thenReturn(mock(MainWindow.class, RETURNS_DEEP_STUBS));

        PowerMockito.mockStatic(Factory.class);
        PowerMockito.when(Factory.getTechProcessImpl()).thenReturn(tp);
        PowerMockito.when(Factory.getInstance()).thenReturn(factory);

        ExtPaymentContainer container = new ExtPaymentContainer();
        container.setPositionsRefund(true);
        container.setRefund(false);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(47L);
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentType("ExternalPayment");
        paymentEntity.setPurchase(purchaseEntity);

        PurchaseEntity returnPurchaseEntity = new PurchaseEntity();
        returnPurchaseEntity.setId(48L);
        returnPurchaseEntity.setSuperPurchase(purchaseEntity);
        when(tp.getCheck()).thenReturn(returnPurchaseEntity);

        Whitebox.setInternalState(container, "tp", tp);
        Whitebox.setInternalState(container, "paymentEntity", paymentEntity);
        Whitebox.setInternalState(container, "extRefundRequest", mock(ExtRefundRequest.class));
        Whitebox.setInternalState(container, "threadPool", mock(InternalCashPoolExecutor.class));

        container.setPaymentId("paymentId");
        container.setPayment(paymentEntity, returnPurchaseEntity);

        Payment paymentResult = new Payment();
        paymentResult.setRefundPaymentAsCash(false);
        container.paymentNotCompleted(paymentResult);
        Supplier<PaymentEntity> returnPayment = () -> controller.getReturnPayments(paymentEntity).get(0);
        assertTrue(returnPayment.get() instanceof PaymentEntity);
        assertEquals("ExternalPayment", returnPayment.get().getPaymentType());

        paymentResult.setRefundPaymentAsCash(true);
        container.paymentNotCompleted(paymentResult);
        assertTrue(returnPayment.get() instanceof CashPaymentEntity);
        assertEquals("CashPaymentEntity", returnPayment.get().getPaymentType());
    }
}
