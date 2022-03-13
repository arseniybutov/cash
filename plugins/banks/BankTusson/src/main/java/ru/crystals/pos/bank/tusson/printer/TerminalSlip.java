package ru.crystals.pos.bank.tusson.printer;

import java.util.List;

public class TerminalSlip {
    private DocumentType documentType;
    private List<String> content;

    public TerminalSlip(DocumentType documentType, List<String> content) {
        this.documentType = documentType;
        this.content = content;
    }

    public TerminalSlip() {
        //
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
