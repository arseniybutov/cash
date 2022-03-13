package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docserviceticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Описывает документ служебного чека внесения/изъятия наличных в кассе
 */
public class DocServiceTicketPrint {
    /**
     * Объект заголовка с реквизитами
     */
    @JsonProperty("header")
    private Header header;
    /**
     * Объект тела. Содержит сумму операции.
     */
    @JsonProperty("body")
    private Body body;
    /**
     * Объект итогов
     */
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
