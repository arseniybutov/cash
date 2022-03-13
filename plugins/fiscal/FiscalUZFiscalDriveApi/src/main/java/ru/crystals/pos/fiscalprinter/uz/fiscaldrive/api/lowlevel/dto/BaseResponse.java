package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseResponse {

    @JsonProperty("AppletVersion")
    private String appletVersion;

    public String getAppletVersion() {
        return appletVersion;
    }

    public void setAppletVersion(String appletVersion) {
        this.appletVersion = appletVersion;
    }
}
