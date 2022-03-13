package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.math.BigInteger;
import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.FiscalMemorySums;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос суммы записей в ФП".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link FiscalMemorySums структура с суммами записей в фискальной памяти}.
 * 
 * @author aperevozchikov
 */
public class GetFiscalMemorySums extends BaseCommand<FiscalMemorySums> {
    /**
     * Тип запроса: «0» – сумма всех записей, «1» – сумма записей после последней перерегистрации
     */
    private byte requestType;

    /**
     * Единственно правильный конструктор.
     * 
     * @param allRecords
     *            флаг-признак: вернуть сумму всех записей (<code>true</code>) или только сумму записей после последней пере-регистрации (
     *            <code>false</code>)
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetFiscalMemorySums(boolean allRecords, int password) {
        super(password);
        this.requestType = allRecords ? (byte) 0 : (byte) 1;
    }

    @Override
    public String toString() {
        return String.format("get-fiscal-mem-sums-cmd [request-type: %s]", requestType);
    }

    @Override
    public byte getCommandCode() {
        return 0x62;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Тип запроса (1 байт)
        result[4] = requestType;

        return result;
    }

    @Override
    public FiscalMemorySums decodeResponse(byte[] response) {
        FiscalMemorySums result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new FiscalMemorySums();

        // Сумма сменных итогов продаж: байты с 6го (мл) по 13й (ст.):
        byte[] sum = Arrays.copyOfRange(response, 5, 14);
        // вот этот байт был "не наш". но мы его все равно взяли и обнулили - что ниже получить гарантировано положительное число
        sum[8] = 0;
        result.setSalesSum(new BigInteger(ShtrihUtils.inverse(sum)));

        // Сумма сменных итог покупок: байты с 14го (мл.) по 19й (ст.)
        // маркер отсутствия данных
        byte[] absentMarker = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        sum = Arrays.copyOfRange(response, 13, 19);
        if (Arrays.equals(sum, absentMarker)) {
            // ФП2 отсутствует
            result.setPurchasesSum(-1L);
        } else {
            // декодируем
            result.setPurchasesSum(ShtrihUtils.getLong(ShtrihUtils.inverse(sum)));
        }

        // Сумма сменных возвратов продаж: байты с 20го (мл.) по 25й (ст.)
        sum = Arrays.copyOfRange(response, 19, 25);
        if (Arrays.equals(sum, absentMarker)) {
            // ФП2 отсутствует
            result.setSalesReturnsSum(-1L);
        } else {
            // декодируем
            result.setSalesReturnsSum(ShtrihUtils.getLong(ShtrihUtils.inverse(sum)));
        }

        // Сумма сменных возвратов покупок: байты с 26го (мл.) по 31й (ст.)
        sum = Arrays.copyOfRange(response, 25, 31);
        if (Arrays.equals(sum, absentMarker)) {
            // ФП2 отсутствует
            result.setPurchasesReturnsSum(-1L);
        } else {
            // декодируем
            result.setPurchasesReturnsSum(ShtrihUtils.getLong(ShtrihUtils.inverse(sum)));
        }

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 32 байта (вместе со служебными символами)
        if (response.length != 32) {
            return false;
        }

        return true;
    }

}
