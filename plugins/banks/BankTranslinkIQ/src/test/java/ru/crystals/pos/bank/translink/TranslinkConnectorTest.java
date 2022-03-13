package ru.crystals.pos.bank.translink;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.translink.api.RequestSender;
import ru.crystals.pos.bank.translink.api.dto.OpenPosResponse;
import ru.crystals.pos.bank.translink.api.dto.PosOperation;
import ru.crystals.pos.bank.translink.api.dto.Result;
import ru.crystals.pos.bank.translink.api.dto.ResultCode;
import ru.crystals.pos.bank.translink.api.dto.TrnState;
import ru.crystals.pos.bank.translink.api.dto.commands.AuthorizeCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDayCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDocCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.LockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RefundCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RemoveCardCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.UnlockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.VoidCommand;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;
import ru.crystals.pos.bank.translink.api.dto.events.CardFlags;
import ru.crystals.pos.bank.translink.api.dto.events.EventType;
import ru.crystals.pos.bank.translink.api.dto.events.OnCardEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnDisplayTextEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnPrintEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnTrnStatusEvent;
import ru.crystals.pos.i18n.I18nConfig;
import ru.crystals.utils.time.DateConverters;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.crystals.pos.bank.translink.api.dto.events.BaseEvent.NO_MORE_EVENTS;

@RunWith(MockitoJUnitRunner.class)
public class TranslinkConnectorTest {

    private static final String AUTH_SLIP = "\\mc\n" +
            "shop\n" +
            "slip\n" +
            "\\cl\n" +
            "slip\n" +
            "\n" +
            "for\n" +
            "\n" +
            "customer";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final LockDeviceCommand LOCK_DEVICE_COMMAND_PARAMS = new LockDeviceCommand(ResBundleBankTranslink.getString("IDLE_TEXT_ON_LOCK"));

    private static final Result DECLINED = new Result(ResultCode.DECLINED, "Command declined");
    private static final Result OK = new Result(ResultCode.OK, "Command accepted");

    private static final LocalDateTime TXN_DATE = LocalDateTime.parse("2020-05-11T15:34:21");
    private static final String NEXT_DOC_NUMBER = "123456ABCDEF";
    private static final String ORIGINAL_DOC_NUMBER = "ORIGINAL_DOC_NUMBER";
    private static final String ORIGINAL_OPERATION_ID = "ORIGINAL_OPERATION_ID";
    private static final String ORIGINAL_RRN = "ORIGINAL_RRN";
    private static final String OPERATION_ID = "284EF2F8865BB5AC";
    private static final String CARD_PAN = "416973******6841";
    private static final String CARD_HASH = "759A98BAD15EE89F2BDC6E7A43654D0D4F505C37";
    private static final String AZN = "AZN";
    private static final String AZN_944 = "944";
    private static final long AMOUNT = 1234;
    private static final List<List<String>> AUTH_SLIP_EXPECTED = Arrays.asList(Arrays.asList("slip", "", "for", "", "customer"),
            Arrays.asList("shop", "slip"));

    @Mock
    RequestSender rs;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    TimerSupplier ts;

    @Mock
    I18nConfig i18nConfig;

    @Mock
    TranslinkSessionProperties props;

    @InjectMocks
    TranslinkConnector tlc;

    private final BankCard CARD;
    private final SaleData SALE_DATA;

    {
        CARD = new BankCard();
        CARD.setCardNumber(CARD_PAN);
        CARD.setCardNumberHash(CARD_HASH);

        SALE_DATA = new SaleData();
        SALE_DATA.setAmount(AMOUNT);
        SALE_DATA.setBankId("translink");
        SALE_DATA.setCurrencyCode(AZN);
    }

