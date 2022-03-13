package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import java.time.LocalDateTime;

/**
 * Программная смена. Будет использоваться, до тех пор, пока не будет пробит первый чек.
 */
public class SoftShift {

    /**
     * Номер смены
     */
    private long number = -1;
    /**
     * Открытие смены
     */
    private LocalDateTime openTime = null;
    /**
     * Закрытие смены
     */
    private LocalDateTime closeTime = null;
    /**
     * Попытка закрыть фр смену
     */
    private boolean tryingCloseHardShift = false;

    public SoftShift() {
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isTryingCloseHardShift() {
        return tryingCloseHardShift;
    }

    public void setTryingCloseHardShift(boolean tryingCloseHardShift) {
        this.tryingCloseHardShift = tryingCloseHardShift;
    }
}
