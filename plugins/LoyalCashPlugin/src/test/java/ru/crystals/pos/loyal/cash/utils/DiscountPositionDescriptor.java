package ru.crystals.pos.loyal.cash.utils;

import ru.crystals.discounts.ActionType;

/**
 * Created by v.osipov on 30.03.2017.
 */
public class DiscountPositionDescriptor {
    public long number;
    public long qnty;
    public long value;
    public long guid;
    public ActionType actionType;

    public DiscountPositionDescriptor(long number, long qnty, long value, long guid, ActionType actionType) {
        this.number = number;
        this.qnty = qnty;
        this.value = value;
        this.guid = guid;
        this.actionType = actionType;
    }
}
