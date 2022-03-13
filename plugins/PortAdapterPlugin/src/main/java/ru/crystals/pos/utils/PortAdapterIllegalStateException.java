package ru.crystals.pos.utils;

/**
 * Ошибка данного вида возникает когда внешнее устройство, с которым ведется инфо-обмен находится в некорректном/неожиданном состоянии. Например, если
 * способ связи "полу-дуплекс" и мы отправили [успешно] запрос этому внешнему устройству и ожидаем, что оно (это устройство) после этого будет в
 * состоянии подготовки ответа, либо в состоянии передачи ответа, а на самом деле оно оказывается в состоянии "ожидания команды".
 * 
 * @author aperevozchikov
 */
public class PortAdapterIllegalStateException extends PortAdapterException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PortAdapterIllegalStateException(String s) {
        super(s);
    }

    public PortAdapterIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortAdapterIllegalStateException(Throwable cause) {
        super(cause);
    }
}
