package ru.crystals.pos.bank.opensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.opensbp.api.status.RefundStatus;

import java.util.Objects;

public class ResponseStatus {
    /**
     * Идентификатор статуса
     */
    private RefundStatus id;

    /**
     * Наименование статуса
     */
    private String name;

    /**
     * Описание статуса
     */
    private String description;

    @JsonCreator
    public ResponseStatus(@JsonProperty("id") RefundStatus id,
                          @JsonProperty("name") String name,
                          @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public RefundStatus getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResponseStatus status = (ResponseStatus) o;
        return id == status.id && Objects.equals(name, status.name) && Objects.equals(description, status.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", name='" + name +
                ", description='" + description +
                '}';
    }
}
