package ru.crystals.pos.fiscalprinter.az.airconn.transport;

import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Интерфейс транспорта для передачи запросов AicConn в программный фискализатор
 */
public interface AirConnTransport {

    void init(String tokenAddress);

    void close();

    void connect() throws FiscalPrinterCommunicationException;

    void disconnect();

    /**
     * Вполняет команду на подключенном AirConn фискализаторе
     *
     * @param command комманда AirConn с данными для запроса,
     *                после выполнения содержит данные ответа {@link BaseResponse}
     * @throws FiscalPrinterException при ошибках в транспорте или в ответе от AirConn
     */
    <C extends BaseCommand> C executeCommand(C command) throws FiscalPrinterException;
}
