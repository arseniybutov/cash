package ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.Document;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPayment;

import java.util.List;
import java.util.Objects;

/**
 * Упрощенное представление {@link Document} для ответа /api/v1/closing/{clientId}/endofdayDocument
 */
public class EndOfDayDocumentResponse {

    @JsonProperty("payments")
    private List<DocumentPayment> payments;

    @JsonProperty("positions")
    private List<EndOfDayPosition> positions;


    public List<DocumentPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<DocumentPayment> payments) {
        this.payments = payments;
    }

    public List<EndOfDayPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<EndOfDayPosition> positions) {
        this.positions = positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndOfDayDocumentResponse that = (EndOfDayDocumentResponse) o;
        return Objects.equals(payments, that.payments) && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payments, positions);
    }

    @Override
    public String toString() {
        return "EndOfDayDocumentResponse{" +
                "payments=" + payments +
                ", positions=" + positions +
                '}';
    }
}
