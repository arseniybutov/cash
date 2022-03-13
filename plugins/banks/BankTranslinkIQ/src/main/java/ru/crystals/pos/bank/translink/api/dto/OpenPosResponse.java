package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

@JsonDeserialize(using = OpenPosResponseDeserilizer.class)
public class OpenPosResponse {

    public static final String ACCESS_TOKEN_FIELD = "accessToken";

    /**
     * Идентификатор сессии. Данное значение необходимо передавать в заголовке HTTP запроса методов getevent, executeposcmd
     */
    private final String accessToken;

    private final Result result;

    public OpenPosResponse(String accessToken, Result result) {
        this.accessToken = accessToken;
        this.result = result;
    }

    public OpenPosResponse(String accessToken) {
        this(accessToken, Result.OK);
    }

    public OpenPosResponse(Result result) {
        this(null, result);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenPosResponse that = (OpenPosResponse) o;
        return Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, result);
    }

    @Override
    public String toString() {
        return "OpenPosResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", result=" + result +
                '}';
    }
}
