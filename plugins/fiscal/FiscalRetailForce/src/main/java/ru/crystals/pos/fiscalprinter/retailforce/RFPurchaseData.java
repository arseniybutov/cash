package ru.crystals.pos.fiscalprinter.retailforce;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Структура для хранения дополнительных данных чека
 */
public class RFPurchaseData {

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("fiscalNumber")
    private Integer fiscalNumber;

    @JsonProperty("store")
    private String store;

    @JsonProperty("terminal")
    private String terminal;

    @JsonProperty("bookDate")
    private OffsetDateTime bookDate;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("qr")
    private String qr;

    @JsonProperty("txStartTime")
    private Long txStartTime;

    @JsonProperty("txFinishTime")
    private Long txFinishTime;

    @JsonProperty("publicKey")
    private String publicKey;

    @JsonProperty("hashAlgorithm")
    private String hashAlgorithm;

    @JsonProperty("signatureCounter")
    private Long signatureCounter;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getFiscalNumber() {
        return fiscalNumber;
    }

    public void setFiscalNumber(Integer fiscalNumber) {
        this.fiscalNumber = fiscalNumber;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public OffsetDateTime getBookDate() {
        return bookDate;
    }

    public void setBookDate(OffsetDateTime bookDate) {
        this.bookDate = bookDate;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public Long getTxStartTime() {
        return txStartTime;
    }

    public void setTxStartTime(Long txStartTime) {
        this.txStartTime = txStartTime;
    }

    public Long getTxFinishTime() {
        return txFinishTime;
    }

    public void setTxFinishTime(Long txFinishTime) {
        this.txFinishTime = txFinishTime;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public Long getSignatureCounter() {
        return signatureCounter;
    }

    public void setSignatureCounter(Long signatureCounter) {
        this.signatureCounter = signatureCounter;
    }
}
