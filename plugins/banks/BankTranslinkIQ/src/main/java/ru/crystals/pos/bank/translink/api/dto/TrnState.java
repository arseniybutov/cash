package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum TrnState {

    /**
     * Transaction is not found, does not exists
     */
    @JsonProperty("Notfound")
    Notfound,
    /**
     * Transaction in the process of authorizing
     */
    @JsonProperty("Authorizing")
    Authorizing,
    /**
     * This is transaction state after original preauthorization or after incremental, only transaction with this state
     * can be affected by INCREMENT and COMPLETE requests
     */
    @JsonProperty("Approved")
    Approved,
    /**
     * Indicates decline
     */
    @JsonProperty("Declined")
    Declined,
    /**
     * Communication timed-out, repeat request
     */
    @JsonProperty("Timeout")
    Timeout,
    /**
     * Initiated reversal for transaction
     */
    @JsonProperty("Reversing")
    Reversing,
    /**
     * Successful reversal completion
     */
    @JsonProperty("Reversed")
    Reversed,
    /**
     * Void was not finished and needs to be repeated
     */
    @JsonProperty("Voiding")
    Voiding,

    @JsonEnumDefaultValue
    UNKNOWN

}
