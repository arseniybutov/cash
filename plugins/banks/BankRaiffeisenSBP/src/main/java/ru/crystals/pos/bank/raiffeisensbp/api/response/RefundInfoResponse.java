package ru.crystals.pos.bank.raiffeisensbp.api.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.raiffeisensbp.api.status.RefundStatus;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Ответ на запрос о возврате по платежу
 */
public class RefundInfoResponse implements ResponseWithMessage {

    /**
     * Код сообщения
     */
    private final ResponseStatusCode code;

    /**
     * Текст сообщения об ошибке
     */
    private final String message;

    /**
     * Сумма возврата в рублях
     */
    private BigDecimal amount;

    /**
     * Код состояния запроса на возврат
     */
    private RefundStatus refundStatus;

    @JsonCreator
    public RefundInfoResponse(@JsonProperty("code") ResponseStatusCode code,
                              @JsonProperty("message") String message,
                              @JsonProperty("amount") BigDecimal amount,
                              @JsonProperty("refundStatus") RefundStatus refundStatus) {
        this.code = code;
        this.message = message;
        this.amount = amount;
        this.refundStatus = refundStatus;
    }

    @Override
    public ResponseStatusCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RefundStatus getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefundInfoResponse that = (RefundInfoResponse) o;
        return code == that.code &&
                Objects.equals(message, that.message) &&
                amount != null && amount.compareTo(that.amount) == 0 &&
                refundStatus == that.refundStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, amount, refundStatus);
    }

    @Override
    public String toString() {
        return "RefundInfoResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                ", refundStatus=" + refundStatus +
                '}';
    }
}
