package ru.crystals.pos.scale.massak.protocol100;

import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.scale.massak.protocol100.response.Error;

public class Protocol100Exception extends ScaleException {

    public Protocol100Exception(Error e) {
        super(e.getDescription());
    }

    public Protocol100Exception(String message) {
        super(message);
    }

}
