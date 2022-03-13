package ru.crystals.pos.bank.odengiqr.api.dto.response.cancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

public class InvoiceCancelRs extends Data {

    /**
     * Вернет true если успех или error код и описание ошибки
     */
    @JsonProperty("success")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
