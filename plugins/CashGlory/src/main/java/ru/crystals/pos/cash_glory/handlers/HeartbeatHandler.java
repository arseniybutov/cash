package ru.crystals.pos.cash_glory.handlers;

import ru.crystals.pos.cash_glory.TCPEventsServer;
import ru.crystals.pos.cash_glory.xml_interfaces.FactoryEvents;
import ru.crystals.pos.cash_machine.Constants;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Следит и проверяет время прошедшее между пришедшими ивентами {@link FactoryEvents#HEARTBEAT}
 */
public class HeartbeatHandler extends AbstractAction {

    private static final long serialVersionUID = 1L;
    /**
     * Время ожидания первого ивента от glory в секундах
     */
    private static final long FIRST_HEARTBEAT_EXCEPTATION = 30 * 1000L;
    /**
     * Скаляр для вычисления частоты прихода ивентов.
     * Используется для оптимального ожидания прихода ивента  от glory.
     * Например, время фактического прихода ивента - 10 сек.
     * Частота будет вычислена как - 10 * 2.0 = 20 сек.
     */
    private static final double HEART_BEAT_FACTOR = 2.0;
    /**
     * Изначальное время старта данного обработчика {@link HeartbeatHandler}
     */
    private final AtomicLong startPulsometerTime = new AtomicLong(System.currentTimeMillis());
    /**
     * Примерная частота прихода ивентов от glory
     * Вычисляется по формуле
     */
    private final AtomicLong heartBeatFrequency = new AtomicLong(0);
    /**
     * Время первого пришедшего ивента от glory
     */
    private final AtomicLong firstHeartBeatTime = new AtomicLong(0);
    /**
     * Время последнего пришедшего ивента от glory
     */
    private final AtomicLong lastHeartbeatTime;
    /**
     * Операция, которая будет вызвана в случае какой-либо неудачи, для перезапуска сервера {@link TCPEventsServer}
     */
    private transient Consumer<TCPEventsServer.ReanimationReason> reactionOfServerClosed;
    private TCPEventsServer tcpEventsServer;
    /**
     * Подсчитываются те ивенты, которые приходят в рамках установленного времени.
     * После достижения определенного количества, стоить сообщить в лог, что связь с glory есть
     */
    private int countOfEvents = 0;

    public HeartbeatHandler(TCPEventsServer tcpEventsServer,
                            Consumer<TCPEventsServer.ReanimationReason> reactionOfServerClosed,
                            AtomicLong lastHeartbeatTime) {
        this.tcpEventsServer = tcpEventsServer;
        this.reactionOfServerClosed = reactionOfServerClosed;
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    /**
     * Следит за временем между приходящими ивентами {@link FactoryEvents#HEARTBEAT}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (tcpEventsServer.isAlive()) {
            long now = System.currentTimeMillis();
            // Если частота появления ивента не вычислено
            if (heartBeatFrequency.get() == 0) {
                // В случае если ивент отсутствует уже долгое время
                if (now - Math.max(startPulsometerTime.get(), lastHeartbeatTime.get()) > FIRST_HEARTBEAT_EXCEPTATION) {
                    heartBeatFrequencyNotCalculated();
                    return;
                }
                // Если к этому моменту уже пришел ивент и его время установлено в lastHeartbeatTime
                if (lastHeartbeatTime.get() != 0 && firstHeartBeatTime.get() == 0) {
                    initFirstHeartBeat();
                // Появление второго ивента , позволяет вычислить частоту с которой ивенты будут приходить
                } else if (lastHeartbeatTime.get() != firstHeartBeatTime.get()) {
                    initFrequencyHeartbeat();
                }
            } else {
                // Если прошло больше времени с последнего появления ивента
                if (now - lastHeartbeatTime.get() > heartBeatFrequency.get()) {
                    heartBeatLost();
                    return;
                }
                // В последующем корректирует частоту появления ивента
                if (lastHeartbeatTime.get() != firstHeartBeatTime.get()) {
                    reinitFrequencyHeartBet();
                }
            }

        }
    }

    /**
     * Устанавливает {@link #firstHeartBeatTime} в случае если уже пришел ивент {@link FactoryEvents#HEARTBEAT}
     */
    private void initFirstHeartBeat() {
        Constants.LOG.debug("{} First pulse of heart! Happy birthday! {}", TCPEventsServer.TAG, Thread.currentThread().getName());
        firstHeartBeatTime.set(lastHeartbeatTime.get());
        tcpEventsServer.setGloryConnected();
    }

    /**
     * Вычисляет частоту прихода ивентов {@link FactoryEvents#HEARTBEAT}
     */
    private void initFrequencyHeartbeat() {
        long hb = lastHeartbeatTime.get() - firstHeartBeatTime.get();
        Constants.LOG.debug("{} The pulse frequency is calculated: {} ms {}", TCPEventsServer.TAG, hb, Thread.currentThread().getName());
        heartBeatFrequency.set((long) (hb * HEART_BEAT_FACTOR));
        Constants.LOG.debug("{} Expected pulse frequency: {}ms", TCPEventsServer.TAG, heartBeatFrequency.get());
    }

    /**
     * Ивент {@link FactoryEvents#HEARTBEAT} отсутствует уже долгое время
     */
    private void heartBeatFrequencyNotCalculated() {
        Constants.LOG.error("{} Heartbeat is not started! Start reanimation {}", TCPEventsServer.TAG, Thread.currentThread().getName());
        reactionOfServerClosed.accept(TCPEventsServer.ReanimationReason.HEARTBEAT_NOT_STARTED);
    }

    /**
     * Связсь с glory потеряна
     */
    private void heartBeatLost() {
        Constants.LOG.error("{} Heartbeat is lost! Start reanimation {}", TCPEventsServer.TAG, Thread.currentThread().getName());
        reactionOfServerClosed.accept(TCPEventsServer.ReanimationReason.HEARTBEAT_IS_LOST);
    }

    /**
     * Перевычисляет частоту прихода ивентов {@link FactoryEvents#HEARTBEAT} от glory
     */
    private void reinitFrequencyHeartBet() {
        heartBeatFrequency.set((long) ((lastHeartbeatTime.get() - firstHeartBeatTime.get()) * HEART_BEAT_FACTOR));
        firstHeartBeatTime.set(lastHeartbeatTime.get());
        if (countOfEvents++ > 9) {
            Constants.LOG.debug("{} Glory is still alive. Current heartbeat frequency is: {}ms", TCPEventsServer.TAG, heartBeatFrequency.get());
            countOfEvents = 0;
        }
    }

}
