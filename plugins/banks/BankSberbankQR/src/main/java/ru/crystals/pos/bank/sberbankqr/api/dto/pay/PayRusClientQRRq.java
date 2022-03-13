package ru.crystals.pos.bank.sberbankqr.api.dto.pay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

/**
 * Запрос на проведение операции оплаты
 */
public class PayRusClientQRRq {

    /**
     * Уникальный ID запроса, формируемый инициатором
     */
    @JsonProperty("rq_uid")
    private String rqUID;

    /**
     * Время формирования операции (запроса)
     */
    @JsonProperty("rq_tm")
    private ZonedDateTime rqTm;

    /**
     * ID партнера, формируется Сбербанком при регистрации в системе
     */
    @JsonProperty("member_id")
    private String memberId;

    /**
     * Номер заказа в CRM Клиента
     */
    @JsonProperty("partner_order_number")
    private String partnerOrderNumber;

    /**
     * Уникальный идентификатор терминала
     */
    @JsonProperty("tid")
    private String terminalId;

    /**
     * IdQR устройства, на котором сформирован заказ
     */
    @JsonProperty("id_qr")
    private String idQR;

    /**
     * PayLoad, полученный при сканировании QR кода, отображаемого Клиентом ФЛ в СБОЛ.
     */
    @JsonProperty("pay_load")
    private String payLoad;

    /**
     * Сумма заказа в минимальных единицах Валюты
     */
    private int amount;

    /**
     * Валюта операции согласно ISO 4217, цифровой код
     */
    private String currency;

    /**
     * Сообщение при платеже, для отображения ТСТ (при наличии технической возможности)
     */
    @JsonProperty("operation_message")
    private String operationMessage;

    public String getRqUID() {
        return rqUID;
    }

    public void setRqUID(String rqUID) {
        this.rqUID = rqUID;
    }

    public ZonedDateTime getRqTm() {
        return rqTm;
    }

    public void setRqTm(ZonedDateTime rqTm) {
        this.rqTm = rqTm;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPartnerOrderNumber() {
        return partnerOrderNumber;
    }

    public void setPartnerOrderNumber(String partnerOrderNumber) {
        this.partnerOrderNumber = partnerOrderNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getIdQR() {
        return idQR;
    }

    public void setIdQR(String idQR) {
        this.idQR = idQR;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOperationMessage() {
        return operationMessage;
    }

    public void setOperationMessage(String operationMessage) {
        this.operationMessage = operationMessage;
    }
}
