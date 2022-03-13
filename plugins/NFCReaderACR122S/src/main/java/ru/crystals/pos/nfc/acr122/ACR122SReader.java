package ru.crystals.pos.nfc.acr122;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.PortAdapterUtils;
import ru.crystals.pos.utils.SerialPortAdapter;

/**
 * Класс для взаимодействия с считывателем бесконтактных карт ACR122S (модель без экранчика).
 * Считыватель подключается по RS-232 для коммуникации, и USB, с которого берёт питание.
 * Внутри себя этот класс неявно использует библиотеку RXTX (http://rxtx.qbang.org), посему её нативная либа
 * должна присутствовать в class path.
 * Документация: http://www.acs.com.hk/en/products/121/acr122s-serial-nfc-reader/
 * Также не лишним будет посмотреть: http://www.usb.org/developers/docs/devclass_docs/DWG_Smart-Card_CCID_Rev110.pdf
 */
public class ACR122SReader {
    /**
     * Таймаут, по превышении которого при открытии порта произойдёт ошибка.
     */
    private static final int PORT_OPEN_TIMEOUT_MS = 3000;
    /**
     * Таймаут чтения порта.
     */
    private static final int PORT_READ_TIMEOUT = 8000;
    /**
     * Сокращенный таймаут чтения порта.
     */
    private static final int PORT_READ_SHORT_TIMEOUT = 100;
    /**
     * Скорость общения с железкой по умолчанию.
     */
    public static final int BAUD_RATE_DEFAULT = 9600;
    /**
     * Маркер начала передачи пакета данных. Всякая передача данных начинается с этого маркера.
     */
    private static final byte START_TRANSMIT_MARKER = 0x02;
    /**
     * Маркер окончания передачи данных. Всякая передача данных оканчивается этим маркером.
     */
    private static final byte END_TRANSMIT_MARKER = 0x03;

    public static final int STATUS_OK = 0;
    public static final int STATUS_GENERIC_ERROR = 0x42;

    /**
     * Пакет для активации SAM. Всякое использование бесконтактного интерфейса или переферии должно происходить при активном SAM.
     * Достаточно включить его лишь один раз.
     */
    private static final byte[] SAM_ACTIVATION_PACKET = {
            0x62, /* Идентификатор команды инициалищации SAM. */
            0x00, 0x00, 0x00, 0x00, /* Размер полезной нагрузки пакета. */
            0x00, /* Номер слота. */
            0x00, /* Номер последовательности. */
            0x00, /* Автоматически выбрать напряжение. */
            0x00, 0x00 /* Зарезервировано. */
    };

    /**
     * Пакет для деактивации SAM.
     */
    private static final byte[] SAM_DEACTIVATION_PACKET = {
            0x63,
            0x00, 0x00, 0x00, 0x00,
            0x00,
            0x00,
            0x00, 0x00, 0x00
    };

    private Logger logger;
    private boolean samActivated = false;
    private SerialPortAdapter serialAdapter;

    public ACR122SReader() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Подключается к железке через UART.
     * @param port адрес порта. В случае линуксов, это путь к порту - /dev/ttyUSB0, например.
     * У программы должны быть права доступа к портам, в противном случае метод кинет {@link PortAdapterException}.
     */
    public void open(String port, int baudRate) throws PortAdapterException, IOException {
        serialAdapter = new SerialPortAdapter();
        serialAdapter.setOwner(this.getClass().getName());
        serialAdapter.setPort(port);
        serialAdapter.setBaudRate(baudRate);
        serialAdapter.setStopBits(1);
        serialAdapter.setDataBits(8);
        serialAdapter.setParity("NONE");
        serialAdapter.setOpenTimeOut(PORT_OPEN_TIMEOUT_MS);
        serialAdapter.openPort();
        // Дадим железке время просраться, просто на всякий случай.
        try {
            Thread.sleep(600);
        } catch(Exception ex) {}
        samActivated = samActivate(serialAdapter);
        if(!samActivated) {
            logger.error("Failed to activate SAM");
            throw new IOException("Failed to activate SAM");
        }
    }

    /**
     * Точка входа в приложение-проверялку работы железки.
     * @param args никаких дополнительных аргументов передавать не следует
     * @throws Exception если что-то пошло не так.
     */
    public static void main(String[] args) throws Exception {
        ACR122SReader reader = new ACR122SReader();
        reader.open("/dev/ttyUSB0", BAUD_RATE_DEFAULT);
        int sc = 0;
        while(true) {
            byte[] res = reader.poll();
            if(res != null) {
                reader.beep();
                StringBuilder sb = new StringBuilder();
                for(int r : res) {
                    sb.append(r);
                }
                System.out.println(sb.toString());
                ++sc;
            }
            if(sc == 3) {
                break;
            }
            Thread.sleep(1900);
        }
        reader.close();
    }

