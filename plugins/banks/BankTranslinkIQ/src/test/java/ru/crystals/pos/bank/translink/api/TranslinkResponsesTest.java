package ru.crystals.pos.bank.translink.api;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.bank.translink.api.dto.AmountAdditional;
import ru.crystals.pos.bank.translink.api.dto.OpenPosResponse;
import ru.crystals.pos.bank.translink.api.dto.Result;
import ru.crystals.pos.bank.translink.api.dto.ResultCode;
import ru.crystals.pos.bank.translink.api.dto.TransactionResult;
import ru.crystals.pos.bank.translink.api.dto.TrnState;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;
import ru.crystals.pos.bank.translink.api.dto.events.CardFlags;
import ru.crystals.pos.bank.translink.api.dto.events.EventProperties;
import ru.crystals.pos.bank.translink.api.dto.events.EventType;
import ru.crystals.pos.bank.translink.api.dto.events.InstallmentCardFlags;
import ru.crystals.pos.bank.translink.api.dto.events.InstallmentForm;
import ru.crystals.pos.bank.translink.api.dto.events.OnCardEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnDisplayTextEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnMsgBoxEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnPrintEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnPromptEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnSelectEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnTrnStatusEvent;

import java.util.Arrays;
import java.util.Collections;


public class TranslinkResponsesTest extends TranslinkJsonConverterTest {

