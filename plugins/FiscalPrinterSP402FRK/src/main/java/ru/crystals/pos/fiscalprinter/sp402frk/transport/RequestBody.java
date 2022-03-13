package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Тело запроса ККТ содержит номер команды, уникальный ID, данные протокола, текущую дату.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class RequestBody {
    @XmlElement(name = "ProtocolLabel")
    private String protocolLabel;
    @XmlElement(name = "ProtocolVersion")
    private String protocolVersion;
    @XmlElement(name = "RequestId")
    private String requestId;
    @XmlElement(name = "DateTime")
    private String dateTime;
    @XmlElement(name = "Command")
    private String command;
    /**
     * 1 – используется ФФД версии 1.0
     * 2 – используется ФФД версии 1.05
     */
    @XmlElement(name = "msgFFDVer")
    private int msgFFDVer = 2;
    /**
     * 0 – контейнер, используемый для передачи данных ФФД 1.0
     * 1 – контейнер, используемый для передачи данных ФФД 1.05
     */
    @XmlElement(name = "msgContVer")
    private int msgContVer = 1;

    public String getProtocolLabel() {
        return protocolLabel;
    }

    public void setProtocolLabel(String protocolLabel) {
        this.protocolLabel = protocolLabel;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
