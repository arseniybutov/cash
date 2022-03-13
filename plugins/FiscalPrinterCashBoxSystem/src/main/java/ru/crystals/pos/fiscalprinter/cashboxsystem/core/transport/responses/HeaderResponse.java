package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.DocHeader;

/**
 * Ответ на запрос печати заголовка с реквизитами
 */
public class HeaderResponse extends BaseResponse {
    /**
     * Документ с реквизитами
     */
    @JsonProperty("data")
    private DocHeader docHeader;

    public DocHeader getDocHeader() {
        return docHeader;
    }

    public void setDocHeader(DocHeader docHeader) {
        this.docHeader = docHeader;
    }
}
