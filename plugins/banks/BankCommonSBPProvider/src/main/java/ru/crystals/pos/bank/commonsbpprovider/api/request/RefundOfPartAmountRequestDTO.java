package ru.crystals.pos.bank.commonsbpprovider.api.request;

public class RefundOfPartAmountRequestDTO {

    /**
     * Номер операции в платежной системе банка
     */
    private String operationId;

    /**
     * Уникальный идентификатор операции в система продавца
     */
    private String id;

    /**
     * Информация о возвращаемой сумме для возврата части суммы
     */
    private long amount;


    /**
     * Конструктор для возврата части суммы
     */
    public RefundOfPartAmountRequestDTO(String operationId, String id, long amount) {
        this.operationId = operationId;
        this.id = id;
        this.amount = amount;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

}
