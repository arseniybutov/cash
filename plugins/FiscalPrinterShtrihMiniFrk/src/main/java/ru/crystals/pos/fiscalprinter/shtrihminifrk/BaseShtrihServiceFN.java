package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFNStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFiscalizationResult;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihItemCode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalV2Ex;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Общая реализация для устройств с ФН
 */
public abstract class BaseShtrihServiceFN extends ShtrihMiniFrkFiscalPrinterServiceImpl {

    @Override
    public void start() throws FiscalPrinterException {
        super.start();
        taxes = getTaxes();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        long result;

        LOG.debug("entering openShift(Cashier). the argument is: cashier [{}]", cashier);

        // 1. запишем в ФР имя кассира, открывшего смену:
        setCashierName(cashier == null ? null : getCashierName(cashier));

        try {
            getConnector().openShift(cashier);
            taxes = getTaxes();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("openShift(Cashier)", e);
        }

        // 2. и вернем номер [типа] текущей смены:
        result = getShiftNumber();

        LOG.debug("leaving openShift(Cashier). the result is: {}", result);

        return result;
    }

    @Override
    public String getEklzNum() {
        LOG.trace("entering getEklzNum() ");
        // SRTZ-811 не кешируем номер ФН, т.к. забывают перезапускать кассу при замене ФН
        String fnNumber = null;
        try {
            fnNumber = getConnector().getFNNumber();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            LOG.error(e.getMessage());
        }
        LOG.trace("leaving getEklzNum(). The result is: {}", fnNumber);
        return fnNumber;
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        final ShtrihFNStateOne fnState = getFNState();
        if (fnState == null) {
            return -1L;
        }
        return fnState.getLastFdNum();
    }

    @Override
    public void printFNReport(Cashier cashier) throws FiscalPrinterException {
        try {
            getConnector().printFNReport(cashier);
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("printFNReport(FontLine)", e);
        }
    }

    ShtrihDiscount makeDiscount(Goods pos) {
        return new ShtrihDiscount(CurrencyUtil.getPositionSum(pos.getEndPricePerUnit(), pos.getQuant()) - pos.getEndPositionPrice(), "");
    }

    public void printLogo() throws FiscalPrinterException {
        try {
            getConnector().printLogo();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("printLogo()", e);
        }
    }

    private ShtrihFNStateOne getFNState() throws FiscalPrinterException {
        ShtrihFNStateOne result = null;
        try {
            result = getConnector().getFNState();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("getFNState()", e);
        }

        return result;
    }

    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws FiscalPrinterException {
        try {
            Optional<Long> result = getConnector().printCorrectionReceipt(correctionReceipt, cashier);
            closeNonFiscalDocument(null);
            return result;
        } catch (Exception e) {
            getConnector().cancelDocument();
            logExceptionAndThrowIt("printCorrectionReceipt()", e);
        }
        return Optional.empty();
    }

    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        try {
            ShtrihFNStateOne fnState = getFNState();
            if (fnState == null) {
                return null;
            }
            FiscalDocumentData fdd = getConnector().getLastDocInfo(fnState.getLastFdNum());
            if (fdd != null && fdd.getType() != FiscalDocumentType.UNKNOWN) {
                fdd.setOperationDate(Optional.ofNullable(fdd.getOperationDate()).orElseGet(Date::new));
                fdd.setFnNumber(fnState.getFnNum());
                fdd.setQrCode(PluginUtils.buildQRCode(fdd, QR_CODE_DATE_PATTERN));
            }
            return fdd;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        LOG.debug("entering printCheckByTemplate(List, Check). The arguments are: check [{}]", check);
        disableDocumentPrinting(check, true);
        try {
            setCashierName(getCashierName(check.getCashier()));
            setDepartNumber(check.getDepart().intValue());

            Map<Integer, Long> payments = getPayments(check);
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case "logo":
                        printLogo();
                        break;
                    case "fiscal":
                        if (check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                            printCheckBarcode(check);
                            printLine(new FontLine(SPACE, Font.NORMAL));
                        }
                        putGoods(check);
                        break;
                    case "cut":
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }

            getConnector().sendCashierInnIfNeeded(check.getCashier().getInn());

            if (check.getClientRequisites() != null) {
                try {
                    getConnector().setClientData(check.getClientRequisites());
                } catch (IOException | PortAdapterException | ShtrihException e) {
                    LOG.error("Error sent client data:" + e.getMessage());
                }
            }
            // закрыть/зарегистрировать чек
            ShtrihReceiptTotalV2Ex receiptTotal = makeReceiptTotalV2(check, payments);

            // добавим данные контрагента, если нужно
            getConnector().addCounterpartyData(check);

            getConnector().closeReceiptV2Ex(receiptTotal);
            closeNonFiscalDocument(check);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        } finally {
            disableDocumentPrinting(check, false);
        }

