package ru.crystals.pos.loyal.cash.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by s.pavlikhin on 27.04.2017.
 */
public class PurchaseDescriptor {

    Long id;
    List<PositionDescriptor> positions;

    public PurchaseDescriptor(Long id, List<PositionDescriptor> positions) {
        this.id = id;
        this.positions = positions;
    }
    public PurchaseDescriptor(Long id, PositionDescriptor ... positions) {
        this(id, Arrays.asList(positions));
    }
}
