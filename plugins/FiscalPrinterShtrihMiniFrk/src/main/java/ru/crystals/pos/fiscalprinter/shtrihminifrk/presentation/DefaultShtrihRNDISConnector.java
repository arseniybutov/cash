package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihRNDISTransport;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;

/**
 * Стандартный {@link ShtrihConnector коннектор} для ФР семейства "Штрих" с подключением через USB-RNDIS.
 */
public class DefaultShtrihRNDISConnector extends ShtrihRetailFConnector {

    public DefaultShtrihRNDISConnector() {
        //дефолтный конструктор
    }

    @Override
    public void open() throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering open()");
        //обновляем файл netcfg с настройкой для RNDIS
        updateNetCfg();

        ShtrihRNDISTransport transportToBe = new ShtrihRNDISTransport();
        transportToBe.setIpAddress(getIpAddress());
        transportToBe.setTcpPort(getTcpPort());

        transport = transportToBe;
        transport.open();
        // надо аннулировать документ, если он открыт
        annul();
        // проинициализируем наше устройство:
        init();
        log.debug("leaving open()");
    }

    @Override
    public String toString() {
        return String.format("Default Shtrih RNDIS connector [ipAddress: %s; tcpPort: %s]", getIpAddress(), getTcpPort());
    }
}