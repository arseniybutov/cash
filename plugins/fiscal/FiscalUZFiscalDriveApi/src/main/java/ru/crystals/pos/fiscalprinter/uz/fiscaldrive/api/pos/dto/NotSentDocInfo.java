package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotSentDocInfo {
    private final LocalDateTime date;
    private final Long number;

    public NotSentDocInfo(LocalDateTime date, Long number) {
        this.date = date;
        this.number = number;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Long getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotSentDocInfo that = (NotSentDocInfo) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, number);
    }

    @Override
    public String toString() {
        return "NotSentDocInfo{" +
                "date=" + date +
                ", number=" + number +
                '}';
    }
}
