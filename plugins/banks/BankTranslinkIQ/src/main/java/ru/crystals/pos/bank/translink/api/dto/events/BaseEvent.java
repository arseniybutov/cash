package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

@JsonDeserialize(using = EventPropertiesDeserializer.class)
public class BaseEvent {

    public static final String EVENT_NAME_FIELD = "eventName";
    public static final String PROPERTIES_FIELD = "properties";

    public static final BaseEvent NO_MORE_EVENTS = new BaseEvent(EventType.NO_MORE_EVENTS);
    public static final BaseEvent UNKNOWN_EVENT = new BaseEvent(EventType.UNKNOWN);

    private final EventType eventType;

    private final EventProperties properties;

    public BaseEvent(EventType eventType, EventProperties properties) {
        this.eventType = eventType;
        this.properties = properties;
    }

    public BaseEvent(final EventType eventType) {
        this(eventType, null);
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventProperties getProperties() {
        return properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BaseEvent baseEvent = (BaseEvent) o;
        return Objects.equals(eventType, baseEvent.eventType) &&
                Objects.equals(properties, baseEvent.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, properties);
    }

    @Override
    public String toString() {
        return eventType.name() + (properties != null ? "(" + properties + ")" : "");
    }

    public boolean noMoreEvents() {
        return getEventType() == EventType.NO_MORE_EVENTS;
    }
}
