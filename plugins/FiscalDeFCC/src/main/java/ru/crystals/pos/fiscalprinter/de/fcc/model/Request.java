package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 * @param <R>
 * @param <T>
 */
public interface Request<R, T> {

    String getUrlPath();

    R getRequestData();

    Class<T> getResponseType();

    default boolean isUseAuthorization() {
        return false;
    }

    default String token() {
        return null;
    }

    default String login() {
        return null;
    }

    default String password() {
        return null;
    }
}
