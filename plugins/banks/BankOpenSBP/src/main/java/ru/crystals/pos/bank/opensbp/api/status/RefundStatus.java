package ru.crystals.pos.bank.opensbp.api.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RefundStatus {

    /**
     * операция завершена успешно
     */
    @JsonProperty("completed")
    COMPLETED,

    /**
     * операция отклонена
     */
    @JsonProperty("rejected")
    REJECTED,

    /**
     * операция в обработке
     */
    @JsonProperty("processing")
    PROCESSING,

}
