package ru.crystals.pos.bank.tinkoffsbp.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public interface TokenSetter {

    void setToken(String token);

    default void setToken(String password, TokenSetter tokenSetter, ObjectMapper mapper) throws IOException {
        // сериализуем без поля token
        String requestWithoutHash = mapper.writerWithView(Views.WithoutToken.class).writeValueAsString(this);

        ObjectReader reader = mapper.readerFor(Map.class);
        Map<String, String> paramsMap = reader.readValue(requestWithoutHash);

        paramsMap.put("Password", password);

        final Map<String, String> sortedParameters = new TreeMap<>(paramsMap);
        final String paramsString = String.join("", sortedParameters.values());

        // хэшируем
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't get instance for hash", e);
        }
        byte[] hash = digest.digest(paramsString.getBytes(StandardCharsets.UTF_8));
        final StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        tokenSetter.setToken(hexString.toString());
    }
}
