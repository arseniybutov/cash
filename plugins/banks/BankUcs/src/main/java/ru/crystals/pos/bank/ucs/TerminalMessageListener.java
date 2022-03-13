package ru.crystals.pos.bank.ucs;

import ru.crystals.pos.bank.BankEvent;

import java.util.Collection;

public interface TerminalMessageListener {

    void showCustomMessageFromTerminal(String message);

    void showCustomMessage(String message);

    void addAll(Collection<BankEvent> listeners);

}
