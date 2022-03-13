package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import ru.crystals.comportemulator.pirit.PiritCommand;

import java.util.Objects;

/**
 * Идентификатор запроса/ответа при обмене с Пиритом
 */
public class PacketId {

    /**
     * Номер пакета
     */
    private final int packetId;
    /**
     * Команда
     */
    private final PiritCommand command;
    /**
     * Команда с подзапросом
     */
    private final ExtendedCommand extendedCommand;

    public PacketId(int packetId, PiritCommand command, ExtendedCommand extendedCommand) {
        this.packetId = packetId;
        this.command = command;
        this.extendedCommand = extendedCommand;
    }

    public PacketId(int packetId, PiritCommand command) {
        this(packetId, command, null);
    }

    public PacketId(int packetId, ExtendedCommand extendedCommand) {
        this(packetId, extendedCommand.getCmd(), extendedCommand);
    }

    public int getPacketId() {
        return packetId;
    }

    public PiritCommand getCommand() {
        return command;
    }

    public ExtendedCommand getExtendedCommand() {
        return extendedCommand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PacketId other = (PacketId) o;
        if (packetId != other.packetId) {
            return false;
        }
        if (command.getCode() != other.command.getCode()) {
            return false;
        }
        if (extendedCommand == null && other.extendedCommand == null) {
            return true;
        }
        if (extendedCommand != null && other.extendedCommand != null) {
            return extendedCommand.getSubCmd() == other.extendedCommand.getSubCmd();
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(packetId, command.getCode(), extendedCommand == null ? 0 : extendedCommand.getSubCmd());
    }

    @Override
    public String toString() {
        return String.format("#%d %s", packetId, extendedCommand != null ? extendedCommand : command);
    }
}
