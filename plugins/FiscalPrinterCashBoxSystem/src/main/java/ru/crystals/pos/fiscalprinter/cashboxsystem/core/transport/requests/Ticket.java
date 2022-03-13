package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.TicketItem;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses.TicketResponse;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос Продажы/Возврата продажи/Покупки/Возврата покупки
 */
public class Ticket extends BaseRequest {

    @JsonIgnore
    protected CheckType type;

    /**
     * Список позиций чека. Должен иметь хотя бы одну позицию.
     */
    @JsonProperty("items")
    private List<TicketItem> items = new ArrayList<>();
    /**
     * Оплата наличными
     */
    @JsonProperty("payment_cash")
    private CbsMoney paymentCash;
    /**
     * Оплата банковской картой
     */
    @JsonProperty("payment_debit")
    private CbsMoney paymentDebit;
    /**
     * Оплата тарой
     */
    @JsonProperty("payment_tare")
    private CbsMoney paymentTare;
    /**
     * Оплата кредитом
     */
    @JsonProperty("payment_credit")
    private CbsMoney paymentCredit;
    /**
     * Штрихкод на чеке
     */
    @JsonProperty("barcode")
    private String barcode;
    /**
     * Список дополнительных строк для печати после итогов на чеке.
     */
    @JsonProperty("additional_text_print")
    private List<String> additionalTextPrint;
    /**
     * E-mail для отправки чека или его копии. (только при разворачивании на сервере)
     */
    @JsonProperty("email")
    private String email;
    /**
     * Назначение параметра в документации не описано, предположительно включает округление
     */
    @JsonProperty("around_total")
    private Boolean aroundTotal;
    /**
     * Не печатать документ (по умолчанию false).
     */
    @JsonProperty("not_print")
    private Boolean notPrint;
    /**
     * Имя оператора.
     */
    @JsonProperty("operator_name")
    private String operatorName;
    /**
     * Код оператора.
     */
    @JsonProperty("operator_code")
    private Long operatorCode;
    /**
     * Номер документа, который будет на чеке.
     */
    @JsonProperty("document_number")
    private Long documentNumber;
    /**
     * Номер кассового места, который будет на чеке.
     */
    @JsonProperty("kkm_pos")
    private Integer kkmPos;
    /**
     * Номер смены, который будет на чеке.
     */
    @JsonProperty("shift_number")
    private Integer shiftNumber;
    /**
     * Порядковый номер запроса для предотвращения дублирования запросов. Значение должно быть больше 0 и отличаться от предыдущего значения.
     */
    @JsonProperty("reqnum")
    private Integer reqnum;

    public Ticket(CheckType type) {
        this.type = type;
    }

    @Override
    public Class<TicketResponse> getResponseClass() {
        return TicketResponse.class;
    }
    @Override
    public String getTarget() {
        return type == CheckType.SALE ? "/api/ticket/sale" : "/api/ticket/sale-return";
    }

    public void addItem(TicketItem item) {
        items.add(item);
    }

    public List<TicketItem> getItems() {
        return items;
    }

    public void setItems(List<TicketItem> items) {
        this.items = items;
    }

    public void addPaymentCash(Long sum) {
        if (paymentCash == null) {
            paymentCash = new CbsMoney();
        }
        paymentCash.addMoneyFromLong(sum);
    }

    public CbsMoney getPaymentCash() {
        return paymentCash;
    }

    public void setPaymentCash(CbsMoney paymentCash) {
        this.paymentCash = paymentCash;
    }

    public void addPaymentDebit(Long sum) {
        if (paymentDebit == null) {
            paymentDebit = new CbsMoney();
        }
        paymentDebit.addMoneyFromLong(sum);
    }

    public CbsMoney getPaymentDebit() {
        return paymentDebit;
    }

    public void setPaymentDebit(CbsMoney paymentDebit) {
        this.paymentDebit = paymentDebit;
    }

    public void addPaymentTare(Long sum) {
        if (paymentTare == null) {
            paymentTare = new CbsMoney();
        }
        paymentTare.addMoneyFromLong(sum);
    }

    public CbsMoney getPaymentTare() {
        return paymentTare;
    }

    public void setPaymentTare(CbsMoney paymentTare) {
        this.paymentTare = paymentTare;
    }

    public void addPaymentCredit(Long sum) {
        if (paymentCredit == null) {
            paymentCredit = new CbsMoney();
        }
        paymentCredit.addMoneyFromLong(sum);
    }

    public CbsMoney getPaymentCredit() {
        return paymentCredit;
    }

    public void setPaymentCredit(CbsMoney paymentCredit) {
        this.paymentCredit = paymentCredit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void addAdditionalTextPrint(String text) {
        //CBS зависает если передать текст в чеке additionalTextPrint.add
    }

    public List<String> getAdditionalTextPrint() {
        return additionalTextPrint;
    }

    public void setAdditionalTextPrint(List<String> additionalTextPrint) {
        this.additionalTextPrint = additionalTextPrint;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAroundTotal() {
        return aroundTotal;
    }

    public void setAroundTotal(Boolean aroundTotal) {
        this.aroundTotal = aroundTotal;
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
        //CBS выдает ошибку: Параметр 'kkm_pos' должены быть числом!, хотя при этом ему передается число
    }

    public Integer getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(Integer shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public Integer getReqnum() {
        return reqnum;
    }

    public void setReqnum(Integer reqnum) {
        this.reqnum = reqnum;
    }
}
