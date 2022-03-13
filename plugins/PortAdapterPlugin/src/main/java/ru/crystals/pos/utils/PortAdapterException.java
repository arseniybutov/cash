package ru.crystals.pos.utils;

/**
 * "Общая" (generic) ошибка при информационном обмене через порт.
 * <p/>
 * Created with IntelliJ IDEA. User: a.gaydenger Date: 10.09.13 Time: 10:42 To change this template use File | Settings | File Templates.
 */
public class PortAdapterException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PortAdapterException(String s) {
        super(s);
    }

    public PortAdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortAdapterException(Throwable cause) {
        super(cause);
    }
}
