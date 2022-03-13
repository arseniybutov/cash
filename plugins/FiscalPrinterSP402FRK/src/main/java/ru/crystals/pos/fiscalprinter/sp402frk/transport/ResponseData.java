package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.ResBundleFiscalPrinterSP;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlAnyElement;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Ответные данные ККТ, после десериализации содержит имя, тип параметра и его данные,
 * так же может содержать в качестве значения список таких же ResponseData параметров.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseData {

    @XmlAttribute(name = "n")
    private String name;
    @XmlAttribute(name = "t")
    private String type;

    @XmlMixed
    @XmlAnyElement(lax = true)
    private List<Object> value;

    public ResponseData() {
    }

    public ResponseData(String name, String type, String value) {
        this.name = name;
        this.type = type;
        getValue().add(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Object> getValue() {
        if (value == null) {
            value = new ArrayList<>();
        }
        return this.value;
    }

    public String getStrValue() throws FiscalPrinterException {
        //Все типы кроме STRUCT и ARRAY могут быть преобразованны в строку
        if (type.equals(KKTDataType.STRUCT) || type.equals(KKTDataType.ARRAY)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_SEND_DATA"), CashErrorType.NEED_RESTART);
        }
        Object elementData = getValue().get(0);
        return (String) elementData;
    }

    public BigDecimal getFloatValue() throws FiscalPrinterException {
        if (!type.equals(KKTDataType.CURRENCY) && !type.equals(KKTDataType.AMOUNT)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_SEND_DATA"), CashErrorType.NEED_RESTART);
        }

        String elementData = getStrValue();
        return new BigDecimal(elementData);
    }

    public int getIntValue() throws FiscalPrinterException {
        if (!type.equals(KKTDataType.UINT)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_SEND_DATA"), CashErrorType.NEED_RESTART);
        }

        String elementData = getStrValue();
        return Integer.parseInt(elementData);
    }

    public Date getDateTimeValue() throws FiscalPrinterException {
        if (!type.equals(KKTDataType.DATE_TIME)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterSP.getString("ERROR_SEND_DATA"), CashErrorType.NEED_RESTART);
        }

        String elementData = getStrValue();
        Date dateTime = null;
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
            dateTime = dateTimeFormat.parse(elementData);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
        return dateTime;
    }
}
