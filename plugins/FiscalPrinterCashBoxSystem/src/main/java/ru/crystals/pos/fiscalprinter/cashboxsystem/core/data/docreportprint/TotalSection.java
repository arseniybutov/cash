package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Итоги по секциям
 */
public class TotalSection {

    @JsonProperty("section_name")
    private String sectionName;

    @JsonProperty("total")
    private TotalOperations total;

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public TotalOperations getTotal() {
        return total;
    }

    public void setTotal(TotalOperations total) {
        this.total = total;
    }
}
