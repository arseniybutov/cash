package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;

/**
 * Запрос на копию Продажы/Возврата продажи/Покупки/Возврата покупки
 */
public class TicketCopy extends Ticket {

    public TicketCopy(CheckType type) {
        super(type);
    }

    @Override
    public String getTarget() {
        return type == CheckType.SALE ? "/api/copy/ticket/sale" : "/api/copy/ticket/sale-return";
    }

    @Override
    public void setNotPrint(Boolean notPrint) {
        //В копии отсутсвует параметр not_print, переопределям чтобы было пустым
    }
}
