package ru.crystals.pos.fiscalprinter.shtrihminifrk.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.AbstractPortAdapter;
import ru.crystals.pos.utils.PortAdapterIllegalStateException;
import ru.crystals.pos.utils.PortAdapterNoConnectionException;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by d.borisov on 03.08.2017.
 */
public abstract class AbstractTransport implements Transport {

    protected static final Logger log = LoggerFactory.getLogger(AbstractTransport.class);

    /**
     * максимальное время ожидания каждого байта от внешенего устройства, в мс
     */
    protected long byteWaitTime = 50;

    /**
     * минимальное время ожидания отклика на управляющий сигнал ENQ, мс
     */
    protected long minEnqResponseTime = 20000;

    /**
     * Количество попыток отправить команду во внешнее устройство; если за столько попыток не удастся отправить данные - будет принято решение. что
     * связи с устройством нет
     */
    protected int dataSendAttempts = 10;

    /**
     * Количество попыток получить данные от внешнего устройства; если за столько попыток не удастся получить данные - будет принято решение. что
     * связи с устройством нет
     */
    protected int dataReadAttempts = 10;

    /**
     * Реализация <em>уровня соединения</em> при информациооном обмене с внешним устройством
     */
    protected AbstractPortAdapter adapter;

    /**
     * просто спим/ждем/тратим-время до следующей попытки чтения данных из внешнего устройства
     */
    private void waitTillNextReadAttempt() {
        try {
            Thread.sleep(byteWaitTime);
        } catch (InterruptedException e) {
            log.warn("what can I do?", e);
        }
    }

    /**
     * Валидация ответа от внешнего устройства.
     *
     * @param response ответ, что надо отвалидировать
     * @return <code>true</code>, если ответ признан валидным; иначе - вернет <code>false</code>
     */
    private boolean validateResponse(byte[] response) {
        // 1. длина ответа должна быть минимум 5 байта: STX + байт длины ответа + байт кода корманды + байт кода ошибки + LRC (контрольная сумма)
        if (response == null || response.length < 5) {
            if (log.isWarnEnabled()) {
                log.warn("the response ({}) length is not valid", PortAdapterUtils.arrayToString(response));
            }
            return false;
        }

        // 2. байт длины должен быть корректным == длина ответа - 3 байта (STX, сам байт длины, и LRC)
        if (ShtrihUtils.getInt(response[1], (byte) 0) != response.length - 3) {
            if (log.isWarnEnabled()) {
                log.warn("the response length byte ({}) of response ({}) is invalid",
                        PortAdapterUtils.toUnsignedByte(response[1]), PortAdapterUtils.arrayToString(response));
            }
            return false;
        }

        // 3. контрольная сумма должна быть правильной
        byte lrc = ShtrihUtils.calcLrc(Arrays.copyOfRange(response, 1, response.length - 1));
        if (lrc != response[response.length - 1]) {
            if (log.isWarnEnabled()) {
                log.warn("the LRC of response ({}) is invalid (calculated value is: {})",
                        PortAdapterUtils.arrayToString(response), PortAdapterUtils.toUnsignedByte(lrc));
            }
            return false;
        }

        return true;
    }

    /**
     * вернет <code>true</code>, если указанный запрос валиден: соответсвует протоколу.
     *
     * @param request запрос, что надо отвалидировать
     * @return <code>false</code>, если аргумент пуст
     */
    private boolean validateRequest(byte[] request) {
        // 1. запрос не может быть пустым
        if (request == null || request.length == 0) {
            log.error("validateRequest(byte[]). The argument is EMPTY!");
            return false;
        }

        // 2. в запросе должно быть как минимум 4 байта: STX, длина, команда, LRC:
        if (request.length < 4) {
            log.error("validateRequest(byte[]). The request ({}) is incomplete!", PortAdapterUtils.arrayToString(request));
            return false;
        }

        // 3. первым байтом должкен быть STX:
        if (ShtrihControls.STX != request[0]) {
            log.error("validateRequest(byte[]). The request ({}) should start with STX [{}]!", PortAdapterUtils.arrayToString(request), ShtrihControls.STX);
            return false;
        }

        // 4. контрольная сумма должна быть правильной
        byte lrc = ShtrihUtils.calcLrc(Arrays.copyOfRange(request, 1, request.length - 1));
        if (lrc != request[request.length - 1]) {
            log.error("validateRequest(byte[]). The LRC of the request ({}) is INVALID! should be: [{}]!",
                    PortAdapterUtils.arrayToString(request), PortAdapterUtils.toUnsignedByte(lrc));
            return false;
        }

        // 5. запрос валиден
        return true;
    }

