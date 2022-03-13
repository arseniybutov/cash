package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда регистрации факта продажи позиции.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 *
 * @author aperevozchikov
 */
public class RegSaleCommand extends BaseCommand<Object> {

    /**
     * Позиция, факт продажи которой надо зарегистрировать
     */
    private ShtrihPosition position;

    /**
     * Единственно правильный конструктор.
     *
     * @param position позиция, факт продажи которой надо зарегистрировать
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException if the argument <code>position</code> is <code>null</code>
     */
    public RegSaleCommand(ShtrihPosition position, int password) {
        super(password);
        if (position == null) {
            throw new NullPointerException("RegSaleCommand(ShtrihPosition): The argument is NULL!");
        }
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("sale-cmd [position: %s]", position);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x80;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 19 байт + длина названия позиции (название не должно быть короче 40 символов)
        int positionNameLength = 40;
        if (position.getName() != null && position.getName().length() > positionNameLength) {
            positionNameLength = position.getName().length();
        }
        byte[] result = new byte[19 + positionNameLength];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Количество (5 байт)
        byte[] quantity = ShtrihUtils.getLongAsByteArray(position.getQuantity());
        quantity = ShtrihUtils.inverse(quantity);
        System.arraycopy(quantity, 0, result, 4, 5);

        // Цена (5 байт)
        byte[] price = ShtrihUtils.getLongAsByteArray(position.getPrice());
        price = ShtrihUtils.inverse(price);
        System.arraycopy(price, 0, result, 9, 5);

        // Номер отдела (1 байт)
        result[14] = position.getDeptNo();

        // Налог 1 (1 байт)
        result[15] = position.getTaxOne();

        // Налог 2 (1 байт)
        result[16] = position.getTaxTwo();

        // Налог 3 (1 байт)
        result[17] = position.getTaxThree();

        // Налог 4 (1 байт)
        result[18] = position.getTaxFour();

        // Текст (40 байт)
        byte[] text = getStringAsByteArray(position.getName(), positionNameLength);
        System.arraycopy(text, 0, result, 19, positionNameLength);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
