package ru.crystals.pos.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Реализация <em>уровня соединения</em> при общении с внешними устройствами через последовательный порт.
 * <p/>
 * Просто передаются и считываются данные - без всяких управляющих символов типа ENQ, ACK, STX и прочих.
 *
 * @author someone else
 * @author aperevozchikov
 */
@SuppressWarnings("unchecked")
public class SerialPortAdapter extends AbstractPortAdapter<SerialPortAdapter> {

    private static final String WRITE = " <-- ";
    private static final String READ = " --> ";

    /**
     * Название последовательно порта .через который ведется информационный обмен. Например, "/dev/ttyS0".
     */
    protected String portID;

    /**
     * Скорость обмена данными через этот последовательный порт, бод
     */
    protected int baudRate = 9600;

    /**
     * количество бит данных в одном байте
     */
    protected int dataBits = DATABITS_8;

    /**
     * количество стоповых бит при передаче одного байта
     */
    protected int stopBits = SerialPort.STOPBITS_1;

    /**
     * бит паритета - добавлять (чтоб "1" в "байте" всегда было четное. либо нечетное количество) или нет (без паритета - по уму)
     */
    protected int parity = SerialPort.PARITY_NONE;

    /**
     * Длительность передачи одного байта данных, в мс. Округление вверх.
     */
    private int byteTransmitTime;

    /**
     * время ожидания открытия порта, мс
     */
    protected int openTimeOut = 2000;

    private boolean isConnected = false;

    /**
     * Имя нашего приложения - чтоб можно было идентифицировать (в другом приложении), кто использует последовательный порт {@link #portID}
     */
    protected String owner = "";

    /**
     * Периодичность проверки появления данных в последовательном порту (при чтении "пакета"), в мс
     */
    private int sleepTime = 5;

    /**
     * Сам последовательный порт. вокруг которого данный объект является "оберткой"
     */
    private SerialPort serialPort;

    /**
     * для записи в этот {@link #serialPort}
     */
    private OutputStream output;

    /**
     * для чтения из {@link #serialPort}
     */
    private InputStream input;


    public SerialPortAdapter setPort(String portID) {
        this.portID = portID;
        return this;
    }

