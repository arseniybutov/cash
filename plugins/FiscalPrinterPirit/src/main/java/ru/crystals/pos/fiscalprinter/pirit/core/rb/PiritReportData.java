package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import javax.xml.bind.DatatypeConverter;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeEntity;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeException;
import ru.crystals.pos.fiscalprinter.datastruct.documents.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PiritReportData {
    private static final int REQUISITE_REPORT_SIZE = 78;
    private static final int TAX_REPORT_SIZE = 39;
    private static final int PIRIT_CHECK_SALE = 2;
    private static final int PIRIT_CHECK_RETURN = 3;
    private static final int PIRIT_MONEY_IN = 4;
    private static final int PIRIT_MONEY_OUT = 5;
    private static final int PIRIT_CHECK_ANNUL = 12;

    public static ControlTapeEntity parseReport(byte[] data, String INN, String regNumber, List<PaymentType> paymentTypeList, boolean ofdMode) throws ControlTapeException {
        //Определим что за документ мы получили
        int docType = AbstractSection.getByte(data[2]);
        switch (docType) {
            case PIRIT_CHECK_SALE:
                return convertToCheck(data, true, INN, regNumber, paymentTypeList, ofdMode);
            case PIRIT_CHECK_RETURN:
                return convertToCheck(data, false, INN, regNumber, paymentTypeList, ofdMode);
            case PIRIT_CHECK_ANNUL:
                return convertToCheck(data, false, INN, regNumber, paymentTypeList, ofdMode);
            case PIRIT_MONEY_IN:
                return convertToCashInOut(data, true, INN, regNumber, ofdMode);
            case PIRIT_MONEY_OUT:
                return convertToCashInOut(data, false, INN, regNumber, ofdMode);
            default:
                throw new ControlTapeException(ResBundleFiscalPrinter.getString("DOCUMENT_NOT_FOUND"));

        }
    }

    private static ControlTapeEntity convertToCashInOut(byte[] data, boolean isMoneyIn, String INN, String regNumber, boolean ofdMode) {
        Money document = new Money();
        ReportDataSection section;
        document.setOperationType(isMoneyIn ? InventoryOperationType.CASH_IN : InventoryOperationType.CASH_OUT);
        Date documentDate = new Date();
        int index = 1;
        while (index < data.length) {
            byte type = data[index];
            switch (type) {
                case 1: {
                    section = new OpenSection(Arrays.copyOfRange(data, index, index + OpenSection.SIZE));
                    document.setCheckNumber( ((OpenSection)section).getCheckNumber());
                    document.setShiftNum(Long.valueOf( ((OpenSection)section).getShiftNumber()));
                    documentDate = ((OpenSection)section).getDate().getDate();
                    Cashier cashier = new Cashier();
                    cashier.setName(((OpenSection)section).getOperatorName());
                    document.setCashier(cashier);
                    index += section.getSize() + 1;
                    break;
                }
                case 2:
                case 3:
                case 4:
                    if (ofdMode){
                        section = new CloseSectionSKNO(Arrays.copyOfRange(data, index, index + CloseSectionSKNO.SIZE));
                        document.setUid( byteArrayToString(((CloseSectionSKNO) section).getSknoUi()) );
                    } else {
                        section = new CloseSection(Arrays.copyOfRange(data, index, index + CloseSection.SIZE));
                    }
                    index += section.getSize() + 1;
                    break;
                case 5:
                    index += ItemSection.SIZE + 1;
                    break;
                case 6:
                case 7:
                    index += DiscountSection.SIZE + 1;
                    break;
                case 8:
                    section = new PaymentSection(Arrays.copyOfRange(data, index, index + PaymentSection.SIZE));
                    document.setSumCoins( ((PaymentSection)section).getSum());
                    index += PaymentSection.SIZE + 1;
                    break;
                case 9:
                case 10:
                case 11:
                case 13:
                case 15:
                case 16:
                    index += TotalSection.SIZE + 1;
                    break;
                case 12:
                    CashInOutSection cash = new CashInOutSection(Arrays.copyOfRange(data, index, index + CashInOutSection.SIZE));
                    document.setSumCoins(cash.getSum());
                    index += CashInOutSection.SIZE + 1;
                    break;

                case 14:
                case 17:
                    index += REQUISITE_REPORT_SIZE + 1;
                    break;

                case 18:
                    index += TAX_REPORT_SIZE + 1;
                    break;

                default:
                    index += 10000;
                    break;
            }
        }
        return new ControlTapeEntity(document, INN, regNumber, documentDate);
    }

    private static ControlTapeEntity convertToCheck(byte[] data, boolean isSale, String INN, String regNumber, List<PaymentType> paymentTypeList, boolean ofdMode) {
        Check document = new Check();
        document.setCopy(false);
        document.setType(isSale ? CheckType.SALE : CheckType.RETURN);
        int index = 1;
        ReportDataSection section;
        Goods currentGood = null;
        long curPositionDiscount = 0L;
        while (index < data.length) {
            byte type = data[index];
            switch (type) {
                case 1:
                    section = new OpenSection(Arrays.copyOfRange(data, index, index + OpenSection.SIZE));
                    document.setCheckNumber(((OpenSection) section).getCheckNumber());
                    document.setShiftNum(Long.valueOf(((OpenSection) section).getShiftNumber()));
                    document.setDate(((OpenSection) section).getDate().getDate());
                    Cashier cashier = new Cashier();
                    cashier.setName(((OpenSection) section).getOperatorName());
                    document.setCashier(cashier);
                    index += section.getSize() + 1;
                    break;
                case 2:
                case 3:
                case 4:
                    if (ofdMode){
                        section = new CloseSectionSKNO(Arrays.copyOfRange(data, index, index + CloseSectionSKNO.SIZE));
                        document.setUid( byteArrayToString(((CloseSectionSKNO) section).getSknoUi()) );
                    } else {
                        section = new CloseSection(Arrays.copyOfRange(data, index, index + CloseSection.SIZE));
                    }
                    index += section.getSize() + 1;
                    if (type == 4) {
                        document.setAnnul(true);
                    }
                    break;
                case 5:
                    section = new ItemSection(Arrays.copyOfRange(data, index, index + ItemSection.SIZE));
                    if (currentGood != null) {
                        currentGood.setSumDiscount(curPositionDiscount);
                    }
                    Goods good = new Goods();
                    good.setName(((ItemSection) section).getItem());
                    good.setStartPricePerUnit(((ItemSection) section).getPrice());
                    good.setStartPositionPrice(((ItemSection) section).getSum());
                    good.setQuant(((ItemSection) section).getQuantity());
                    currentGood = good;
                    index += section.getSize() + 1;
                    document.getGoods().add(good);
                    break;

                case 6:
                case 7:
                    section = new DiscountSection(Arrays.copyOfRange(data, index, index + DiscountSection.SIZE));
                    long discSum = ((DiscountSection) section).getSum();
                    if (currentGood != null && ((DiscountSection) section).getType() != 0) {
                        curPositionDiscount += discSum;
                    }
                    index += DiscountSection.SIZE + 1;
                    break;

                case 8:
                    if (currentGood != null) {
                        currentGood.setSumDiscount(curPositionDiscount);
                    }
                    section = new PaymentSection(Arrays.copyOfRange(data, index, index + PaymentSection.SIZE));
                    Payment payment = new Payment();
                    payment.setSum(((PaymentSection) section).getSum());
                    payment.setPaymentType(((PaymentSection) section).getTypePayment());
                    payment.setIndexPayment(-1);
                    document.getPayments().add(payment);
                    index += section.getSize() + 1;
                    break;
                case 9:
                case 10:
                case 11:
                case 13:
                case 15:
                case 16:
                    if (type == 10) {
                        document.setCheckSumEnd(new TotalSection(Arrays.copyOfRange(data, index, index + TotalSection.SIZE)).getSum());
                    }
                    index += TotalSection.SIZE + 1;
                    break;

                case 12:
                    index += CashInOutSection.SIZE + 1;
                    break;
                case 14:
                case 17:
                    index += REQUISITE_REPORT_SIZE + 1;
                    break;

                case 18:
                    index += TAX_REPORT_SIZE + 1;
                    break;

                default:
                    index += 10000;
                    break;
            }
        }

        return new ControlTapeEntity(document, INN, regNumber, paymentTypeList);
    }

    private static String byteArrayToString(byte[] uid){
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < uid.length; i += 2){
            byte[] quatro = new byte[2];
            System.arraycopy(uid, i, quatro, 0, 2);
            result.append(DatatypeConverter.printHexBinary(quatro));
            result.append(" ");
        }
        return result.toString();
    }

}
