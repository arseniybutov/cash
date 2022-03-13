package ru.crystals.loyal.providers;

import ru.crystals.pos.CashException;

/**
 * Ошибки, возникшие при процессинге "лояльности". {@link #getMessage() сообщение ошибки} содержит описание в виде, что можно показать пользователю
 * (кассиру).
 * <br/>
 * Этот класс исключений дополнительно снабжен полем {@link #getProcessingName()}, которое содержит имя процессинга, бросившего исключение, однако
 * нет гарантий, что это поле всегда будет заполнено.
 * @author aperevozchikov
 */
public class LoyProcessingException extends CashException {
    private static final long serialVersionUID = 1L;
    private String processingName;

    public LoyProcessingException(String message) {
        super(message);
    }

    public LoyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link LoyProcessingException}.
     * @param processingName название процессинга ({@link LoyProvider#getProviderName()}), который выкинул это исключение/
     * @param message текст исключения
     * @param cause внутреннее исключение (если было)
     */
    public LoyProcessingException(String processingName, String message, Throwable cause) {
        super(message, cause);
        this.processingName = processingName;
    }

    /**
     * Возвращает имя процессинга, который выкинул это исключение.
     * @return имя процессинга, который выкинул это исключение.<br/>
     * Нет гарантии, что у всякого экземпляра {@link LoyProcessingException} это поле будет заполнено.
     */
    public String getProcessingName() {
        return processingName;
    }

    /**
     * Устанавливает имя процессинга, который выкинул это исключение.
     * @param processingName имя процессинга, который выкинул это исключение.
     */
    public void setProcessingName(String processingName) {
        this.processingName = processingName;
    }
}