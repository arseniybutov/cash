package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AuditData;

public class AuditCommand extends BaseCommand<AuditData, Object> {

    public AuditCommand(AuditData auditData) {
        getRequest().setOperationId("audit");
        setParameters(auditData);
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
