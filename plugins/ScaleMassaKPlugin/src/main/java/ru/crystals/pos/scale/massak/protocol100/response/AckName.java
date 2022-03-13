package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

/**
 * Data = [ ScalesId, Name ] <br>
 * <p>
 * Cmd : 0x21 - код ответа на {@link ru.crystals.pos.scale.massak.protocol100.request.GetName GetName}. <br>
 * <b>Блок Data:</b> <br>
 * ScalesId : int, 4 байта - id весового устройства. <br>
 * Name : char[], 2-27 байт - имя весового устройства. <br>
 */
public class AckName extends Response {

    private final int idStartIndex = 6;
    private final int nameStartIndex = 10;

    public AckName(byte[] answer) throws Protocol100Exception {
        super(0x21, answer);
    }

    public int getId() {
        return parseInt(idStartIndex);
    }

    public String getName() {
        return parseString(nameStartIndex);
    }

}
