package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

import java.util.Arrays;

/**
 * Data = [ PMax, PMin, Pe, PT, Fix, CalCode, POVer, POSum ] <br>
 * <p>
 * Cmd = 0x76 - код ответа на {@link ru.crystals.pos.scale.massak.protocol100.request.GetScalePar GetScalePar}. <br>
 * <b>Блок Data:</b> <br>
 * PMax : char[], 2-20 байт - максимальная нагрузка. <br>
 * PMin : char[], 2-20 байт - минимальная нагрузка. <br>
 * Pe : char[], 2-10 байт - поверочный интервал весов 'e'. <br>
 * PT : char[], 2-10 байт - максимальная масса тары. <br>
 * Fix : char[], 7 байт - параметр фиксации веса. <br>
 * CalCode : char[], 12 байт - код юстировки. <br>
 * POVer : char[], 2-9 байт - версия ПО датчика взвешивания. <br>
 * POSum : char[], 2-8 байт - контрольная сумма ПО датчика взвешивания. <br>
 */
public class AckScalePar extends Response {

    public AckScalePar(byte[] answer) throws Protocol100Exception {
        super(0x76, answer);
    }

    public String getPMax() {
        return parseString(0);
    }

    public String getPMin() {
        return parseString(1);
    }

    public String getPe() {
        return parseString(2);
    }

    public String getPT() {
        return parseString(3);
    }

    public String getFix() {
        return parseString(4);
    }

    public String getCalCode() {
        return parseString(5);
    }

    public String getPOVer() {
        return parseString(6);
    }

    public String getPOSum() {
        return parseString(7);
    }

    /**
     * Поскольку блок Data состоит только из текстовой информации,
     * можно разбирать его целиком и брать нужный параметр из полученного массива строк
     * @param paramNumber номер следования параметра в блоке
     * @return текстовое значение параметра
     */
    @Override
    protected String parseString(int paramNumber) {
        return new String(Arrays.copyOfRange(answer, dataIndex, crcIndex))
                .split("\r\n")[paramNumber];
    }

}
