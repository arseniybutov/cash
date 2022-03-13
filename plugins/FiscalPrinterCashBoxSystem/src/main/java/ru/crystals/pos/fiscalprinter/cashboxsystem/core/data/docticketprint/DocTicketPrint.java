package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описывает поля чекового документа для печати
 */
public class DocTicketPrint {
    @JsonProperty("header")
    private Header header;
    @JsonProperty("body")
    private Body body;
    @JsonProperty("footer")
    private Footer footer;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Footer getFooter() {
        return footer;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }
}