    @Before
    public void setUp() throws BankCommunicationException {
        when(props.getDocNumber()).thenReturn(Optional.empty());
        when(props.getAccessToken()).thenReturn(Optional.empty());
        when(props.nextDocNumber()).thenReturn(NEXT_DOC_NUMBER);
        when(i18nConfig.getLocale()).thenReturn(Locale.forLanguageTag("az-AZ"));

        when(rs.openPos()).thenReturn(new OpenPosResponse(ACCESS_TOKEN));
        whenSent(Command.UNLOCKDEVICE).thenReturn(OK);
        whenSent(Command.LOCKDEVICE).thenReturn(OK);
        whenSent(Command.REMOVECARD).thenReturn(OK);
        whenSent(Command.CLOSEDAY).thenReturn(OK);
        whenSent(Command.CLOSEDOC).thenReturn(OK);
        whenSent(Command.AUTHORIZE).thenReturn(OK);
        whenSent(Command.REFUND).thenReturn(OK);
        whenSent(Command.VOID).thenReturn(OK);

        when(ts.getCardReadTimer().isNotExpired()).thenReturn(true);
        when(ts.getCardReadTimer().isExpired()).thenReturn(false);
        when(ts.getCloseDayTimer().isNotExpired()).thenReturn(true);
        when(ts.getCloseDayTimer().isExpired()).thenReturn(false);
        when(ts.getNowTime()).thenReturn(TXN_DATE);
    }

