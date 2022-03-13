package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * При получении данного события программное обеспечение ECR  должно отобразить полученное текстовое сообщение на экране ECR с возможностью выбора кассиром переданных
 * опций.
 */
public class OnSelectEvent implements EventProperties {

    /**
     * Текстовое сообщение, которое будет отображаться на экране ECR
     */
    private final String displayText;

    /**
     * Список опций, которые необходимо отобразить на экране ECR
     */
    private final List<String> options;

    @JsonCreator
    public OnSelectEvent(@JsonProperty("displayText") String displayText,
                         @JsonProperty("options") List<String> options) {
        this.displayText = displayText;
        this.options = options;
    }

    public String getDisplayText() {
        return displayText;
    }

    public List<String> getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnSelectEvent that = (OnSelectEvent) o;
        return Objects.equals(displayText, that.displayText) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText, options);
    }

    @Override
    public String toString() {
        return "OnSelectEvent{" +
                "displayText='" + displayText + '\'' +
                ", options=" + options +
                '}';
    }
}
