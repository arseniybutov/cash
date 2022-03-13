package ru.crystals.pos.loyal.cash.converter;

import ru.crystals.loyal.check.Position;
import ru.crystals.pos.check.PositionEntity;

import java.util.List;

/**
 * @author Vladimir Popov &lt;v.popov@crystals.ru&gt;
 */
public class PositionNotFoundException extends Exception {
    public PositionNotFoundException() {
    }

    public PositionNotFoundException(String message) {
        super(message);
    }

    public PositionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PositionNotFoundException(Throwable cause) {
        super(cause);
    }

    public PositionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PositionNotFoundException(Position position, List<PositionEntity> cashPositions) {
        // TODO Добавить описание ошибки
    }
}
