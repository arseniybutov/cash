package ru.crystals.pos.bank.gascardservice;

public enum ServiceBankOperationType {
    GET_FULL_REPORT(ResBundleBankGasCardService.getString("FULL_REPORT_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("FULL_REPORT_FORM_TITLE"), ResBundleBankGasCardService.getString("FULL_REPORT_SPINNER_MESSAGE"),
            false, "6",
            "0", "0"),
    GET_SLIP_COPY(ResBundleBankGasCardService.getString("GET_SLIP_COPY_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("GET_SLIP_COPY_FORM_TITLE"), ResBundleBankGasCardService.getString("GET_SLIP_COPY_SPINNER_MESSAGE"),
            true, "32", "0"),
    UPDATE_TERMINAL_SOFTWARE(ResBundleBankGasCardService.getString("UPDATE_TERMINAL_SOFTWARE_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("UPDATE_TERMINAL_SOFTWARE_FORM_TITLE"),
            ResBundleBankGasCardService.getString("UPDATE_TERMINAL_SOFTWARE_SPINNER_MESSAGE"), false, "36", "0", "0"),
    GET_WORKING_KEY(ResBundleBankGasCardService.getString("GET_WORKING_KEY_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("GET_WORKING_KEY_FORM_TITLE"), ResBundleBankGasCardService.getString("GET_WORKING_KEY_SPINNER_MESSAGE"),
            false, "5", "0", "0"),
    LOAD_TERMINAL_PARAMS(ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_FORM_TITLE"),
            ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_SPINNER_MESSAGE"), false, "35", "0", "0"),
    LOAD_TERMINAL_PARAMS_AND_SOFTWARE(ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_AND_SOFTWARE_COMMAND_TITLE"),
            ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_AND_SOFTWARE_FORM_TITLE"),
            ResBundleBankGasCardService.getString("LOAD_TERMINAL_PARAMS_AND_SOFTWARE_SPINNER_MESSAGE"), false, "28", "0", "0");
    private String commandTitle;
    private String formTitle;
    private String spinnerMessage;
    private String[] params;
    private boolean needsInLastTransactionID;

    ServiceBankOperationType(String commandTitle, String formTitle, String spinnerMessage, boolean needsInLastTransactionID, String... params) {
        this.needsInLastTransactionID = needsInLastTransactionID;
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

    public boolean needsInLastTransactionID() {
        return needsInLastTransactionID;
    }
}
