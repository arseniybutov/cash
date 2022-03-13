package ru.crystals.pos.fiscalprinter.az.airconn.model.requests;

public class BaseRequest {

    private final Integer version = 1;
    private Object parameters;
    private String operationId;

    public Object getParameters() {
        return parameters;
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Integer getVersion() {
        return version;
    }
}
