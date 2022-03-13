package ru.crystals.pos.bank.odengiqr.api.dto.request.create;

/**
 * Класс для сериализации Request без поля hash через ObjectMapper
 */
public class Views {

    /**
     * Все поля Request, кроме hash
     */
    public interface WithoutHash {}

    /**
     * Все поля Request
     */
    public interface WithHash extends WithoutHash {}
}
