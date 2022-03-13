package ru.crystals.pos.loyal.sc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.providers.FeedbackBundle;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.sc.SCConnectionSettings;
import ru.crystals.pos.sc.SCService;
import ru.crystals.pos.sc.exception.SCException;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by agaydenger on 26.07.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SCLoyProviderTest {
    private static final String TEST_COUPON_NUMBER = "TEST_COUPON_NUMBER";
    private static final CardTypeEntity TARGET_FAKE_CARD = CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, ru.crystals.pos.sc.SCService.PROVIDER_NAME);
    private static final long TEST_DATE_MILLIS = 5431223L;
    private static final long RECEIPT_ID = 235436457L;

    @Mock
    private ILoyTechProcess loyTechProcess;

    @Mock
    private SCService scService;

    @Mock
    private Purchase sourceMockPurchase;

    @Mock
    private Purchase resultMockPurchase;

    @Mock
    private PurchaseEntity mockPurchaseEntity;

    @Mock
    private CheckService checkService;

    @Mock
    private AdvertisingActionResultEntity firstResult;

    @Mock
    private AdvertisingActionResultEntity secondResult;

    @Mock
    private SCConnectionSettings scConnectionSettings;

    @Mock
    private LoyExtProviderFeedback feedback;

    @Spy
    @InjectMocks
    private SCLoyProvider service = new SCLoyProvider();

    @Before
    public void init() {
        doReturn(RECEIPT_ID).when(mockPurchaseEntity).getId();
    }

    @Test
    public void testGetCouponType() {
        //given
        doReturn(true).when(scService).isEnabled();
        doReturn(true).when(scService).isSmChCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(scService);
        order.verify(scService).isEnabled();
        order.verify(scService).isSmChCoupon(TEST_COUPON_NUMBER);
        assertThat(couponType).isEqualTo(CardTypes.ExternalCoupon);
    }

    @Test
    public void testGetCouponTypeOnNotSCCoupon() {
        //given
        doReturn(true).when(scService).isEnabled();
        doReturn(false).when(scService).isSmChCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(scService);
        order.verify(scService).isEnabled();
        order.verify(scService).isSmChCoupon(TEST_COUPON_NUMBER);
        assertThat(couponType).isEqualTo(CardTypes.CardNotFound);
    }

    @Test
    public void testGetCouponTypeSCIsNotEnabled() {
        //given
        doReturn(false).when(scService).isEnabled();
        doReturn(true).when(scService).isSmChCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(scService);
        order.verify(scService).isEnabled();
        verifyNoMoreInteractions(scService);
        assertThat(couponType).isEqualTo(CardTypes.CardNotFound);
    }

    @Test
    public void testGetCouponTypeOnBarcodeDecoded() {
        //given
        doReturn(true).when(scService).isEnabled();
        doReturn(true).when(scService).isSmChCoupon(anyString());
        //when
        CardData couponData = new CardData(TEST_COUPON_NUMBER);
        couponData.setBarcodeDecoded(true);
        CardTypes couponType = service.getCouponType(couponData, loyTechProcess);
        //then
        assertThat(couponType).isEqualTo(CardTypes.CardNotFound);
    }

    @Test
    public void testGetProviderName() {
        //given

        //when
        String result = service.getProviderName();
        //then
        assertThat(result).isNotNull().isEqualTo(SCService.PROVIDER_NAME);
    }

    @Test
    public void testGetFakeSCCard() {
        //given

        //when
        CardTypeEntity result = service.getFakeSCCardType();
        //then
        assertThat(result.equalsForTest(TARGET_FAKE_CARD)).isTrue();
    }

    @Test
    public void testProcess() throws Exception {
        //given
        doReturn(true).when(scService).isEnabled();
        doReturn(new Date(TEST_DATE_MILLIS)).when(mockPurchaseEntity).getDateCreate();
        doReturn(RECEIPT_ID).when(mockPurchaseEntity).getId();
        Purchase target = new Purchase();
        doReturn(target).when(scService).calcDiscount(any(Purchase.class), anyString(), anyLong(), any(ILoyTechProcess.class));
        //when
        Purchase result = service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
        //then
        verify(mockPurchaseEntity).getDateCreate();
        verify(mockPurchaseEntity).getId();
        verify(scService).calcDiscount(sourceMockPurchase, String.valueOf(TEST_DATE_MILLIS), RECEIPT_ID, loyTechProcess);
        assertThat(result).isNotNull().isSameAs(target);
        verifyNoMoreInteractions(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
    }

    @Test
    public void testProcessCalcDiscountThrowsException() throws Exception {
        //given
        doReturn(true).when(scService).isEnabled();
        doReturn(new Date(TEST_DATE_MILLIS)).when(mockPurchaseEntity).getDateCreate();
        doReturn(RECEIPT_ID).when(mockPurchaseEntity).getId();
        doThrow(new SCException("")).when(scService).calcDiscount(any(Purchase.class), anyString(), anyLong(), any(ILoyTechProcess.class));
        doReturn(resultMockPurchase).when(sourceMockPurchase).cloneWithDisc();
        doReturn(scConnectionSettings).when(scService).getSettings();
        //when
        Purchase result = service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
        //then
        verify(scService).getSettings();
        verify(scConnectionSettings).getActionGuid();
        assertThat(result).isNotNull().isSameAs(resultMockPurchase);
    }

    @Test
    public void testSendFeedback() {
        //given
        doReturn(true).when(scService).isEnabled();
        //when
        service.sendFeedback(new FeedbackBundle(feedback, null, null));
        //then
        verify(scService).isEnabled();
        verify(scService).commitDiscounts(feedback);
        verifyNoMoreInteractions(scService, feedback);
    }

    @Test
    public void testSendFeedbackDoNothingWhenServiceUnavailable() {
        //given
        doReturn(false).when(scService).isEnabled();
        //when
        service.sendFeedback(new FeedbackBundle(feedback, null, null));
        //then
        verify(scService).isEnabled();
        verifyNoMoreInteractions(scService, feedback);
    }

    @Test
    public void testCheckCanceled() {
        //given
        doReturn(true).when(scService).isEnabled();
        //when
        service.checkCanceled(mockPurchaseEntity);
        //then
        verify(scService).isEnabled();
        verify(scService).checkCanceled(mockPurchaseEntity);
        verifyNoMoreInteractions(scService, mockPurchaseEntity);
    }

}
