package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Заводской и регистрационный номера ФР семейства Штрих.
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihRegNum {
    
    /**
     * Заводской номер (7 байт) 00000000000000…99999999999999
     */
    private long deviceNo;
    
    /**
     * РНМ (7 байт) 00000000000000…99999999999999
     */
    private long regNo;
    
    
    @Override
    public String toString() {
        return String.format("reg-num [deviceNo: %s; regNo: %s]", getDeviceNo(), getRegNo());
    }

    public long getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(long deviceNo) {
        this.deviceNo = deviceNo;
    }

    public long getRegNo() {
        return regNo;
    }

    public void setRegNo(long regNo) {
        this.regNo = regNo;
    }
}
