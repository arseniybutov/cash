package ru.crystals.pos.fiscalprinter.retailforce.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.Document;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPayment;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionBase;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionItem;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalCountry;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalResponse;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalResponseAdditionalFields;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.PaymentTypes;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;

public class RFRequestsTest extends RFJsonConverterTest {

    @Test
    public void storeDocumentReceipt() throws JsonProcessingException {
        final FiscalResponseAdditionalFields additionalFields = new FiscalResponseAdditionalFields();
        additionalFields.setTransactionStartTime(1614970809L);
        additionalFields.setTransactionEndTime(0L);
        additionalFields.setTseSerial("e66573e9e6e52ddb63dc46de2a71d21e60f1e766f7468ebfd9c9acaa2befb4ef");
        additionalFields.setTseTimeFormat("utcTime");
        additionalFields.setTseHashAlgorithm("ecdsa-plain-SHA256");
        additionalFields.setTsePublicKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEsvma5yPJVe9k+skzDBzKHZ7GZtC12jsnnubUiZPaJycKuGeNx1GwX+pd/+UTpYGAyrvGRWXx5fbvf5zN1Pycuw==");
        additionalFields.setTseSignatureCounter(15987L);


        final Document receipt = Document.builder()
                .fiscalResponse(FiscalResponse.builder()
                        .fiscalisationDocumentNumber(8475)
                        .fiscalisationDocumentRevision(1)
                        .fiscalCountry(FiscalCountry.GERMANY)
                        .requestTime(OffsetDateTime.parse("2021-03-05T19:00:09.1595244Z"))
                        .requestCompletionTime(OffsetDateTime.parse("2021-03-05T19:00:09.6316611Z"))
                        .signature("mVSrLFpZwicIdSEccpZzKzKUaGjQFdurcUphm9HWW2vVHxbXibo5YBE/LXDJygbHymQ/u2HK4kOIACLyZEiutQ==")
                        .additionalFields(additionalFields).build())
                .uniqueClientId("5c13392b-5951-4053-a05a-c536771ca475")
                .documentGuid("b8dd838f-66da-4b5d-9dbc-89555289c538")
                .documentId("1c26576c-ac9e-4e56-8081-875ce1941b91")
                .createDate(OffsetDateTime.parse("2021-03-05T22:00:09.663+03:00"))
                .bookDate(OffsetDateTime.parse("2021-03-05T22:00:09.663+03:00"))
                .isTraining(true)
                .fiscalDocumentNumber(8475)
                .fiscalDocumentRevision(1)
                .fiscalDocumentStartTime(1620719730L)
                .documentType(DocumentType.RECEIPT)
                .user(new User("1234", "Testuser"))
                .positions(Collections.singletonList(
                        DocumentPositionItem.builder()
                                .common(DocumentPositionBase.builder()
                                        .type(DocumentPositionType.ITEM)
                                        .positionNumber(0)
                                        .build())
                                .itemId("000714")
                                .itemCaption("Blume grün")
                                .vatIdentification(1)
                                .vatPercent(BigDecimal.valueOf(19.00))
                                .baseTaxValue(BigDecimal.valueOf(0.69))
                                .baseNetValue(BigDecimal.valueOf(3.61))
                                .baseGrossValue(BigDecimal.valueOf(4.3))
                                .quantity(BigDecimal.valueOf(1.0))
                                .taxValue(BigDecimal.valueOf(0.69))
                                .netValue(BigDecimal.valueOf(3.61))
                                .grossValue(BigDecimal.valueOf(4.3))

                                .build()
                ))
                .payments(Collections.singletonList(
                        DocumentPayment.builder()
                                .amount(BigDecimal.valueOf(4.3))
                                .isCash(true)
                                .currencyIsoCode("EUR")
                                .caption("Наличными")
                                .paymentType(PaymentTypes.CASH)
                                .build()
                ))
                .build();

        final String actual = writer.writeValueAsString(receipt);

        checkSerialization("requests/store_document_request", actual);
    }

    private void checkSerialization(String expectedResultFileName, String actualResult) {
        final String expected = readAsString(expectedResultFileName);
        Assert.assertEquals(expected, actualResult);
    }

}
