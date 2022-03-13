package ru.crystals.pos.bank.commonsbpprovider.api.request;

public class RefundOfFullAmountDTO {

    /**
     * Id операции в системе банка
     */
    private String operationId;

    /**
     * Id операции в системе продавца
     */
    private String id;

    /**
     * сумма возврата
     */
    private long amount;

    public RefundOfFullAmountDTO(String referenceId, String id) {
        this.operationId = referenceId;
        this.id = id;
    }

    public RefundOfFullAmountDTO(String operationId, String id, long amount) {
        this.operationId = operationId;
        this.id = id;
        this.amount = amount;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
