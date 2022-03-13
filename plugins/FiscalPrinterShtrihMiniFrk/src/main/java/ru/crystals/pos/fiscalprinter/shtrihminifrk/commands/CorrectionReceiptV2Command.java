package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihCorrectionReceiptV2;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда Сформировать чек коррекции V2.
 * <p>
 * {@link #decodeResponse(byte[]) Ответом} данной команды будет номер ФД
 */
public class CorrectionReceiptV2Command extends BaseCommand<Long> {

    /**
     * Total чека
     */
    private ShtrihCorrectionReceiptV2 correctionReceiptV2;

    /**
     * Единственно правильный конструктор.
     *
     * @param correctionReceiptV2 total чека коррекции, что надо сформировать
     * @param password            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws NullPointerException if the argument <code>correctionReceiptV2</code> is <code>null</code>
     */
    public CorrectionReceiptV2Command(ShtrihCorrectionReceiptV2 correctionReceiptV2, int password) {
        super(password);

        if (correctionReceiptV2 == null) {
            throw new NullPointerException("CorrectionReceiptV2Command(ShtrihCorrectionReceiptV2): The argument is NULL!");
        }

        this.correctionReceiptV2 = correctionReceiptV2;
    }

    @Override
    public String toString() {
        return String.format("correction-receipt-cmd [total: %s; password: %s]", correctionReceiptV2, PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x4A;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 72 байт
        byte[] result = new byte[67];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Тип коррекции
        result[4] = correctionReceiptV2.getCorrectionType();

        // Признак расчёта
        result[5] = correctionReceiptV2.getCalculationSign();

        // Сумма всего (5 байт)
        byte[] sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumAll());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 6, 5);

        // Сумма по чеку наличными (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumCash());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 11, 5);

        // Сумма по чеку электронными (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumElectron());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 16, 5);

        // Сумма по чеку предоплатой (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumPrepayment());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 21, 5);

        // Сумма по чеку постоплатой (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumPostpay());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 26, 5);

        // Сумма по чеку встречным представлением (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getSumCounteroffer());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 31, 5);

        // Сумма НДС 18% (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxOne());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 36, 5);

        // Сумма НДС 10% (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxTwo());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 41, 5);

        // Сумма расчета по ставке 0% (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxThree());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 46, 5);

        // Сумма расчета по чеку без НДС (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxFour());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 51, 5);

        // Сумма расчета по чеку 18/118 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxFive());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 56, 5);

        // Сумма расчета по расч. ставке 10/110 (5 байт)
        sum = ShtrihUtils.getLongAsByteArray(correctionReceiptV2.getTaxSix());
        sum = ShtrihUtils.inverse(sum);
        System.arraycopy(sum, 0, result, 61, 5);

        // Код применяемой системы налогооблажения (1 байт)
        result[66] = correctionReceiptV2.getTaxSystem();

        return result;
    }

    @Override
    public Long decodeResponse(byte[] response) {
        long result;

        if (!validateResponse(response)) {
            return null;
        }

        byte[] change = Arrays.copyOfRange(response, 7, 11);
        change = ShtrihUtils.inverse(change);
        result = ShtrihUtils.getLong(change);
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        return super.validateResponse(response) && response.length == 16;
    }
}
