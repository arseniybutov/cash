package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.Error;

/**
 * Базовй класс Http ответа от CBS, содержит общие поля с данными об ошибках.
 */
public class BaseResponse {

    /**
     * Результат выполнения запроса
     */
    @JsonProperty("error")
    private Integer error;
    /**
     * Текст ошибки понятный для человека. Присутствует если error != API_ERROR_NO
     */
    @JsonProperty("error_text")
    private String errorText;

    public Error getError() {
        return Error.getErrorByCode(error);
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
