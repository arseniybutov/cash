package ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentRequest {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("doc_type")
    private String docType;
    private DocumentBody data = new DocumentBody();

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public DocumentBody getData() {
        return data;
    }

    /**
     * При содании запроса создается и объект DocumentBody, для его заполнения
     * используется getData(). Эта команда нужна для записи нового DocumentBody.
     *
     * @param data
     */
    public void setData(DocumentBody data) {
        this.data = data;
    }
}
