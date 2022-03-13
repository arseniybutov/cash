package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

/**
 * Cmd : 0x27 - код ответа на любой Set-запрос. <br>
 * <b>Блок Data:</b> <br>
 * Отсутствует
 */
public class AckSet extends Response {

    public AckSet(byte[] answer) throws Protocol100Exception {
        super(0x27, answer);
    }

}
