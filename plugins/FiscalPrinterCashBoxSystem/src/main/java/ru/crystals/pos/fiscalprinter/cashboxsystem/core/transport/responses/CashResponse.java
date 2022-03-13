package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docserviceticketprint.DocServiceTicketPrint;

/**
 * Ответ на команду внесения/изъятия наличных в кассу
 */
public class CashResponse extends BaseResponse {

    /**
     * Документ для печати с реквизитами и данными служебного чека
     */
    @JsonProperty("data")
    private DocServiceTicketPrint docServiceTicketPrint;

    public DocServiceTicketPrint getDocServiceTicketPrint() {
        return docServiceTicketPrint;
    }

    public void setDocServiceTicketPrint(DocServiceTicketPrint docServiceTicketPrint) {
        this.docServiceTicketPrint = docServiceTicketPrint;
    }
}
