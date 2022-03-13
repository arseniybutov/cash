package ru.crystals.pos.bank.sberbankqr.api.core;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.junit.Test;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.revocation.OrderRevocationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Для ручного тестирования интеграции в процессе разработки (API Сбербанка еще будет меняться какое-то время)
 */
@Ignore
public class SberbankApiManualTest {

    private SberbankApi createSberbankApi() throws IOException, BankConfigException {
        String myCertificate = getCase64EncodedCertificate("C:\\t.sokolova@crystals.ru.p12");
        SberbankApiConfig config2 = new SberbankApiConfig();
        config2.setClientId("3a1dba4c-c34a-4297-a528-fcf718aaa2e3");
        config2.setClientSecret("mJ6cA6vA0cL0uF8lD3nB0yQ3sT8xW4oA3lY4pT1bW5nG8oK6dF");
        config2.setCertificate(myCertificate);
        config2.setCertificatePassword("Sokolova12");
        config2.setUrl(SberbankQrUrl.TEST);
        config2.setMemberId("000001");
        config2.setTerminalId("21325622");
        config2.setIdQR("20305");
        return new SberbankApi(config2);
    }

    private SberbankApi createSberbankApiPay() throws IOException, BankConfigException {
        String certificate = getCase64EncodedCertificate("C:\\yakovlev.al.ale@sberbank.ru.p12");
        SberbankApiConfig config1 = new SberbankApiConfig();
        config1.setClientId("6a98e5a7-5073-403a-abf5-5e40dab0e8ed");
        config1.setClientSecret("U4cL5bP1qF7tJ1qU3kP7kM8rN8aO5jF3yN7oJ4bN6pJ8rU6wV7");
        config1.setCertificate(certificate);
        config1.setCertificatePassword("Yakovlevaa1");
        config1.setUrl(SberbankQrUrl.TEST);
        config1.setMemberId("000001");
        config1.setTerminalId("21325622");
        config1.setIdQR("20305");
        return new SberbankApi(config1);
    }

    @Test
    public void testGetAuthToken() throws BankException, IOException {
        String token = createSberbankApiPay().getAuthorizationToken(SberbankApiScope.PAY);
        System.out.println(token);
    }

    @Test
    public void testPay() throws BankException, IOException {
        String qrPayLoad = "https://sberbank.ru/qr/?ClientIdQr=40ac47c5ed7944cf8e0775a398552f2c" +
                "&HashId=bf891fc9fb52ba8816cdd4c022fe6db5ec0d63296a7f3dafc17110180fadc303&TimeStamp=1592834161&online";
        int amount = 48000;
        PayRusClientQRRs.Status response = createSberbankApiPay().pay(qrPayLoad, amount);
        System.out.println(response);
    }

    @Test
    public void testStatus() throws BankException, IOException {
        OrderStatusRequestQrRs.Status response = createSberbankApi().status("63685a0e319149a9aec6adf98b5761cc").getBody().getStatus();
        System.out.println(response);
    }

    @Test
    public void testCancel() throws BankException, IOException {
        String orderId = "10001000518956637";
        String operationId = "10001HFYYR8956637";
        String authCode = "885967";
        String originalIdQr = "20306";
        int cancelOperationSum = 30000;
        OrderCancelQrRs.Status response = createSberbankApi().cancel(orderId, operationId, authCode, cancelOperationSum, originalIdQr);
        System.out.println(response);
    }

    @Test
    public void testCreation() throws Exception {
        OrderCreationQrRs.Status response = createSberbankApi().creation(100);
        System.out.println(response);
    }

    @Test
    public void testRevocation() throws Exception {
        OrderRevocationQrRs.Status response = createSberbankApi().revocation("aaaaa");
        System.out.println(response);
    }

    private static String getCase64EncodedCertificate(String fileName)
            throws IOException {
        File file = new File(fileName);
        byte[] bytes = loadFile(file);
        byte[] encoded = Base64.encodeBase64(bytes);
        return new String(encoded);
    }

    private static byte[] loadFile(File file) throws IOException {
        byte[] bytes;
        try (InputStream is = new FileInputStream(file)) {
            bytes = new byte[(int) file.length()];

            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        }
        return bytes;
    }
}
