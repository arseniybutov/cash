package ru.crystals.pos.bank.belinvest.ds;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Tatarinov Eduard on 17.11.16.
 */
@XmlRootElement(name = "PerformCardOperation")
@XmlAccessorType(XmlAccessType.FIELD)
public class PerformCardOperation {

    @XmlElement(name = "OperationDataRq")
    private OperationDataRq operationDataRq;

    public PerformCardOperation() {
    }

    public PerformCardOperation(OperationDataRq operationDataRq) {
        this.operationDataRq = operationDataRq;
    }


    public OperationDataRq getOperationDataRq() {
        return operationDataRq;
    }

    public void setOperationDataRq(OperationDataRq operationDataRq) {
        this.operationDataRq = operationDataRq;
    }
}
