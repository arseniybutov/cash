package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Команда используется для информирования через устройство POS держателя карты, о том, что данная карта не обслуживается.
 * Команда инициирует отображение информационного сообщения для держателя карты, в котором указывается причина, из-за которой данная карта не обслуживается.
 */
public class RemoveCardCommand implements CommandParams {

    /**
     * Информационное сообщение.
     */
    private final String text;

    public RemoveCardCommand(String text) {
        this.text = text;
    }

    @JsonGetter("text")
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoveCardCommand that = (RemoveCardCommand) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "RemoveCardCommand{" +
                "text='" + text + '\'' +
                '}';
    }
}
