package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.cashboxsystem.ResBundleFiscalPrinterCBS;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Синглтон, шифрующий данные для подписи запросов в CBS
 */
public class RSAEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(RSAEncoder.class);

    private static final String KEY_FILE_NAME = "private_client.pem";
    private static final String KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String KEY_FOOTER = "-----END PRIVATE KEY-----";
    private static final String NEW_LINE_REGEX = "[\\r\\n]";

    private static final String ENCODE_ALGORITHM = "SHA256withRSA";
    private RSAPrivateKey privateKey;

    private static RSAEncoder ourInstance;

    private RSAEncoder() {
        try {
            String privateKeyString = getKeyFromFile();
            privateKeyString = privateKeyString.
                    replace(KEY_HEADER, "").
                    replace(KEY_FOOTER, "").
                    replaceAll(NEW_LINE_REGEX, "");
            byte[] decodedKey = Base64.getDecoder().decode(privateKeyString);

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            LOG.error("Error on decoding private key! ", e);
        }
    }

    public static RSAEncoder getInstance() {
        if (ourInstance == null) {
            ourInstance = new RSAEncoder();
        }
        return ourInstance;
    }

    /**
     * Шифрует строку в digest_sha256_rsa и затем в base64. Приватный ключ RSA предоставлен разработчиком CBS.
     * @param dataToEncode строка для шифрования
     * @return шифрованная в SHA256_RSA и BASE64 строка
     * @throws IOException вызывается если отсутствует ключ для кодирования или при шифровании произошла ошибки
     */
    public String encodeData(byte[] dataToEncode) throws IOException {
        if (privateKey == null) {
            throw new IOException(ResBundleFiscalPrinterCBS.getString("ERROR_SIGN_DATA"));
        }

        try {
            Signature signature = Signature.getInstance(ENCODE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(dataToEncode);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            LOG.error("Error on encoding data!", e);
            throw new IOException(ResBundleFiscalPrinterCBS.getString("ERROR_SIGN_DATA"));
        }
    }

    private String getKeyFromFile() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream(KEY_FILE_NAME)) {
            if (inputStream == null) {
                throw new IOException("Error, missing pem key!");
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
