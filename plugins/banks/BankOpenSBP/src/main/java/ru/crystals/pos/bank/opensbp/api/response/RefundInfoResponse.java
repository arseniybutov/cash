package ru.crystals.pos.bank.opensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.opensbp.api.request.Amount;

public class RefundInfoResponse {


    /**
     * Номер операции в платежной системе банка
     */
    private String operationId;

    /**
     * Информация о статуса запроса на возврат
     */
    private ResponseStatus status;

    /**
     * Идентификатор сессии в Operation Server
     */
    private String session;

    /**
     * Получатель
     */
    private String pam;

    /**
     * Идентификатор операции в СБП
     */
    private String externalId;

    /**
     * Уникальный идентификатор операции. (требуется сохранять для дальнейшей проверки запроса статуса по возврату)
     */
    private String id;

    /**
     * Телефон Получателя
     */
    private String phone;

    /**
     * Данные о сумме возврата
     */
    private Amount amount;

    /**
     * Идентификатор Банка Получателя из справочника участников СБП (Members-API).
     */
    private String recipientBankId;

    /**
     * Референсный идентификатор исходной Операции СБП
     */
    private String referenceId;


    @JsonCreator
    public RefundInfoResponse(@JsonProperty("id") String id,
                              @JsonProperty("phone") String phone,
                              @JsonProperty("amount") Amount amount,
                              @JsonProperty("recipientBankId") String recipientBankId,
                              @JsonProperty("session") String session,
                              @JsonProperty("pam") String pam,
                              @JsonProperty("externalId") String externalId,
                              @JsonProperty("operationId") String operationId,
                              @JsonProperty("referenceId") String referenceId,
                              @JsonProperty("status") ResponseStatus status) {
        this.id = id;
        this.phone = phone;
        this.amount = amount;
        this.recipientBankId = recipientBankId;
        this.referenceId = referenceId;
        this.session = session;
        this.pam = pam;
        this.externalId = externalId;
        this.operationId = operationId;
        this.status = status;
    }

    public RefundInfoResponse(String operationId, ResponseStatus status, Amount amount, String id) {
        this.operationId = operationId;
        this.status = status;
        this.amount = amount;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getRecipientBankId() {
        return recipientBankId;
    }

    public String getSession() {
        return session;
    }

    public String getPam() {
        return pam;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getOperationId() {
        return operationId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getReferenceId() {
        return referenceId;
    }
}
