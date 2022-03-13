package ru.crystals.pos.fiscalprinter.retailforce.api;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.BusinessTransactionType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPayment;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayDocumentResponse;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayPosition;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayPositionType;

import java.math.BigDecimal;
import java.util.Arrays;

public class RFResponsesTest extends RFJsonConverterTest {


    @Test
    public void endOfDayResponse() {

        EndOfDayDocumentResponse expected = new EndOfDayDocumentResponse();
        expected.setPayments(Arrays.asList(
                DocumentPayment.builder()
                        .amount(new BigDecimal("1784.54"))
                        .foreignAmount(BigDecimal.ZERO)
                        .foreignAmountExchangeRate(BigDecimal.ZERO)
                        .currencyIsoCode("EUR")
                        .isCash(false)
                        .build(),
                DocumentPayment.builder()
                        .amount(new BigDecimal("823.32"))
                        .foreignAmount(BigDecimal.ZERO)
                        .foreignAmountExchangeRate(BigDecimal.ZERO)
                        .currencyIsoCode("EUR")
                        .isCash(true)
                        .build()
        ));
        final EndOfDayPosition otherPosition = makeOtherPosition();
        expected.setPositions(Arrays.asList(
                makeSalesPosition(new BigDecimal("1957.32")),
                makeSalesPosition(new BigDecimal("-249.46")),
                makeMoneyPosition(BusinessTransactionType.PAY_IN, new BigDecimal("1000.0")),
                makeMoneyPosition(BusinessTransactionType.PAY_OUT, new BigDecimal("-100.0")),
                otherPosition,
                otherPosition,
                otherPosition));

        checkDeserialization("responses/end_of_day_document_response", EndOfDayDocumentResponse.class, expected);
    }

    private EndOfDayPosition makeSalesPosition(BigDecimal amount) {
        final EndOfDayPosition result = new EndOfDayPosition();
        result.setType(EndOfDayPositionType.BOOKING);
        result.setBusinessTransactionType(BusinessTransactionType.REVENUE);
        result.setGrossValue(amount);
        return result;
    }


    private EndOfDayPosition makeMoneyPosition(BusinessTransactionType type, BigDecimal amount) {
        final EndOfDayPosition result = new EndOfDayPosition();
        result.setType(EndOfDayPositionType.BOOKING);
        result.setBusinessTransactionType(type);
        result.setGrossValue(amount);
        return result;
    }

    private EndOfDayPosition makeOtherPosition() {
        final EndOfDayPosition result = new EndOfDayPosition();
        result.setType(EndOfDayPositionType.UNKNOWN);
        return result;
    }

    private <T> void checkDeserialization(String expectedResultFileName, Class<T> responseType, T expected) {
        final T actualResult = readAsObject(expectedResultFileName, responseType);
        Assert.assertEquals(expected, actualResult);
    }
}
