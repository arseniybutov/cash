package ru.crystals.loyal;

/**
 * Исключение, которое бросает если внешний потребитель чекой недоступен
 * @author s.pavlikhin
 */
public class ExternalLoyaltyConsumerNetworkException extends Exception {

    public ExternalLoyaltyConsumerNetworkException(Throwable cause) {
        super(cause);
    }
}