    public SerialPortAdapter setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    public SerialPortAdapter setDataBits(int dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    public SerialPortAdapter setStopBits(int stopBits) {
        this.stopBits = stopBits;
        return this;
    }

    public SerialPortAdapter setParity(int parity) {
        this.parity = parity;
        return this;
    }

    public SerialPortAdapter setParity(String parity) {
        if (parity.equalsIgnoreCase("NONE")) {
            this.parity = SerialPort.PARITY_NONE;
        } else if (parity.equalsIgnoreCase("EVEN")) {
            this.parity = SerialPort.PARITY_EVEN;
        } else if (parity.equalsIgnoreCase("MARK")) {
            this.parity = SerialPort.PARITY_MARK;
        } else if (parity.equalsIgnoreCase("SPACE")) {
            this.parity = SerialPort.PARITY_SPACE;
        } else {
            this.parity = SerialPort.PARITY_ODD;
        }
        return this;
    }

    public SerialPortAdapter setOpenTimeOut(int openTimeOut) {
        this.openTimeOut = openTimeOut;
        return this;
    }

    public SerialPortAdapter setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    @Override
    public void openPort() throws IOException, PortAdapterException {
        if (portID == null) {
            throw new IOException("Port is null");
        }
        CommPortIdentifier portIdentifier = null;
        //если не выполнить эту строку перед getPortIdentifier - при исчезании порта в ОС java упадёт.
        CommPortIdentifier.getPortIdentifiers();
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portID);
        } catch (NoSuchPortException e) {
            throw new PortAdapterException("No such port " + portID, e);
        }
        if (portIdentifier.isCurrentlyOwned()) {
            throw new PortAdapterException("Port already in use " + portID);
        }
        try {
            CommPort commPort = portIdentifier.open(owner, openTimeOut);
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
            } else {
                throw new IOException("Non-serial port " + portID + " (" + (commPort != null ? commPort.getClass().getName() : "null") + ")");
            }
        } catch (PortInUseException e) {
            throw new PortAdapterException("Port already in use " + portID, e);
        }
        try {
            serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
        } catch (UnsupportedCommOperationException e) {
            throw new PortAdapterException("Unsupported operation ", e);
        }
        output = serialPort.getOutputStream();
        input = serialPort.getInputStream();
        isConnected = true;

        byteTransmitTime = getByteTransmitTime();

        LOG.info("connection [{}] was opened successfully", this);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + WRITE + PortAdapterUtils.arrayToString(b));
        }
        if (output != null) {
            output.write(b);
            output.flush();
        } else {
            throw new IOException("Init device first");
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        byte[] result = new byte[input.available()];
        input.read(result, 0, result.length);
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + READ + PortAdapterUtils.arrayToString(result));
        }
        return result;
    }

    /**
     * Проверяет наличие данных для чтения.
     *
     * @return <code>true</code>, если есть данные для чтения
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     */
    public boolean isDataAvailable() throws IOException {
        return input.available() > 0;
    }

    /**
     * Ожидает появления данных в этом последовательном порту и считывает их (данные).
     *
     * @param waitTimeout
     *            максимальное время ожидания появления данных, в мс
     * @param byteWaitTime
     *            допустимое время ожидания получением очередного байта (уже после того, как примем начался), в мс;
     *            <p/>
     *            <code>null</code> распознается как время передачи одного байта;
     *            <p/>
     *            невалидное значение (не положительное) означает, что ожидания "появления" очередного байта не будет.
     * @return считанные данные; не <code>null</code>: если данных нет, то будет {@link PortAdapterNoConnectionException}
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     * @throws PortAdapterNoConnectionException
     *             если не дождались отклика от внешнего устройства за <code>waitTimeout</code> мс (т.е., на основании этого решили. что связи с
     *             внешним устройством [больше] нет)
     */
    public byte[] readBytes(long waitTimeout, Long byteWaitTime) throws IOException, PortAdapterNoConnectionException {
        byte[] result = null;

        // 1. ожидаем появления данных
        if (!waitForAnyData(waitTimeout)) {
            // так и не дождались отклика от устройства
            throw new PortAdapterNoConnectionException("no connection");
        }

        // 2. считываем
        result = readBytes();
        if (result == null) {
            // неожиданно! похоже, кто-то (другой поток?) еще читает из "нашего" порта
            LOG.error("seems, some other thread \"stole\" my data!");
            throw new PortAdapterNoConnectionException("no connection");
        }

        // 3. возможно, в порту еще что-то появится в этом же "кадре"?
        long byteWaitTimeout = byteWaitTime == null ? byteTransmitTime : byteWaitTime;
        //  P.S.: не знаю какой именно inputStream возвращает SerialPort - возможно, в куске кода ниже и нет надобности
        while(waitForAnyData(byteWaitTimeout)) {
            // да. м/у передачей последнего байте в предыдущей пачке и появлением следующего байта прошло ..не так много времени
            int lenghtBefore = result.length;
            byte[] newChunk = readBytes();
            LOG.trace("{} \"extra\" bytes were read in addition to existing {} ones", newChunk.length, lenghtBefore);

            result = Arrays.copyOf(result, lenghtBefore + newChunk.length);
            for (int i = 0; i < newChunk.length; i++) {
                result[i + lenghtBefore] = newChunk[i];
            }
        }

        return result;
    }

    /**
     * Вернет длительность передачи одного байта данных, в мс (округление вверх).
     *
     * @return <code>0</code>, если {@link #baudRate скорость передачи} невалидна
     */
    private int getByteTransmitTime() {
        int result = 0;

        if (baudRate <= 0) {
            return 0;
        }

        // биты данных:
        int bitsCount = dataBits;

        // + количество стоповых бит
        if (stopBits == SerialPort.STOPBITS_1) {
            bitsCount++;
        } else {
            bitsCount += 2;
        }

        // + бит паритета
        if (parity != SerialPort.PARITY_NONE) {
            bitsCount++;
        }

        result = 1000 * bitsCount / baudRate;
        if (1000 * bitsCount % baudRate != 0) {
            result++;
        }

        return result;
    }

    /**
     * Ожидает полявления данных в порту.
     * <p/>
     * Implementation Note: наличие данных в порту проверяется с периодичностью {@link #sleepTime} мс.
     *
     * @param waitTimeout
     *            время ожидания появления данных, в мс.
     * @return <code>false</code>, если данные в порту так и не появились за <code>waitTimeout</code> мс
     * @throws IOException
     */
    public boolean waitForAnyData(long waitTimeout) throws IOException {
        boolean result = false;
        long timeLimit = System.currentTimeMillis() + waitTimeout;

        if (input.available() > 0) {
            // данные в порту уже есть - не надо ничего ждать
            return true;
        }
        if (waitTimeout <= 0) {
            return false;
        }
        do {
            long remaining = timeLimit - System.currentTimeMillis();
            if (remaining <= 0) {
                break;
            }
            try {
                Thread.sleep(Math.min(remaining, sleepTime));
            } catch (InterruptedException ie) {
                // не понятно как на это реагировать. Продолжим ждать!
                LOG.error("thread interrupted!", ie);
            }
            if (input.available() > 0) {
                // данные в порту появились
                result = true;
                break;
            }
        } while (System.currentTimeMillis() < timeLimit);

        return result;
    }

    @Override
    public void write(int b) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + WRITE + PortAdapterUtils.toUnsignedByte(b));
        }
        output.write(b);
        output.flush();
    }

    @Override
    public int read() throws IOException {
        int result = -1;
        if (input.available() > 0) {
            result = input.read();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + READ + PortAdapterUtils.toUnsignedByte(result));
        }
        return result;
    }

    /**
     * Считывает байт из порта. Если в течении timeout миллисекунд байт в порт не пришел, бросается исключение.
     * Это довольно медленный метод, если нет необходимости использовать таймаут, лучше применять {@link #read()}.
     * @param timeout таймаут, в течении которого байт должен быть считан из порта.
     * @return считанный байт
     * @throws IOException если таймаут или произошла иная ошибка ввода-вывода.
     */
    public int readTimeout(int timeout) throws IOException {
        if(!waitForAnyData(timeout))
            throw new IOException("No data has arrived in " + timeout + "ms.");
        return read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int result = input.read(b);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Total received from "+ portID+ " "+ result + "bytes");
            LOG.debug(portID + READ + PortAdapterUtils.arrayToString(b));
        }
        return result;
    }

    @Override
    public int[] read(int lastByte) throws Exception {
        List<Integer> readByteList = new ArrayList<Integer>();

        long startTime = System.currentTimeMillis();

        boolean proceed = true;
        while (proceed) {
            if (System.currentTimeMillis() - startTime < openTimeOut) {
                Thread.sleep(sleepTime);
                while (input.available() > 0) {
                    int _byte = input.read();
                    readByteList.add(_byte);
                    if (_byte == lastByte) {
                        proceed = false;
                        break;
                    }
                }
            } else {
                throw new Exception("Device is not available");
            }
        }
        clearInputStream();

        return getIntArray(readByteList);
    }

    @Override
    public int getInputStreamBufferSize() throws IOException {
        return input.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = input.read(b, off, len);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Total received from "+ portID+ " "+ result + "bytes");
            LOG.debug(portID + READ + PortAdapterUtils.arrayToString(b));
        }
        return result;
    }

    @Override
    public void close() {
        if (isConnected()) {
            try {
                input.close();
            } catch (IOException e) {
            }
            try {
                output.close();
            } catch (IOException e) {
            }
            try {
                serialPort.removeEventListener();
                serialPort.close();
            } catch (Exception e) {
            }
            isConnected = false;
        }
    }

    public boolean isCD() {
        return isConnected && serialPort.isCD();
    }

    public void setRTS(boolean b) {
        serialPort.setRTS(b);
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void clearInputStream() throws IOException {
        while (input.available() > 0) {
            input.read();
        }
    }

    private int[] getIntArray(List<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = integerList.get(i);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + READ + PortAdapterUtils.arrayToString(intArray));
        }
        return intArray;
    }

    public String getOwner() {
        return owner;
    }

    public SerialPortAdapter setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public String readAll(String charset) throws IOException {
        // FIXME опеределиться реализацией
        return IOUtils.readLines(input).get(0);
    }

    @Override
    public int[] readAll() throws Exception {
        List<Integer> readByteList = new ArrayList<Integer>();
        while (input.available() > 0) {
            int _byte = input.read();
            readByteList.add(_byte);
        }

        clearInputStream();

        return getIntArray(readByteList);
    }

    @Override
    public String toString() {
        return String.format("serial-port-adapter [name: %s; rate: %s; data-bits: %s; stop-bits: %s; parity: %s; byte-transfer-time: %s]",
            portID, baudRate, dataBits, stopBits, parity, byteTransmitTime);
    }

    public void write(byte[] buffer, int packetLength, byte[] prefix) throws IOException {
        if (serialPort != null) {
            if (packetLength > 0) {
                byte[] realPrefix = new byte[prefix.length + 1];
                realPrefix[0] = 0x00;
                System.arraycopy(prefix, 0, realPrefix, 1, prefix.length);
                for (int i = 0; i < buffer.length; i += packetLength) {
                    byte[] mes = Arrays.copyOfRange(buffer, i, i + packetLength > buffer.length ? buffer.length : i + packetLength);
                    byte[] message = new byte[realPrefix.length + mes.length];
                    System.arraycopy(realPrefix, 0, message, 0, realPrefix.length);
                    System.arraycopy(mes, 0, message, realPrefix.length, mes.length);
                    write(message);
                }
            } else {
                throw new IOException("Message part must be contains at least 1 character! Please, set correct packetLength argument");
            }
        } else {
            throw new IOException("Init device first");
        }
    }

    protected void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public byte[] readBytes(int expectedLength) throws IOException {
        byte[] expectedMessage = new byte[expectedLength];
        int actualLength = input.read(expectedMessage);
        if (LOG.isDebugEnabled()) {
            LOG.debug(portID + READ + PortAdapterUtils.arrayToString(expectedMessage));
        }
        if (actualLength < expectedLength) {
            byte[] resultMessage = new byte[actualLength];
            System.arraycopy(expectedMessage, 0, resultMessage, 0, actualLength);
            return resultMessage;
        }
        return expectedMessage;
    }

    public SerialPortAdapter setStopBits(String stopBits) {
        if (stopBits.equalsIgnoreCase("2")) {
            this.stopBits = SerialPort.STOPBITS_2;
        } else if (stopBits.equalsIgnoreCase("1.5") || stopBits.equalsIgnoreCase("1,5")) {
            this.stopBits = SerialPort.STOPBITS_1_5;
        } else {
            this.stopBits = SerialPort.STOPBITS_1;
        }
        return this;
    }

    public SerialPortAdapter setBaudRate(String baudRate) {
        this.baudRate = Integer.valueOf(baudRate);
        return this;
    }

}
