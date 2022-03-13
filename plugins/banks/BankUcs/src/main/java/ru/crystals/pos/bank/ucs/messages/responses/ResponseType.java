package ru.crystals.pos.bank.ucs.messages.responses;

public enum ResponseType {

    FINALIZE_DAY_TOTALS_RESPONSE("22"),

    LOGIN_RESPONSE("31", LoginResponse.class),
    PRINT_LINE("32", PrintLineResponse.class),
    AUTHORIZATION_RESPONSE("60", AuthorizationResponse.class),

    INITIAL_OK_RESPONSE("50"),
    INITIAL_REQUIRES_LOGIN_FIRST_RESPONSE("51"),
    PIN_ENTRY_REQUIRED("52"),
    ONLINE_AUTHORISATION_REQUIRED("53"),
    INITIAL_NO_PREVIOUS_TRANSACTION_WITH_SUCH_REF("54"),

    INITIAL_ERROR_RESPONSE("5X", InitialErrorResponse.class),

    HOLD("55"),
    CONSOLE_MESSAGE("5M", ConsoleMessageResponse.class),

    UNKNOWN("00", UnknownResponse.class),

    ;

    private String classAndCode;
    private Class<? extends Response> implementationClass = SimpleWithoutDataResponse.class;

    ResponseType(String classAndCode) {
        this.classAndCode = classAndCode;
    }

    ResponseType(String classAndCode, Class<? extends Response> implementationClass) {
        this.classAndCode = classAndCode;
        this.implementationClass = implementationClass;
    }

    public String getClassAndCode() {
        return classAndCode;
    }

    public Class<? extends Response> getImplementationClass() {
        return implementationClass;
    }

    public static ResponseType getType(String substring) {
        for (ResponseType type : ResponseType.values()) {
            if (substring.equalsIgnoreCase(type.getClassAndCode())) {
                return type;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return classAndCode + " (" + name() + ")";
    }
}
