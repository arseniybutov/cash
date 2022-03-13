package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BeginCloseShiftCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BeginOpenShiftCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFNNumberCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFNStateCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetLastFiscalizationResult;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFieldStructureV2Command;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.LoadTLVStructCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintExtGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintFNReportCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintZReportCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.SetExchangeParamCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihResponseParseException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihTransport;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * {@link ShtrihConnector коннектор} для модели ФР "Retail-01F".
 *
 * @author Tatarinov Eduard
 */
public class ShtrihRetailFConnector extends BaseShtrihConnector {

    private static final String ETH_CONFIG_FILE = "/opt/netcfg";

    @Override
    public void open() throws IOException, PortAdapterException, ShtrihException {
        try {
            super.open();
        } catch (Exception e) {
            // Если не смогли подключиться, то возможно сброшена скорость, попробуем настроить
            close();
            setExchangeParam(SetExchangeParamCommand.BaudRate.DEFAULT);
            super.open();
        }
    }

    /**
     * Метод пробует подключиться на дефолтной скорости(4800) и установить рекомендуемую скорость(115200).
     * <p>
     * SRTB-1960
     */
    protected void setExchangeParam(SetExchangeParamCommand.BaudRate baudRate) {
        try {
            log.info("Try to set recommended baud rate...");
            ShtrihTransport temporaryTransport = new ShtrihTransport();
            temporaryTransport.setByteWaitTime(400);
            temporaryTransport.setPortName(getPortName());
            temporaryTransport.setBaudRate(baudRate.getBaudRate());
            transport = temporaryTransport;
            transport.open();
            executeAndThrowExceptionIfError(new SetExchangeParamCommand(getPassword(), 6));
            log.info("Success set recommended baud rate!");
        } catch (Exception e) {
            // нормально, ничего не делаем, может скорость уже настроена, и логично, что не смогли подключиться на дефолтной
            log.info("Fail to change baud rate.");
        } finally {
            try {
                transport.close();
            } catch (Exception ignore) {
            } finally {
                transport = null;
            }
        }
    }

    //дефолтный конструктор
    public ShtrihRetailFConnector() {
    }

    @Override
    protected int getLinesBetweenThermoHeadAndKnife() {
        return 4;
    }

    @Override
    protected List<FontLine> getHeader() throws IOException, PortAdapterException, ShtrihException {
        List<FontLine> result = new ArrayList<>(super.getHeader());

        result.add(0, new FontLine("", Font.NORMAL));

        return result;
    }

    @Override
    public void openShift(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        if (cashier != null && cashier.getInn() != null){
            executeAndThrowExceptionIfError(new BeginOpenShiftCommand(getPassword()));
            sendCashierInnIfNeeded(cashier.getInn());
        }
        super.openShift(cashier);
        printReportEnd();
    }

    @Override
    public void printZReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering printZReport()");

        // 1. дождемся когда принтер закончит предыдущую печать (если есть)
        ShtrihStateDescription state = waitForPrinting();

        // 2. и, если смена не открыта, сначала пробьем нулевой чек
        if (!ShtrihModeEnum.canCloseShift(state.getMode().getStateNumber())) {
            // надо пробить нулевой чек в 1м попавшемся отделе - тупо чтоб открыть смену
            // 2.1. регистрируем нулевую позицию:
            ShtrihPosition position = new ShtrihPosition("", 0L, 0L, (byte) 1);
            regSale(position);

            // 2.2. и закрываем этот нулевой чек
            ShtrihReceiptTotal receipt = new ShtrihReceiptTotal("", 0L);
            closeReceipt(receipt);
        }

        // 3. Передадим ИНН кассира, если возможно
        if (cashier != null && cashier.getInn() != null) {
            executeAndThrowExceptionIfError(new BeginCloseShiftCommand(getPassword()));
            sendCashierInnIfNeeded(cashier.getInn());
        }

        // 4. распечатаем Z-отчет
        PrintZReportCommand cmd = new PrintZReportCommand(getPassword());
        executeAndThrowExceptionIfError(cmd);

        // 5. и шапку следующего документа
        printReportEnd();

