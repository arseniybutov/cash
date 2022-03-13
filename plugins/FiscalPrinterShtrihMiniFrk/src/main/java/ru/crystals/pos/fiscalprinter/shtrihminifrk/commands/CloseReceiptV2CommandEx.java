package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalV2Ex;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.util.Arrays;

/**
 * Команда расширенное закрытие чека вариант 2.
 * <p>
 * {@link #decodeResponse(byte[]) Ответом} данной команды будет размер сдачи, в МДЕ.
 */
public class CloseReceiptV2CommandEx extends BaseCommand<Long> {

    /**
     * Total чека, что будем закрывать
     */
    private ShtrihReceiptTotalV2Ex receiptTotalEx;

    /**
     * Единственно правильный конструктор.
     *
     * @param receiptTotalEx total чека, что надо закрыть
     * @param password       пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException if the argument <code>receiptTotalEx</code> is <code>null</code>
     */
    public CloseReceiptV2CommandEx(ShtrihReceiptTotalV2Ex receiptTotalEx, int password) {
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
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x45;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 180 байт
        byte[] result = new byte[180];

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

        // Округление до рубля в копейках (1 байт)
        result[84] = receiptTotalEx.getRoundingSumm();

        // Налог 1 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxOne());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 85, 5);

        // Налог 2 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxTwo());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 90, 5);

        // Налог 3 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxThree());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 95, 5);

        // Налог 4 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxFour());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 100, 5);

        // Налог 5 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxFive());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 105, 5);

        // Налог 6 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(receiptTotalEx.getTaxSix());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 110, 5);

        // Система налогообложения (1 байт)
        result[115] = receiptTotalEx.getTaxSystem();

        // Текст (0-62 байт)
        byte[] text = getStringAsByteArray(receiptTotalEx.getText(), 62);
        System.arraycopy(text, 0, result, 116, 62);

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

        // длина ответа должна быть 14 байт:
        // Код ошибки: 1 байт, Сдача ( 5 байт), Номер ФД :4 байта, Фискальный признак: 4 байта
        // // STX, байт длины, байт команды, код ошибки, номер оператора, сдача (5 байт), LRC
        return response.length == 11;

    }
}
