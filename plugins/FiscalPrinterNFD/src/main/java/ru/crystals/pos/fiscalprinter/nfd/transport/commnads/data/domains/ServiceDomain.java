package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains;

public class ServiceDomain extends CommonDomain {

    /**
     * Номер счета за оказанную услугу.
     */
    private String accountNumberFieldName = "accountNumber";


    public ServiceDomain(String accountNumber) {
        type = "emul:ServiceDomain";
        setAccountNumber(accountNumber);
    }

    public String getAccountNumber() {
        return (String) getParam(accountNumberFieldName);
    }

    public void setAccountNumber(String accountNumber) {
        addParam(accountNumberFieldName, accountNumber);
    }

}
