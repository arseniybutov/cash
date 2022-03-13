package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ZReportsStats extends BaseResponse {
    @JsonProperty("TotalClosedUnAckZReportsCount")
    private Long totalClosedUnAckZReportsCount;
    @JsonProperty("LastUnAckZReportsNumbers")
    private List<Integer> lastUnAckZReportsNumbers;
    @JsonProperty("TotalClosedZReportsCount")
    private Long totalClosedZReportsCount;

    public Long getTotalClosedUnAckZReportsCount() {
        return totalClosedUnAckZReportsCount;
    }

    public void setTotalClosedUnAckZReportsCount(Long totalClosedUnAckZReportsCount) {
        this.totalClosedUnAckZReportsCount = totalClosedUnAckZReportsCount;
    }

    public List<Integer> getLastUnAckZReportsNumbers() {
        return lastUnAckZReportsNumbers;
    }

    public void setLastUnAckZReportsNumbers(List<Integer> lastUnAckZReportsNumbers) {
        this.lastUnAckZReportsNumbers = lastUnAckZReportsNumbers;
    }

    public Long getTotalClosedZReportsCount() {
        return totalClosedZReportsCount;
    }

    public void setTotalClosedZReportsCount(Long totalClosedZReportsCount) {
        this.totalClosedZReportsCount = totalClosedZReportsCount;
    }

    @Override
    public String toString() {
        return "ZReportsStats{" +
                "totalClosedUnAckZReportsCount=" + totalClosedUnAckZReportsCount +
                ", lastUnAckZReportsNumbers=" + lastUnAckZReportsNumbers +
                ", totalClosedZReportsCount=" + totalClosedZReportsCount +
                '}';
    }
}
