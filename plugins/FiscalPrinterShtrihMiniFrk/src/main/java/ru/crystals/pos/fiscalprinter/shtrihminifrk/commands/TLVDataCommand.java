package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Передать ТЛВ". Передать структуру TLV в ФН.
 * Данные документа ФН в формате TLV (согласно документу ФНС «Форматы фискальных документов»).
 * Например, чтобы передать тэг 1008 «адрес покупателя» со значением 12345678
 * следует записать в TLVData следующую последовательность байт:
 * F0h 03h 08h 00h 31h 32h 33h 34h 35h 36h 37h 38h,
 * где F0h03h – код тэга, 08h00h – длина сообщения.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является строка или фрагмент "сыром" виде.
 */
public class TLVDataCommand<T> extends BaseCommand<String> {

    private T clientData;
    private Tags tag;

    /**
     * Единственно правильный конструктор.
     *
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public TLVDataCommand(Tags tag, T clientData, int password) {
        super(password);

        this.tag = tag;
        this.clientData = clientData;
    }

    @Override
    public String toString() {
        return String.format("load-tlv-data-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x0C;
    }

    @Override
    public byte[] getArguments() {
        byte[] bytesClientData = null;
        if (clientData instanceof String) {
            bytesClientData = ((String) clientData).getBytes(Charset.forName("cp866"));
        } else if (clientData instanceof Date) {
            int unixTime = (int) ((((Date) clientData).getTime() + GregorianCalendar.getInstance().getTimeZone().getRawOffset()) / 1000);
            bytesClientData = ShtrihUtils.inverse(ByteBuffer.allocate(4).putInt(unixTime).array());
        }
        // 4 байта - пароль, 4 байта -заголовок TLV, данные
        byte[] result = new byte[4 + 4 + bytesClientData.length];

        // Пароль оператора (4 байта)
        int resByteCount = 4;
        System.arraycopy(password, 0, result, 0, resByteCount);
        for (byte code : tag.getCode()) {
            result[resByteCount++] = code;
        }
        result[resByteCount++] = (byte) bytesClientData.length;
        result[resByteCount++] = (byte) 0x00;
        System.arraycopy(bytesClientData, 0, result, resByteCount, bytesClientData.length);

        return result;
    }

    @Override
    public String decodeResponse(byte[] response) {
        String result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        // наш ответ - в байтах с 5го по препоследний
        result = getString(Arrays.copyOfRange(response, 5, response.length - 1));

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть как минимум 6 символов (как минимум 1 байт "полезной нагрузки" и 5 служебных байтов)
        return response.length >= 6;
    }

    /**
     * перечисление используемых тегов
     */
    public enum Tags {
        /**
         * наименование основания для коррекции(строка)
         */
        CORRECTION_RECEIPT_REASON(1177, new byte[]{(byte) 0x99, (byte) 0x04}),
        /**
         * дата документа основания для коррекции(дата, время должно быть равно 0:0:0:0)
         */
        CORRECTION_RECEIPT_DATE(1178, new byte[]{(byte) 0x9A, (byte) 0x04}),
        /**
         * номер документа основания для коррекции(строка)
         */
        CORRECTION_RECEIPT_DOC_NUMBER(1179, new byte[]{(byte) 0x9B, (byte) 0x04}),

        /**
         * ИНН кассира(строка - 12 символов, фиксированой длины)
         */
        CASHIER_INN(1203, new byte[]{(byte) 0xB3, (byte) 0x04}),

        /**
         * Телефон или email клиента (строка - 12 символов, фиксированой длины)
         */
        BAYER_EMAIL(1008, new byte[]{(byte) 0xF0, (byte) 0x03});

        /**
         * Номер тега
         */
        private int number;
        /**
         * код тега
         */
        private byte[] code;

        Tags(int number, byte[] code) {
            this.number = number;
            this.code = code;
        }

        public byte[] getCode() {
            return code;
        }
    }
}