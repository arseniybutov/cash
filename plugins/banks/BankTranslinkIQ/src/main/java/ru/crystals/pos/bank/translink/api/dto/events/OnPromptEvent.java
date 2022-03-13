package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * При получении данного сообщения программное обеспечение ECR должно отобразить сообщение с возможностью ввода информации кассиром.
 */
public class OnPromptEvent implements EventProperties {

    /**
     * Текст, который должен отображается на экране ECR.
     */
    private final String text;

    /**
     * маска ввода запрашиваемой информации.
     */
    private final String inputMask;

    @JsonCreator
    public OnPromptEvent(@JsonProperty("text") String text,
                         @JsonProperty("inputMask") String inputMask) {
        this.text = text;
        this.inputMask = inputMask;
    }

    public String getText() {
        return text;
    }

    public String getInputMask() {
        return inputMask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnPromptEvent that = (OnPromptEvent) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(inputMask, that.inputMask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, inputMask);
    }

    @Override
    public String toString() {
        return "OnPromptEvent{" +
                "text='" + text + '\'' +
                ", inputMask='" + inputMask + '\'' +
                '}';
    }
}
