package ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.az.airconn.ResBundleFiscalAirConn;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.Arrays;
import java.util.List;

/**
 * Типы оплат используемые в чеках AirConn
 */
public enum PaymentTypes {

    /**
     * Оплата наличными
     */
    PAYMENT_CASH(PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex()),
    /**
     * Оплата безналичными
     */
    PAYMENT_CARD(
            PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASHLESS.getIndex(),
            PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_BONUS_SBERBANK.getIndex(),
            PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_BANK_TERMINAL.getIndex(),
            // Почему-то с самого начала для рассрочки по карте (BankInstallmentPaymentEntity) в кофниге поставили индекс
            // Пока непонятно точно на что надо мапить эту оплату в AirCon, не будем заниматься xq для конфигов, а замапим 10 как оплату карту
            PaymentType.NonFFDFiscalType.ID_10.getIndex(),
            PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CHILD_CARD.getIndex()
    ),
    /**
     * Оплачено подарочными сертификатами
     */
    PREPAYMENT(PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_PREPAYMENT.getIndex()),
    /**
     * Оплачено кредитом
     */
    PAYMENT_CREDIT(PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CREDIT.getIndex()),
    /**
     * оплачено бонусами
     */
    PAYMENT_BONUS(PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_BONUS_CARD.getIndex());
    /**
     * Индекс платежа в кассе
     */
    private final List<Long> paymentCodes;

    PaymentTypes(Long... paymentCodes) {
        this.paymentCodes = Arrays.asList(paymentCodes);
    }

    /**
     * Получить тип оплаты из списка оплат по коду
     *
     * @param paymentCode код оплаты
     * @return Тип оплаты
     * @throws FiscalPrinterException если
     */
    public static PaymentTypes typeFromCode(Long paymentCode) throws FiscalPrinterException {
        for (PaymentTypes paymentType : PaymentTypes.values()) {
            if (paymentType.getPaymentCodes().contains(paymentCode)) {
                return paymentType;
            }
        }
        throw new FiscalPrinterException(ResBundleFiscalAirConn.getString("ERROR_UNSUPPORTED_PAYMENT"), CashErrorType.NEED_RESTART);
    }

    public List<Long> getPaymentCodes() {
        return paymentCodes;
    }
}
