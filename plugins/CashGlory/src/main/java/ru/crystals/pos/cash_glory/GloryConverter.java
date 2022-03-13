package ru.crystals.pos.cash_glory;

import java.math.BigInteger;

import jp.co.glory.bruebox.DenominationType;
import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

public class GloryConverter {

    public DenominationType convToDenomintionType(DenominationInterface denomination) {
        if (denomination instanceof DenominationType) {
            return (DenominationType) denomination;
        }
        DenominationType dt = new DenominationType();
        dt.setCc(denomination.getCurrencyInf());
        dt.setDevid(BigInteger.valueOf(denomination.getDevidInf()));
        dt.setFv(BigInteger.valueOf(denomination.getValueInf()));
        dt.setPiece(BigInteger.valueOf(denomination.getPieceInf()));
        dt.setStatus(BigInteger.valueOf(denomination.getStatusInf()));
        return dt;
    }

    public Response convResponse(int responseId) {
        Response result = Response.NA;
        switch (responseId) {
            case 0:
                result = Response.SUCCESS;
                break;
            case 3:
                result = Response.OCCUPIED;
                break;
            case 5:
                result = Response.NOT_OCCUPIED;
                break;
            case 10:
                result = Response.CHANGE_SHORTAGE;
                break;
            case 11:
                result = Response.EXCLUSIVE_ERROR;
                break;
            case 21:
                result = Response.INVALID_SESSION;
                break;
            case 22:
                result = Response.SESSION_TIMEOUT;
                break;
            case 99:
                result = Response.PROGRAMM_INNER_ERROR;
                break;
            case 100:
                result = Response.DEVICE_ERROR;
                break;
        }
        return result;
    }

}
