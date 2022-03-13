package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Позиция в чеке.
 * 
 * @author aperevozchikov
 */
public class ShtrihPosition {

    /**
     * название позиции
     */
    private String name = "";

    /**
     * ЦЕНА позиции, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long price;

    /**
     * Количество товара в позиции, в тысячных долях от международных систем единиц СИ - в "граммах"
     */
    private long quantity;

    /**
     * Номер отдела, в котором продана эта позиция
     */
    private byte deptNo = 1;

    /**
     * Налог 1 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxOne;

    /**
     * Налог 2 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxTwo;

    /**
     * Налог 3 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxThree;

    /**
     * Налог 4 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxFour;
    
    
    public ShtrihPosition() {
    }

    /**
     * Удобный конструктор.
     * 
     * @param name
     *            название позиции
     * @param price
     *            ЦЕНА позиции, в "копейках"
     * @param quantity
     *            количество товара в позиции, в "граммах"
     * @param deptNo
     *            номер отдела. в котором продали эту позицию
     */
    public ShtrihPosition(String name, long price, long quantity, byte deptNo) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.deptNo = deptNo;
    }
    
    @Override
    public String toString() {
        return String.format("shtrih-position [name: \"%s\"; price: %s; quantity: %s; dept: %s]", getName(), getPrice(), getQuantity(), getDeptNo());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public byte getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(byte deptNo) {
        this.deptNo = deptNo;
    }

    public byte getTaxOne() {
        return taxOne;
    }

    public void setTaxOne(byte taxOne) {
        this.taxOne = taxOne;
    }

    public byte getTaxTwo() {
        return taxTwo;
    }

    public void setTaxTwo(byte taxTwo) {
        this.taxTwo = taxTwo;
    }

    public byte getTaxThree() {
        return taxThree;
    }

    public void setTaxThree(byte taxThree) {
        this.taxThree = taxThree;
    }

    public byte getTaxFour() {
        return taxFour;
    }

    public void setTaxFour(byte taxFour) {
        this.taxFour = taxFour;
    }
}