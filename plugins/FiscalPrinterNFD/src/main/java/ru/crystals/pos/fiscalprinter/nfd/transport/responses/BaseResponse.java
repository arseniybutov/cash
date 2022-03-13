package ru.crystals.pos.fiscalprinter.nfd.transport.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public abstract class BaseResponse {

    @JacksonXmlProperty(isAttribute = true)
    private int type;

    @JacksonXmlProperty(localName = "return")
    private ResponseReturn returnField;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ResponseReturn getReturn() {
        return returnField;
    }

    public void setReturnField(ResponseReturn returnField) {
        this.returnField = returnField;
    }

    @Override
    public String toString() {
        return "Response:" +
                "return=" + returnField +
                '}';
    }
}
