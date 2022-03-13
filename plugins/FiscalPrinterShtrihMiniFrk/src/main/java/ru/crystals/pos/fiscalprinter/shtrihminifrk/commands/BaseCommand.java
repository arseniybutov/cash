package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihControls;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Базовая команда "общения" с внешним устройством. По факту просто задает шаблон техпроцесса инфо-обмена.
 *
 * @param <T>
 *            тип результата выполнения команды - ответа от внешнего устройства.
 * @author aperevozchikov
 */
public abstract class BaseCommand<T> {

    protected static final Logger log = LoggerFactory.getLogger(BaseCommand.class);

    /**
     * Кодировка, в которой ВСЕ строки отправляются в ФР семейства "Штрих"
     */
    public static final Charset ENCODING = Charset.forName("windows-1251");
    
    /**
     * Пробел в кодировке {@link #ENCODING} - для заполнения "пустот"
     */
    public static final byte SPACE = 0x20;
    
    /**
     * пароль оператора, от имени которого эта команда будет исполнена
     */
    protected byte[] password = new byte[4];
    
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public BaseCommand(int password) {
        // пароль оператора: от младшего байта к старшему:
        this.password[0] = (byte) password;
        this.password[1] = (byte) (password >>> 8 & 0xFF);
        this.password[2] = (byte) (password >>> 16 & 0xFF);
        this.password[3] = (byte) (password >>> 24 & 0xFF);
    }
    
    /**
     * Вернет строковое представление указанного массива байт в кодировке {@link #ENCODING}.
     * 
     * @param data
     *            массив байт, что надо преобразовать в строку
     * @return не <code>null</code>
     */
    public static String getString(byte[] data) {
        return new String(data, ENCODING);
    }

    /**
     * По факту обратный метод методу {@link #getString(byte[])}: указанную строку вернет в виде массива байт в кодировке {@link #ENCODING}.
     * 
     * @param string
     *            строка, чье "байтовое" представление надо вернуть
     * @return не <code>null</code>
     */
    public static byte[] getBytes(String string) {
        return string.getBytes(ENCODING);
    }

    /**
     * Вернет указанную строку текста в виде массива байт.
     * <p/>
     * NOTE: аргументы не валидируются.
     * 
     * @param text
     *            строка текста, что надо преобразовать в массив байт перед записью/печатью в ФР
     * @param length
     *            задает размер возвращаемого массива; если строка в результате преобразования в массив этого размера не поместится, то последние
     *            символы будут просто отброшены
     * @return представление строки-аргумента в виде массива байт в кодировке {@link #ENCODING}.
     */
    public static byte[] getStringAsByteArray(String text, int length) {
        byte[] result = new byte[length];

        // 1. заполним всю строку пробелами
        Arrays.fill(result, SPACE);

        // 2. а теперь текстом:
        if (StringUtils.isEmpty(text)) {
            // аргумент пуст - вернем строку из пробелов
            return result;
        }
        byte[] textAsArray = text.getBytes(ENCODING);

        System.arraycopy(textAsArray, 0, result, 0, Math.min(textAsArray.length, length));

        return result;
    }
    
    /**
     * Вернет код команды.
     * 
     * @return 00..0xFF
     */
    public abstract byte getCommandCode();

    /**
     * Префикс команды для двухбайтовых команд
     * @return 00..0xFF (по умолчанию 0x00 - команда однобайтовая)
     */
    public byte getCommandPrefix(){ return (byte) 0x00;};

    /**
     * Вернет представление аргументов данной команды в виде массива байт.
     * 
     * @return аргументы данной команды; вернет <code>null</code>, если аргументов у данной команды нет
     */
    public abstract byte[] getArguments();

    /**
     * Вернет объектное представление указанного ответа от внешнего устройства на эту команду.
     * 
     * @param response
     *            валидный ответ от внешнего устройства
     * @return <code>null</code>, если аргумент не валиден: <code>null</code>, указанный ответ - это ответ не на эту команду, у указанного ответа
     *         невалидная контрольная сумма, указанный ответ сигнализирует об ошибке (вместо ожидаемого ответа Внешнее Устройство прислало код ошибки)
     *         <p/>
     *         NOTE: но <code>null</code> не означает, что ответ невалиден: возможно, сама команда не подразумевает получение ответа (за исключением
     *         подтверждения приема команды) от внешнего устройства
     */
    public abstract T decodeResponse(byte[] response);
    