    @Test
    public void openPosResponse() {
        checkDeserialization("openpos", new OpenPosResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZWZhdWx0IiwianRpIjoiYTk2ZDkzODQ" +
                        "tYjE1OS00Mjk5LTkzYjEtMzFjMzk1MjYxMGEzIiwiaWF0IjoiNC8yMS8yMDIwIDc6NDM6NTggQU0iLCJ" +
                        "uYmYiOjE1ODc0NTUwMzgsImV4cCI6MTU4NzQ2NzAzOCwiaXNzIjoiQXNoYnVybiBJbnRlcm5hdGlvbmF" +
                        "sIiwiYXVkIjoieENvbm5lY3QgUE9TIn0.JJaWUXaaIKHMK19vc9ocZd6Dp2TBiLXQVkA9OHCVeS0", Result.OK));
    }

    @Test
    public void openPosAlreadyOpenedResponse() {
        checkDeserialization("openpos_already_opened", new OpenPosResponse(new Result(ResultCode.ANOTHER_OPERATION_IN_PROGRESS,
                "Session is already open, close it before opening a new one")));
    }

    @Test
    public void resultResponse() {
        checkDeserialization("result", new Result(ResultCode.DECLINED, "Transaction is not found, does not exists"));
    }

    @Test
    public void transactionResultResponse() {
        checkDeserialization("transaction_result", TransactionResult.builder()
                .operationId("FCAB2EB6D0DFD12")
                .amountAuthorized(1000)
                .documentNr("129663")
                .cryptogram("4E6998DF9D9CD4CF70F90BA05160ED7D890F3134D8FA6C162FDF5B6C363085B0275AEC227F1" +
                        "7DB99A45EB11C61925301DBEB71D759AA93925A9F382CD931309BF5864922B378AEEB4F6B0C0CC6C58EC")
                .authCode("047625")
                .rrn("936107031714")
                .stan("1")
                .cardType("Visa")
                .state(TrnState.Approved)
                .amountAdditional(Arrays.asList(
                        new AmountAdditional("CA", "978", 14),
                        new AmountAdditional("40", "978", 1)
                ))
                .result(new Result(ResultCode.OK, "Transaction approved"))
                .build()
        );
    }

    @Test
    public void onNoMoreEvents() {
        final BaseEvent expected = BaseEvent.NO_MORE_EVENTS;
        final BaseEvent actual = checkEventDeserialization("no_more_events", expected);
        Assert.assertSame(expected, actual);
    }

    @Test
    public void onUnknownEvent() {
        final BaseEvent expected = BaseEvent.UNKNOWN_EVENT;
        final BaseEvent actual = checkEventDeserialization("SOME_UNKNOWN_EVENT", expected);
        Assert.assertSame(expected, actual);
    }

    @Test
    public void onCard() {
        checkEventDeserialization(EventType.ONCARD, OnCardEvent.builder()
                .track1("1000")
                .track2("0")
                .track3("129663")
                .pan("************8261")
                .cardType("Visa")
                .currencyCode("978")
                .hash("")
                .additionalCurrencyCodes(Collections.singletonList("840"))
                .flags(CardFlags.builder()
                        .allowAuthorize(true)
                        .allowPreAuthorize(true)
                        .allowRefund(true)
                        .fullAmountOnly(false)
                        .noDiscounts(false)
                        .reqPANL4Digit(false)
                        .allowCashBack(false)
                        .reqOriginalRRN(true)
                        .build())
                .installmentCardFlags(InstallmentCardFlags.builder()
                        .allowInstallmentIssuer(false)
                        .allowInstallmentAcquirer(true)
                        .allowInstallmentMerchant(true)
                        .installmentFormAcquirer(new InstallmentForm(3, 9, null))
                        .installmentFormMerchant(new InstallmentForm(4, 10, Arrays.asList(4, 5, 7, 9, 10)))
                        .build())
                .build());
    }

    @Test
    public void onCardRemove() {
        checkEventDeserialization(EventType.ONCARDREMOVE, null);
    }

    @Test
    public void onDisplayText() {
        checkEventDeserialization(EventType.ONDISPLAYTEXT, new OnDisplayTextEvent("Информационное сообщение"));
    }

    @Test
    public void onMsgBox() {
        checkEventDeserialization(EventType.ONMSGBOX, new OnMsgBoxEvent("Проверьте подпись покупателя на чеке",
                "OK", "signatureCheck"));
    }

    @Test
    public void onPrint() {
        checkEventDeserialization(EventType.ONPRINT, new OnPrintEvent("\n\n" +
                "           Random Response\n" +
                "Торговец: name\n" +
                "Адрес: Vil\n" +
                "\n" +
                "Терминал: T2543543\n" +
                "Торговец: T35453435435435\n" +
                "\n" +
                "Набор 1 \"Visa\"                     AZN\n" +
                "Операции:    1                    9.99\n" +
                "Отменено:    0                    0.00\n" +
                "ИТОГО:       1                    9.99\n" +
                "\n" +
                "Итого                              AZN\n" +
                "Операции:    1                    9.99\n" +
                "Отменено:    0                    0.00\n" +
                "ИТОГО:       1                    9.99\n" +
                "______________________________________\n" +
                "                   29.04.2020 14:16:29\n" +
                "\n"));
    }

    @Test
    public void onSelect() {
        checkEventDeserialization(EventType.ONSELECT, new OnSelectEvent("Выберите валюту из списка",
                Arrays.asList("643", "978", "840")));
    }

    @Test
    public void onTrnStatus() {
        checkEventDeserialization(EventType.ONTRNSTATUS, OnTrnStatusEvent.builder()
                .operationId("FCAB2EB6D0DFD12")
                .amountAuthorized(1000)
                .documentNr("129663")
                .cryptogram("4E6998DF9D9CD4CF70F90BA05160ED7D890F3134D8FA6C162FDF5B6C363" +
                        "085B0275AEC227F17DB99A45EB11C61925301DBEB71D759AA93925A9F382CD9" +
                        "31309BF5864922B378AEEB4F6B0C0CC6C58EC")
                .authCode("047625")
                .rrn("936107031714")
                .stan("2")
                .cardType("Visa")
                .amountAdditional(Arrays.asList(
                        new AmountAdditional("CA", "978", 14),
                        new AmountAdditional("40", "978", 1)
                ))
                .state(TrnState.Approved)
                .text("000 - OK")
                .build());
    }

    @Test
    public void onPrompt() {
        checkEventDeserialization(EventType.ONPROMPT, new OnPromptEvent("Введите 4 последние цифры номера карты", "NNNN"));
    }

    private BaseEvent checkEventDeserialization(EventType eventType, EventProperties properties) {
        return checkEventDeserialization(eventType.name(), new BaseEvent(eventType, properties));
    }

    private BaseEvent checkEventDeserialization(String eventName, BaseEvent expected) {
        return checkDeserialization("events/" + eventName.toLowerCase(), expected);
    }

    private <T> T checkDeserialization(String fileName, T expected) {
        final T actual = (T) readAsObject("responses/" + fileName, expected.getClass());
        Assert.assertEquals(expected, actual);
        return actual;
    }

}