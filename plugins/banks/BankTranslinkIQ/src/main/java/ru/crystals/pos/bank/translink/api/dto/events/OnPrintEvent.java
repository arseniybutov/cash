package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Событие информирует ECR, что POS  подготовил данные чека для печати. Если принтер ECR во время получения чека не доступен либо занят, полученную информацию надо
 * кешировать.
 */
public class OnPrintEvent implements EventProperties {

    /**
     * Текстовые данные документа для печати
     */
    private final String receiptText;

    @JsonCreator
    public OnPrintEvent(@JsonProperty("receiptText") @JsonAlias("displayText") String receiptText) {
        this.receiptText = receiptText;
    }

    public String getReceiptText() {
        return receiptText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnPrintEvent that = (OnPrintEvent) o;
        return Objects.equals(receiptText, that.receiptText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptText);
    }

    @Override
    public String toString() {
        return "receiptText='" + receiptText + '\'';
    }
}
