package ru.crystals.pos.bank.opensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

public class PaymentInfoResponse {
    private List<Data> data;

    @JsonCreator
    public PaymentInfoResponse(@JsonProperty("data") List<Data> data) {
        this.data = data;
    }

    public String getMessage() {
        if (isNull(data)) {
            return "Have no info about payment response";
        } else {
            return data.get(0).getInfoMessage();
        }
    }


    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentInfoResponse that = (PaymentInfoResponse) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "PaymentInfoResponse{" +
                "data=" + data +
                '}';
    }
}
