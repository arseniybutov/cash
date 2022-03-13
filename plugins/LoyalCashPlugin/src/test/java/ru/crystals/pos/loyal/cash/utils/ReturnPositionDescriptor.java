package ru.crystals.pos.loyal.cash.utils;

/**
 * Created by v.osipov on 30.03.2017.
 */
public class ReturnPositionDescriptor {
    public long number;
    public long qnty;

    public ReturnPositionDescriptor(long number, long qnty) {
        this.number = number;
        this.qnty = qnty;
    }
}
