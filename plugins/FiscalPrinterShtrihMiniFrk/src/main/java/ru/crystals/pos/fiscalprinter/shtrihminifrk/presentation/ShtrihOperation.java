package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;

/**
 * Операция в чеке.
 *
 * @author borisov
 */
public class ShtrihOperation {

    /**
     * Позиция
     */
    private Goods goods;

    /**
     * Тип операции.
     * <ol>
     * Возможные значения:
     * <li> Приход</li>
     * <li> Возврат прихода</li>
     * <li> расход</li>
     * <li> возврат расхода</li>
     * </ol>
     */
    private byte checkType = 1;

    /**
     * Количество (до 6 знаков после запятой)
     */
    private Long quantity;

    /**
     * ЦЕНА позиции, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private Long price;

    /**
     * Сумма операции.
     * Если сумма операции 0xffffffffff то сумма операции рассчитывается кассой как цена х количество,
     * в противном случае сумма операции берётся из команды и не должна отличаться более чем на +-1 коп от рассчитанной кассой.
     */
    private Long summ;

    /**
     * Сумма нолога, в МДЕ (Минимальных Денежных Единицах) - в "копейках".
     * В режиме начисления налогов 1 ( 1 Таблица) налоги на позицию и на чек должны передаваться из верхнего ПО.
     * Если в сумме налога на позицию передать 0xFFFFFFFFFF то считается что сумма налога на позицию не указана,
     * в противном случае сумма налога учитывается ФР и передаётся в ОФД.
     */
    private Long taxValue;

    /**
     * Налоговая ставка. «0» – нет, «1»…«6» – налоговая группа.
     * Для налогов 3 и 4 сумма налога всегда считается равной нулю и в ОФД не передаётся.
     */
    private byte taxOne;

    /**
     * Отдел (0..16 режим свободной продажи, 255 –режим продажи по коду товара)
     */
    private byte department;

    /**
     * Признак способа расчета.
     * <ol>
     * Возможные значения:
     * <li> Предоплата 100%</li>
     * <li> Частичная предоплата</li>
     * <li> Аванс</li>
     * <li> Полный расчет</li>
     * <li> Частичный расчет и кредит</li>
     * <li> Передача в кредит</li>
     * <li> Оплата кредита</li>
     * </ol>
     */
    private byte paymentTypeSing;

    /**
     * Признак предмета расчета.
     * <ol>
     * Возможные значения:
     * <li> Товар</li>
     * <li> Подакцизный товар</li>
     * <li> Работа</li>
     * <li> Услуга</li>
     * <li> Ставка азартной игры</li>
     * <li> Выигрыш азартной игры</li>
     * <li> Лотерейный билет</li>
     * <li> Выигрыш лотереи</li>
     * <li> Предоставление РИД</li>
     * <li> Платеж</li>
     * <li> Составной предмет расчета</li>
     * <li> Иной предмет расчета</li>
     * </ol>
     */
    private byte paymentItemSing;

    /**
     * Наименование товара. 0..128.
     * Если строка начинается символами //, то она передаётся на сервер ОФД но не печатается на кассе.
     */
    private String stringForPrinting;

    private boolean addItemCode = false;

    public ShtrihOperation() {
    }

    /**
     * Удобный конструктор.
     *
     * @param stringForPrinting название операции
     * @param price             ЦЕНА операции, в "копейках"
     * @param quantity          количество (!!!до 6 знаков после запятой!!! касса оперирует граммами(1/1000),
     *                          значит не забыть умножить еще на 1000!)
     * @param department        номер отдела
     */
    public ShtrihOperation(String stringForPrinting, long price, long quantity, byte department) {
        this.stringForPrinting = stringForPrinting;
        this.price = price;
        this.quantity = quantity;
        this.department = department;
    }

    public ShtrihOperation(Goods goods, String stringForPrinting, long price, long quantity, byte department) {
        this.goods = goods;
        this.stringForPrinting = stringForPrinting;
        this.price = price;
        this.quantity = quantity;
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("shtrih-operation [name: \"%s\"; price: %s; quantity: %s; dept: %s, operation: %s]",
                getStringForPrinting(), getPrice(), getQuantity(), getDepartment(), getCheckType());
    }

    public byte getCheckType() {
        return checkType;
    }

    public void setCheckType(byte checkType) {
        this.checkType = checkType;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getSumm() {
        return summ;
    }

    public void setSumm(Long summ) {
        this.summ = summ;
    }

    public Long getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(Long taxValue) {
        this.taxValue = taxValue;
    }

    public byte getTaxOne() {
        return taxOne;
    }

    public void setTaxOne(byte taxOne) {
        this.taxOne = taxOne;
    }

    public byte getDepartment() {
        return department;
    }

    public void setDepartment(byte department) {
        this.department = department;
    }

    public byte getPaymentTypeSing() {
        return paymentTypeSing;
    }

    public void setPaymentTypeSing(byte paymentTypeSing) {
        this.paymentTypeSing = paymentTypeSing;
    }

    public byte getPaymentItemSing() {
        return paymentItemSing;
    }

    public void setPaymentItemSing(byte paymentItemSing) {
        this.paymentItemSing = paymentItemSing;
    }

    public String getStringForPrinting() {
        return stringForPrinting;
    }

    public void setStringForPrinting(String stringForPrinting) {
        this.stringForPrinting = stringForPrinting;
    }

    public boolean isAddItemCode() {
        return addItemCode;
    }

    public void setAddItemCode(boolean addItemCode) {
        this.addItemCode = addItemCode;
    }

    public Goods getGoods() {
        return goods;
    }
}