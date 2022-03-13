package ru.crystals.pos.card.replacement.visualization.model;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardModelInfo {
    private String oldCardNumber;
    private String newCardNumber;
    private String contractId;
    private String applicationNumber;
    private String mobilePhone;

    public ReplaceCardModelInfo(String oldCardNumber) {
        this.oldCardNumber = oldCardNumber;
    }

    public String getOldCardNumber() {
        return oldCardNumber;
    }

    public void setOldCardNumber(String oldCardNumber) {
        this.oldCardNumber = oldCardNumber;
    }

    public String getNewCardNumber() {
        return newCardNumber;
    }

    public void setNewCardNumber(String newCardNumber) {
        this.newCardNumber = newCardNumber;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }
}
