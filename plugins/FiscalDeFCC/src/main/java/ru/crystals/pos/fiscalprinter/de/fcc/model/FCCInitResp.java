package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.util.List;

/**
 *
 * @author dalex
 */
public class FCCInitResp {

    private List<String> serialNumbers;
    private String description;

    public FCCInitResp() {
    }

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(List<String> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
