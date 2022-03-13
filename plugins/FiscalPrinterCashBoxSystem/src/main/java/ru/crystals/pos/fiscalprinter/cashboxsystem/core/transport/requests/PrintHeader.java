package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.HeaderResponse;

/**
 * Печать заголовка с реквизитами организации. Эти данные касса получает от ОФД.
 */
public class PrintHeader extends BaseRequest {

    /**
     * Не печатать документ (по умолчанию false)
     */
    @JsonProperty("not_print")
    private Boolean notPrint;
    /**
     * Порядковый номер запроса для предотвращения дублирования запросов. Значение должно быть больше 0 и отличаться от предыдущего значения.
     */
    @JsonProperty("reqnum")
    private Long reqNum;

    @Override
    public String getTarget() {
        return "/api/printer/print-header";
    }
    @Override
    public Class<HeaderResponse> getResponseClass() {
        return HeaderResponse.class;
    }

    public Boolean getNotPrint() {
        return notPrint;
    }

    public void setNotPrint(Boolean notPrint) {
        this.notPrint = notPrint;
    }

    public Long getReqNum() {
        return reqNum;
    }

    public void setReqNum(Long reqNum) {
        this.reqNum = reqNum;
    }
}
