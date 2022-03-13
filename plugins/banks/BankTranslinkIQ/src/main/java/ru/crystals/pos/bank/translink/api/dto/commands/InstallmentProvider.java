package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Провайдер рассрочки (семантика каждого типа зависит от конкретного банка и магазина)
 */
public enum InstallmentProvider {
    /**
     * Issuer (банк-эмитент)
     */
    @JsonProperty("I")
    ISSUER,

    /**
     * Merchant (магазин)
     */
    @JsonProperty("M")
    MERCHANT,

    /**
     * Acquirer (банк-эквайрер)
     */
    @JsonProperty("A")
    ACQUIRER
}
