package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Надбавка".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author Eduard Tatarinov
 */
public class RegMarginCommand extends BaseCommand<Object> {

    /**
     * Сама скидка. что надо зарегистрировать
     */
    private ShtrihDiscount discount;

    /**
     * Единственно правильный конструктор.
     *
     * @param discount
     *            скидка. что надо зарегистрировать
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>discount</code> == <code>null</code>
     */
    public RegMarginCommand(ShtrihDiscount discount, int password) {
        super(password);

        if (discount == null) {
            throw new IllegalArgumentException("RegMarginCommand: the argument is NULL!");
        }
        this.discount = discount;
    }

    @Override
    public String toString() {
        return String.format("reg-margin-cmd [discount: %s]", discount);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x87;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 53 байта
        byte[] result = new byte[53];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Сумма (5 байт) 0000000000…9999999999
        byte[] sum = ShtrihUtils.getLongAsByteArray(discount.getSum());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 4, 5);

        // Налог 1 (1 байт) «0» – нет, «1»…«4» – налоговая группа
        result[9] = discount.getTaxOne();

        // Налог 2 (1 байт) «0» – нет, «1»…«4» – налоговая группа
        result[10] = discount.getTaxTwo();

        // Налог 3 (1 байт) «0» – нет, «1»…«4» – налоговая группа
        result[11] = discount.getTaxThree();

        // Налог 4 (1 байт) «0» – нет, «1»…«4» – налоговая группа
        result[12] = discount.getTaxFour();

        // Текст (40 байт)
        byte[] text = getStringAsByteArray(discount.getText(), 40);
        System.arraycopy(text, 0, result, 13, 40);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}