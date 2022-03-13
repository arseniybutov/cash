package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Описание типа устройства.
 * 
 * @author aperevozchikov
 */
public class ShtrihDeviceType {

    /**
     * Код типа устройства. 0..255.
     * <p/>
     * Известные типы (2015-12-29): <og>
     * <li>0 - ККМ;
     * <li>1 - весы;
     * <li>2 - фискальная память для POS-терминалов;
     * <li>3 - КУ ТРК;
     * <li>4 - MemoPlus;
     * <li>5 - Чековый принтер;
     * <li>6 - АСПД. </og>
     */
    private int typeId;

    /**
     * Подтип устройства. 0..255
     */
    private int subTypeId;

    /**
     * Версия протокола для данного устройства. 0..255
     */
    private int protocolVersion;

    /**
     * Подверсия протокола для данного устройства. 0..255
     */
    private int protocolSubVersion;

    /**
     * Модель устройства. 0..255
     */
    private int deviceId;

    /**
     * Язык устройства. 0..255. русский – <code>0</code>; английский – <code>1</code>
     */
    private int language;

    /**
     * Название устройства
     */
    private String name;
    
    @Override
    public String toString() {
        return String.format("shtrih-device-type [device-type: %s.%s; protocol-version: %s.%s; model-id: %s; lang: %s; device-name: %s]", 
            getDeviceId(), getSubTypeId(), getProtocolVersion(), getProtocolSubVersion(), getDeviceId(), getLanguage(), getName());
    }
    
    // getters & setters

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSubTypeId() {
        return subTypeId;
    }

    public void setSubTypeId(int subTypeId) {
        this.subTypeId = subTypeId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getProtocolSubVersion() {
        return protocolSubVersion;
    }

    public void setProtocolSubVersion(int protocolSubVersion) {
        this.protocolSubVersion = protocolSubVersion;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}