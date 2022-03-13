package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Каждая операция инициированная из ECR должна подтверждаться вызовом данного метода. При вызове данного метода POS может инициировать создание дополнительных
 * квитанций, для получения которых будет сгенерировано событие ONPRINT.
 */
public class CloseDocCommand implements CommandParams {

    /**
     * Идентификатор операции, полученный в ответе команды AUTHORIZE.
     */
    private final List<String> operations;

    /**
     * Номер платежного документа (уникальный номер в течение банковского дня), переданные ECR в команде AUTHORIZE/REFUND
     */
    private final String documentNr;


    public CloseDocCommand(String operationId, String documentNr) {
        this.operations = operationId == null ? null : Collections.singletonList(operationId);
        this.documentNr = documentNr;
    }

    @JsonGetter("operations")
    public List<String> getOperations() {
        return operations;
    }

    @JsonGetter("documentNr")
    public String getDocumentNr() {
        return documentNr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloseDocCommand that = (CloseDocCommand) o;
        return Objects.equals(operations, that.operations) &&
                Objects.equals(documentNr, that.documentNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operations, documentNr);
    }

    @Override
    public String toString() {
        return "CloseDocCommand{" +
                "operations=" + operations +
                ", documentNr='" + documentNr + '\'' +
                '}';
    }
}