        log.trace("leaving printZReport()");
    }

    @Override
    protected FieldStructure getFieldStructure(byte tableNo, byte fieldNo) throws IOException, PortAdapterException, ShtrihException {
        FieldStructure result = null;
        log.trace("entering getFieldStructure(byte, byte). The arguments are: tableNo [{}], fieldNo [{}]", tableNo, fieldNo);

        // Исполним запрос
        GetFieldStructureV2Command cmd = new GetFieldStructureV2Command(tableNo, fieldNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);

        if (result == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }
        log.trace("leaving getFieldStructure(byte, byte). The result is: {}", result);
        return result;
    }

    @Override
    public String getFNNumber() throws IOException, PortAdapterException, ShtrihException {
        String result;

        GetFNNumberCommand cmd = new GetFNNumberCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        return result;
    }

    @Override
    public ShtrihFNStateOne getFNState() throws IOException, PortAdapterException, ShtrihException {
        ShtrihFNStateOne result;

        GetFNStateCommand cmd = new GetFNStateCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        return result;
    }

    @Override
    public ShtrihFiscalizationResult getLastFiscalizationResult() throws IOException, PortAdapterException, ShtrihException {
        GetLastFiscalizationResult cmd = new GetLastFiscalizationResult(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        ShtrihFiscalizationResult result = cmd.decodeResponse(response);

        return Optional.ofNullable(result).orElseThrow(()->new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response))));
    }

    @Override
    public void setClientData(String clientData) throws IOException, PortAdapterException, ShtrihException {
        LoadTLVStructCommand cmd = new LoadTLVStructCommand(clientData, getPassword());
        executeOnce(cmd);
    }

    @Override
    public void printFNReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        executeAndThrowExceptionIfError(new PrintFNReportCommand(getPassword()));
        printReportEnd();
    }

    @Override
    public void printLogo() throws IOException, PortAdapterException, ShtrihException {
        if (getImageFirstLine() != null && getImageLastLine() != null && getImageLastLine() > getImageFirstLine()) {
            executeAndThrowExceptionIfError(new PrintExtGraphicsCommand(getImageFirstLine(), getImageLastLine(), getPassword()));
        }
    }

    @Override
    public String toString() {
        return String.format("shtrih-retailf-connector [post: %s; rate: %s]", getPortName(), getBaudRate());
    }

    @Override
    public void setCashierName(byte cashierNo, String cashierName) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering setCashierName(byte, String). The arguments are: cashierNo [{}], cashierName [{}]", cashierNo, cashierName);

        // 1. Сначала узнаем допустимое количество кассиров:
        TableStructure tableStructure = getTableStructure(getCashiersTableNo());
        int cashiersCount = tableStructure.getRowsCount();
        if (cashiersCount < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности "регистрации" кассиров
            log.warn("leaving setCashierName(byte, String): this fiscal registry supports [{}] cashiers", cashiersCount);
            return;
        }
        if (cashierNo < 1 || cashierNo > cashiersCount) {
            // аргумент невалиден
            log.warn("leaving setCashierName(byte, String): the \"cashierNo\" argument (== {}) is INVALID: more than {} or less than 1", cashierNo, cashiersCount);
            return;
        }

        // 2. аргументы валидны. Узнаем "ширину" поля имени кассира:
        FieldStructure cashierNameFieldStructure = getFieldStructure(getCashiersTableNo(), getCashierNameFieldNo());
        if (cashierNameFieldStructure.getFieldWidth() < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности "регистрации" кассиров
            log.warn("leaving setCashierName(byte, String): this fiscal registry supports [{}]-bytes-width cashier names", cashierNameFieldStructure.getFieldWidth());
            return;
        }

        // 3. и запишем в фискальник:
        byte[] value = getStringAsByteArray(cashierName, cashierNameFieldStructure.getFieldWidth());
        writeTable(getCashiersTableNo(), cashierNo, getCashierNameFieldNo(), value);
        //SRTB-1325 Вероятно из-за ошибки в прошивке ФР Retail-01F, нам был предложен фикс, в котором мы создаем дополнительную запись в 30 строку
        //для того чтобы корректно отображались ФИО в чеках продажи/возврата
        writeTable(getCashiersTableNo(), 30, getCashierNameFieldNo(), value);
        log.debug("leaving setCashierName(byte, String)");

    }

    //обновляет файл netcfg с настройкой для RNDIS
    public synchronized void updateNetCfg() {
        Charset charset = StandardCharsets.UTF_8;
        Path path = Paths.get(ETH_CONFIG_FILE);
        try {
            if (Files.exists(path)) {
                Files.write(path, new String(Files.readAllBytes(path), charset).
                        replace("SHTRIHRNDISSTART=\"0\"", "SHTRIHRNDISSTART=\"1\"").getBytes(charset));
            } else {
                log.error("File not found");
            }
        } catch (IOException exc) {
            log.error("Error during loading the file ", exc);
        }
    }
}