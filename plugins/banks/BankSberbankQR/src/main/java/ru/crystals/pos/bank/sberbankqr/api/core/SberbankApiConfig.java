package ru.crystals.pos.bank.sberbankqr.api.core;

import java.util.Base64;
import java.util.Objects;

public class SberbankApiConfig {

    /**
     * client-id в Сбербанке (GUID)
     */
    private String clientId;

    /**
     * Client Secret client в Сбербанке
     */
    private String clientSecret;

    /**
     * Токен для запросов к /oauth. Base64 от строки "[client-id]:[client-secret]"
     */
    private String authToken;

    /**
     * Сертификат в виде Base64 строки
     */
    private String certificate;

    /**
     * Пароль сертификата
     */
    private String certificatePassword;

    private SberbankQrUrl url = SberbankQrUrl.PRODUCTION;

    /**
     * ID партнера, формируется Сбербанком при регистрации в системе
     */
    private String memberId;

    /**
     * Уникальный идентификатор терминала
     */
    private String terminalId;

    /**
     * IdQR устройства, на котором сформирован заказ
     */
    private String idQR;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        this.authToken = null;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        this.authToken = null;
    }

    public String getAuthToken() {
        if (authToken == null) {
            Objects.requireNonNull(clientId);
            Objects.requireNonNull(clientSecret);
            authToken = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        }
        return authToken;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public SberbankQrUrl getUrl() {
        return url;
    }

    public void setUrl(SberbankQrUrl url) {
        this.url = url;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getIdQR() {
        return idQR;
    }

    public void setIdQR(String idQR) {
        this.idQR = idQR;
    }
}
