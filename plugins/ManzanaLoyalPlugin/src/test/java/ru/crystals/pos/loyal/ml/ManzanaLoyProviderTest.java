package ru.crystals.pos.loyal.ml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discounts.enums.MessageDisplayTime;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.providers.FeedbackBundle;
import ru.crystals.loyal.providers.LoyProcessingException;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.CardAlreadyAddedException;
import ru.crystals.pos.ml.MLConnectionSettings;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.ml.exception.MLConnectionException;
import ru.crystals.pos.ml.exception.MLInternalException;
import ru.crystals.pos.ml.exception.MLShouldApplyFakeCardException;

/**
 * Created by agaydenger on 26.07.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ManzanaLoyProviderTest {
    private static final String TEST_COUPON_NUMBER = "TEST_COUPON_NUMBER";
    private static final CardTypeEntity TARGET_FAKE_CARD = CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME);
    private static final String TEST_EXCEPTION_TEXT = "TEST_EXCEPTION_TEXT";
    private static final int TEST_CHECK_NUM = 123;
    private static final long TEST_DATE_MILLIS = 5431223L;
    private static final long TEST_ACTION_GUID = 324012L;
    private static final long RECEIPT_ID = 235436457L;

    @Mock
    private ILoyTechProcess loyTechProcess;

    @Mock
    private MLService mlService;

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
    private MLConnectionSettings mlConnectionSettings;

    @Mock
    private LoyExtProviderFeedback feedback;

    @Spy
    @InjectMocks
    private ManzanaLoyProvider service = new ManzanaLoyProvider();
    
    @Before
    public void init() {
        doReturn(RECEIPT_ID).when(mockPurchaseEntity).getId();
    }

    @Test
    public void testGetCouponType() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn(true).when(mlService).isMlCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(mlService);
        order.verify(mlService).isEnabled();
        order.verify(mlService).isMlCoupon(TEST_COUPON_NUMBER);
        assertThat(couponType).isEqualTo(CardTypes.ExternalCoupon);
    }

    @Test
    public void testGetCouponTypeOnNotMLCoupon() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn(false).when(mlService).isMlCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(mlService);
        order.verify(mlService).isEnabled();
        order.verify(mlService).isMlCoupon(TEST_COUPON_NUMBER);
        assertThat(couponType).isEqualTo(CardTypes.CardNotFound);
    }

    @Test
    public void testGetCouponTypeMLIsNotEnabled() throws Exception {
        //given
        doReturn(false).when(mlService).isEnabled();
        doReturn(true).when(mlService).isMlCoupon(anyString());
        //when
        CardTypes couponType = service.getCouponType(new CardData(TEST_COUPON_NUMBER), loyTechProcess);
        //then
        InOrder order = inOrder(mlService);
        order.verify(mlService).isEnabled();
        verifyNoMoreInteractions(mlService);
        assertThat(couponType).isEqualTo(CardTypes.CardNotFound);
    }

    @Test
    public void testGetProviderName() throws Exception {
        //given

        //when
        String result = service.getProviderName();
        //then
        assertThat(result).isNotNull().isEqualTo(MLService.PROVIDER_NAME);
    }

    @Test
    public void testGetFakeMLCard() throws Exception {
        //given

        //when
        CardTypeEntity result = service.getFakeMLCardType();
        //then
        assertThat(result.equalsForTest(TARGET_FAKE_CARD)).isTrue();
    }

    @Test
    public void testProcess() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn((long)TEST_CHECK_NUM).when(mockPurchaseEntity).getNumber();
        doReturn(resultMockPurchase).when(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        //when
        Purchase result = service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
        //then
        assertThat(resultMockPurchase).isSameAs(result);
        verify(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        verifyNoMoreInteractions(resultMockPurchase, sourceMockPurchase);
    }

    @Test
    public void testProcessThrowsConnectionExceptionButIgnored() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn(new Date(TEST_DATE_MILLIS)).when(mockPurchaseEntity).getDateCreate();
        doReturn((long)TEST_CHECK_NUM).when(mockPurchaseEntity).getNumber();
        doReturn(sourceMockPurchase).when(sourceMockPurchase).cloneWithDisc();
        doReturn(mlConnectionSettings).when(mlService).getSettings();
        doReturn(TEST_ACTION_GUID).when(mlConnectionSettings).getActionGuid();
        doThrow(new MLConnectionException(TEST_EXCEPTION_TEXT)).when(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        doReturn(Arrays.asList(firstResult, secondResult)).when(sourceMockPurchase).getAdvertisingActionResults();
        ArgumentCaptor<List> actionResultCaptor = ArgumentCaptor.forClass(List.class);
        //when
        Purchase result = service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
        //then
        verifyPurchaseOnCatchedException(actionResultCaptor, result);
    }

    @Test
    public void testProcessThrowsInternalExceptionButIgnored() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn(sourceMockPurchase).when(sourceMockPurchase).cloneWithDisc();
        doReturn((long)TEST_CHECK_NUM).when(mockPurchaseEntity).getNumber();
        doReturn(mlConnectionSettings).when(mlService).getSettings();
        doReturn(TEST_ACTION_GUID).when(mlConnectionSettings).getActionGuid();
        doThrow(new MLInternalException(TEST_EXCEPTION_TEXT)).when(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        doReturn(Arrays.asList(firstResult, secondResult)).when(sourceMockPurchase).getAdvertisingActionResults();
        ArgumentCaptor<List> actionResultCaptor = ArgumentCaptor.forClass(List.class);
        //when
        Purchase result = service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
        //then
        verifyPurchaseOnCatchedException(actionResultCaptor, result);
    }

    @Test
    public void testProcessThrowsShouldApplyFakeCard() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn((long)TEST_CHECK_NUM).when(sourceMockPurchase).getNumber();
        doReturn((long)TEST_CHECK_NUM).when(mockPurchaseEntity).getNumber();
        doReturn(sourceMockPurchase).when(sourceMockPurchase).cloneWithDisc();
        doThrow(new MLShouldApplyFakeCardException(TEST_EXCEPTION_TEXT)).when(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        doReturn(TEST_COUPON_NUMBER).when(mlService).getFakeCardNo();
        doReturn(TARGET_FAKE_CARD).when(service).getFakeMLCardType();
        doReturn(TEST_CHECK_NUM).when(checkService).getCurrentCheckNum();
        //when
        try {
            service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
            fail("Expected LoyProcessingException but never throws");
        } catch (LoyProcessingException e) {
            //then
            verifyOnLoyProcessingException(e);
        }
    }

    @Test
    public void testProcessThrowsShouldApplyFakeCardFailedButIgnored() throws Exception {
        //given
        doReturn(true).when(mlService).isEnabled();
        doReturn((long)TEST_CHECK_NUM).when(sourceMockPurchase).getNumber();
        doReturn((long)TEST_CHECK_NUM).when(mockPurchaseEntity).getNumber();
        doThrow(new MLShouldApplyFakeCardException(TEST_EXCEPTION_TEXT)).when(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        doReturn(TEST_COUPON_NUMBER).when(mlService).getFakeCardNo();
        doReturn(TARGET_FAKE_CARD).when(service).getFakeMLCardType();
        doReturn(TEST_CHECK_NUM).when(checkService).getCurrentCheckNum();
        doThrow(new CardAlreadyAddedException()).when(checkService).addCard(anyString(), any(CardTypeEntity.class), anyInt(), any(InsertType.class));
        //when
        try {
            service.process(sourceMockPurchase, mockPurchaseEntity, loyTechProcess);
            fail("Expected LoyProcessingException but never throws");
        } catch (LoyProcessingException e) {
            verifyOnLoyProcessingException(e);

        }
    }

    private void verifyOnLoyProcessingException(LoyProcessingException e) throws MLConnectionException, MLShouldApplyFakeCardException, MLInternalException, CardAlreadyAddedException {
        //then
        verify(mockPurchaseEntity, times(3)).getNumber();
        verify(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        verify(mlService).getFakeCardNo();
        verify(service).getFakeMLCardType();
        verify(checkService).addCard(TEST_COUPON_NUMBER, TARGET_FAKE_CARD, TEST_CHECK_NUM, null);
        assertThat(e.getMessage()).isEqualTo(TEST_EXCEPTION_TEXT);
    }

    private void verifyPurchaseOnCatchedException(ArgumentCaptor<List> actionResultCaptor, Purchase result) throws MLConnectionException, MLShouldApplyFakeCardException, MLInternalException {
        verify(mockPurchaseEntity, times(3)).getNumber();
        verify(sourceMockPurchase).cloneWithDisc();
        verify(mlService).calcDiscount(sourceMockPurchase, "" + TEST_CHECK_NUM, RECEIPT_ID, loyTechProcess);
        verify(sourceMockPurchase, times(2)).setAdvertisingActionResults(actionResultCaptor.capture());
        assertThat(result).isSameAs(sourceMockPurchase);
        List<AdvertisingActionResultEntity> advActionsResult = actionResultCaptor.getValue();
        assertThat(advActionsResult).hasSize(1);
        AdvertisingActionResultEntity firstResultEntity = advActionsResult.get(0);
        assertThat(firstResultEntity).isInstanceOf(MessageAdvertisingActionResultEntity.class);
        assertThat(((MessageAdvertisingActionResultEntity)firstResultEntity).getDisplayTime()).isEqualTo(MessageDisplayTime.SUBTOTAL);
        assertThat(((MessageAdvertisingActionResultEntity)firstResultEntity).getOperatorMsg()).hasSize(1).containsOnly(TEST_EXCEPTION_TEXT);
        assertThat(firstResultEntity.getAdvertisingActionGUID()).isEqualTo(TEST_ACTION_GUID);
    }

    @Test
    public void testSendFeedback() {
        //given
        doReturn(true).when(mlService).isEnabled();
        //when
        service.sendFeedback(new FeedbackBundle(feedback, null, null));
        //then
        verify(mlService).isEnabled();
        verify(mlService).commitDiscounts(feedback);
        verifyNoMoreInteractions(feedback, mlService);
    }

    @Test
    public void testSendFeedbackMLServiceUnavailable() {
        //given
        doReturn(false).when(mlService).isEnabled();
        //when
        service.sendFeedback(new FeedbackBundle(feedback, null, null));
        //then
        verify(mlService).isEnabled();
        verify(mlService, never()).commitDiscounts(feedback);
        verifyNoMoreInteractions(feedback, mlService);
    }
}
