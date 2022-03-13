package ru.crystals.pos.fiscalprinter.shtrihminifrk.transport;

import java.io.IOException;

import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.PortAdapterIllegalStateException;
import ru.crystals.pos.utils.PortAdapterNoConnectionException;
import ru.crystals.pos.utils.SerialPortAdapter;

/**
 * Описание контракта <em>транспортного уровня</em> при общении с внешними устройствами через <em>последовательный</em> порт.
 * <p/>
 * NOTE: отличие от {@link SerialPortAdapter} состоит в том, что реализации данного уровня еще и управляющие сигналы (типа: ENQ, STX, ACK, и проч)
 * отправляют/анализируют (в соответствии со своим протоколом) и, возможно, делают несколько попыток отправить/получить данные - т.е., по-простому
 * реализации этого интерфейса ...реализуют чуть более высокий уровень/слой в протоколе обмена с внешними устройствами.
 * 
 * @author aperevozchikov
 */
public interface Transport {

    /**
     * Открытие порта, через который будет вестись информационный обмен.
     * 
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     * @throws PortAdapterException
     *             при различных [логических] ошибках при открытии порта: порт не существует, уже занят другим приложением, порт не удалось настроить
     */
    void open() throws IOException, PortAdapterException;

    /**
     * закрытие порта.
     */
    void close();

    /**
     * Производит сеанс "общения" с внешним устройством: отправляет указанные данные и получает ответ (который и возвращается в результате этого
     * метода).
     * 
     * @param data
     *            данные, что надо записать
     * @param maxResponseTime
     *            максимальное время ожидания отклика на эту команду, в мс (т.е., данный аргумент указывает на то. сколько времени может понадобиться
     *            ВУ (внешнему устройству) на подготовку ответа)
     * @return ответ от внешнего устройства; не <code>null</code> - в крайнем случае будет выброшен Exception
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     * @throws IllegalArgumentException
     *             если аргумент невалиден: <em>пуст</em>, не сооветствует протоколу, не совпадает контрольная сумма, и проч. если возникли проблемы
     *             ввода/вывода
     * @throws PortAdapterNoConnectionException
     *             если в результате попытки "общения" данных сделали вывод, что связи с внешним устройством нет
     * @throws PortAdapterIllegalStateException
     *             если внешнее устройство не планирует отвечать на запрос: находится в состоянии "ожидания запроса" - т.е., в не корректном состоянии
     */
    byte[] execute(byte[] data, long maxResponseTime)
        throws IOException, IllegalArgumentException, PortAdapterNoConnectionException, PortAdapterIllegalStateException;
}