        LOG.debug("leaving printCheckByTemplate(List, Check)");
    }

    private void disableDocumentPrinting(Check check, boolean printingDisabled) {
        if (!check.isDisablePrint()) {
            return;
        }
        try {
            connector.disableDocumentPrinting(printingDisabled);
        } catch (Exception e) {
            LOG.warn("Error on set disableDocumentPrinting={}", printingDisabled, e);
        }
    }

    protected abstract void putGoods(Check check) throws PortAdapterException, ShtrihException, IOException, FiscalPrinterException;

    protected ShtrihReceiptTotalV2Ex makeReceiptTotalV2(Check check, Map<Integer, Long> payments) {
        // закрыть/зарегистрировать чек
        ShtrihReceiptTotalV2Ex receiptTotal = new ShtrihReceiptTotalV2Ex(check, StringUtils.EMPTY, Optional.ofNullable(payments.get(0)).orElse(0L));
        // оплаты по типам(наличные передали в конструктор):
        // электронными
        receiptTotal.setSumm2(Optional.ofNullable(payments.get(1)).orElse(0L));
        // предварительная оплата(Аванс)
        receiptTotal.setSumm14(Optional.ofNullable(payments.get(13)).orElse(0L));
        // сумма полследующей оплаты (кредит)
        receiptTotal.setSumm15(Optional.ofNullable(payments.get(14)).orElse(0L));
        // иная форма оплаты (оплата встречным представлением)
        receiptTotal.setSumm16(Optional.ofNullable(payments.get(15)).orElse(0L));
        // других типов оплаты в документации не указано и касса пока не использует,
        // а если даже начнет использовать, то не понятно пока как их мапить на таблицу в ФР
        // налоги и скидки в ФР пишем по нулям

        return receiptTotal;
    }

    protected void putCodingMark(Goods good) throws PortAdapterException, ShtrihException, IOException {
        ShtrihItemCode itemCode = getCodeMark(good);
        getConnector().sendItemCode(itemCode);
    }

    protected abstract ShtrihItemCode getCodeMark(Goods good);

    @Override
    public Date getEKLZActivizationDate() throws FiscalPrinterException {
        Date result = null;
        LOG.debug("entering getEKLZActivizationDate()");
        try {
            ShtrihFiscalizationResult fiscalizationResult = getConnector().getLastFiscalizationResult();
            result = fiscalizationResult.getFiscalizationDate();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("getEKLZActivizationDate()", e);
        }
        LOG.debug("leaving getEKLZActivizationDate(). The result is: {}", result);
        return result;
    }

    @Override
    protected Map<Integer, Long> getPayments(Check check) {
        Map<Integer, Long> result = new HashMap<>();

        if (check == null) {
            LOG.error("getPayments(Check): the argument is NULL!");
            return result;
        }

        if (CollectionUtils.isEmpty(check.getPayments())) {
            LOG.warn("getPayments(Check): the argument has no payments!");
        } else {
            for (Payment payment : CheckUtils.reduceAndSortPaymentsByIndexPaymentFDD(check.getPayments())) {
                result.put((int) payment.getIndexPaymentFDD100(), payment.getSum());
            }
        }
        return result;
    }

    @Override
    public boolean isFFDDevice() {
        return true;
    }
}
