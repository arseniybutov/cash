package ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents;

/**
 * Тип документа передаваемый в парметр "doc_type" {@link DocumentRequest}
 */
public interface DocumentTypes {
    /**
     * Чек внесения денег в денежный ящик
     */
    String DEPOSIT = "deposit";
    /**
     * Чек продажи
     */
    String SALE = "sale";
    /**
     * Чек аннулирования чека продажи в рамках открытой смены
     */
    String ROLLBACK = "rollback";
    /**
     * Чек возврата товара проданного как в открытых так и в уже закрытых сменах
     */
    String MONEY_BACK = "money_back";
    /**
     * Чек изъятия денег из денежного ящика
     */
    String WITHDRAW = "withdraw";
    /**
     * Чек коррекции в случае невнесения информации в токен (СКМН)
     */
    String CORRECTION = "correction";
}