package ru.crystals.pos.bank.commonsbpprovider;

/**
 * Интерфейс для методов общих для всех банков конфигураций
 */
public interface SBPProviderConfig {
    /**
     * @return Идентификатор ТСП
     */
    String getSbpMerchantId();

    /**
     * @return Время жизни QR кода в минутах
     */
    Long getQrExpiration();

    /**
     * @return Урл для запросов к банку
     */
    String getUrl();

    /**
     * @return Перерыв между одинаковыми запросами к банку
     */
    default long getDelayInSeconds() {
        return 0;
    }

    /**
     * @return Максимальное количество попыток одинаковых запросов к банку
     */
    default int getMaxNumberOfRequest() {
        return 0;
    }

    /**
     * @return Максимальное количество попыток запроса при проблемах соединения с банком
     */
    default int getMaxNumberOfRetries() {
        return 0;
    }

    /**
     * Пусть, по каторому будут идти запросы
     */
    void setUrl(String url);
}
