package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Установка параметров обмена".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 */
public class SetExchangeParamCommand extends BaseCommand<Object> {

    private int baudRate;

    /**
     * Единственно правильный конструктор.
     * <p>
     * <table>
     * <tr>
     * <th>Значение параметра BaudRate</th>
     * <th>Скорость обмена, бод</th>
     * </tr>
     * <tr><td>0</td><td>2400</td></tr>
     * <tr><td>1</td><td>4800</td></tr>
     * <tr><td>2</td><td>9600</td></tr>
     * <tr><td>3</td><td>19200</td></tr>
     * <tr><td>4</td><td>38400</td></tr>
     * <tr><td>5</td><td>57600</td></tr>
     * <tr><td>6</td><td>115200</td></tr>
     * </table>
     *
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     * @param baudRate код скорости обмена
     */
    public SetExchangeParamCommand(int password, int baudRate) {
        super(password);
        this.baudRate = baudRate;
    }

    @Override
    public String toString() {
        return String.format("set-exchange--param-cmd");
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x14;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 8 байт
        byte[] result = new byte[7];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        // Номер порта (1 байт) 0…255
        result[4] = (byte) 0;
        // Код скорости обмена (1 байт)
        result[5] = (byte) baudRate;
        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }

    @Override
    public long getMaxResponseTime() {
        // ожидание отклика - до 60 сек.
        return 60_000L;
    }

    public enum BaudRate {
        DEFAULT(4800),
        RECOMMENDED(115200);

        private int baudRate;


        BaudRate(int baudRate) {
            this.baudRate = baudRate;
        }

        public int getBaudRate() {
            return baudRate;
        }
    }
}