package ru.crystals.pos.scale.massak.protocol100.response;

public enum Error {

    HIGH_LOAD(0x08, "Нагрузка на весовом устройстве превышает НПВ"),
    NOT_IN_WEIGHT_MODE(0x09, "Весовое устройство не в режиме взвешивания"),
    INPUT_DATA(0x0A, "Ошибка входных данных"),
    SAVE_DATA(0x0B, "Ошибка сохранения данных"),
    WIFI_UNAVAILABLE(0x10, "Интерфейс WiFi не поддерживается"),
    ETH_UNAVAILABLE(0x11, "Интерфейс Ethernet не поддерживается"),
    ZERO_SET_UNAVAILABLE(0x15, "Установка >0< невозможна"),
    WEIGHT_ON_POWER_ON(0x18, "Установлена нагрузка на платформу при включении весового устройства"),
    SCALES_IS_DEFECTIVE(0x19, "Весовое устройство неисправно"),
    NO_CONNECTION(0x17, "Нет связи с модулем взвешивающим"),
    ;

    private static final byte command = 0x28;
    private final byte code;
    private final String description;

    Error(int code, String description) {
        this.code = (byte) code;
        this.description = description;
    }

    public static boolean is(int code) {
        for (Error e : values()) {
           if (e.code == code) {
               return true;
           }
        }
        return false;
    }

    public static Error of(int code) {
        for (Error e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Can't create Error from this code (" + code + ")");
    }

    public static byte getCommand() {
        return command;
    }

    public byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
