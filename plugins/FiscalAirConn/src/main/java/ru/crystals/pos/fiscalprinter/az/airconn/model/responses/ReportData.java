package ru.crystals.pos.fiscalprinter.az.airconn.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.az.airconn.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class ReportData {
    @JsonProperty("document_id")
    private String documentId;
    private String shiftOpenAtUtc;
    private String createdAtUtc;
    private Long reportNumber;
    private Long firstDocNumber;
    private Long lastDocNumber;
    private Long docCountToSend;
    private List<Currency> currencies = new ArrayList<>();

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getShiftOpenAtUtc() {
        return shiftOpenAtUtc;
    }

    public void setShiftOpenAtUtc(String shiftOpenAtUtc) {
        this.shiftOpenAtUtc = shiftOpenAtUtc;
    }

    public String getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(String createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public Long getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(Long reportNumber) {
        this.reportNumber = reportNumber;
    }

    public Long getFirstDocNumber() {
        return firstDocNumber;
    }

    public void setFirstDocNumber(Long firstDocNumber) {
        this.firstDocNumber = firstDocNumber;
    }

    public Long getLastDocNumber() {
        return lastDocNumber;
    }

    public void setLastDocNumber(Long lastDocNumber) {
        this.lastDocNumber = lastDocNumber;
    }

    public Long getDocCountToSend() {
        return docCountToSend;
    }

    public void setDocCountToSend(Long docCountToSend) {
        this.docCountToSend = docCountToSend;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    /**
     * Получаем данные по счетчикам AZN валюты
     *
     * @return {@link Currency} если есть валюта AZN, иначе null
     */
    public Currency getCurrency() {
        return currencies.stream().findFirst().orElse(null);
    }
}