    /**
     * Максимально допустимое время выполнения команды (ожидания отклика), в мс.
     * 
     * @return 20 сек. по дефолту
     */
    public long getMaxResponseTime() {
        return 20_000L;
    }

    /**
     * Вернет <code>true</code>, если указанный ответ от внешнего устройства валиден: имеет правильную структуру, совпадает контрольная сумма, не
     * является ответом об ошибке, и является ответом на данную команду.
     * 
     * @param response
     *            ответ от внешнего устройства, что надо отвалидировать
     * @return <code>false</code>, если аргумент == <code>null</code>
     */
    public boolean validateResponse(byte[] response) {
        // 1. длина ответа должна быть минимум 5 байта: STX + байт длины ответа + байт кода корманды + байт кода ошибки + LRC (контрольная сумма)
        if (response == null || response.length < 5) {
            return false;
        }
        
        // 2. байт длины должен быть корректным == длина ответа - 3 байта (STX, сам байт длины, и LRC)
        if (ShtrihUtils.getInt(response[1], (byte) 0) != response.length - 3) {
            return false;
        }
        
        // 3. это не должен быть ответ об ошибке (код ошибки == 0):
        if ( (getCommandPrefix() == 0x00 ? response[3] : response[4]) != 0) {
            return false;
        }
        
        // 4. это должен быть ответ на данную команду
        if ((getCommandPrefix() == 0x00 ? response[2] : response[3]) != getCommandCode()) {
            return false;
        }
        if ( (getCommandPrefix() != 0x00 ? response[2] : 0x00) != getCommandPrefix()){
            return false;
        }
        
        // 5. контрольная сумма должна быть правильной
        byte lrc = ShtrihUtils.calcLrc(Arrays.copyOfRange(response, 1, response.length - 1));
        if (lrc != response[response.length - 1]) {
            return false;
        }
        
        return true;
    }

    /**
     * Вернет представление этой команды в виде массива байт - в виде, уже готовом к отправке к внешнему устройству: вместе со
     * {@link ShtrihControls#STX стартовым байтом}, байтом длины и контрольной суммой.
     * 
     * @return не <code>null</code>
     */
    public byte[] getCommandAsByteArray() {
        byte[] result;
        int lenServiceBytes = 4;
        int startArgsPos = 3;
        if(getCommandPrefix() != 0x00){
            lenServiceBytes = 5;
        }
        // полная длина команды == длина аргументов + 4 служебных байта: STX, байт длины, байт команды, LRC
        byte[] args = getArguments();
        result = new byte[lenServiceBytes + (args == null ? 0 : args.length)];
        
        // STX
        result[0] = ShtrihControls.STX;
        
        // байт длины
        result[1] = (byte) (result.length - 3); // STX, байт длины, и LRC не считаются
        
        // код команды
        if (getCommandPrefix() == 0x00) {
            result[2] = getCommandCode();
        } else {
            result[2] = getCommandPrefix();
            result[3] = getCommandCode();
            startArgsPos = 4;
        }
        
        // аргументы команды
        if (args != null && args.length != 0) {
            System.arraycopy(args, 0, result, startArgsPos, args.length);
        }
        
        // LRC
        byte lrc = ShtrihUtils.calcLrc(Arrays.copyOfRange(result, 1, result.length - 1));
        result[result.length - 1] = lrc;

        if (log.isDebugEnabled()) {
            String cmdCode = (getCommandPrefix() == 0x00 ? "" : String.format("%02X", getCommandPrefix())) + String.format("%02X", getCommandCode());
            log.debug("getCommandAsByteArray(). Command Code: [{}h];  Command Name: {};", cmdCode, this.getClass().getSimpleName());
        }
        
        return result;
    }
}
