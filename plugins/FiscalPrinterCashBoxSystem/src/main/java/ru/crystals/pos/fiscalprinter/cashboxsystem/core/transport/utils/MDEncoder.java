package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Тулза для дайджеста сообщений
 */
public class MDEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(MDEncoder.class);

    public static final String ENCODE_ALGORITHM_SHA512 = "SHA-512";
    public static final String ENCODE_ALGORITHM_MD5 = "MD5";

    /**
     * Кодирует строку сначала в SHA-512, затем в Base64
     * @param value строка для кодирования
     * @return закодированная строка, при ошибке кодированя null
     */
    public static String encodeInSHA512Base64(String value) {
        return encodeInBase64(digestData(value, ENCODE_ALGORITHM_SHA512));
    }

    /**
     * Кодирует строку по указанному алгоритму
     * @param value строка для кодирования
     * @return закодированные данные, при ошибке кодированя пустой массив
     */
    public static byte[] digestData(String value, String encodeAlgorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(encodeAlgorithm);
            return md.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e){
            LOG.error("Error on digesting string: ", e);
        }
        return new byte[0];
    }

    /**
     * Кодирует данные в строку Base64
     * @param dataToEncode данные для кодировки
     * @return строка Base64
     */
    private static String encodeInBase64(byte[] dataToEncode) {
        return Base64.getEncoder().encodeToString(dataToEncode);
    }
}
