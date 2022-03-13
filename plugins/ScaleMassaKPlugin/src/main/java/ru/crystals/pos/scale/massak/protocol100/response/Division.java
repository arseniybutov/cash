package ru.crystals.pos.scale.massak.protocol100.response;

/**
 * Цена деления. 0 = 100мг, 1 = 1г, 2 = 10г, 3 = 100г, 4 = 1кг.
 */
public enum Division {

    MILLIGRAM_100,
    GRAM_1,
    GRAM_10,
    GRAM_100,
    KILOGRAM_1,
    ;

    public static Division valueOf(int code) {
        return values()[code];
    }
}
