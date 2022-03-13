package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("id")
    private String id;

    @JsonProperty("caption")
    private String caption;

    public User() {
    }

    public User(final String id, final String caption) {
        this.id = id;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }
}

