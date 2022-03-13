package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;


import org.apache.commons.lang.StringUtils;

/**
 * Хранит данные Штриха не изменяющиеся в процессе работы кассы
 */
public class ShtrihDataStorage {
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

    public void clearSavedData() {
        deviceINN = "";
        registrationNum = "";
        factoryNum = "";
    }

    public void setDataFromState(ShtrihStateDescription deviceState) {
        if (deviceState != null) {
            deviceINN = "" + deviceState.getTin();
            factoryNum = "" + deviceState.getDeviceNo();
        }
    }

    public void setRegistrationNum(String registrationNum) {
        this.registrationNum = registrationNum;
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

    public boolean isDeviceINNEmpty() {
        return StringUtils.isEmpty(deviceINN);
    }

    public boolean isRegistrationNumEmpty() {
        return StringUtils.isEmpty(registrationNum);
    }

    public boolean isFactoryNumEmpty() {
        return StringUtils.isEmpty(factoryNum);
    }
}
