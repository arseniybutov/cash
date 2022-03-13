package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.io.IOException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.SetExchangeParamCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihTransport;
import ru.crystals.pos.utils.PortAdapterException;

/***
 * SRTB-1742
 * Использует COM+USB
 */
public class ShtrihMini01FConnector extends ShtrihRetailFConnector {

    @Override
    public void open() throws PortAdapterException, ShtrihException, IOException {
        try {
            // баг прошивки штрих мини(и штрих-м-01(02)ф),
            // он игнорирует значение скорости в таблице и по факту продолжает работать на 115200, хотя значение скорости может быть дргуим.
            // всеравно попробуем подключиться и выставить корректную скорость в таблице
            setExchangeParam(SetExchangeParamCommand.BaudRate.RECOMMENDED);
            start();
        } catch (Exception e) {
            // Если не смогли подключиться, то возможно сброшена скорость, попробуем настроить
            close();
            setExchangeParam(SetExchangeParamCommand.BaudRate.DEFAULT);
            start();
        }
    }

    private void start() throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering open()");

        //обновляем файл netcfg с настройкой для RNDIS
        updateNetCfg();

        ShtrihTransport transportToBe = new ShtrihTransport();
        transportToBe.setByteWaitTime(800);
        transportToBe.setPortName(getPortName());
        transportToBe.setBaudRate(getBaudRate());

        transport = transportToBe;
        transport.open();

        // надо аннулировать документ, если он открыт
        try {
            annul();
        } catch (Exception e) {
            // баг в работе штрих мини - если пробовали посылать команду на другой скорости,
            // то после переконекта первую команду игнорирует, попробуем второй раз
            annul();
        }

        // проинициализируем наше устройство:
        init();

        log.debug("leaving open()");
    }
}
