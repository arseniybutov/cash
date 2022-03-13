package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents.DocumentRequest;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.DocumentResponse;

public class CreateDocumentCommand extends BaseCommand<DocumentRequest, DocumentResponse> {

    public CreateDocumentCommand() {
        getRequest().setOperationId("createDocument");
    }

    public CreateDocumentCommand(DocumentRequest document) {
        this();
        setParameters(document);
    }

    public void setDocumentRequest(DocumentRequest document) {
        setParameters(document);
    }

    public Class<DocumentResponse> getResponseDataClass() {
        return DocumentResponse.class;
    }
}
