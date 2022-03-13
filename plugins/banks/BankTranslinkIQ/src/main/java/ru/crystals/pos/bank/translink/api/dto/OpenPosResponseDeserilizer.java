package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class OpenPosResponseDeserilizer extends JsonDeserializer<OpenPosResponse> {
    @Override
    public OpenPosResponse deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final JsonNode json = p.getCodec().readTree(p);
        final JsonNode accessToken = json.get(OpenPosResponse.ACCESS_TOKEN_FIELD);
        if (accessToken != null && accessToken.isTextual()) {
            return new OpenPosResponse(accessToken.asText());
        }
        final JsonNode resultCode = json.get(Result.RESULT_CODE_FIELD);
        if (resultCode != null) {
            return new OpenPosResponse(p.getCodec().treeToValue(json, Result.class));
        }
        return new OpenPosResponse(new Result(ResultCode.UNKNOWN, "Invalid response"));
    }
}
