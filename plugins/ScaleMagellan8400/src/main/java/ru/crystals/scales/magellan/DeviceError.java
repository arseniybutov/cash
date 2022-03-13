package ru.crystals.scales.magellan;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DeviceError {

    OK("", 0x34),
    NOT_CONNECTED(ResBundleScaleMagellan8400.getString("NOT_CONNECTED")),
    CAN_NOT_SEND_COMMAND(ResBundleScaleMagellan8400.getString("CAN_NOT_SEND_COMMAND")),
    SCALES_NOT_READY(ResBundleScaleMagellan8400.getString("SCALES_NOT_READY"), 0x30),
    SCALES_STABILIZING(ResBundleScaleMagellan8400.getString("SCALES_STABILIZING"), 0x31),
    SCALES_OVERLOAD(ResBundleScaleMagellan8400.getString("SCALES_OVERLOAD"), 0x32),
    ZERO_WEIGHT(ResBundleScaleMagellan8400.getString("ZERO_WEIGHT"), 0x33),
    NEGATIVE_WEIGHT(ResBundleScaleMagellan8400.getString("NEGATIVE_WEIGHT"), 0x35),
    SCALES_NOT_STABILIZE(ResBundleScaleMagellan8400.getString("SCALES_NOT_STABILIZED")),
    SCALES_NOT_RESPOND(ResBundleScaleMagellan8400.getString("SCALES_NOT_RESPOND")),
    UNKNOWN_ERROR(ResBundleScaleMagellan8400.getString("UNKNOWN_ERROR"));

    private static final Map<Integer, DeviceError> nativeErrorsByCode = Arrays.stream(values())
            .filter(v -> v.getErrorCode() != 0)
            .collect(Collectors.toMap(DeviceError::getErrorCode, Function.identity()));

    private final String message;
    private final int errorCode;

    DeviceError(String message, int errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    DeviceError(String message) {
        this(message, 0);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static DeviceError getByErrorCode(char errorCode) {
        try {
            return nativeErrorsByCode.getOrDefault((int) errorCode, UNKNOWN_ERROR);
        } catch (NumberFormatException nfe) {
            return UNKNOWN_ERROR;
        }
    }

    public String getMessage() {
        return message;
    }
}
