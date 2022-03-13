package ru.crystals.pos.fiscalprinter.sp402frk.support;


import org.apache.commons.lang.StringUtils;

/**
 * Хранит данные ККТ не изменяющиеся в процессе работы кассы
 */
public class KKTDataStorage {

    /**
     * ИНН пользователя
     */
    private String deviceINN;
    /**
     * регистрационный номер ККТ
     */
    private String registrationNum;
    /**
     * заводской номер ККТ
     */
    private String factoryNum;
    /**
     * номер фискального накопителя
     */
    private String fnNumber;
    /**
     * версия прошивки
     */
    private String firmwareVer;
    /**
     * дата последней регистрации/перерегистрации
     */
    private String registrationDate;

    public void setDeviceINN(String deviceINN) {
        this.deviceINN = deviceINN;
    }

    public void setRegistrationNum(String registrationNum) {
        this.registrationNum = registrationNum;
    }

    public void setFactoryNum(String factoryNum) {
        this.factoryNum = factoryNum;
    }

    public void setFnNumber(String fnNumber) {
        this.fnNumber = fnNumber;
    }

    public void setFirmwareVer(String firmwareVer) {
        this.firmwareVer = firmwareVer;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getDeviceINN() {
        return deviceINN;
    }

    public String getRegistrationNum() {
        return registrationNum;
    }

    public String getFactoryNum() {
        return factoryNum;
    }

    public String getFnNumber() {
        return fnNumber;
    }

    public String getFirmwareVer() {
        return firmwareVer;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public boolean isDeviceINNEmpty() {
        return StringUtils.isEmpty(deviceINN);
    }

    public boolean isRegistrationNumEmpty() {
        return StringUtils.isEmpty(registrationNum);
    }

    public boolean isFactoryNumEmpty() {
        return StringUtils.isEmpty(factoryNum);
    }

    public boolean isFnNumberEmpty() {
        return StringUtils.isEmpty(fnNumber);
    }

    public boolean isFirmwareVerEmpty() {
        return StringUtils.isEmpty(firmwareVer);
    }

    public boolean isRegistrationDateEmpty() {
        return StringUtils.isEmpty(registrationDate);
    }
}