    /**
     * Пикает встроенной в считыватель пикалкой. Издаёт один пик продолжительностью 100мс.
     */
    public void beep() {
        periferialControl(0, 0x01010101);
    }

    /**
     * Создаёт и передаёт железке команду на управление переферийными устройствами.
     * В ACR122S из переферии есть пищалка и двуцветный светодиод. Несмотря на то, что на корпусе находятся два светодиода,
     * управлять можно только крайним справа, который является сдвоенным (красный+зелёный).
     * @param ledState определяет состояние светодиодов. См. документацию на ACR122S для подробностей.
     * @param blinkState управляет миганием и пищанием пищалкой. См. документацию ACR122S для подробностей.
     * @return состояние светодиодов после изменения состояния.
     * @see <a href="http://www.acs.com.hk/download-manual/1875/CMP-ACR122S-2.02.pdf">Документация</a>
     */
    private int periferialControl(int ledState, int blinkState) {
        try {
            byte[] pereferialControlPacket = new byte[] {
                    (byte)0xFF, 0x00, 0x40,
                    (byte)ledState, 0x04,
                    (byte) (blinkState >> 24),
                    (byte) (blinkState >> 16),
                    (byte) (blinkState >> 8),
                    (byte) (blinkState)
            };
            if(STATUS_OK == transmitBlock(serialAdapter, pereferialControlPacket)) {
                byte[] res = receiveBlock(serialAdapter);
                if(res == null || res.length != 2) {
                    return STATUS_GENERIC_ERROR;
                } else {
                    return res[0] == 0x90 ? STATUS_OK : res[1];
                }
            }
            return 0;
        } catch(IOException ioex) {
            logger.error("", ioex);
            return STATUS_GENERIC_ERROR;
        }
    }

    /**
     * Инициирует поиск и считывание бесконтактной карты. Это неблокирующий метод, если в поле зрения считывателя не найдена карта,
     * он вернёт пакет с 0 карт. Этот метод настроен считывать карты формата ISO 14443-4 тип A, MIFARE 1K/4K.
     * @return массив байт, представляющих собой UID карты или null, если считать номер карты не удалось.
     */
    public byte[] poll() {
        try {
            /*
             * Сначала установим нулевое время ожидания, т.е сделаем железку немедленно возвращать результат, а не только когда
             * придут данные. Это нужно потому, что если сделать железку отдавать данные только по приходу карты, она не отвиснет, пока
             * не получит карту, а значит при перезагрузке приложения кассы последнее зависнет (передадим железке SAM-activation, а обратно данные
             * не получим потому что она карту ждёт).
             * Хинт: однако, в протоколе есть понятие слота команды. И может статься, что пуллить можно в одном слоте, а общаться с железкой в другом.
             * Это могло бы устранить проблемы с зависанием, однако исследовано не было.
             */
            int ret = transmitBlock(serialAdapter, new byte[] { (byte)0xFF, 0, 0, 0, 6, (byte)0xD4, 0x32, 5, 0, 0, 1});
            if(ret != STATUS_OK) {
                logger.error("Failed to set poll timeout (return code {})", ret);
                return null;
            }
            byte[] res = receiveBlock(serialAdapter);
            if(res == null) {
                logger.error("Failed to get timeout result");
                return null;
            }
            if(res.length != 4) {
                logger.error("Unexpected timeout result: (expected: 0xD5 0x33 0x90 0x00, found {})", PortAdapterUtils.arrayToString(res));
                return null;
            }
            if( res[0] != (byte)0xD5 || res[1] != (byte)0x33 || res[2] != (byte)0x90 || res[3] != 0) {
                logger.error("Unexpected timeout result: (expected: 0xD5 0x33 0x90 0x00, found {})", PortAdapterUtils.arrayToString(res));
                return null;
            }
            // Тут мы опрашиваем эфир на предмет карт ISO-14443-4 A. Их код это 0xD44A0100.
            // Можно опрашивать и иные виды карт, как это делать смотрите в документации.
            ret = transmitBlock(serialAdapter, new byte[]{ (byte)0xFF, 0, 0, 0, 4, (byte)0xD4, 0x4A, 0x01, 0x00});
            if(ret == STATUS_OK) {
                res = receiveBlock(serialAdapter);
                if(res == null) {
                    logger.error("Polling received null block");
                    return null;
                }
                logger.trace("Polled {}", PortAdapterUtils.arrayToString(res));
                if(res.length < 4) {
                    logger.error("Unexpected poll length (expected at least 4, found {}", res.length);
                    return null;
                }
                if(res[0] != (byte)0xD5 || res[1] != (byte)0x4B) {
                    logger.error("Incorrect magic. Expected: 0xD54B, found 0x{}{}", res[0], res[1]);
                    return null;
                }
                if(res[2] != 1) {
                    // Вполне может быть найдено 0 или 2 метки, однако нас интересует только одна.
                    logger.trace("Incorrect tag number. Expected 1, found {}", res[2]);
                    return null;
                }
                if(res[res.length - 1] != 0 || res[res.length - 2] != (byte)0x90) {
                    logger.error("Error detected (expected 0x9000, found 0x{}{})", res[res.length - 2], res[res.length - 1]);
                    return null;
                }
                int uidLength = res[7];
                // Два последних байта - код возврата, UID начинается с седьмого байта
                // (это справедливо тольо для ответа от ISO-14443-4 A, у иных карт ответ может сильно различаться).
                if(uidLength > res.length - 2 - 7 || uidLength == 0) {
                    logger.error("Incorrect UID length {}", uidLength);
                    return null;
                }
                byte[] uid = new byte[uidLength];
                for(int i = 0; i < uidLength; ++i) {
                    uid[i] = res[8 + i];
                }
                return uid;
            } else {
                logger.error("Polling returned {}", ret);
                return null;
            }
        } catch(Exception ex) {
            logger.error("", ex);
        }
        return null;
    }

