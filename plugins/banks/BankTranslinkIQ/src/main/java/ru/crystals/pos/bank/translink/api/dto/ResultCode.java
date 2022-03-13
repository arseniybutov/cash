package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ResultCode {
    /**
     * Operation completed successfully. Any other value (including null or empty string) means that operation failed.
     */
    OK,
    /**
     * One of the arguments is invalid. (For example: DocumentNr is null or empty).
     */
    INVALID_ARG,
    /**
     * No connection to Ashburn POS device or some other connection problem.
     */
    CONNECTION_ERROR,
    /**
     * Timeout waiting for operation to complete.
     */
    TIMEOUT,
    /**
     * Example: Authorize method is called and Void method is called before waiting for OnAuthorize event.
     */
    ANOTHER_OPERATION_IN_PROGRESS,
    /**
     * Operation was declined.
     */
    DECLINED,
    /**
     * openpos method was not called.
     */
    NOT_INITILIAZED,

    @JsonEnumDefaultValue
    UNKNOWN

}
