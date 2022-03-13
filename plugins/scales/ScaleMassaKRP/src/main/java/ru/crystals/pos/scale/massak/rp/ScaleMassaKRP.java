package ru.crystals.pos.scale.massak.rp;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.ResBundleScale;
import ru.crystals.pos.scale.exception.ScaleConnectionException;
import ru.crystals.pos.scale.exception.ScaleDisconnectedException;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.user.UserEntity;
import ru.crystals.pos.user.events.UserLoginListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Плагин для работы с весами Масса-К серии R по TCP
 * <p>
 * Особенность плагина - работа с весами только при залогиненном пользователе,
 * поскольку эти же весы одновременно используются как весы самообслуживания (правда сотрудниками магазина).
 */
public class ScaleMassaKRP extends AbstractScalePluginImpl implements UserLoginListener {

    private static final Logger log = LoggerFactory.getLogger(ScaleMassaKRP.class);
    private static final byte[] HEADER = new byte[]{(byte) 0xF8, 0x55, (byte) 0xCE};
    /**
     * CMD_TCP_GET_WEIGHT - запрос текущей массы, цены деления и при-знака стабильности показаний
     * [0..2] фиксированный заголовок
     * [3..4] длина тела сообщения (для этой команды фиксированная - 1)
     * [5] ID команды (0xA0 для CMD_TCP_GET_WEIGHT)
     * [6..7] CRC
     * <p>
     * Весы нормально воспринимают нули в качестве CRC, а на чтении ответа по TCP есть сомнение в необходимости проверки.
     */
    private static final byte[] GET_WEIGHT = new byte[]{(byte) 0xF8, 0x55, (byte) 0xCE, 0x00, 0x01, (byte) 0xA0, 0x00, 0x00};
    private static final int CMD_TCP_ACK_WEIGHT = 0x10;
    private static final int ACK_WEIGHT_ANSWER_SIZE = 14;

    private static final ScaleDisconnectedException SCALE_DISCONNECTED_EXCEPTION = new ScaleDisconnectedException();

    private final ScaleMassaKRPConfig config = new ScaleMassaKRPConfig();

    private ScaleMassaKRPConnector connector;
    private ScaleTCPSession currentSession;

    /**
     * Касса подключаются к весам при логине и отключается при разлогине,
     * если сразу не получилось подключиться, то благодаря этому флагу мы будем пытаться подключиться после логина.
     */
    private AtomicBoolean needStartSession = new AtomicBoolean();

    @Override
    public void start() {
        connector = new ScaleMassaKRPConnector(config);
        testConnection();
        final TechProcessEvents tpe = BundleManager.get(TechProcessEvents.class);
        tpe.addUserLoginListener(this);
    }

    private void testConnection() {
        try (ScaleTCPSession session = connector.newSession()) {
            getWeight(session);
        } catch (Exception e) {
            log.error("Unable to test connection to scale");
        }
    }

    @Override
    public void stop() {
        disconnectFromScales();
    }

    @Override
    public int getWeight() throws ScaleException {
        if (currentSession != null) {
            return getWeight(currentSession);
        }
        if (!needStartSession.get()) {
            // если мы разлогинены, то возвращаем весовому модулю эксепшен, чтобы нарисовалась иконка отключенных весов
            throw SCALE_DISCONNECTED_EXCEPTION;
        }
        createSessionIfRequired();
        needStartSession.set(false);
        return getWeight(currentSession);
    }

    private int getWeight(ScaleTCPSession session) throws ScaleException {
        final byte[] packet;
        try {
            packet = session.sendAndRead(GET_WEIGHT);
        } catch (IOException e) {
            if (!needStartSession.get()) {
                // если мы разлогинились, то игнорируем ошибки (скорее всего тут про то, что stream закрыт)
                log.trace("Error on get weight after log out", e);
                return 0;
            }
            throw new ScaleException(ResBundleScale.getString("ERROR_GET_WEIGHT"), e);
        }
        if (!isValidPacket(packet)) {
            if (log.isTraceEnabled() && packet.length > 0) {
                log.trace("--> {}", bytesToHex(packet));
            }
            throw new ScaleException(ResBundleScale.getString("ERROR_GET_WEIGHT"));
        }
        return extractWeight(packet);
    }