    /**
     * Закрывает порт и прекращает взаимодействие с железкой.
     * Хинт: закрытие порта не означает сброс железки.
     */
    public void close() {
        if(serialAdapter == null) {
            return;
        }
        if(samActivated) {
            if(!samDeactivate(serialAdapter)) {
                logger.error("Failed to deactivate SAM");
            }
        }
        samActivated = false;
        serialAdapter.close();
        serialAdapter = null;
    }

    private boolean samActivate(SerialPortAdapter port) {
        int r;
        try {
            r = transmit(port, SAM_ACTIVATION_PACKET);
        } catch(IOException ex) {
            logger.error("Failed to activate SAM", ex);
            return false;
        }
        if(r != STATUS_OK) {
            return false;
        }
        try {
            // Ответ нужно вычитать всё равно, однако его содержимому у нас нет применения.
            byte[] rec = receive(port);
        } catch(IOException ioex) {
            logger.error("Failed to activate SAM", ioex);
            return false;
        }
        return true;
    }

    private boolean samDeactivate(SerialPortAdapter port) {
        int r;
        try {
            r = transmit(port, SAM_DEACTIVATION_PACKET);
        } catch(IOException ex) {
            logger.error("Failed to activate SAM", ex);
            return false;
        }
        if(r != STATUS_OK) {
            return false;
        }
        try {
            byte[] rec = receive(port);
        } catch(IOException ioex) {
            logger.error("Failed to activate SAM", ioex);
            return false;
        }
        return true;
    }

    private int transmitBlock(SerialPortAdapter port, byte[] payload) throws IOException {
        transmitBlockVoid(port, payload);
        byte[] rec = receive(port);
        if(rec == null) {
            return STATUS_GENERIC_ERROR;
        }
        if(rec.length != 1) {
            return STATUS_GENERIC_ERROR;
        }
        return rec[0];
    }

    private void transmitBlockVoid(SerialPortAdapter port, byte[] payload) throws IOException {
        byte[] frame = new byte[3 + 10 + payload.length]; // старт-стоп маркеры, контрольная сумма, заголовок.
        frame[0] = START_TRANSMIT_MARKER;
        frame[1] = 0x6F; // Magic

        frame[2] = (byte) (payload.length); // LSB длины полезной нагрузки
        frame[3] = (byte) (payload.length >> 8);
        frame[4] = (byte) (payload.length >> 16);
        frame[5] = (byte) (payload.length >> 24); // MSB длины полезной нагрузки

        frame[6] = 0; // Номер слота.
        frame[7] = 0; // Номер последовательности.
        frame[9] = 0; // Зарезервировано.
        frame[10] = 0; // Зарезервировано.
        System.arraycopy(payload, 0, frame, 11, payload.length);
        frame[frame.length - 2] = (byte)checksum(frame, 1, frame.length - 1);
        frame[frame.length - 1] = END_TRANSMIT_MARKER;
        logger.trace("TX: {}", PortAdapterUtils.arrayToString(frame));
        port.write(frame);
    }

