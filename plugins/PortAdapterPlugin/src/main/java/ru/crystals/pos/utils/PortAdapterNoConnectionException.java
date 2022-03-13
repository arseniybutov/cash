package ru.crystals.pos.utils;

/**
 * Данная разновидность {@link PortAdapterException ошибки информационного обмена через порт} возникает при отсутствии связи с внешним усройством.
 * 
 * @author aperevozchikov
 */
public class PortAdapterNoConnectionException extends PortAdapterException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PortAdapterNoConnectionException(String s) {
        super(s);
    }

    public PortAdapterNoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortAdapterNoConnectionException(Throwable cause) {
        super(cause);
    }

}
