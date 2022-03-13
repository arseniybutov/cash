package ru.crystals.pos.bank.odengiqr.api.core;

import org.junit.Ignore;
import org.junit.Test;
import ru.crystals.pos.bank.odengiqr.api.dto.ODengiCommand;
import ru.crystals.pos.bank.odengiqr.api.dto.RequestResponseContainer;
import ru.crystals.pos.bank.odengiqr.api.dto.request.create.CreateInvoiceRq;
import ru.crystals.pos.bank.odengiqr.api.dto.response.cancel.InvoiceCancelRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.create.CreateInvoiceRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.status.StatusPaymentRs;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ODengiApiTest {

    private ODengiApi api() {
        ODengiConfig config = new ODengiConfig();
        config.setSellerID("2752075121");
        config.setSellerPassword("!2|HGX2YWTF31A&");
        config.setUrl(ODengiURL.TEST);
        return new ODengiApi(config);
    }

    @Ignore("Тест для запуска вручную (идет в песочницу)")
    @Test
    public void testCreateInvoice() throws Exception {
        RequestResponseContainer response = api().createInvoice(100, "KGS", "Описание платежа SetRetail");
        assertEquals("createInvoice", response.getCommand());

        CreateInvoiceRs createInvoiceRs = (CreateInvoiceRs) response.getData();
        assertNull(createInvoiceRs.getError());
    }

    @Ignore("Тест для запуска вручную (идет в песочницу)")
    @Test
    public void testStatusPayment() throws Exception {
        RequestResponseContainer response = api().statusPayment("775521380646", "62e8d72e-f356-4884-b8fc-05b7c0db9a3a");
        assertEquals("statusPayment", response.getCommand());

        StatusPaymentRs statusPaymentRs = (StatusPaymentRs) response.getData();
        assertNull(statusPaymentRs.getError());
    }

    @Ignore("Тест для запуска вручную (идет в песочницу)")
    @Test
    public void testInvoiceCancel() throws Exception {
        RequestResponseContainer response = api().invoiceCancel("775521380646");
        assertEquals("invoiceCancel", response.getCommand());

        InvoiceCancelRs invoiceCancelRs = (InvoiceCancelRs) response.getData();
        assertNull(invoiceCancelRs.getError());
    }

    @Test
    public void testHash() throws Exception {
        RequestResponseContainer request = new RequestResponseContainer();
        request.setCommand(ODengiCommand.CREATE_INVOICE.getCommand());
        request.setVersion(1005);
        request.setSellerID("2752075121");
        request.setMkTime(Instant.ofEpochMilli(161063693691L));
        request.setLang("ru");
        CreateInvoiceRq createInvoiceRq = new CreateInvoiceRq();
        createInvoiceRq.setOrderId("111");
        createInvoiceRq.setDesc("aaa");
        createInvoiceRq.setCurrency("KGS");
        request.setData(createInvoiceRq);

        String expected = "7887600d5ac4ca472477a0d9c5868124";
        api().setHash(request);
        String actual = request.getHash();
        assertEquals(expected, actual);
    }
}