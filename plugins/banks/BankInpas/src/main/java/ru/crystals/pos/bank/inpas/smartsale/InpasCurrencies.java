package ru.crystals.pos.bank.inpas.smartsale;

/**
 * Список валют поддерживаемых банковким терминалом Inpas
 */
public enum InpasCurrencies {
    RUB(643L),
    KZT(398L);

    /**
     * Целочисленный код валюты
     */
    private Long code;

    InpasCurrencies(Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }

    public static String nameFromCode(Long code) {
        for (InpasCurrencies currency : InpasCurrencies.values()) {
            if (currency.getCode().equals(code)) {
                return currency.name();
            }
        }
        return "";
    }

    public static Long codeFromName(String name) {
        for (InpasCurrencies currency : InpasCurrencies.values()) {
            if (currency.name().equals(name)) {
                return currency.getCode();
            }
        }
        return 0L;
    }
}
