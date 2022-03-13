package ru.crystals.pos.visualization.check;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.check.CheckStatus;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.payments.CashPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.payments.cftgiftcard.integration.CFTGiftCardPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.cftgiftcard.model.CFTGiftCardPaymentModel;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CFTGiftCardPaymentCheckContainerTest {
    private static class CFTGiftCardPaymentPluginAdapterMock extends CFTGiftCardPaymentPluginAdapter {
        @Override
        protected CFTGiftCardPaymentModel getModel() {
            return super.getModel();
        }
    }

    private static final Long SURCHARGE = 10L;
    private static final Long PAID = 20L;

    private CheckContainer checkContainer;
    private CFTGiftCardPaymentPluginAdapterMock paymentContainer;
    private PaymentEntity paymentEntity = new PaymentEntity();

    @Before
    public void beforeTest() {
        PurchaseEntity check = generatePurchase(0L, SURCHARGE);
        TechProcessInterface tp = Mockito.mock(TechProcessInterface.class);
        BundleManager.add(TechProcessInterface.class, tp);
        Mockito.doReturn(check).when(tp).getCheck();
        Mockito.doReturn(new Properties()).when(tp).getProperties();

        checkContainer = new CheckContainer();
        paymentContainer = Mockito.spy(new CFTGiftCardPaymentPluginAdapterMock());
        Mockito.doNothing().when(paymentContainer).reset();
        paymentEntity.setPaymentSettings(Mockito.mock(PaymentType.class));
        paymentContainer.setPaymentOnly(paymentEntity);
        checkContainer.setPaymentContainer(paymentContainer);
    }

    /**
     * Тест проверяет, что в методе CheckContainer.setupPaymentContainer перед вызовом
     * метода setSurcharge уже установлен платёж PaymentEntity
     */
    @Test
    public void setupPaymentContainerTest() throws Exception {
        checkContainer.setRefund(true);
        paymentContainer.setRefund(true);
        paymentContainer.setFullRefund(true);
        paymentContainer.setPaymentOnly(paymentEntity);
        Mockito.doAnswer(invocation -> {
            PaymentModel model = paymentContainer.getModel();
            Assert.assertNotNull(model.getPayment());
            return null;
        }).when(paymentContainer).setSurcharge(Mockito.anyLong());

        Whitebox.invokeMethod(checkContainer, "setupPaymentContainer", SURCHARGE, PAID);

        Mockito.verify(paymentContainer).setPayment(Mockito.any(), Mockito.any());
        Mockito.verify(paymentContainer).setSurcharge(Mockito.anyLong());
    }

    /**
     * Сгенерить простой чек
     *
     * @param num   номер чека
     * @param sum   сумма чека
     */
    protected PurchaseEntity generatePurchase(long num, long sum) {
        PurchaseEntity p = new PurchaseEntity();
        // Header
        p.setNumber(num);
        p.setCheckSumStart(sum);
        p.setCheckSumEnd(sum);
        p.setCheckStatus(CheckStatus.Registered);
        p.setDiscountValueTotal(0L);
        p.setSale();
        p.setLoyalTransactionId(-1L);
        p.setDateCommit(new Date());
        p.setSentToServerStatus(SentToServerStatus.SENT);
        // Positions
        List<PositionEntity> pss = new ArrayList<PositionEntity>();
        PositionEntity pos = new PositionEntity();
        pos.setItem("111");
        pos.setNds(10F);
        pos.setSum(sum);
        pos.setQnty(1000L);
        pos.setProductType("ProductPieceEntity");
        pos.setPurchase(p);
        pss.add(pos);
        ProductConfig pc = new ProductConfig(){
            @Override
            public boolean isReturnPossible(PositionEntity position) {
                return true;
            }

            @Override
            public Boolean getReturnPossible() {
                return true;
            }
        };
        pos.setProductSettings(pc);
        p.setPositions(pss);
        // Payments
        List<PaymentEntity> pms = new ArrayList<PaymentEntity>();
        CashPaymentEntity pay = new CashPaymentEntity();
        pay.setSumPay(sum);
        pay.setSumPayBaseCurrency(sum);
        pay.setChange(0L);
        pay.setPurchase(p);
        pay.setPaymentType("CashPaymentEntity");
        pay.setNumber(1L);
        pms.add(pay);
        p.setPayments(pms);
        return p;
    }
}
