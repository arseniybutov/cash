package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto;

import java.util.List;
import java.util.Objects;

public class ReceiptVO {

    private final long cashPaymentSum;
    private final long cardPaymentSum;
    private final List<PositionVO> positions;

    public ReceiptVO(long cashPaymentSum, long cardPaymentSum, List<PositionVO> positions) {
        Objects.requireNonNull(positions);
        this.cashPaymentSum = cashPaymentSum;
        this.cardPaymentSum = cardPaymentSum;
        this.positions = positions;
    }

    public long getCashPaymentSum() {
        return cashPaymentSum;
    }

    public long getCardPaymentSum() {
        return cardPaymentSum;
    }

    public List<PositionVO> getPositions() {
        return positions;
    }
}
