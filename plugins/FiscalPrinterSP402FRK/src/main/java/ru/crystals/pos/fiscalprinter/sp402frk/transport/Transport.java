package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.sp402frk.commands.KKTCommands;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.ResBundleFiscalPrinterSP;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Transport {

    private final Logger logger = LoggerFactory.getLogger(Transport.class);

    private static final String ENCODING_HANDLER = "com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler";

    /**
     * Параметры идентификации протакола передачи
     */
    private static final String LABEL = "OFDFNARMUKM";
    private static final String VERSION = "11.1";

    /**
     * Заголовок для XML пакета, без параметра standalone="yes"
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    /**
     * Елементы XML ArmResponse с данными об ошибке
     */
    private static final String XML_RESULT = "<Result>";
    private static final String XML_ERROR_CODE = "<ErrorCode>";

    /**
     * Кодировка пакетов КТТ
     */
    private static final Charset CHARSET_NAME = Charset.forName("UTF-8");

    /**
     * Таймауты, используемые в протоколе обмена, указаны в миллисекундах.
     */
    public static final int TIME_OUT1 = 200;
    public static final int TIME_OUT2 = 8000;
    private static final int THREAD_SLEEP_TIME = 10;
    /**
     * время ожидания байта от ККТ, в миллисекундах
     */
    private static final int BYTE_WAIT_TIME = 50;

    private int additionalTimeOut = 0;

    /**
     * Число копий пакета запроса, определяет количество повторных запросов в случаи сбоев при приеме
     */
    private static final int REQUESTS_COPY_NUM = 2;
    private LinkedList<String> currRequestCopies = new LinkedList<>();
    /**
     * Максимальное число сохраняемых ответов от ККТ
     */
    private static final int MAX_STORED_RESPONSES = 3;
    private List<ArmResponse> lastKKTResponses = new ArrayList<>();
    private boolean useLastResponse = false;

    private SerialPort serialPort;
    private StringBuffer buffer;

    private InputStream is;
    private BufferedInputStream in;

    private OutputStream os;
    private BufferedWriter out;

    public void open(String portName, int baudeRate, boolean useFlowControl) throws FiscalPrinterException {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                throw new FiscalPrinterException("Port " + portName + " is busy", CashErrorType.FISCAL_ERROR_REBOOT);
            }

            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);

            if (useFlowControl) {
                serialPort.setFlowControlMode((SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT));
                serialPort.setRTS(true);
            }

            serialPort.setSerialPortParams(baudeRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            logger.debug("Opened port: " + portName + " baudeRate: " + Integer.toString(baudeRate));


            is = serialPort.getInputStream();
            in = new BufferedInputStream(is);

            os = serialPort.getOutputStream();
            out = new BufferedWriter(new OutputStreamWriter(os));

            buffer = new StringBuffer();
            resetTimeOut();
        } catch (Exception e) {
            throw new FiscalPrinterCommunicationException("Unable to open device port", e);
        }
    }

    /**
     * Закрывает потоки чтения/записи последовательного порта, а также поток
     * буффера данных.
     */
    public void close() {
        try {
            serialPort.close();
        } catch (Exception e) {
            logger.debug("close SerialPort error: ", e);
        }
        try {
            is.close();
        } catch (Exception e) {
            logger.debug("close InputStream error: ", e);
        }
        try {
            in.close();
        } catch (Exception e) {
            logger.debug("close BufferedInputStream error: ", e);
        }
        try {
            os.close();
        } catch (Exception e) {
            logger.debug("close OutputStream error: ", e);
        }
        try {
            out.close();
        } catch (Exception e) {
            logger.debug("close BufferedWriter error: ", e);
        }

        buffer.delete(0, buffer.length());
        lastKKTResponses.clear();
    }

    /**
     * Ожидание доступности данных во входном потоке. Блокирует выполнение до
     * тех пор, пока не будут доступны данные для чтения, или не истечет
     * таумаут, заданный в параметре. Данные из входного потока не извлекаются
     *
     * @param timeout таймаут ожидания, в миллисекундах
     * @return true, если данные стали доступны до истечения таймаута, false
     * otherwise
     */
    private boolean waitAnyData(int timeout) {
        if (timeout < 0) {
            return false;
        }
        long checkPoint = System.currentTimeMillis();
        try {
            while (in.available() <= 0) {
                Thread.sleep(THREAD_SLEEP_TIME);
                if ((System.currentTimeMillis() - checkPoint) > timeout) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("waitAnyData error: ", e);
            return false;
        }
    }

    /**
     * Добавляет указаное в миллисекундах врямя ожидания к базовому,
     * дополнительное время обнуляется в getCurrentTimeOut()
     *
     * @param timeout дополнительный таймаут, в миллисекундах
     */
    public void addTimeOut(int timeout) {
        additionalTimeOut += timeout;
    }

    private void resetTimeOut() {
        additionalTimeOut = 0;
    }

    private int getCurrentTimeOut() {
        int fullTimeOut = TIME_OUT2 + additionalTimeOut;
        resetTimeOut();
        return fullTimeOut;
    }

    /**
     * Получить случайный id для идентификации запроса
     *
     * @return строка с UUID
     */
    private String getRandomRequestId() {
        return "{" + UUID.randomUUID().toString() + "}";
    }

    private void saveResponse(ArmResponse response) {
        if (lastKKTResponses.size() >= MAX_STORED_RESPONSES) {
            lastKKTResponses.remove(0);
        }
        lastKKTResponses.add(response);
    }

    /**
     * Проверка выдаст ли запрос данных с ККТ туже информацию которую получали недавно.
     *
     * @param cmdNum номер команды ККТ
     * @param lastId номер елемента в списке последних команд с которого нужно начать поиск(в обратном порядке)
     * @return true, если запрос выдаст информацию которую уже получали.
     */
    private boolean checkResponseForRepeat(int cmdNum, int lastId) {
        if (lastId >= 0 && lastId < lastKKTResponses.size()) {
            ArmResponse response = lastKKTResponses.get(lastId);
            int requestCmdNum = response.getResponseBody().getCommand() - 1;
            if (UtilsSP.contains(KKTCommands.NO_REPEAT_COMMANDS, requestCmdNum)) {
                if (requestCmdNum == cmdNum) {
                    logger.debug("command({}) repeated, using last response", cmdNum);
                    return true;
                }
                lastId--;
                return checkResponseForRepeat(cmdNum, lastId);
            }
        }
        return false;
    }

    private ArmResponse getLastResponse(int cmdNum) throws FiscalPrinterCommunicationException {
        for (ArmResponse response : lastKKTResponses) {
            if (response.getResponseBody().getCommand() == cmdNum) {
                logger.debug("<<<< last command requestId: {}", response.getResponseBody().getRequestId());
                return response;
            }
        }
        throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_COMAND_DATA_NOT_FOUND"));
    }

    /**
     * Поиск кодов ошибок в ответе на команду без десериализации
     *
     * @param responseStr - ответ ККТ на команду
     * @return ArmResponse без данных с найденными кодами ошибок.
     */
    private ArmResponse parseResponseError(String responseStr) throws FiscalPrinterCommunicationException {
        logger.debug("parseResponseError()");
        ArmResponse response = new ArmResponse();
        ResponseBody responseBody = new ResponseBody();

        int result = parseXmlData(XML_RESULT, responseStr);
        int errorCode = parseXmlData(XML_ERROR_CODE, responseStr);
        logger.debug("parsed {} = {}, {} = {}", XML_RESULT, result, XML_ERROR_CODE, errorCode);

        //Если не смогли распарсить оба элемента выдаем эксепшен десериализации
        if (result == -1 && errorCode == -1) {
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_DESERIALIZE_DATA"), CashErrorType.FISCAL_ERROR_REBOOT);
        }

        //Если <Result> не известен, проверяем ответ только по <ErrorCode>
        if (result == -1) {
            result = errorCode;
        }

        responseBody.setResult(result);
        responseBody.setErrorCode(errorCode);
        responseBody.setErrorDescription(ResBundleFiscalPrinterSP.getString("ERROR_IN_COMMAND"));
        response.setResponseBody(responseBody);

        return response;
    }

    /**
     * Парсинг целочисленного елемента XML
     *
     * @param dataName    - название элемента XML
     * @param responseStr - строка с XML
     * @return целочисленное значение елемента XML, -1 если не смогли распарсить
     */
    private int parseXmlData(String dataName, String responseStr) {
        String resultStr;
        try {
            int dataPos = responseStr.indexOf(dataName);
            int dataEndPos = responseStr.indexOf("</", dataPos);
            resultStr = "" + responseStr.substring(dataPos + dataName.length(), dataEndPos);
        } catch (Exception e) {
            logger.debug("Xml element {} not found, returning -1", dataName);
            resultStr = null;
        }
        if (resultStr != null) {
            try {
                return Integer.parseInt(resultStr);
            } catch (Exception e) {
                logger.debug("Response <Result> invalid: {}", resultStr);
            }
        }
        return -1;
    }

    /**
     * Создание и сериализация объекта запроса
     *
     * @param cmd  - команда ККТ
     * @param data - параметры команды
     * @return строка с командой в XML
     * @throws FiscalPrinterCommunicationException выбрасывается при ошибках сериализации
     */
    private String serializeRequest(int cmd, String data) throws FiscalPrinterCommunicationException {

        try {
            ArmRequest request = new ArmRequest();
            RequestBody requestBody = new RequestBody();
            requestBody.setProtocolLabel(LABEL);
            requestBody.setProtocolVersion(VERSION);
            requestBody.setRequestId(getRandomRequestId());
            logger.debug(">>>> requestId: {}", requestBody.getRequestId());
            requestBody.setDateTime(UtilsSP.getCurrentDateTime());
            requestBody.setCommand(Integer.toString(cmd));
            request.setRequestBody(requestBody);
            request.setRequestData(data);

            JAXBContext jaxbContext = JAXBContext.newInstance(ArmRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            //Для корректного формирования CDATA необходима собственная имплементация CharacterEscapeHandler из com.sun.xml.*
            Class escapeHandlerInterface = Class.forName(ENCODING_HANDLER);
            Object escapeHandler = Proxy.newProxyInstance(escapeHandlerInterface.getClassLoader(),
                    new Class[]{escapeHandlerInterface},
                    new CharEscapeInvocationHandler());
            jaxbMarshaller.setProperty(ENCODING_HANDLER, escapeHandler);

            StringWriter requestStr = new StringWriter();
            jaxbMarshaller.marshal(request, requestStr);

            return requestStr.toString();

        } catch (Exception e) {
            logger.debug("Serialization error: ", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_SERIALIZE_DATA"), e);
        }
    }

    private Object deserializeData(String dataStr, Class... classesToBeBound) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(dataStr);
        return jaxbUnmarshaller.unmarshal(reader);
    }

    /**
     * Десериализация ответа от ККТ
     *
     * @param responseStr - строка данных приниая от ККТ
     * @return десереализованный пакет ответа ККТ
     * @throws FiscalPrinterCommunicationException выбрасывается при ошибках десериализации
     */
    private ArmResponse deserializeResponse(String responseStr) throws FiscalPrinterCommunicationException {

        try {
            ArmResponse response = (ArmResponse) deserializeData(responseStr, ArmResponse.class);
            //Десереализуем данные в ответе сразу, для оброботки ошибок десириализации
            if (StringUtils.isNotEmpty(response.getResponseData())) {
                response.setDeserializedData((ResponseData) deserializeData(response.getResponseData(), ResponseData.class));
            } else {
                response.setDeserializedData(new ResponseData());
            }
            return response;
        } catch (Exception e) {
            logger.debug("Deserialization error: ", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_DESERIALIZE_DATA"), CashErrorType.FISCAL_ERROR_REBOOT, e);
        }
    }

    /**
     * Формирует и отправляет команду в ККТ.
     *
     * @param cmd  - номер команды ККТ
     * @param data - параметры комнады
     * @throws FiscalPrinterCommunicationException выбрасывается при ошибках сериализации или записи данных в порт
     */
    public synchronized void sendRequest(int cmd, String data) throws FiscalPrinterCommunicationException {
        logger.debug(">>>> command({}) {}", cmd, data);
        if (checkResponseForRepeat(cmd, lastKKTResponses.size() - 1)) {
            useLastResponse = true;
            return;
        }
        useLastResponse = false;
        String requestStr = XML_HEADER + serializeRequest(cmd, data);
        currRequestCopies.clear();
        for (int i = 0; i < REQUESTS_COPY_NUM; i++) {
            //Сохраняем сформированный запрос для перезапросов в случаи повреждения ответного пакета
            currRequestCopies.push(requestStr);
        }
        sendPacket(requestStr);
    }

    /**
     * Отправляет пакет данных.
     *
     * @param requestStr - строка с командой ККТ
     * @throws FiscalPrinterCommunicationException выбрасывается при ошибках записи данных в порт
     */
    private void sendPacket(String requestStr) throws FiscalPrinterCommunicationException {
        try {
            out.write(requestStr);
            out.flush();
        } catch (Exception e) {
            logger.debug("sendPacket error: ", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_SEND_DATA"), e);
        }
    }

    /**
     * Получает ответ от ККТ
     *
     * @return объект ArmResponse с данными ответа от ККМ
     * @throws FiscalPrinterCommunicationException выбрасывается в случае ошибок чтения данных из порта или десериализации
     */
    public synchronized ArmResponse receiveResponse(int cmd) throws FiscalPrinterCommunicationException {
        if (useLastResponse) {
            logger.debug("<<<< return last response for command ({}) ", cmd);
            //Номер команды в ответе увеличен на 1
            return getLastResponse(cmd + 1);
        }
        String responseStr = receivePacket();
        logger.debug("<<<< command({}) response: {}", cmd, responseStr);
        try {
            ArmResponse response = deserializeResponse(responseStr);
            saveResponse(response);
            return response;
        } catch (Exception e) {
            //От ККТ принят битый пакет
            if (UtilsSP.contains(KKTCommands.NO_RESPONSE_COMMANDS, cmd)) {
                //Если ответные данные из команды не используются, парсим коды ошибок вручную
                return parseResponseError(responseStr);
            } else {
                //Если команда была запросом статуса делаем перезапрос
                if (!currRequestCopies.isEmpty()) {
                    logger.debug("Resending request");
                    sendPacket(currRequestCopies.pop());
                    return receiveResponse(cmd);
                }
            }
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_DESERIALIZE_DATA"), CashErrorType.FISCAL_ERROR_REBOOT, e);
        }
    }

    /**
     * Получает пакет данных от ККМ.
     *
     * @return строка с данными ответа от ККМ
     * @throws FiscalPrinterCommunicationException выбрасывается в случае ошибок чтения данных из порта, превышении таймаутов передачи данных
     */
    private String receivePacket() throws FiscalPrinterCommunicationException {
        try {
            buffer.setLength(0);
            if (waitAnyData(getCurrentTimeOut())) {
                boolean isPacketEnd = false;
                while (!isPacketEnd) {
                    int packetSize = in.available();
                    if (packetSize > 0) {
                        byte[] packetData = new byte[packetSize];
                        in.read(packetData, 0, packetSize);
                        String packetStr = new String(packetData, CHARSET_NAME);
                        buffer.append(packetStr);
                    } else {
                        //Дополнительное время для чтения оставшейся части пакета
                        Thread.sleep(BYTE_WAIT_TIME);
                        if (in.available() <= 0) {
                            isPacketEnd = true;
                        }
                    }
                }
                return buffer.toString();
            } else {
                throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_TIME_OUT_RECEIVE_DATA"));
            }
        } catch (FiscalPrinterCommunicationException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("receivePacket error: ", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterSP.getString("ERROR_READ_DATA"), e);
        }
    }
}