package ru.crystals.scales.ncr7872;


import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum NcrStatus {

    UNKNOWN(ResBundleScalesNCR7872.getString("UNKNOWN_ERROR")),
    NO_ERROR(""),
    SCALES_NOT_RESPOND(ResBundleScalesNCR7872.getString("SCALES_NOT_RESPOND")),
    SCALES_NOT_READY(0x30, ResBundleScalesNCR7872.getString("SCALES_NOT_READY")),
    SCALES_UNSTABLE(0x31, ResBundleScalesNCR7872.getString("SCALES_NOT_STABILIZED")),
    SCALES_OVERLOAD(0x32, ResBundleScalesNCR7872.getString("SCALES_OVERLOAD")),
    STABLE_ZERO_WEIGHT(0x33, ResBundleScalesNCR7872.getString("ZERO_WEIGHT")),
    NEGATIVE_WEIGHT(0x35, ResBundleScalesNCR7872.getString("NEGATIVE_WEIGHT")),
    ;

    private static Map<Integer, NcrStatus> mappedByCode = Arrays.stream(values())
            .filter(e -> Objects.nonNull(e.getCode()))
            .collect(Collectors.toMap(NcrStatus::getCode, Function.identity()));

    private Integer code;
    private String description;

    NcrStatus(String description) {
        this(null, description);
    }

    NcrStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NcrStatus getByCode(char code) {
        return mappedByCode.getOrDefault((int) code, UNKNOWN);
    }

}
