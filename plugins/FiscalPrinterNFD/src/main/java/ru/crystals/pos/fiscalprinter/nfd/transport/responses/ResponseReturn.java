package ru.crystals.pos.fiscalprinter.nfd.transport.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ResponseReturn {

    @JacksonXmlProperty(localName = "resultObject")
    protected BaseResultObject resultObject;

    @JacksonXmlProperty(localName = "code")
    private int code;

    @JacksonXmlProperty(localName = "message")
    private String message;

    public ResponseReturn() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BaseResultObject getResultObject() {
        return resultObject;
    }

    public void setResultObject(BaseResultObject resultObject) {
        this.resultObject = resultObject;
    }

    @Override
    public String toString() {
        return "Return{" +
                "resultObject=" + resultObject +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
