package ru.crystals.pos.bank.ucs;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.BankEvent;

import java.util.ArrayList;
import java.util.Collection;

public class BankUcsEftposMessageListener implements TerminalMessageListener {
    public static final int MAX_STRING_LENGTH = 29;
    private Collection<BankEvent> listeners = new ArrayList<>();

    @Override
    public void showCustomMessageFromTerminal(String message) {
        showTerminalMessage(getPreparedTerminalMessage(message));
    }

    private void showTerminalMessage(String terminalMessage) {
        eventShowCustomProcessMessage(ResBundleBankUcs.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL") + "\n\u2014 " + terminalMessage);
    }

    private void eventShowCustomProcessMessage(String message) {
        for (BankEvent bankEvent : listeners) {
            bankEvent.eventShowCustomPaymentProcessMessage(message);
        }
    }

    @Override
    public void showCustomMessage(String message) {
        eventShowCustomProcessMessage(message);
    }

    private String getPreparedTerminalMessage(String message) {
        return splitStringByMaxLength(StringUtils.trimToEmpty(message), MAX_STRING_LENGTH);
    }

    private String splitStringByMaxLength(String message, int length) {
        StringBuilder result = new StringBuilder();
        if (message != null) {
            String[] splitted = message.split("\\s+");
            StringBuilder line = new StringBuilder();
            int itemIndexPerLine = 0;
            for (int i = 0; i < splitted.length; i++) {
                int newLength = line.length() + splitted[i].length();
                if (newLength < length) {
                    if (itemIndexPerLine != 0) {
                        line.append(" ");
                    }
                    line.append(splitted[i]);
                    itemIndexPerLine++;
                } else {
                    result.append(line);
                    line = new StringBuilder();
                    line.append(splitted[i]);
                    itemIndexPerLine = 1;
                }
                if (i == splitted.length - 1) {
                    if (result.length() > 0) {
                        result.append("\n");
                    }
                    result.append(line);
                }
            }
        }
        return result.toString();
    }

    @Override
    public void addAll(Collection<BankEvent> listeners) {
        this.listeners.addAll(listeners);
    }
}