package ru.crystals.pos.cash_glory.constants;

import java.math.BigInteger;

public enum DeviceType {
    RBW(BigInteger.ONE),
    RCW(BigInteger.valueOf(2));

    private BigInteger type;

    private DeviceType(BigInteger type) {
        this.type = type;
    }

    public BigInteger getType() {
        return type;
    }

    public static DeviceType valueOf(int type) {
        return type == 1 ? DeviceType.RBW : DeviceType.RCW;
    }

}
