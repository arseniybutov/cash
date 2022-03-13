package ru.crystals.scales.bizerba;

public enum  ErrorCode {
    OK(0),
    GENERAL_ERROR(1),
    PARITY_ERROR(2),
    INCORRECT_RECORD(10),
    NO_UNIT_PRICE(11),
    NO_TARE_VALUE(12),
    NO_TEXT(13),
    IN_MOTION(20),
    NO_MOTION_SINCE_LAST_OPERATION(21),
    NOT_AVAILABLE_PRICE_CALC(22),
    MIN_RANGE(30),
    UNDERLOAD(31),
    OVERLOAD(32),
    UNKNOWN(33),
    CONNECTION_TIMEOUT(404);

    private int value;

    private ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ErrorCode getType(int value) {
        for (ErrorCode dt : values()) {
            if (dt.getValue() == value) {
                return dt;
            }
        }
        return UNKNOWN;
    }
}
