package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

public enum RegistrationQRStatus {

    /**
     * QR успешно зарегестрирован
     */
    @JsonProperty("CREATED")
    CREATED(Status.SUCCESS),

    /**
     * Любое другое значение
     */
    @JsonEnumDefaultValue
    UNKNOWN(Status.UNKNOWN);

    private final Status commonStatus;

    RegistrationQRStatus(Status commonStatus) {
        this.commonStatus = commonStatus;
    }

    public Status getCommonStatus() {
        return commonStatus;
    }
}