    private boolean isValidPacket(byte[] packet) {
        if (packet.length == 0) {
            log.error("No weight response from scales");
            return false;
        }
        if (packet.length != ACK_WEIGHT_ANSWER_SIZE) {
            log.error("Invalid response length (expected 14): {}. Response: {}", packet.length, bytesToHex(packet));
            return false;
        }
        if (packet[0] != HEADER[0] || packet[1] != HEADER[1] || packet[2] != HEADER[2]) {
            log.error("Invalid response header. Response: {}", bytesToHex(packet));
            return false;
        }
        if (packet[5] != CMD_TCP_ACK_WEIGHT) {
            log.error(String.format("Invalid response command: %02X. Response: %s", packet[5], bytesToHex(packet)));
            return false;
        }
        return true;
    }

    /**
     * CMD_TCP_ACK_WEIGHT - передача текущей массы, цены деления и признака стабильности показаний
     * [0..2] фиксированный заголовок
     * [3..4] длина тела сообщения (для этой команды фиксированная - 7)
     * [5] ID команды (0x10 для CMD_TCP_ACK_WEIGHT)
     * [6..9] текущая масса со знаком
     * [10] Цена деления: 0 - 100 мг, 1 - 1 г, 2 - 10 г, 3 - 100 г, 4 - 1 кг
     * [11] Признак стабилизации массы: 0 - нестабилен, 1 - стабилен
     * [12..13] CRC
     * <p>
     * На чтении ответа по TCP есть сомнение в необходимости проверки CRC
     */
    int extractWeight(byte[] tcpAckWeightPacket) throws ScaleException {
        final byte stable = tcpAckWeightPacket[11];
        final int weight = ByteBuffer.wrap(tcpAckWeightPacket, 6, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        final byte division = tcpAckWeightPacket[10];
        final int converted = convertWeight(weight, division);
        if (log.isTraceEnabled()) {
            log.trace("--> {}: weight={}, division={}, stable={}: {}",
                    bytesToHex(tcpAckWeightPacket),
                    weight, division, stable,
                    converted);
        }
        if (stable == 0 || weight < 0) {
            return 0;
        }
        return converted;
    }

    private int convertWeight(int weight, byte division) throws ScaleException {
        if (weight == 0) {
            return 0;
        }
        switch (division) {
            case 1:
                return weight;
            case 0:
                return (int) (weight * 0.100);
            case 2:
                return weight * 10;
            case 3:
                return weight * 100;
            case 4:
                return weight * 1000;
            default:
                log.error("Invalid division value: {}. Expected to be between 0 and 4.", division);
                throw new ScaleException(ResBundleScale.getString("ERROR_GET_WEIGHT"));
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return true;
    }

    @Override
    public void eventUserLogin(UserEntity loggedUser) {
        try {
            createSessionIfRequired();
            needStartSession.set(false);
        } catch (ScaleConnectionException e) {
            log.error("Unable to connect to scale on login (will try to connect later)", e);
            needStartSession.set(true);
        }
    }

    @Override
    public void eventUserLogout(UserEntity loggedUser) {
        disconnectFromScales();
    }

    private void disconnectFromScales() {
        needStartSession.set(false);
        if (currentSession != null) {
            currentSession.close();
            currentSession = null;
        }
    }

    private void createSessionIfRequired() throws ScaleConnectionException {
        if (currentSession != null) {
            return;
        }
        currentSession = connector.newSession();
    }

    private String bytesToHex(byte[] packet) {
        return new String(Hex.encodeHex(packet)).toUpperCase();
    }

    public void setIp(String ip) {
        config.setIp(ip);
    }

    public void setTcpPort(int tcpPort) {
        config.setTcpPort(tcpPort);
    }

}