    /**
     * Этот метод дожидается готовности внешенего устройства к приему данных.
     *
     * @throws IOException                      если возникли проблемы ввода/вывода
     * @throws PortAdapterNoConnectionException если в результате попытки получения данных сделали вывод, что связи с внешним устройством нет
     */
    private void waitForCts() throws IOException, PortAdapterNoConnectionException {
        // TODO: в документации Штриха не написано сколько попыток "заставить" ФР слушать может понадобиться
        //  так что ниже возможен бесконечный цикл (do-while)
        do {
            //  1.1. Отправить ENQ
            adapter.write(new byte[]{ShtrihControls.ENQ});

            // 1.2. дождаться появления ответных данных
            waitForAnyData(minEnqResponseTime);

            // 1.3. считать эти данные
            byte nak = (byte) adapter.read();
            // данное ожидание влияет на скорость печати ФР (RNDIS)
            while (ShtrihControls.EOF == nak) {
                waitForAnyData(minEnqResponseTime);
                nak = (byte) adapter.read();
            }

            if (ShtrihControls.NAK == nak) {
                // внешнее устройство готово нас слушать
                break;
            } else {
                // эта скотина собирается нам что-то прислать - считаем ответ и проигнорим его!
                byte[] read = adapter.readBytes(byteWaitTime, byteWaitTime);
                log.error("unexpected response from the device: {}", PortAdapterUtils.arrayToString(read));

                // подтвердить получение - пусть успокоится:
                adapter.write(new byte[]{ShtrihControls.ACK});
            }
        } while (true);
    }

    /**
     * Ожидает появления данных в порту максимум указанное количество времени.
     *
     * @param waitTimeout максимальное время ожидания данных, в мс
     * @throws IOException                      если возникли проблемы ввода/вывода
     * @throws PortAdapterNoConnectionException если появления данных в порту за указанное время так и не дождались
     */
    private void waitForAnyData(long waitTimeout) throws IOException, PortAdapterNoConnectionException {
        if (!adapter.waitForAnyData(waitTimeout)) {
            // так и не дождались отклика от устройства
            throw new PortAdapterNoConnectionException("no connection");
        }
    }

    @Override
    public synchronized byte[] execute(byte[] data, long maxResponseTime) throws IOException, PortAdapterNoConnectionException, PortAdapterIllegalStateException {
        byte[] result;
        long stopWatch = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("entering execute(byte[], long). The arguments are: data: {}; maxResponseTime: {}",
                    PortAdapterUtils.arrayToString(data), maxResponseTime);
        }

        // 0. сначала валидируем запрос:
        if (!validateRequest(data)) {
            log.error("leaving execute(byte[], long): argument [{}] is INVALID!", PortAdapterUtils.arrayToString(data));
            throw new IllegalArgumentException("leaving execute(byte[], long): argument is INVALID!");
        }

        // макс. дительность ожидания отклика на эту команду
        int writeAttemptsCount = 0;
        boolean responseIsValid = false;
        do {
            // 1. делаем запрос
            // 1.1. убедиться, что внешнее устройство готово к приему данных:
            log.trace("starting to wait for CTS (Clear to Send)...");
            waitForCts();

            // 1.2. отправка данных
            writeAndWaitForAck(data);

            // 2. ACK получен. ждем ответа от внешнего устройства
            log.trace("waiting for response...");
            result = readData(maxResponseTime, byteWaitTime);

            // 2.1. ответ получен. анализируем
            if (validateResponse(result)) {
                // ответ валиден. выйдем
                responseIsValid = true;
                break;
            }

            // 2.2. Реакция на невалидный ответ: ответить NAK'ом и повторить попытку чтения
            //  2.2.1. Послать NAK
            adapter.write(new byte[]{ShtrihControls.NAK});

            //  2.2.2. Подождать до повторения попытки запроса (до следующего ENQ)
            waitTillNextReadAttempt();
        } while (++writeAttemptsCount <= dataReadAttempts);

        if (!responseIsValid) {
            // нормального ответа так и не получили - значит нет связи
            log.error("leaving execute(byte[], long). NO CONNECTION!");
            throw new PortAdapterNoConnectionException(String.format("no one valid response was received in spite of %s attempts!", writeAttemptsCount));

        }

        // 2.3. получили валидный ответ - надо ответить ACK'ом:
        adapter.write(new byte[]{ShtrihControls.ACK});
        // 2.4. ожидаем конец обмена сообщениями - 0xFF
        waitEndMessage();

        if (writeAttemptsCount > 1) {
            // а почему с первого раза нам не ответили нормально? связь не очень стабильная? устройство на грани разрушения?
            if (log.isWarnEnabled()) {
                log.warn("it took {} attempts to read data: {}", writeAttemptsCount, PortAdapterUtils.arrayToString(result));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("leaving execute(byte[], long). The result is : {}; it took {} [ms]",
                    PortAdapterUtils.arrayToString(result), System.currentTimeMillis() - stopWatch);
        }

        return result;
    }

    protected void waitEndMessage() throws IOException {
        // Реализация необходима только для RNDIS транспорта
    }

