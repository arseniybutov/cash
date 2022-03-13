package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Контейнер запросов команд ККТ, содержит номер команды и ее параметры,
 * так же уникальный ID, данные протокола и текущую дату.
 */
@XmlRootElement(name = "ArmRequest")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmRequest {

    @XmlElement(name = "RequestBody")
    private RequestBody requestBody;
    @XmlElement(name = "RequestData") @XmlJavaTypeAdapter(AdapterCDATA.class)
    private String requestData;

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }
}

