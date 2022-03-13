package ru.crystals.loyal.providers;

/**
 * Данная разновидность ошибок {@link LoyProcessingException "калькуляции" "преференций"} сигнализирует о том, что процесс расчета "преференций" можно
 * повторить: возможно "поставщик лояльности" как-то изменил чек (например, удалил из него карту или добавил фиктивную) в результате чего, возможно,
 * ошибка более не повторится.
 * 
 * @author aperevozchikov
 */
public class LoyProcessingTryItAgainException extends LoyProcessingException {
    private static final long serialVersionUID = 1L;

    public LoyProcessingTryItAgainException(String message) {
        super(message);
    }

    public LoyProcessingTryItAgainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link LoyProcessingTryItAgainException}.
     * @param message сообщение об ошибке
     * @param processingName имя процессинга, который кинул это исключение
     * @param cause внутренняя причина ошибки
     */
    public LoyProcessingTryItAgainException(String message, String processingName, Throwable cause) {
        super(processingName, message, cause);
    }
}