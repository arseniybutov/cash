package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда регистрации факта возврата позиции.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class RegReturnCommand extends BaseCommand<Object> {

    /**
     * Позиция, факт возврата которой надо зарегистрировать
     */
    private ShtrihPosition position;

    /**
     * Единственно правильный конструктор.
     * 
     * @param position
     *            позиция, факт возврата которой надо зарегистрировать
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException
     *             if the argument <code>position</code> is <code>null</code>
     */
    public RegReturnCommand(ShtrihPosition position, int password) {
        super(password);
        if (position == null) {
            throw new NullPointerException("RegReturnCommand(ShtrihPosition): The argument is NULL!");
        }
        this.position = position;
    }
    
    @Override
    public String toString() {
        return String.format("ret-cmd [position: %s; password: %s]", position, PortAdapterUtils.arrayToString(password));
    }
    
    @Override
    public byte getCommandCode() {
        return (byte) 0x82;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 59 байт
        byte[] result = new byte[59];
        
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
        byte[] text = getStringAsByteArray(position.getName(), 40);
        System.arraycopy(text, 0, result, 19, 40);
        
        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}