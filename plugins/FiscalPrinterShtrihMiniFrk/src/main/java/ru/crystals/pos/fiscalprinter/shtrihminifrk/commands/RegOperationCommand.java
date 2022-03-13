package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihOperation;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда Операция. Замена команде добавления товара в чек для ФФД 1.00
 * <p>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 *
 * @author borisov
 */
public class RegOperationCommand extends BaseCommand<Object> {

    /**
     * Позиция, факт продажи которой надо зарегистрировать
     */
    private ShtrihOperation operation;

    /**
     * Единственно правильный конструктор.
     *
     * @param operation позиция, факт продажи которой надо зарегистрировать
     * @param password  пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException if the argument <code>position</code> is <code>null</code>
     */
    public RegOperationCommand(ShtrihOperation operation, int password) {
        super(password);
        if (operation == null) {
            throw new NullPointerException("RegSaleCommand(ShtrihPosition): The argument is NULL!");
        }
        this.operation = operation;
    }

    @Override
    public String toString() {
        return String.format("sale-cmd [operation: %s]", operation);
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x46;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 30 байт + длина названия позиции (название не должно быть короче 40 символов)
        int positionNameLength = 0;
        if (operation.getStringForPrinting() != null && operation.getStringForPrinting().length() > positionNameLength) {
            positionNameLength = operation.getStringForPrinting().length();
        }
        byte[] result = new byte[30 + positionNameLength];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Тип операции (1 байт)
        result[4] = operation.getCheckType();

        // Количество (6 байт)
        byte[] quantity = ShtrihUtils.getLongAsByteArray(operation.getQuantity());
        quantity = ShtrihUtils.inverse(quantity);
        System.arraycopy(quantity, 0, result, 5, 6);

        // Цена (5 байт)
        byte[] price = ShtrihUtils.getLongAsByteArray(operation.getPrice());
        price = ShtrihUtils.inverse(price);
        System.arraycopy(price, 0, result, 11, 5);

        // сумма операции (5 байт)
        // если сумма операции 0 x ff ff ff ff ff то сумма операции рассчитывается кассой как цена х количество,
        // в противном случае сумма операции берётся из команды и не должна отличаться более чем на +-1 коп от рассчитанной кассой.
        byte[] summ = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        if (operation.getSumm() != null && operation.getSumm() > 0) {
            summ = ShtrihUtils.getLongAsByteArray(operation.getSumm());
            summ = ShtrihUtils.inverse(summ);
        }
        System.arraycopy(summ, 0, result, 16, 5);

        // Сумма налога (5 байт)
        // В режиме начисления налогов 1 ( 1 Таблица) налоги на позицию и на чек должны передаваться из верхнего ПО.
        // Если в сумме налога на позицию передать 0 x FF FF FF FF FF то считается что сумма налога на позицию не указана,
        // в противном случае сумма налога учитывается ФР и передаётся в ОФД.
        byte[] taxValue = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        if (operation.getTaxValue() != null && operation.getTaxValue() > 0) {
            taxValue = ShtrihUtils.getLongAsByteArray(operation.getTaxValue());
            taxValue = ShtrihUtils.inverse(taxValue);
        }
        System.arraycopy(taxValue, 0, result, 21, 5);

        // Налоговая ставка (1 байт)
        result[26] = (byte) (1 << (operation.getTaxOne() - 1));

        // Номер отдела (1 байт)
        result[27] = operation.getDepartment();

        // Признак способа расчета (1 байт)
        result[28] = operation.getPaymentTypeSing();

        // Признак предмета расчета (1 байт)
        result[29] = operation.getPaymentItemSing();

        // Наименование товара (0-128 байт)
        byte[] text = getStringAsByteArray(operation.getStringForPrinting(), positionNameLength);
        System.arraycopy(text, 0, result, 30, positionNameLength);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