    /**
     * Записывает указанные данные в порт и дожидается отклика от внешнего устройства.
     * <p>
     * Note: аргумент должен быть отвалидирован ДО вызова этого метода.
     *
     * @param data данные/команда, что надо записать
     * @throws IOException                      если возникли проблемы ввода/вывода
     * @throws PortAdapterNoConnectionException если в результате попытки "общения" данных сделали вывод, что связи с внешним устройством нет (т.е., подтверждения получения данных
     *                                          от ВУ так и не получили)
     */
    private void writeAndWaitForAck(byte[] data) throws IOException, PortAdapterNoConnectionException {
        int attemptsCount = 0;
        boolean ackReceived = false;
        do {
            //  1. отправить данные
            adapter.write(data);

            //  2. получить подтверждение
            byte ack = readByte(byteWaitTime);
            if (ShtrihControls.ACK == ack) {
                ackReceived = true;
            } else {
                // какой-то непонятный левак получен. может, в следующей попытке ответит что-то членораздельное
                if (log.isWarnEnabled()) {
                    log.warn("non-ACK response was received: {}", PortAdapterUtils.toUnsignedByte(ack));
                }

                // просто дочитаем что там еще есть в порту и залоггируем
                byte[] junk = adapter.readBytes(byteWaitTime, byteWaitTime);
                if (log.isWarnEnabled()) {
                    log.warn("junk received: {}", PortAdapterUtils.arrayToString(junk));
                }
            }
            if (ackReceived) {
                // внешнее устройство приняло команду
                break;
            }
        } while (++attemptsCount <= dataSendAttempts);

        // 4. анализируем результат инфо-обмена
        if (!ackReceived) {
            // подтверждения мы так и не получили: нас "не услышали" - примем решение, что связи нет
            log.error("leaving write(byte). NO CONNECTION!");
            throw new PortAdapterNoConnectionException(String.format("ACK was not received in spite of %s attempts!", attemptsCount));
        }

        // 5. передача данных успешно завершена
        if (attemptsCount > 1) {
            // а почему с первого раза то нас не поняли? связь не очень стабильная? устройство на грани разрушения?
            if (log.isWarnEnabled()) {
                log.warn("it took {} attempts to send {}", attemptsCount, PortAdapterUtils.arrayToString(data));
            }
        }
    }

    /**
     * При необходимости подождет появления данных в порту указанное количество времени и, если данные появятся, считает и вернет первый байт.
     *
     * @param byteWaitTime допустимое время ожидания появления данных в порту, в мс
     * @return появившиеся данные
     * @throws IOException
     * @throws PortAdapterNoConnectionException если данные в порту так и не появились за указанное время
     */
    private byte readByte(long byteWaitTime) throws IOException, PortAdapterNoConnectionException {
        byte result;

        // 1. подождем данных
        waitForAnyData(byteWaitTime);

        // 2. и считаем данные
        result = (byte) adapter.read();

        return result;
    }

    /**
     * При необходимости подождет появления данных в порту указанное количество времени и, если данные появятся, считает и вернет их.
     *
     * @param waitTime     допустимое время ожидания появления данных в порту, в мс
     * @param byteWaitTime допустимая задержка м/у появлениями байт данных в порту, в мс
     * @return не <code>null</code> - в крайнем случае будет Exception
     * @throws IOException
     * @throws PortAdapterNoConnectionException если данные в порту так и не появились за указанное время
     */
    private byte[] readData(long waitTime, long byteWaitTime) throws IOException, PortAdapterNoConnectionException {
        byte[] result = null;

        // 1. подождем данных
        waitForAnyData(waitTime);

        // 2. и считаем данные
        boolean responseComplete = false;
        do {
            // 2.1. считаем что уже есть
            byte[] chunk = adapter.readBytes();

            // 2.2. добавим считанный кусок в результат:
            result = concatenateArrays(result, chunk);

            // 2.3. проверим. возможно ответ уже полон - т.е., больше не надо ничего ждать:
            if (validateResponse(result)) {
                // да, ответ уже полностью получен
                responseComplete = true;
            } else {
                // ответ еще не полон. подождем:
                try {
                    waitForAnyData(byteWaitTime);
                } catch (PortAdapterNoConnectionException panc) {
                    // больше нет данных. и мы знаем. что ответ невалиден
                    log.error("invalid response was received: {}", PortAdapterUtils.arrayToString(result));
                    responseComplete = true;
                }
            }
        } while (!responseComplete);

        return result;
    }

    /**
     * Вернет результат конкатенации указанных массивов.
     *
     * @param first  первый массив
     * @param second второй массив
     * @return не <code>null</code> - в крайнем случае вернет пустой массив
     */
    private static byte[] concatenateArrays(byte[] first, byte[] second) {
        byte[] result = new byte[(first == null ? 0 : first.length) + (second == null ? 0 : second.length)];

        if (first != null && first.length > 0) {
            System.arraycopy(first, 0, result, 0, first.length);
        }
        if (second != null && second.length > 0) {
            System.arraycopy(second, 0, result, first == null ? 0 : first.length, second.length);
        }

        return result;
    }

    @Override
    public void close() {
        log.trace("entering close()");
        adapter.close();
        log.trace("leaving close()");
    }

    public long getByteWaitTime() {
        return byteWaitTime;
    }

    public void setByteWaitTime(long byteWaitTime) {
        this.byteWaitTime = byteWaitTime;
    }
}
