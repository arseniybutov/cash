package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Версия payload QR кода
 */
public enum TemplateVersion {

    @JsonProperty("01")
    TEMPLATE_VERSION_1

}
