package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class Receipt {

    @JsonProperty("ReceivedCash")
    private Long receivedCash;
    @JsonProperty("Time")
    private LocalDateTime time;
    @JsonProperty("Items")
    private List<Item> items;
    @JsonProperty("ReceivedCard")
    private Long receivedCard;

    public Long getReceivedCash() {
        return receivedCash;
    }

    public void setReceivedCash(Long receivedCash) {
        this.receivedCash = receivedCash;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Long getReceivedCard() {
        return receivedCard;
    }

    public void setReceivedCard(Long receivedCard) {
        this.receivedCard = receivedCard;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receivedCash=" + receivedCash +
                ", time=" + time +
                ", items=" + items +
                ", receivedCard=" + receivedCard +
                '}';
    }
}
