package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.cashboxsystem.ResBundleFiscalPrinterCBS;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Типы оплат используемые в чеках CBS
 */
public enum CbsPaymentType {

    /**
     * Оплата наличными
     */
    PAYMENT_CASH(0, 0),
    /**
     * Оплата дебитной картой
     */
    PAYMENT_CARD(1, 1),
    /**
     * Оплата кредитом
     */
    PAYMENT_CREDIT(2, 13),
    /**
     * Оплата тарой
     */
    PAYMENT_TARE(3, -1);

    private final int code;
    /**
     * Соответствующий код в ffd100
     */
    private final int ffd100Code;
    /**
     * Последний код электронного типа оплаты Пирит 2Ф
     */
    public static final int PAYMENT_CARD_LAST_CODE = 12;

    CbsPaymentType(int code, int ffd100Code) {
        this.code = code;
        this.ffd100Code = ffd100Code;
    }

    public final int getCode() {
        return code;
    }

    public int getFfd100Code() {
        return ffd100Code;
    }

    /**
     * Получить тип оплаты из списка оплат по коду
     * @param code код оплаты
     * @return Тип оплаты
     * @throws FiscalPrinterException если
     */
    public static CbsPaymentType typeFromCode(int code) throws FiscalPrinterException {
        for(CbsPaymentType paymentType : CbsPaymentType.values()) {
            if (paymentType.getFfd100Code() == code) {
                return paymentType;
            }
        }
        // Проверяем относится ли тип оплаты к ЭЛЕКТРОННЫМ
        if (code > PAYMENT_CARD.getFfd100Code() && code <= PAYMENT_CARD_LAST_CODE) {
            return PAYMENT_CARD;
        }
        throw new FiscalPrinterException(ResBundleFiscalPrinterCBS.getString("ERROR_UNSUPPORTED_PAYMENT"), CashErrorType.NEED_RESTART);
    }
}
