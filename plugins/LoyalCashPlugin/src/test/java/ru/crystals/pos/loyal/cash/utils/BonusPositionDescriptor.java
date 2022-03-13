package ru.crystals.pos.loyal.cash.utils;

/**
 * Created by v.osipov on 30.03.2017.
 */
public class BonusPositionDescriptor {
    public long number;
    public long value;
    public long guid;

    public BonusPositionDescriptor(long number, long value, long guid) {
        this.number = number;
        this.value = value;
        this.guid = guid;
    }
}
