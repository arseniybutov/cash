package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

public class GetTrnStatusCommand implements CommandParams {

    /**
     * Идентификатор операции, полученный в ответе команды AUTHORIZE
     */
    private final String operationId;

    /**
     * Номер кассового чека
     */
    private final String documentNr;

    /**
     * Криптограмма транзакции
     */
    private final String cryptogram;

    public GetTrnStatusCommand(String operationId, String documentNr, String cryptogram) {
        this.operationId = Objects.requireNonNull(operationId);
        this.documentNr = documentNr;
        this.cryptogram = cryptogram;
    }

    @JsonGetter("operationId")
    public String getOperationId() {
        return operationId;
    }

    @JsonGetter("documentNr")
    public String getDocumentNr() {
        return documentNr;
    }

    @JsonGetter("cryptogram")
    public String getCryptogram() {
        return cryptogram;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetTrnStatusCommand that = (GetTrnStatusCommand) o;
        return Objects.equals(operationId, that.operationId) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(cryptogram, that.cryptogram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, documentNr, cryptogram);
    }

    @Override
    public String toString() {
        return "GetTrnStatusCommand{" +
                "operationId='" + operationId + '\'' +
                ", documentNr='" + documentNr + '\'' +
                ", cryptogram='" + cryptogram + '\'' +
                '}';
    }
}
