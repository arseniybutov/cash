package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Тело ответа ККТ содержит номер команды+1, уникальный ID, данные протокола, текущую дату и данные об ошибках.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ResponseBody {
    @XmlElement(name = "ProtocolLabel")
    private String protocolLabel;
    @XmlElement(name = "ProtocolVersion")
    private String protocolVersion;
    @XmlElement(name = "RequestId")
    private String requestId;
    @XmlElement(name = "Result")
    private int result;
    @XmlElement(name = "ErrorCategory")
    private int errorCategory;
    @XmlElement(name = "ErrorSource")
    private int errorSource;
    @XmlElement(name = "ErrorCode")
    private int errorCode;
    @XmlElement(name = "ErrorDescription")
    private String errorDescription;
    @XmlElement(name = "Command")
    private int command;
    @XmlElement(name = "msgFFDVer")
    private int msgFFDVer;
    @XmlElement(name = "msgContVer")
    private int msgContVer;

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

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(int errorCategory) {
        this.errorCategory = errorCategory;
    }

    public int getErrorSource() {
        return errorSource;
    }

    public void setErrorSource(int errorSource) {
        this.errorSource = errorSource;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getMsgFFDVer() {
        return msgFFDVer;
    }

    public void setMsgFFDVer(int msgFFDVer) {
        this.msgFFDVer = msgFFDVer;
    }

    public int getMsgContVer() {
        return msgContVer;
    }

    public void setMsgContVer(int msgContVer) {
        this.msgContVer = msgContVer;
    }
}
