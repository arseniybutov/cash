package ru.crystals.pos.loyal.cash.utils;

/**
 * Created by v.osipov on 30.03.2017.
 */
public class PositionDescriptor {
    public long number;
    public long qnty;
    public long price;
    public long id;

    public PositionDescriptor(long number, long qnty, long price, long id) {
        this.number = number;
        this.qnty = qnty;
        this.price = price;
        this.id = id;
    }
}
