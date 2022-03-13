package ru.crystals.pos.bank.odengiqr.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import ru.crystals.pos.bank.odengiqr.api.dto.request.create.Views;
import ru.crystals.pos.bank.odengiqr.api.dto.response.cancel.InvoiceCancelRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.create.CreateInvoiceRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.status.StatusPaymentRs;

import java.time.Instant;

/**
 * Общие данные запроса/ответа
 */
public class RequestResponseContainer {

    /**
     * Версия API
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("version")
    private int version = 1005;

    /**
     * Язык интерфейса
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("lang")
    private String lang;

    /**
     * Команда
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("cmd")
    private String command;

    /**
     * Уникальный идентификатор торговца
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("sid")
    private String sellerID;

    /**
     * Данные запроса
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("data")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "cmd", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = CreateInvoiceRs.class, name = "createInvoice"),
            @JsonSubTypes.Type(value = StatusPaymentRs.class, name = "statusPayment"),
            @JsonSubTypes.Type(value = InvoiceCancelRs.class, name = "invoiceCancel")
    })
    private Data data;

    /**
     * Дата и время в микросекундах
     */
    @JsonView(Views.WithoutHash.class)
    @JsonProperty("mktime")
    private Instant mkTime;

    /**
     * Подпись запроса
     *
     * ВАЖНО! Формируется из JSON с остальными полями запроса - генерится с помощью Hmac MD5 используя пароль.
     */
    @JsonView(Views.WithHash.class)
    @JsonProperty("hash")
    private String hash;

    public RequestResponseContainer() {
    }

    public RequestResponseContainer(Data data) {
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Instant getMkTime() {
        return mkTime;
    }

    public void setMkTime(Instant mkTime) {
        this.mkTime = mkTime;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
