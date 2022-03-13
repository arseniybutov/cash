package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.ErrorEventInterface;

/**
 * The error-reporting is notified. Application can refer the “RecoveryURL” for getting recovery help animation image.
 * 
 * @author p.tykvin
 */
@XmlType
public class EventError implements ErrorEventInterface {

    private int deviceId;

    @XmlElement(name = "ErrorCode")
    private Integer errorCode;

    @XmlElement(name = "RecoveryURL")
    private String recoveryURL;


    @Override
    public String getRecoveryURL() {
        return recoveryURL;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

}
