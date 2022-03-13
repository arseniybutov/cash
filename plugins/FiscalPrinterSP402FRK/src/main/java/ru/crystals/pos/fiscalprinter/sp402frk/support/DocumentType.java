package ru.crystals.pos.fiscalprinter.sp402frk.support;

/**
 * Тип открываемого документа<br/>
 * NONFISCAL - НЕФИСКАЛЬНЫЙ ДОКУМЕНТ<br/>
 * SALE - ЧЕК ПРОДАЖИ<br/>
 * RETURN - ЧЕК ВОЗВРАТА<br/>
 * CASHIN - ЧЕК ВНЕСЕНИЯ ДЕНЕГ<br/>
 * CASHOUT - ЧЕК ИЗЪЯТИЯ ДЕНЕГ<br/>
 * CANCELED - АННУЛИРОВАННЫЙ ЧЕК<br/>
 * DEFERRED - ОТЛОЖЕННЫЙ ЧЕК<br/>
 */
public enum DocumentType {
    NONFISCAL,
    SALE,
    RETURN,
    CASHIN,
    CASHOUT,
    CANCELED,
    DEFERRED
}
