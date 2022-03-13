package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.ApiVersionResponse;

/**
 * Версия протокола CBS
 */
public class GetApiVersion extends BaseRequest {
    @Override
    public String getTarget() {
        return "/api/version";
    }
    @Override
    public Class<ApiVersionResponse> getResponseClass() {
        return ApiVersionResponse.class;
    }
}
