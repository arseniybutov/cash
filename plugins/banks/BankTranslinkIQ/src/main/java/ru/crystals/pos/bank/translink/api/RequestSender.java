package ru.crystals.pos.bank.translink.api;

import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.translink.api.dto.OpenPosResponse;
import ru.crystals.pos.bank.translink.api.dto.Result;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;

public interface RequestSender {

    OpenPosResponse openPos() throws BankCommunicationException;

    Result closePos() throws BankCommunicationException;

    Result sendCommand(Command command) throws BankCommunicationException;

    Result sendCommand(Command command, CommandParams params) throws BankCommunicationException;

    BaseEvent getEvent() throws BankCommunicationException;

    void setAccessToken(String accessToken);

    void setHost(String host);

    void setPort(int port);

    void setLicenseToken(String licenseToken);

    void setVersion(String version);
}
