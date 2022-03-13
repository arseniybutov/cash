package ru.crystals.pos.loyal.cash.utils;

/**
 * s.pavlikhin
 * Created by v.osipov on 30.03.2017.
 */
public class BonusTxDescriptor {

    public long sumAmount;
    public long bonusAmount;
    public long guid;
    public Long sberBnsChange;
    public Long plastekBnsChange;

    public BonusTxDescriptor(long sumAmount, long bonusAmount, long guid, Long sberBnsChange, Long plastekBnsChange) {
        this.sumAmount = sumAmount;
        this.bonusAmount = bonusAmount;
        this.guid = guid;
        this.sberBnsChange = sberBnsChange;
        this.plastekBnsChange = plastekBnsChange;
    }

    public BonusTxDescriptor(long sumAmount, long bonusAmount, long guid) {
        this.sumAmount = sumAmount;
        this.bonusAmount = bonusAmount;
        this.guid = guid;
        this.sberBnsChange = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BonusTxDescriptor)) return false;

        BonusTxDescriptor that = (BonusTxDescriptor) o;

        if (sumAmount != that.sumAmount) return false;
        if (bonusAmount != that.bonusAmount) return false;
        if (sberBnsChange != that.sberBnsChange) return false;
        if (plastekBnsChange != that.plastekBnsChange) return false;
        return guid == that.guid;
    }

    @Override
    public int hashCode() {
        int result = (int) (sumAmount ^ (sumAmount >>> 32));
        result = 31 * result + (int) (bonusAmount ^ (bonusAmount >>> 32));
        result = 31 * result + (sberBnsChange == null ? 0 : sberBnsChange.hashCode());
        result = 31 * result + (plastekBnsChange == null ? 0 : plastekBnsChange.hashCode());
        result = 31 * result + (int) (guid ^ (guid >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BonusTxDescriptor{" +
                "sumAmount=" + sumAmount +
                ", bonusAmount=" + bonusAmount +
                ", guid=" + guid +
                ", sberBnsChange=" + sberBnsChange +
                ", plastekBnsChange=" + plastekBnsChange +
                '}';
    }
}
