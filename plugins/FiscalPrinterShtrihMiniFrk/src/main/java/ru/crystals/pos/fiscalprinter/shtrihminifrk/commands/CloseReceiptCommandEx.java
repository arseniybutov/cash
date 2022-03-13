package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalEx;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.util.Arrays;

/**
 * Команда расширенное закрытие чека.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды будет размер сдачи, в МДЕ.
 */
public class CloseReceiptCommandEx extends BaseCommand<Long> {

    /**
     * Total чека, что будем закрывать
     */
    private ShtrihReceiptTotalEx receiptTotalEx;

    /**
     * Единственно правильный конструктор.
     *
     * @param receiptTotalEx
     *            total чека, что надо закрыть
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException
     *             if the argument <code>receiptTotalEx</code> is <code>null</code>
     */
    public CloseReceiptCommandEx(ShtrihReceiptTotalEx receiptTotalEx, int password) {
        super(password);
        
        if (receiptTotalEx == null) {
            throw new NullPointerException("CloseReceiptCommand(ShtrihReceiptTotal): The argument is NULL!");
        }

        this.receiptTotalEx = receiptTotalEx;
    }
    
    @Override
    public String toString() {
        return String.format("close-receipt-cmd [total: %s; password: %s]", receiptTotalEx, PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x8E;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 130 байт
        byte[] result = new byte[130];
        
        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        
        // Сумма наличных (5 байт)
        byte[] sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm1());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 4, 5);
        
        // Сумма типа оплаты 2 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm2());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 9, 5);
        
        // Сумма типа оплаты 3 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm3());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 14, 5);
        
        // Сумма типа оплаты 4 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm4());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 19, 5);

        // Сумма типа оплаты 5 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm5());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 24, 5);

        // Сумма типа оплаты 6 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm6());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 29, 5);

        // Сумма типа оплаты 7 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm7());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 34, 5);

        // Сумма типа оплаты 8 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm8());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 39, 5);

        // Сумма типа оплаты 9 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm9());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 44, 5);

        // Сумма типа оплаты 10 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm10());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 49, 5);

        // Сумма типа оплаты 11 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm11());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 54, 5);

        // Сумма типа оплаты 12 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm12());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 59, 5);

        // Сумма типа оплаты 13 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm13());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 64, 5);

        // Сумма типа оплаты 14 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm14());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 69, 5);

        // Сумма типа оплаты 15 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm15());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 74, 5);

        // Сумма типа оплаты 16 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getSumm16());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 79, 5);

        // Скидка/Надбавка(в случае отрицательного значения) в % на чек от 0 до 99,99 % (2 байта со знаком) -9999…9999
        byte[] discount = new byte[2];
        byte[] discount8 = ShtrihUtils.getLongAsByteArray(Math.abs(receiptTotalEx.getDiscountPercent()));
        System.arraycopy(discount8, 6, discount, 0, 2);
        if (receiptTotalEx.getDiscountPercent() < 0) {
            discount = ShtrihUtils.changeSign(discount);
        }
        discount = ShtrihUtils.inverse(discount);
        System.arraycopy(discount, 0, result, 84, 2);
        
        // Налог 1 (1 байт)
        result[86] = receiptTotalEx.getTaxOne();
        
        // Налог 2 (1 байт)
        result[87] = receiptTotalEx.getTaxTwo();
        
        // Налог 3 (1 байт)
        result[88] = receiptTotalEx.getTaxThree();
        
        // Налог 4 (1 байт)
        result[89] = receiptTotalEx.getTaxFour();
        
        // Текст (40 байт)
        byte[] text = getStringAsByteArray(receiptTotalEx.getText(), 40);
        System.arraycopy(text, 0, result, 90, 40);
        
        return result;
    }

    @Override
    public Long decodeResponse(byte[] response) {
        long result;
        
        if (!validateResponse(response)) {
            return null;
        }
        
        // сдача записана в байтах с 6го по 10й (от мл. байта к старшему)
        byte[] change = Arrays.copyOfRange(response, 5, 10);
        change = ShtrihUtils.inverse(change);
        result = ShtrihUtils.getLong(change);
        
        return result;
    }
    
    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }
        
        // длина ответа должна быть 11 байт: 
        //  STX, байт длины, байт команды, код ошибки, номер оператора, сдача (5 байт), LRC
        return response.length == 11;

    }
}
