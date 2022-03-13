package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint.DocTicketPrint;

/**
 * Ответ на команду Продажы, Возврата продажи, Покупки Возврата покупки
 */
public class TicketResponse extends BaseResponse {

    /**
     * Документ для печати с реквизитами и данными чека
     */
    @JsonProperty("data")
    private DocTicketPrint docTicketPrint;

    public DocTicketPrint getDocTicketPrint() {
        return docTicketPrint;
    }

    public void setDocTicketPrint(DocTicketPrint docTicketPrint) {
        this.docTicketPrint = docTicketPrint;
    }
}
