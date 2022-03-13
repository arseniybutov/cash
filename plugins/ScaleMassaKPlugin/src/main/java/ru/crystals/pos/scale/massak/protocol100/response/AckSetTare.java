package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

/**
 * Ответ на {@link ru.crystals.pos.scale.massak.protocol100.request.SetTare SetTare}.
 * Cmd : 0x12 - команда установки тары выполнена успешно. <br>
 * Cmd : 0x15 - невозможно установить тару. <br>
 * <b>Блок Data:</b> <br>
 * Отсутствует
 */
public class AckSetTare extends Response {

    private final byte NACK_TARE = 0x15;

    public AckSetTare(byte[] answer) throws Protocol100Exception {
        super(0x12, answer);
    }

    @Override
    protected void checkNack(byte received) throws Protocol100Exception {
        if (received == NACK_TARE) {
            throw new Protocol100Exception("Unable to set Tare");
        }
        super.checkNack(received);
    }

}
