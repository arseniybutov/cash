package ru.crystals.pos.bank.ucs.messages.responses;

import java.lang.reflect.Constructor;

public class ResponseFactory {
    public static Response parse(String response) {
        try {
            ResponseType type = ResponseType.getType(response.substring(0, 2));
            Constructor<? extends Response> co = type.getImplementationClass().getDeclaredConstructor(String.class);
            return co.newInstance(response);
        } catch (Exception e) {
            return new UnknownResponse(response);
        }
    }
}
