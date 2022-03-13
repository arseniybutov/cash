package ru.crystals.pos.bank.belinvest;

public enum ServiceBankOperationType {
    GET_BALANCE(ResBundleBankBelInvest.getString("GET_BALANCE_COMMAND_TITLE"),
            ResBundleBankBelInvest.getString("GET_BALANCE_FORM_TITLE"), ResBundleBankBelInvest.getString("GET_BALANCE_SPINNER_MESSAGE"),
            "Balance", "BYN"),
    GET_DAY_REPORT(ResBundleBankBelInvest.getString("GET_DAY_REPORT_COMMAND_TITLE"),
            ResBundleBankBelInvest.getString("GET_DAY_REPORT_FORM_TITLE"), ResBundleBankBelInvest.getString("GET_DAY_REPORT_SPINNER_MESSAGE"),
            "DayReport", "ChequeFull"),
    TEST_CONNECT(ResBundleBankBelInvest.getString("TEST_CONNECT_COMMAND_TITLE"),
            ResBundleBankBelInvest.getString("TEST_CONNECT_FORM_TITLE"), ResBundleBankBelInvest.getString("TEST_CONNECT_SPINNER_MESSAGE"),
            "TestBank"),
    GET_DAY_LOG("DayLog", "DayLog", "DayLog", "CloseDay", "ChequeFull");
    private String commandTitle;
    private String formTitle;
    private String spinnerMessage;
    private String[] params;

    ServiceBankOperationType(String commandTitle, String formTitle, String spinnerMessage, String... params) {
        this.params = params;
        this.commandTitle = commandTitle;
        this.formTitle = formTitle;
        this.spinnerMessage = spinnerMessage;
    }

    public String getFormTitle() {
        return formTitle;
    }

    public String getSpinnerMessage() {
        return spinnerMessage;
    }

    public String getCommandTitle() {
        return commandTitle;
    }

    public String[] getParams() {
        return params;
    }
}
