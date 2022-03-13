package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда закрытия чека.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды будет размер сдачи, в МДЕ.
 * 
 * @author aperevozchikov
 *
 */
public class CloseReceiptCommand extends BaseCommand<Long> {
    
    /**
     * Total чека, что будем закрывать
     */
    private ShtrihReceiptTotal receiptTotal;

    /**
     * Единственно правильный конструктор.
     * 
     * @param receiptTotal
     *            total чека, что надо закрыть
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException
     *             if the argument <code>receiptTotal</code> is <code>null</code>
     */
    public CloseReceiptCommand(ShtrihReceiptTotal receiptTotal, int password) {
        super(password);
        
        if (receiptTotal == null) {
            throw new NullPointerException("CloseReceiptCommand(ShtrihReceiptTotal): The argument is NULL!");
        }

        this.receiptTotal = receiptTotal;
    }
    
    @Override
    public String toString() {
        return String.format("close-receipt-cmd [total: %s; password: %s]", receiptTotal, PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x85;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 70 байт
        byte[] result = new byte[70];
        
        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        
        // Сумма наличных (5 байт)
        byte[] sum = ShtrihUtils.getLongAsByteArray(receiptTotal.getCashSum());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 4, 5);
        
        // Сумма типа оплаты 2 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotal.getSecondPaymentTypeSum());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 9, 5);
        
        // Сумма типа оплаты 3 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotal.getThirdPaymentTypeSum());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 14, 5);
        
        // Сумма типа оплаты 4 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotal.getFourthPaymentTypeSum());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 19, 5);
        
        // Скидка/Надбавка(в случае отрицательного значения) в % на чек от 0 до 99,99 % (2 байта со знаком) -9999…9999
        byte[] discount = new byte[2];
        byte[] discount8 = ShtrihUtils.getLongAsByteArray(Math.abs(receiptTotal.getDiscountPercent()));
        System.arraycopy(discount8, 6, discount, 0, 2);
        if (receiptTotal.getDiscountPercent() < 0) {
            discount = ShtrihUtils.changeSign(discount);
        }
        discount = ShtrihUtils.inverse(discount);
        System.arraycopy(discount, 0, result, 24, 2);
        
        // Налог 1 (1 байт)
        result[26] = receiptTotal.getTaxOne();
        
        // Налог 2 (1 байт)
        result[27] = receiptTotal.getTaxTwo();
        
        // Налог 3 (1 байт)
        result[28] = receiptTotal.getTaxThree();
        
        // Налог 4 (1 байт)
        result[29] = receiptTotal.getTaxFour();
        
        // Текст (40 байт)
        byte[] text = getStringAsByteArray(receiptTotal.getText(), 40);
        System.arraycopy(text, 0, result, 30, 40);
        
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
        if (response.length != 11) {
            return false;
        }
        
        return true;
    }
}
