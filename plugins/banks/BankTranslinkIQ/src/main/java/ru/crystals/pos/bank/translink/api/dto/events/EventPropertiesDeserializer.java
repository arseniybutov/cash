package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EventPropertiesDeserializer extends JsonDeserializer<BaseEvent> {

    private static final Logger log = LoggerFactory.getLogger(EventPropertiesDeserializer.class);

    @Override
    public BaseEvent deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        final JsonNode json = p.getCodec().readTree(p);
        final JsonNode eventName = json.get(BaseEvent.EVENT_NAME_FIELD);
        if (eventName == null || eventName.isNull()) {
            return BaseEvent.NO_MORE_EVENTS;
        }
        final EventType eventType = p.getCodec().readValue(eventName.traverse(), EventType.class);
        if (eventType == EventType.UNKNOWN) {
            log.error("Unsupported event ({}): {}", eventName, json);
            return BaseEvent.UNKNOWN_EVENT;
        }
        final JsonNode properties = json.get(BaseEvent.PROPERTIES_FIELD);
        if (properties == null) {
            return new BaseEvent(eventType);
        }
        final Class<? extends EventProperties> payloadClass = eventType.getPayloadClass();
        if (payloadClass == null) {
            log.warn("Not expected payload for event {}: {}", eventType, properties);
            return new BaseEvent(eventType);
        }
        final EventProperties eventProperties = p.getCodec().readValue(properties.traverse(), payloadClass);
        return new BaseEvent(eventType, eventProperties);
    }
}