    private int transmit(SerialPortAdapter port, byte[] payload) throws IOException {
        byte[] data = new byte[3 + payload.length]; // Старт-стоп маркеры + 1 байт на контрольную сумму.
        data[0] = START_TRANSMIT_MARKER;
        System.arraycopy(payload, 0, data, 1, payload.length);
        data[data.length - 2] = (byte)checksum(payload, 0, payload.length);
        data[data.length - 1] = END_TRANSMIT_MARKER;
        logger.trace("TX: {}", PortAdapterUtils.arrayToString(data));
        port.write(data);
        byte[] rec = receive(port);
        if(rec == null) {
            return STATUS_GENERIC_ERROR;
        }
        if(rec.length != 1) {
            return STATUS_GENERIC_ERROR;
        }
        return rec[0];
    }

    private byte[] receiveBlock(SerialPortAdapter port) throws IOException {
        // не прерывать чтение после стопового байта
        byte[] rec = receive(port, false);
        if(rec == null) {
            return rec;
        }
        int dataSize = rec[4] << 24 | (rec[3] & 0xFF) << 16 | (rec[2] & 0xFF) << 8 | (rec[1] & 0xFF);
        byte[] packet = new byte[dataSize];
        System.arraycopy(rec, 10, packet, 0, dataSize);
        return packet;
    }

    private byte[] receive(SerialPortAdapter port) throws  IOException {
        return receive(port, true);
    }

    private byte[] receive(SerialPortAdapter port, boolean breakOnEndTransmitMarker) throws  IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        if(!port.waitForAnyData(PORT_READ_TIMEOUT)) {
            throw new IOException("No data has been arrived in " + PORT_READ_TIMEOUT + " ms.");
        }
        int b = port.read();
        if( b != START_TRANSMIT_MARKER) {
            logger.error("First transmission byte is not 0x02 ({}). Aborting reading.", b);
            return null;
        }
        boolean endMarkerReceived = false;
        while((b = readTimeout(port, (endMarkerReceived ? PORT_READ_SHORT_TIMEOUT : PORT_READ_TIMEOUT))) != -1) {
            endMarkerReceived = b == END_TRANSMIT_MARKER;
            buffer[bytesRead] = (byte)b;
            ++bytesRead;
            if (endMarkerReceived && breakOnEndTransmitMarker) {
                // прервать чтение после стопового байта
                break;
            }
            if(bytesRead == 1024) {
                logger.warn("Can't keep up! Reader has been trying to send more than 1024 bytes of data at once!");
                return null;
            }
        }
        // Note: после маркера конца передачи данных быть не должно
        // и если они есть, что-то пошло сильно не так, что в дальнейшем приведёт к некорректному стейту.
        if(bytesRead < 2 || !endMarkerReceived) {
            return null;
        }

        if(bytesRead == 2) {
            byte[] finalBuf = new byte[1];
            logger.trace("RX: {}", PortAdapterUtils.arrayToHexString(buffer, 0, 1));
            System.arraycopy(buffer, 0, finalBuf, 0, 1);
            return finalBuf;
        }
        if(checksum(buffer, 0, bytesRead - 1) != buffer[bytesRead] ) {
            logger.trace("RX: {}", PortAdapterUtils.arrayToHexString(buffer, 0, bytesRead - 1));
            logger.debug("Incorrect checksum! {} != {}", checksum(buffer, 0, bytesRead - 1), buffer[bytesRead - 1]);
            return null;
        }
        byte[] finalBuf = new byte[bytesRead - 2];
        logger.trace("RX: {}", PortAdapterUtils.arrayToHexString(buffer, 0, bytesRead - 1));
        System.arraycopy(buffer, 0, finalBuf, 0, bytesRead - 2);
        return finalBuf;
    }

    private int readTimeout(SerialPortAdapter port, int timeout) throws IOException {
        if(!port.waitForAnyData(timeout)) {
            return -1;
        }
        return port.read();
    }

    /**
     * Вычисляет контрольную сумму массива данных.
     * @param data массив данных, контрольную сумму которого требуется посчитать.
     * @param start начальный индекс, от которого следует начинать считать контрольную сумму.
     * @param len число элементов, для которых требуется расчитать контрольную сумму.
     * @return контрольная сумма указанного массива данных.
     */
    private int checksum(byte[] data, int start, int len) {
        byte result = 0;
        for(int i = start; i < len; ++i) {
            result ^= data[i];
        }
        return result;
    }
}
