package ru.crystals.pos.bank.zvt.protocol;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ZVTResponse {

    private String responseCode;

    private Map<TransactionField, String> fields = Collections.emptyMap();


    public ZVTResponse(String responseCode) {
        this.responseCode = responseCode;
    }

    public ZVTResponse(Map<TransactionField, String> fields) {
        this.fields = fields;
        responseCode = fields.getOrDefault(TransactionField.RESULT_CODE, "");
    }

    public ZVTResponse(String responseCode, Map<TransactionField, String> fields) {
        this.responseCode = responseCode;
        this.fields = fields;
    }

    public boolean isSuccessful() {
        return ResponseCodes.OK.equals(responseCode);

    }

    public String getResponseCode() {
        return responseCode;
    }

    public Map<TransactionField, String> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZVTResponse that = (ZVTResponse) o;
        return Objects.equals(responseCode, that.responseCode) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseCode, fields);
    }

    @Override
    public String toString() {
        return "ZVTResponse{" +
                "responseCode='" + responseCode + '\'' +
                ", fields=" + fields +
                '}';
    }
}
