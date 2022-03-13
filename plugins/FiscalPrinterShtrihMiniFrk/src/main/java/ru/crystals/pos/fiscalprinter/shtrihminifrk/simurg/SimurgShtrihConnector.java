package ru.crystals.pos.fiscalprinter.shtrihminifrk.simurg;

import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.BaseShtrihConnector;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.TableStructure;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;

/**
 * Вариация коннектора по системе команд Штрих, с учетом особенностей ФР Симург
 */
public class SimurgShtrihConnector extends BaseShtrihConnector {

    // 193й денежный регистр - сумма налиных продаж
    private static final byte CASH_SALE_SUM_REGISTRY = (byte) 0xC1;
    // 195й денежный регистр - сумма наличных возвратов
    private static final byte CASH_RETURN_SUM_REGISTRY = (byte) 0xC3;
    // 209й денежный регистр - сумма продаж с НДС 15%
    private static final byte SALE_SUM_NDS_15_REGISTRY = (byte) 0xD1;
    // 210й денежный регистр - сумма возвратов с НДС 15%
    private static final byte RETURN_SUM_NDS_15_REGISTRY = (byte) 0xD2;
    // 211й денежный регистр - сумма продаж с НДС 0%
    private static final byte SALE_SUM_NDS_0_REGISTRY = (byte) 0xD3;
    // 212й денежный регистр - сумма возвратов с НДС 0%
    private static final byte RETURN_SUM_NDS_0_REGISTRY = (byte) 0xD4;
    // 245й денежный регистр - сумма внесений
    private static final byte CASH_IN_SUM_REGISTRY = (byte) 0xF5;
    // 246й денежный регистр - сумма изъятий
    private static final byte CASH_OUT_SUM_REGISTRY = (byte) 0xF6;
    // 153й операционный регистр - количество чеков продаж в смене
    private static final byte SALE_COUNT_REGISTRY = (byte) 0x99;
    // 154й операционный регистр - количество чеков возврата в смене
    private static final byte RETURN_COUNT_REGISTRY = (byte) 0x9A;

    @Override
    public void printLine(FontLine line) throws IOException, PortAdapterException, ShtrihException {
        //Симург используется как фискализатор, текст чека печатается на ПЧ
    }

    @Override
    public void setCashierName(byte cashierNo, String cashierName) throws IOException, PortAdapterException, ShtrihException {
        // 1. Допустимое количество кассиров:
        TableStructure tableStructure = getTableStructure(getCashiersTableNo());
        int cashiersCount = tableStructure.getRowsCount();
        if (cashiersCount < 1) {
            log.warn("leaving setCashierName(byte, String): this fiscal registry supports [{}] cashiers", cashiersCount);
            return;
        }
        if (cashierNo < 1 || cashierNo > cashiersCount) {
            // аргумент невалиден
            log.warn("leaving setCashierName(byte, String): the \"cashierNo\" argument (== {}) is INVALID: more than {} or less than 1", cashierNo, cashiersCount);
            return;
        }

        //2. Ширина поля имени кассира, константа т.к. GetFieldStructure не может ее выдать на Симурге
        final int cashierNameFieldWidth = 21;

        // 3. Запишем таблицу:
        byte[] value = getStringAsByteArray(cashierName, cashierNameFieldWidth);
        writeTable(getCashiersTableNo(), cashierNo, getCashierNameFieldNo(), value);
    }

    @Override
    public long getCashAccumulation() throws IOException, PortAdapterException, ShtrihException {
        long cashSaleSum = getCashRegister(CASH_SALE_SUM_REGISTRY);
        long cashReturnSum = getCashRegister(CASH_RETURN_SUM_REGISTRY);
        long cashInSum = getCashRegister(CASH_IN_SUM_REGISTRY);
        long cashOutSum = getCashRegister(CASH_OUT_SUM_REGISTRY);

        return cashSaleSum - cashReturnSum + cashInSum - cashOutSum;
    }

    @Override
    protected long getSalesSum() throws IOException, PortAdapterException, ShtrihException {
        long saleSumNDS15 = getCashRegister(SALE_SUM_NDS_15_REGISTRY);
        long saleSumNDS0 = getCashRegister(SALE_SUM_NDS_0_REGISTRY);
        return saleSumNDS15 + saleSumNDS0;
    }

    @Override
    protected long getReturnsSum() throws IOException, PortAdapterException, ShtrihException {
        long returnSumNDS15 = getCashRegister(RETURN_SUM_NDS_15_REGISTRY);
        long returnSumNDS0 = getCashRegister(RETURN_SUM_NDS_0_REGISTRY);
        return returnSumNDS15 + returnSumNDS0;
    }

    @Override
    protected long getSalesCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(SALE_COUNT_REGISTRY);
    }

    @Override
    protected long getReturnsCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(RETURN_COUNT_REGISTRY);
    }

    @Override
    protected byte getCashInCountRegistry() {
        // 155й регистр в Симург - количество внесений
        return (byte) 0x9B;
    }

    @Override
    protected byte getCashOutCountRegistry() {
        // 156й регистр в Симург - количество изъятий
        return (byte) 0x9C;
    }

    @Override
    protected void printDocEnd() throws IOException, PortAdapterException, ShtrihException {
        //На симурге свободная печать не работает, и в ней нет необходимости. Поэтому  печать препринта не нужна.
    }
}
