package ru.crystals.pos.fiscalprinter.sp402frk.support;

/**
 * Сисок оплат для использования в плагине, в СП402-ФРК id оплат нет и принимаются отдельными параметрами в командах
 * ID оплат соответсвуют FiscalNumFDD100 из payments--config.xml
 */
public class KKTPaymentType {
    public static final int CASH = 0;
    public static final int NON_CASH = 1;
    public static final int PREPAYMENT = 13;
    public static final int POSTPAYMENT = 14;
    public static final int ONCOMING = 15;
}
