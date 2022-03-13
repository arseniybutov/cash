package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * При получении данного события программное обеспечение ECR должно отобразить полученное текстовое сообщение   в экране ECR.
 */
public class OnDisplayTextEvent implements EventProperties {

    /**
     * Текст, который должен отображается на экране ECR.
     */
    private final String displayText;

    @JsonCreator
    public OnDisplayTextEvent(@JsonProperty("displayText") String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnDisplayTextEvent that = (OnDisplayTextEvent) o;
        return Objects.equals(displayText, that.displayText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText);
    }

    @Override
    public String toString() {
        return "OnDisplayTextEvent{" +
                "displayText='" + displayText + '\'' +
                '}';
    }
}