    @Test
    public void closeDayTest() throws BankException, InterruptedException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint("a\n\nb"));

        final List<String> slip = tlc.closeDay();

        Assert.assertEquals(Arrays.asList("a", "", "b"), slip);

        final InOrder io = inOrder(rs);
        io.verify(rs).openPos();
        io.verify(rs).setAccessToken(ACCESS_TOKEN);
        io.verify(rs).getEvent();
        io.verify(rs).sendCommand(Command.CLOSEDAY, new CloseDayCommand(null, null));
        io.verify(rs, times(3)).getEvent();
        io.verify(rs).closePos();
        io.verifyNoMoreInteractions();

        verify(ts.getCloseDayTimer(), times(2)).isExpired();
        verify(ts, times(2)).sleep(anyLong());
    }

    private BaseEvent makeOnPrint(String s) {
        return new BaseEvent(EventType.ONPRINT, new OnPrintEvent(s));
    }

    @Test
    public void closeDayTimerExpiredTest() throws InterruptedException, BankCommunicationException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS);
        when(ts.getCloseDayTimer().isExpired())
                .thenReturn(false)
                .thenReturn(true);

        try {
            tlc.closeDay();
            Assert.fail("No exceptions on expired timeout (unexpected)");
        } catch (Exception e) {
            assertTrue(e instanceof BankException);
            assertEquals(ResBundleBankTranslink.getString("TERMINAL_TIMEOUT"), e.getMessage());
        }

        final InOrder io = inOrder(rs);
        io.verify(rs).openPos();
        io.verify(rs).setAccessToken(ACCESS_TOKEN);
        io.verify(rs).getEvent();
        io.verify(rs).sendCommand(Command.CLOSEDAY, new CloseDayCommand(null, null));
        io.verify(rs, times(2)).getEvent();
        io.verify(rs).closePos();
        io.verifyNoMoreInteractions();

        verify(ts.getCloseDayTimer(), times(2)).isExpired();
        verify(ts, times(1)).sleep(anyLong());
        verify(props).getDocNumber();
        verify(props, never()).nextDocNumber();
    }

    @Test
    public void closeDayTimerErrorOnSend() throws InterruptedException, BankCommunicationException {
        whenSent(Command.CLOSEDAY).thenReturn(DECLINED);
        when(rs.getEvent()).thenReturn(NO_MORE_EVENTS);

        try {
            tlc.closeDay();
            Assert.fail("No exceptions on expired timeout (unexpected)");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof BankException);
            assertEquals(ResBundleBankTranslink.getString("RC_DECLINED"), e.getMessage());
        }

        final InOrder io = inOrder(rs);
        io.verify(rs).openPos();
        io.verify(rs).setAccessToken(ACCESS_TOKEN);
        io.verify(rs).getEvent();
        io.verify(rs).sendCommand(Command.CLOSEDAY, new CloseDayCommand(null, null));
        io.verify(rs).closePos();
        io.verifyNoMoreInteractions();

        verify(ts.getCloseDayTimer(), never()).isNotExpired();
        verify(ts, never()).sleep(anyLong());
    }

    @Test
    public void saleTest() throws BankException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onCard(CardFlags.builder().allowAuthorize(true)))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите PIN"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint(AUTH_SLIP))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onTrnStatus(TrnState.Approved));

        final AuthorizationData actualAd = tlc.sale(SALE_DATA);

        AuthorizationData expectedAd = makeAuthData(true, BankOperationType.SALE, PosOperation.AUTHORIZE);
        assertEquals(expectedAd, actualAd);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.AUTHORIZE));
        inOrder.verify(rs, times(4)).getEvent();
        inOrder.verify(props).nextDocNumber();
        inOrder.verify(rs).sendCommand(Command.AUTHORIZE, makeAuthCommand());
        inOrder.verify(rs, times(6)).getEvent();
        inOrder.verify(rs).sendCommand(Command.CLOSEDOC, new CloseDocCommand(OPERATION_ID, NEXT_DOC_NUMBER));
        inOrder.verify(props).closeDocNumber();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();

        verify(props).nextDocNumber();
    }

    @Test
    public void saleCardNotAllowedTest() throws BankCommunicationException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(onCard(CardFlags.builder().allowAuthorize(false)));

        saleWithError(SALE_DATA, ResBundleBankTranslink.getString("NOT_ALLOWED_TO_AUTHORIZE"), null);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.AUTHORIZE));
        inOrder.verify(rs, times(2)).getEvent();
        inOrder.verify(rs).sendCommand(Command.REMOVECARD, new RemoveCardCommand(ResBundleBankTranslink.getString("NOT_ALLOWED_TO_AUTHORIZE")));
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void saleOnDeclinedTxnTest() throws BankException {
        when(props.getDocNumber()).thenReturn(Optional.of("NOT_CLOSED_DOC"));
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onCard(CardFlags.builder().allowAuthorize(true)))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите PIN"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint(AUTH_SLIP))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onTrnStatus(TrnState.Declined));

        final AuthorizationData actualAd = tlc.sale(SALE_DATA);

        AuthorizationData expectedAd = makeAuthData(false, BankOperationType.SALE, PosOperation.AUTHORIZE);
        expectedAd.setResponseCode(TrnState.Declined.name());
        assertEquals(expectedAd, actualAd);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs).sendCommand(Command.CLOSEDOC, new CloseDocCommand(null, "NOT_CLOSED_DOC"));
        inOrder.verify(props).closeDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.AUTHORIZE));
        inOrder.verify(rs, times(4)).getEvent();
        inOrder.verify(props).nextDocNumber();
        inOrder.verify(rs).sendCommand(Command.AUTHORIZE, makeAuthCommand());
        inOrder.verify(rs, times(6)).getEvent();
        inOrder.verify(rs).sendCommand(Command.CLOSEDOC, new CloseDocCommand(OPERATION_ID, NEXT_DOC_NUMBER));
        inOrder.verify(props).closeDocNumber();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();

        verify(props).nextDocNumber();
    }


    private void saleWithError(SaleData sd, String message, AuthorizationData expectedAuthData) {
        BankException actualError = null;
        try {
            tlc.sale(sd);
        } catch (BankException be) {
            actualError = be;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception has not expected type: " + e.getClass());
        }
        assertNotNull("No expected exception", actualError);
        assertEquals(actualError.getMessage(), message);
        assertEquals(expectedAuthData, actualError.getAuthorizationData());
    }

    @Test
    public void refundTest() throws BankException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onCard(CardFlags.builder()
                        .allowAuthorize(true)
                        .allowRefund(true)
                        .reqOriginalRRN(false)
                ))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите PIN"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint(AUTH_SLIP))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onTrnStatus(TrnState.Approved));

        RefundData rd = new RefundData();
        rd.setAmount(AMOUNT);
        rd.setBankId("translink");
        rd.setCurrencyCode(AZN);
        rd.setAuthCode(ORIGINAL_OPERATION_ID);
        rd.setRefNumber(ORIGINAL_RRN);
        rd.setMerchantId(ORIGINAL_DOC_NUMBER);

        final AuthorizationData actualAd = tlc.refund(rd);

        AuthorizationData expectedAd = makeAuthData(true, BankOperationType.REFUND, PosOperation.CREDIT);
        assertEquals(expectedAd, actualAd);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.CREDIT));
        inOrder.verify(rs, times(4)).getEvent();
        inOrder.verify(props).nextDocNumber();
        inOrder.verify(rs).sendCommand(Command.REFUND, RefundCommand.builder()
                .amount(AMOUNT)
                .documentNr(NEXT_DOC_NUMBER)
                .currencyCode(AZN_944)
                .build());
        inOrder.verify(rs, times(6)).getEvent();
        inOrder.verify(rs).sendCommand(Command.CLOSEDOC, new CloseDocCommand(OPERATION_ID, NEXT_DOC_NUMBER));
        inOrder.verify(props).closeDocNumber();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void refundByRRNTest() throws BankException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onCard(CardFlags.builder()
                        .allowAuthorize(true)
                        .allowRefund(true)
                        .reqOriginalRRN(true)
                ))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите PIN"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint(AUTH_SLIP))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onTrnStatus(TrnState.Approved));

        RefundData rd = new RefundData();
        rd.setAmount(AMOUNT);
        rd.setBankId("translink");
        rd.setCurrencyCode(AZN);
        rd.setAuthCode(ORIGINAL_OPERATION_ID);
        rd.setRefNumber(ORIGINAL_RRN);
        rd.setMerchantId(ORIGINAL_DOC_NUMBER);

        final AuthorizationData actualAd = tlc.refund(rd);

        AuthorizationData expectedAd = makeAuthData(true, BankOperationType.REFUND, PosOperation.CREDIT);
        assertEquals(expectedAd, actualAd);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.CREDIT));
        inOrder.verify(rs, times(4)).getEvent();
        inOrder.verify(props).nextDocNumber();
        inOrder.verify(rs).sendCommand(Command.REFUND, RefundCommand.builder()
                .amount(AMOUNT)
                .documentNr(NEXT_DOC_NUMBER)
                .currencyCode(AZN_944)
                .rrn(ORIGINAL_RRN)
                .build());
        inOrder.verify(rs, times(6)).getEvent();
        inOrder.verify(rs).sendCommand(Command.CLOSEDOC, new CloseDocCommand(OPERATION_ID, NEXT_DOC_NUMBER));
        inOrder.verify(props).closeDocNumber();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void refundWithoutOriginalWhenRRNRequiredTest() throws BankException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onCard(CardFlags.builder()
                        .allowAuthorize(true)
                        .allowRefund(true)
                        .reqOriginalRRN(true)
                ))
                .thenReturn(NO_MORE_EVENTS);

        RefundData rd = new RefundData();
        rd.setAmount(AMOUNT);
        rd.setBankId("translink");
        rd.setCurrencyCode(AZN);

        BankException actualError = null;
        try {
            tlc.refund(rd);
        } catch (BankException be) {
            actualError = be;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception has not expected type: " + e.getClass());
        }
        assertNotNull("No expected exception", actualError);
        assertEquals(actualError.getMessage(), ResBundleBankTranslink.getString("UNABLE_TO_REFUND_WITHOUT_RRN"));
        assertNull(actualError.getAuthorizationData());

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.CREDIT));
        inOrder.verify(rs, times(4)).getEvent();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void reversalTest() throws BankException {
        when(rs.getEvent())
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnDisplayText("Введите карту"))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(makeOnPrint(AUTH_SLIP))
                .thenReturn(NO_MORE_EVENTS)
                .thenReturn(onTrnStatus(TrnState.Approved));

        ReversalData rd = new ReversalData();
        rd.setAmount(AMOUNT);
        rd.setBankId("translink");
        rd.setCurrencyCode(AZN);
        rd.setAuthCode(OPERATION_ID);
        rd.setRefNumber(NEXT_DOC_NUMBER);

        final AuthorizationData actualAd = tlc.reversal(rd);

        AuthorizationData expectedAd = makeAuthData(true, BankOperationType.REVERSAL, PosOperation.NOOPERATION);
        expectedAd.setCard(null);
        assertEquals(expectedAd, actualAd);

        final InOrder inOrder = inOrder(rs, props);
        inOrder.verify(rs).openPos();
        inOrder.verify(rs).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).setAccessToken(ACCESS_TOKEN);
        inOrder.verify(props).getDocNumber();
        inOrder.verify(rs, times(1)).getEvent();
        inOrder.verify(rs).sendCommand(Command.UNLOCKDEVICE, makeUnlock(PosOperation.NOOPERATION));
        inOrder.verify(rs).sendCommand(Command.VOID, new VoidCommand(OPERATION_ID));
        inOrder.verify(rs, times(6)).getEvent();
        inOrder.verify(rs).sendCommand(Command.LOCKDEVICE, LOCK_DEVICE_COMMAND_PARAMS);
        inOrder.verify(rs).closePos();
        inOrder.verify(props).clearAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    private BaseEvent onCard(CardFlags.CardFlagsBuilder flags) {
        return new BaseEvent(EventType.ONCARD, OnCardEvent.builder()
                .flags(flags.build())
                .hash(CARD_HASH)
                .pan(CARD_PAN)
                .currencyCode(AZN_944)
                .build());
    }

    private BaseEvent onTrnStatus(TrnState approved) {
        return new BaseEvent(EventType.ONTRNSTATUS, OnTrnStatusEvent.builder()
                .amountAuthorized(AMOUNT)
                .authCode("20228Z")
                .rrn(ORIGINAL_RRN)
                .stan("28")
                .operationId(OPERATION_ID)
                .documentNr(NEXT_DOC_NUMBER)
                .state(approved)
                .text("Approved - 00")
                .build());
    }

    private AuthorizeCommand makeAuthCommand() {
        return AuthorizeCommand.builder()
                .amount(AMOUNT)
                .documentNr(NEXT_DOC_NUMBER)
                .currencyCode(AZN_944)
                .build();
    }

    private AuthorizationData makeAuthData(boolean status, BankOperationType opType, PosOperation operation) {
        AuthorizationData expectedAd = new AuthorizationData();
        expectedAd.setSlips(AUTH_SLIP_EXPECTED);
        expectedAd.setAuthCode(OPERATION_ID);
        expectedAd.setRefNumber(ORIGINAL_RRN);
        expectedAd.setMerchantId(NEXT_DOC_NUMBER);
        expectedAd.setResponseCode("Approved");
        expectedAd.setMessage("Approved - 00");
        expectedAd.setDate(DateConverters.toDate(TXN_DATE));
        expectedAd.setCard(CARD);
        expectedAd.setStatus(status);
        expectedAd.setOperationType(opType);
        expectedAd.setOperationCode((long) operation.getCode());
        return expectedAd;
    }

    private BaseEvent makeOnDisplayText(String s) {
        return new BaseEvent(EventType.ONDISPLAYTEXT, new OnDisplayTextEvent(s));
    }

    private UnlockDeviceCommand makeUnlock(PosOperation operation) {
        return UnlockDeviceCommand.builder()
                .posOperation(operation)
                .language("AZ")
                .currencyCode(AZN_944)
                .ecrVersion("CSI-PASHABANK-1.1.0")
                .amount(AMOUNT)
                .idleText(ResBundleBankTranslink.getString("IDLE_TEXT_ON_UNLOCK"))
                .build();
    }

    private OngoingStubbing<Result> whenSent(Command command) throws BankCommunicationException {
        if (command.getParamClass() == null) {
            return Mockito.when(rs.sendCommand(eq(command)));
        }
        return Mockito.when(rs.sendCommand(eq(command), any(command.getParamClass())));
    }

}