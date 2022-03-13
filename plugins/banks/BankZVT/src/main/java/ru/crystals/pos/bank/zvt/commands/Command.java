package ru.crystals.pos.bank.zvt.commands;

public interface Command {

    /**
     * Возможен ли по этой команде получение промежуточного (04-FF) или итогового (04-0F) статуса
     */
    default boolean hasStatus() {
        return false;
    }

}