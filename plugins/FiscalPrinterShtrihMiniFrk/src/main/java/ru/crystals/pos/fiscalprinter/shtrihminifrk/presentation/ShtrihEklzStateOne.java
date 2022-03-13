package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.util.Date;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Состояние по коду 1 ЭКЛЗ.
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihEklzStateOne {
    
    /**
     * Итог документа последнего КПК: 0000000000..9999999999
     */
    private long lastKpkTotal;
    
    /**
     * Дата и время последнего КПК
     */
    private Date lastKpkDate;
    
    /**
     * Номер последнего КПК: 00000000..99999999
     */
    private long lastKpk;
    
    /**
     * Номер ЭКЛЗ: 0000000000..9999999999
     */
    private long eklzNum;
    
    /**
     * какие-то флаги ЭКЛЗ - подробного описания не нашел.
     */
    private byte eklzFlags;

    @Override
    public String toString() {
        return String.format("shtrih-eklz-state-1 [kpk-total: %s; kpk-date: %s; kpk: %s; eklz-num: %s; eklz-flags: %s]", getLastKpkTotal(),
            getLastKpkDate() == null ? "(NULL)" : String.format("%1$tF %1$tH:%1$tM", getLastKpkDate()), getLastKpk(), getEklzNum(),
            PortAdapterUtils.toUnsignedByte(getEklzFlags()));
    }
    
    // getters & setters
    
    public long getLastKpkTotal() {
        return lastKpkTotal;
    }

    public void setLastKpkTotal(long lastKpkTotal) {
        this.lastKpkTotal = lastKpkTotal;
    }

    public Date getLastKpkDate() {
        return lastKpkDate;
    }

    public void setLastKpkDate(Date lastKpkDate) {
        this.lastKpkDate = lastKpkDate;
    }

    public long getLastKpk() {
        return lastKpk;
    }

    public void setLastKpk(long lastKpk) {
        this.lastKpk = lastKpk;
    }

    public long getEklzNum() {
        return eklzNum;
    }

    public void setEklzNum(long eklzNum) {
        this.eklzNum = eklzNum;
    }

    public byte getEklzFlags() {
        return eklzFlags;
    }

    public void setEklzFlags(byte eklzFlags) {
        this.eklzFlags = eklzFlags;
    }
}