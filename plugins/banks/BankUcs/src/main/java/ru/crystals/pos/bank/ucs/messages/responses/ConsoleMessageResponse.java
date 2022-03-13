package ru.crystals.pos.bank.ucs.messages.responses;

public class ConsoleMessageResponse extends Response {
    private String message;

    public ConsoleMessageResponse(String response) {
        super(response);
        if (getData() != null) {
            message = getData();
        }
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void setLoggableFields() {
        getLoggerUtil().add("message", getMessage());
    }
}
