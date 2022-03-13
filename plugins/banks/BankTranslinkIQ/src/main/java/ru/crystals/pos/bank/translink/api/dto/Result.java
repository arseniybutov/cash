package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Result {

    public static final String RESULT_CODE_FIELD = "resultCode";
    public static final Result OK = new Result(ResultCode.OK, "Success");

    /**
     * Код результата выполнения команды
     */
    private final ResultCode resultCode;
    /**
     * Текстовое описание результата выполнения команды
     */
    private final String resultMessage;

    @JsonCreator
    public Result(@JsonProperty(RESULT_CODE_FIELD) ResultCode resultCode,
                  @JsonProperty("resultMessage") String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result that = (Result) o;
        return Objects.equals(resultCode, that.resultCode) &&
                Objects.equals(resultMessage, that.resultMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultCode, resultMessage);
    }

    @Override
    public String toString() {
        return resultCode.name() + "(" + resultMessage + ")";
    }
}
