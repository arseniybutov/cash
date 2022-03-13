package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Контейнер ответов на команды от ККТ, Содержит номер команды+1 и ее ответные параметры,
 * так же уникальный ID, данные протокола, текущую дату и данные об ошибках.
 */
@XmlRootElement(name = "ArmResponse")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmResponse {

    @XmlElement(name = "ResponseBody")
    private ResponseBody responseBody;
    @XmlElement(name = "ResponseData") @XmlJavaTypeAdapter(AdapterCDATA.class)
    private String responseData;
    //Десериализованные данные из <ResponseData>
    private ResponseData deserializedData;

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public ResponseData getDeserializedData() {
        return deserializedData;
    }

    public void setDeserializedData(ResponseData deserializedData) {
        this.deserializedData = deserializedData;
    }

}
