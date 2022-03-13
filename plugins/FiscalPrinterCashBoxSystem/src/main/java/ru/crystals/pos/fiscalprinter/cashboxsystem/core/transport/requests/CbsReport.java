package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.ReportResponse;

/**
 * Базовый класс для запроса отчетов.
 */
public abstract class CbsReport extends BaseRequest {

    /**
     * Не печатать документ (по умолчанию false)
     */
    @JsonProperty("not_print")
    private Boolean notPrint;
    /**
     * Имя оператора
     */
    @JsonProperty("operator_name")
    private String operatorName;
    /**
     * Код оператора
     */
    @JsonProperty("operator_code")
    private Long operatorCode;
    /**
     * Номер документа, который будет на чеке
     */
    @JsonProperty("document_number")
    private Long documentNumber;
    /**
     * Номер кассового места, который будет на чеке
     */
    @JsonProperty("kkm_pos")
    private Integer kkmPos;
    /**
     * Номер смены, который будет на чеке
     */
    @JsonProperty("shift_number")
    private Integer shiftNumber;
    /**
     * Порядковый номер запроса для предотвращения дублирования запросов. Значение должно быть больше 0 и отличаться от предыдущего значения.
     */
    @JsonProperty("reqnum")
    private Long reqNum;

    @Override
    public Class<ReportResponse> getResponseClass() {
        return ReportResponse.class;
    }

    public Boolean getNotPrint() {
        return notPrint;
    }

    public void setNotPrint(Boolean notPrint) {
        this.notPrint = notPrint;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(Long operatorCode) {
        this.operatorCode = operatorCode;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getKkmPos() {
        return kkmPos;
    }

    public void setKkmPos(Integer kkmPos) {
        this.kkmPos = kkmPos;
    }

    public Integer getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(Integer shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public Long getReqNum() {
        return reqNum;
    }

    public void setReqNum(Long reqNum) {
        this.reqNum = reqNum;
    }
}
