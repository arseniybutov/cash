package ru.crystals.pos.bank.sberbank;

/**
 * Настройка плагина сбербанка (для функциональных тестов)
 *
 * @author Tatarinov Eduard
 */
public interface SberbankServiceConfigure {

    void setNeedGenerateRequestId(boolean needGenerateRequestId);

    void setResponseTimeout(long responseTimeout);

    void setInnerSlipCount(Integer count);

    long getResponseTimeout();

}
