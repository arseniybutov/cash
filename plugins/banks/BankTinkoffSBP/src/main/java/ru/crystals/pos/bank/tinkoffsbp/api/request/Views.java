package ru.crystals.pos.bank.tinkoffsbp.api.request;

/**
 * Класс для сериализации Request без поля token через ObjectMapper
 */
public class Views {

    /**
     * Все поля Request, кроме hash
     */
    public interface WithoutToken {}

    /**
     * Все поля Request
     */
    public interface WithToken extends WithoutToken {}

    /**
     * Все поля InitRequest, кроме Card (CustomerKey)
     */
    public interface  WithoutCard {}

    /**
     * Все поля InitRequest
     */
    public interface WithCard extends WithoutCard {}
}
