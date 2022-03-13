package ru.crystals.pos.fiscalprinter.az.airconn.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AccessToken;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.DocumentResponse;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.InfoData;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ReportData;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ShiftStatus;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Эмуляция ответов службы для тестирования плагина
 */
public class MockTransport implements AirConnTransport {
    private static final Logger LOG = LoggerFactory.getLogger(MockTransport.class);

    /**
     * Копии ответов полученных от фискализатора
     */
    private final String loginResult = "{\"code\":0,\"message\":\"Success operation\",\"data\":{\"access_token\":\"pC/2ipixHe9/IUoKUUj+uA==\"}}";
    private final String createDocumentResult = "{\"code\":0,\"message\":\"Success operation\",\"data\":{\"document_id\":\"933TdtDQU3ubyDbfSJptLq8yiYgaNkZjxhRT8Sff2GUv" +
            "\",\"short_document_id\":\"933TdtDQU3ub\",\"document_number\":32,\"shift_document_number\":5}}";
    private final String getInfoResult = "{\"code\":0,\"message\":\"Success operation\",\"data\":{\"cashbox_tax_number\":\"test_00048\",\"company_tax_number\":\"7802781104\"," +
            "\"company_name\":\"\\\"NBA TECHNOLOGİES INTERNATİONAL\\\" MƏHDUD MƏSULİYYƏTLİ CƏMİYYƏTİ\",\"object_tax_number\":\"1003268241-20001\"," +
            "\"object_address\":\"AZ1025 BAKI ŞƏHƏRİ XƏTAİ RAYONU BABƏK PR. ev.12A m.BLOK C QAPI 1\",\"object_name\":\"OFİS\"," +
            "\"cashbox_factory_number\":\"004E001F594B500920373357\",\"cashregister_factory_number\":\"all\",\"cashregister_model\":\"test_all\",\"qr_code_url\":\" " +
            "https://monitoring.e-kassa.az/#/index?doc=\",\"not_before\":\"2020-02-04T12:02:21Z\",\"not_after\":\"2023-02-03T12:02:51Z\"," +
            "\"firmware_version\":\"2.2.0\",\"state\":\"ACTIVE\",\"last_online_time\":\"2020-02-06T11:12:59Z\",\"oldest_document_time\":\"2020-02-07T14:07:29Z\"}}";
    private final String closeShiftResult = "{\"code\":0,\"message\":\"Success operation\",\"data\":{\"document_id\":\"49ve7Kk6h6JttHtXpBhDgi72PmXDmGj4q3z7GMqLQEAn\"," +
            "\"createdAtUtc\":\"2020-02-11T15:35:28Z\",\"shiftOpenAtUtc\":\"2020-02-10T13:31:37Z\",\"reportNumber\":2,\"firstDocNumber\":2,\"lastDocNumber\":27," +
            "\"docCountToSend\":30,\"currencies\":[{\"currency\":\"AZN\",\"saleCount\":24,\"saleSum\":9997.75,\"saleCashSum\":9997.75,\"saleCashlessSum\":0.0," +
            "\"salePrepaymentSum\":0.0,\"saleCreditSum\":0.0,\"saleBonusSum\":0.0,\"saleVatAmounts\":[{\"vatSum\":9997.75,\"vatPercent\":20.0}],\"depositCount\":2," +
            "\"depositSum\":200.0,\"withdrawCount\":0,\"withdrawSum\":0.0,\"moneyBackCount\":0,\"moneyBackSum\":0.0,\"moneyBackCashSum\":0.0," +
            "\"moneyBackCashlessSum\":0.0,\"moneyBackPrepaymentSum\":0.0,\"moneyBackCreditSum\":0.0,\"moneyBackBonusSum\":0.0,\"moneyBackVatAmounts\":[]," +
            "\"rollbackCount\":0,\"rollbackSum\":0.0,\"rollbackCashSum\":0.0,\"rollbackCashlessSum\":0.0,\"rollbackPrepaymentSum\":0.0,\"rollbackCreditSum\":0.0," +
            "\"rollbackBonusSum\":0.0,\"rollbackVatAmounts\":[],\"correctionCount\":0,\"correctionSum\":0.0,\"correctionCashSum\":0.0,\"correctionCashlessSum\":0.0," +
            "\"correctionPrepaymentSum\":0.0,\"correctionCreditSum\":0.0,\"correctionBonusSum\":0.0,\"correctionVatAmounts\":[]}]}}";
    private final String getShiftResult = "{\"code\":0,\"message\":\"Success operation\",\"data\":{\"shift_open\":true,\"shift_open_time\":\"2020-02-11T15:36:05Z\"}}";
    private final String defaultResult = "{\"code\":0,\"message\":\"Success operation\"}";

    @Override
    public void init(String tokenAddress) {
        LOG.info("init({})", tokenAddress);
    }

    @Override
    public void close() {
        LOG.info("close");
    }

    @Override
    public void connect() throws FiscalPrinterCommunicationException {
        LOG.info("connect()");
    }

    @Override
    public void disconnect() {
        LOG.info("disconnect()");
    }

    @Override
    public <C extends BaseCommand> C executeCommand(C command) throws FiscalPrinterException {
        LOG.debug("executing command({})", command.getOperationId());
        LOG.debug("data : {}", command.serializeRequest());

        String response = getMockResponse(command.getResponseDataClass());
        command.deserializeResponse(response);
        command.checkForApiError();
        LOG.debug("execute end");

        return command;

    }

    /**
     * На основе класса запроса выдаем нужный ответ
     * @return копия ответа от фискализатора
     */
    private String getMockResponse(Class responseDataClass) {
        if (AccessToken.class.equals(responseDataClass)) {
            return loginResult;
        }
        if (DocumentResponse.class.equals(responseDataClass)) {
            return createDocumentResult;
        }
        if (InfoData.class.equals(responseDataClass)) {
            return getInfoResult;
        }
        if (ReportData.class.equals(responseDataClass)) {
            return closeShiftResult;
        }
        if (ShiftStatus.class.equals(responseDataClass)) {
            return getShiftResult;
        }
        return defaultResult;
    }
}
