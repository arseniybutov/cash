package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.entities.AbstractInventoryResponse;
import ru.crystals.pos.cash_machine.entities.interfaces.CashInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitsInterface;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class InventoryResponse extends AbstractInventoryResponse {

    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "SeqNo")
    private String seqNo;

    @XmlElement(name = "User")
    private String user;

    @XmlElements({@XmlElement(name = "Cash", type = Cash.class),})
    private List<Cash> cash;

    @XmlElements({@XmlElement(name = "CashUnits", type = CashUnits.class)})
    private List<CashUnits> cashUnits;

    @Override
    public CashUnitInterface getNewCashUnit() {
        return new CashUnit();
    }

    @Override
    public List< ? extends CashUnitsInterface> getCashUnits() {
        return cashUnits;
    }

    @Override
    public List< ? extends CashInterface> getCash() {
        return cash;
    }

    @Override
    public Response getResponse() {
        return Response.SUCCESS;
    }

    @Override
    public void setResponse(Response response) {

    }

}
