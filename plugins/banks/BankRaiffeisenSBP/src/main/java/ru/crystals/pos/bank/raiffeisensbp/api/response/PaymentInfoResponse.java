package ru.crystals.pos.bank.raiffeisensbp.api.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.raiffeisensbp.api.status.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Ответ на запрос о статусе платежа
 */
public class PaymentInfoResponse implements ResponseWithMessage {

    /**
     * Дополнительная информация, заполняемая по желанию партнёра при генерации QR-кода.
     * Может быть доступна для пользователя в зависимости от банка.
     */
    private String additionalInfo;

    /**
     * Сумма платежа
     */
    private BigDecimal amount;

    /**
     * Код сообщения
     */
    private final ResponseStatusCode code;

    /**
     * Текст сообщения об ошибке
     */
    private final String message;

    /**
     * Время формирования заявки
     */
    private OffsetDateTime createDate;

    /**
     * Валюта платежа
     */
    private String currency;

    /**
     * Уникальный идентификатор партнёра, выданный банком
     */
    private Long merchantId;

    /**
     * Уникальный идентификатор заказа в системе партнёра
     */
    private String order;

    /**
     * Статус платежа
     */
    private PaymentStatus paymentStatus;

    /**
     * Уникальный идентификатор QRC, выданный СБП при запросе генерации QRC
     */
    private String qrId;

    /**
     * Уникальный идентификатор партнёра, выданный СБП
     */
    private String sbpMerchantId;

    /**
     * Дата и время проведения платежа
     */
    private OffsetDateTime transactionDate;

    /**
     * Идентификатор операции платежа в Райффайзенбанке
     */
    private Integer transactionId;

    @JsonCreator
    public PaymentInfoResponse(@JsonProperty("additionalInfo") String additionalInfo,
                               @JsonProperty("amount") BigDecimal amount,
                               @JsonProperty("code") ResponseStatusCode code,
                               @JsonProperty("message") String message,
                               @JsonProperty("createDate") OffsetDateTime createDate,
                               @JsonProperty("currency") String currency,
                               @JsonProperty("merchantId") Long merchantId,
                               @JsonProperty("order") String order,
                               @JsonProperty("paymentStatus") PaymentStatus paymentStatus,
                               @JsonProperty("qrId") String qrId,
                               @JsonProperty("sbpMerchantId") String sbpMerchantId,
                               @JsonProperty("transactionDate") OffsetDateTime transactionDate,
                               @JsonProperty("transactionId") Integer transactionId) {
        this.additionalInfo = additionalInfo;
        this.amount = amount;
        this.code = code;
        this.message = message;
        this.createDate = createDate;
        this.currency = currency;
        this.merchantId = merchantId;
        this.order = order;
        this.paymentStatus = paymentStatus;
        this.qrId = qrId;
        this.sbpMerchantId = sbpMerchantId;
        this.transactionDate = transactionDate;
        this.transactionId = transactionId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public ResponseStatusCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getOrder() {
        return order;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getQrId() {
        return qrId;
    }

    public String getSbpMerchantId() {
        return sbpMerchantId;
    }

    public OffsetDateTime getTransactionDate() {
        return transactionDate;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentInfoResponse that = (PaymentInfoResponse) o;
        return Objects.equals(additionalInfo, that.additionalInfo) &&
                amount != null && amount.compareTo(that.amount) == 0 &&
                code == that.code &&
                Objects.equals(message, that.message) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(merchantId, that.merchantId) &&
                Objects.equals(order, that.order) &&
                paymentStatus == that.paymentStatus &&
                Objects.equals(qrId, that.qrId) &&
                Objects.equals(sbpMerchantId, that.sbpMerchantId) &&
                Objects.equals(transactionDate, that.transactionDate) &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(additionalInfo, amount, code, message, createDate, currency, merchantId, order, paymentStatus, qrId, sbpMerchantId, transactionDate, transactionId);
    }

    @Override
    public String toString() {
        return "PaymentInfoResponse{" +
                "additionalInfo='" + additionalInfo + '\'' +
                ", amount=" + amount +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", createDate=" + createDate +
                ", currency='" + currency + '\'' +
                ", merchantId=" + merchantId +
                ", order='" + order + '\'' +
                ", paymentStatus=" + paymentStatus +
                ", qrId='" + qrId + '\'' +
                ", sbpMerchantId='" + sbpMerchantId + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionId=" + transactionId +
                '}';
    }
}
