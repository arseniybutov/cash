// copy of ru.crystals.pos.fiscalprinter.atol.UtilsAtol

package ru.crystals.pos.fiscalprinter.atol3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.Command;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Disc;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Margin;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Tax;
import ru.crystals.pos.fiscalprinter.datastruct.state.FatalStatus;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;

/**
 * Вспомогательные функции и константы
 *
 * @author Yanovsky
 */
public class UtilsAtol {

    /**
     * Формирует пакет данных в формате TLV для передачи в команды Атол
     * @param tag тег реквизита ОФД
     * @param data данные реквизита
     * @return TLV пакет
     */
    public static String createTLVPacket(int tag, String data) {
        byte[] byteData = data.getBytes(Command.CHARSET_CP866);
        byte[] tlvData = new byte[4 + byteData.length];
        tlvData[0] = (byte) (tag & 0xFF);
        tlvData[1] = (byte) (tag >>> 8);
        tlvData[2] = (byte) (byteData.length & 0xFF);
        tlvData[3] = (byte) (byteData.length >>> 8);
        System.arraycopy(byteData, 0, tlvData, 4, byteData.length);
        return new String(tlvData, Command.CHARSET_CP866);
    }

    public static int bytesToInt(byte b1, byte b2, byte b3, byte b4) {
        int first = b1;
        int second = b2;
        int third = b3;
        int fourth = b4;
        if (second < 0)
            second += 256;
        if (third < 0)
            third += 256;
        if (fourth < 0)
            fourth += 256;
        return ((((first * 256) + second) * 256) + third) * 256 + fourth;
    }

    /**
     * Преобразование команды из числа в трехсимвольную строку
     *
     * @param num
     * @return
     */
    public static byte[] intToCmd(int num) {
        byte bytes[] = new byte[3];
        for (int i = 2; i >= 0; i--) {
            byte tmp = (byte) ((num % 10) + 0x30);
            bytes[i] = tmp;
            num /= 10;
        }
        return bytes;
    }

    public static String byteArray2Sring(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (byte b : bytes) {
            result.append(String.format("%02X", b & 0xFF)).append(" ");
        }
        return result.toString().trim() + "]";
    }

    /**
     * Переводим hex данные из строкогого предстовления в массив байтов.
     * Пример: "0005" -> {0x00, 0x05}
     * @param hexStr строка с hex
     * @return byte[]
     */
    public static byte[] hexStringDataToByteArray(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Перобразует список объектов в строку
     *
     * @param list {@link List}
     * @return {@link String}
     */
    public static String list2String(List<?> list) {
        String result = "[\r\n";
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof String) {
                result += "\t" + list.get(i);
            } else if (list.get(i) instanceof Tax) {
                Tax line = (Tax) list.get(i);
                result += "\t TaxClass: " + line.getTaxClass() + " (Tax: " + line.getTax() + ", TaxSum: " + line.getTaxSum() + ")";
            } else if (list.get(i) instanceof FontLine) {
                FontLine line = (FontLine) list.get(i);
                result += "\t" + line.getContent() + " (font: " + line.getFont().name() + ")";
            } else if (list.get(i) instanceof DocumentSection) {
                DocumentSection line = (DocumentSection) list.get(i);
                result += "\t" + line.getName() + " (content: " + list2String(line.getContent()) + ")";
            } else {
                result += "\t" + list.get(i).toString();
            }
            if (i < list.size() - 1)
                result += ", ";
            result += "\r\n";
        }
        result += "]";
        return result;
    }

    /**
     * Перобразует карту объектов в строку
     *
     * @param map {@link Map}
     * @return {@link String}
     */
    public static String map2String(Map<?, ?> map) {
        String result = "[";
        for (int i = 0; i < map.keySet().size(); i++) {
            Object key = map.keySet().toArray()[i];
            Object value = map.get(key);

            result += "\t" + key + "=" + value;
            if (i < map.keySet().size() - 1)
                result += ", ";
            result += "\r\n";
        }
        result += "]";
        return result;
    }

    /**
     * Перобразует дату в строку
     *
     * @param date {@link Date}
     * @return {@link String}
     */
    public static String date2String(Date date) {
        return SimpleDateFormat.getDateTimeInstance().format(date);
    }

    /**
     * Перобразует {@link ShiftCounters} в строку
     *
     * @param shiftCounters {@link ShiftCounters}
     * @return {@link String}
     */
    public static String shiftCounters2String(ShiftCounters shiftCounters) {
        String result = "";
        result += "ShiftNum: " + shiftCounters.getShiftNum() + "\r\n";
        result += "CountCashlessPurchase: " + shiftCounters.getCountCashlessPurchase() + "\r\n";
        result += "CountCashlessReturn: " + shiftCounters.getCountCashlessReturn() + "\r\n";
        result += "CountCashPurchase: " + shiftCounters.getCountCashPurchase() + "\r\n";
        result += "CountCashReturn: " + shiftCounters.getCountCashReturn() + "\r\n";
        result += "CountReturn: " + shiftCounters.getCountReturn() + "\r\n";
        result += "CountSale: " + shiftCounters.getCountSale() + "\r\n";
        result += "ShiftNum: " + shiftCounters.getShiftNum() + "\r\n";
        result += "SumCashEnd: " + shiftCounters.getSumCashEnd() + "\r\n";
        result += "SumCashlessPurchase: " + shiftCounters.getSumCashlessPurchase() + "\r\n";
        result += "SumCashlessReturn: " + shiftCounters.getSumCashlessReturn() + "\r\n";
        result += "SumCashPurchase: " + shiftCounters.getSumCashPurchase() + "\r\n";
        result += "SumCashReturn " + shiftCounters.getSumCashReturn() + "\r\n";
        result += "SumReturn: " + shiftCounters.getSumReturn() + "\r\n";
        result += "SumSale: " + shiftCounters.getSumSale();
        return result;
    }

    /**
     * Перобразует {@link FatalStatus} в строку
     *
     * @param fatalStatus {@link FatalStatus}
     * @return {@link String}
     */
    public static String fatalStatus2String(FatalStatus fatalStatus) {
        return "Status: " + fatalStatus.getStatus() + ", FatalStatus: " + fatalStatus.getFatalStatus() + ", Descriptions: "
                + list2String(fatalStatus.getDescriptions());
    }

    /**
     * Перобразует {@link StatusFP} в строку
     *
     * @param status {@link StatusFP}
     * @return {@link String}
     */
    public static String status2String(StatusFP status) {
        return "Status: " + status.getLongStatus() + ", FatalStatus: " + status.getStatus() + ", Descriptions: " + list2String(status.getDescriptions());
    }

    /**
     * Перобразует {@link PrinterState} в строку
     *
     * @param printerState {@link PrinterState}
     * @return {@link String}
     */
    public static String PrinterState2String(PrinterState printerState) {
        return "Status: " + printerState.getLongState() + ", FatalStatus: " + printerState.getState() + ", Descriptions: "
                + list2String(printerState.getDescriptions());
    }

    /**
     * Перобразует {@link Cashier} в строку
     *
     * @param cashier {@link Cashier}
     * @return {@link String}
     */
    public static String cahiers2String(Cashier cashier) {
        return cashier.getTabNum() + " " + cashier.getName();
    }

    /**
     * Перобразует {@link FiscalDocument} в строку
     *
     * @param document {@link FiscalDocument}
     * @return {@link String}
     */
    public static String fiscalDocument2String(FiscalDocument document) {
        String result = "";
        //@formatter:off
        if (document instanceof Check) {
            result += "CHECK:\r\n";
            Check check = (Check) document;
            result += "\tShopName: " + check.getShopName() + "\r\n" +
                    "\tCashier: " + cahiers2String(check.getCashier()) + "\r\n" +
                    "\tShiftNum: " + check.getShiftNum() + "\r\n" +
                    "\tCheckNumber: " + check.getCheckNumber() + "\r\n" +
                    "\tType: " + check.getType() + "\r\n" +
                    "\tCheckSumStart: " + check.getCheckSumStart() + "\r\n" +
                    "\tDiscountValue: " + check.getDiscountValue() + "\r\n" +
                    "\tDiscountValueTotal: " + check.getDiscountValueTotal() + "\r\n" +
                    "\tCheckSumEnd: " + check.getCheckSumEnd() + "\r\n" +
                    "\tAnnul: " + check.isAnnul() + "\r\n" +
                    "\tCopy: " + check.isCopy() + "\r\n";
            result += "\t\tPositions:\r\n";
            for (Goods goods : check.getGoods()) {
                result += "\t\t\tPositionNum: " + goods.getPositionNum() + "\r\n" +
                        "\t\t\t\tInsertType: " + goods.getInsertType() + "\r\n" +
                        "\t\t\t\tItem: " + goods.getItem() + "\r\n" +
                        "\t\t\t\tBarcode: " + goods.getBarcode() + "\r\n" +
                        "\t\t\t\tDepartNumber: " + goods.getDepartNumber() + "\r\n" +
                        "\t\t\t\tName: " + goods.getName() + "\r\n" +
                        "\t\t\t\tQuant: " + goods.getQuant() + "\r\n" +
                        "\t\t\t\tStartPositionPrice: " + goods.getStartPositionPrice() + "\r\n" +
                        "\t\t\t\tStartPricePerUnit: " + goods.getStartPricePerUnit() + "\r\n" +
                        "\t\t\t\tSumDiscount: " + goods.getSumDiscount() + "\r\n" +
                        "\t\t\t\tEndPositionPrice: " + goods.getEndPositionPrice() + "\r\n" +
                        "\t\t\t\tEndPricePerUnit: " + goods.getEndPricePerUnit() + "\r\n" +
                        "\t\t\t\tTaxSum: " + goods.getTaxSum() + "\r\n";
            }
            result += "\t\tDiscs:\r\n";
            for (Disc disc : check.getDiscs()) {
                result += "\t\t\tType: " + disc.getType() + "\r\n" +
                        "\t\t\t\t\tName: " + disc.getName() + "\r\n" +
                        "\t\t\t\t\tValue: " + disc.getValue() + "\r\n";
            }
            result += "\t\tMargins:\r\n";
            for (Margin margin : check.getMargins()) {
                result += "\t\t\tType: " + margin.getType() + "\r\n" +
                        "\t\t\t\tName: " + margin.getName() + "\r\n" +
                        "\t\t\t\tValue: " + margin.getValue() + "\r\n";
            }
            result += "\t\tTaxes:\r\n";
            for (Tax tax : check.getTaxes()) {
                result += "\t\t\tTaxClass: " + tax.getTaxClass() + "\r\n" +
                        "\t\t\t\tTax: " + tax.getTax() + "\r\n" +
                        "\t\t\t\tTaxSum: " + tax.getTaxSum() + "\r\n";
            }
            result += "\t\tPayments:\r\n";
            for (Payment payment : check.getPayments()) {
                result += "\t\t\tIndexPayment: " + payment.getIndexPayment() + "\r\n" +
                        "\t\t\t\tSum: " + payment.getSum() + "\r\n";
            }
        } else if (document instanceof Money) {
            result += "MONEY:\r\n";
            Money check = (Money) document;
            result += "\tShopName: " + check.getShopName() + " " +
                    "\tCashier: " + cahiers2String(check.getCashier()) + " " +
                    "\tShiftNum: " + check.getShiftNum() + " " +
                    "\tCheckNumber: " + check.getCheckNumber() + "\r\n" +
                    "\tOperationType: " + check.getOperationType() + "\r\n" +
                    "\tValue: " + check.getValue() + "\r\n" +
                    "\tSumCoins: " + check.getSumCoins() + "\r\n";
            result += "\t\tBankNotes:\r\n";
            for (BankNote note : check.getBankNotes()) {
                result += "\t\t\tCount: " + note.getCount() + "\r\n" +
                        "\t\t\tValue: " + note.getValue() + "\r\n";
            }
        }
        return result;
        //@formatter:on
    }

    /**
     * Перобразует {@link SimpleServiceDocument} в строку
     *
     * @param serviceDocument {@link SimpleServiceDocument}
     * @return {@link String}
     */
    public static String serviceDociment2String(SimpleServiceDocument serviceDocument) {
        //@formatter:off
        String result = "";
        result += "Cashier: " + cahiers2String(serviceDocument.getCashier()) + "\r\n" +
                "Depart: " + serviceDocument.getDepart() + "\r\n";
        result += "\tRows:\r\n";
        for (Row row : serviceDocument.getRows()) {
            result += "\t\tValue: " + row.getValue() + "\r\n";
        }
        return result;
        //@formatter:on
    }

    /**
     * Перобразует {@link Report} в строку
     *
     * @param report {@link Report}
     * @return {@link String}
     */
    public static String report2String(Report report) {
        //@formatter:off
        return "Cashier: " + cahiers2String(report.getCashier()) + "\r\n" +
                "Depart: " + report.getDepart() + "\r\n" +
                "ShopName: " + report.getShopName() + "\r\n" +
                "ShiftNum: " + report.getShiftNum() + "\r\n" +
                "DocumentNumber: " + report.getDocumentNumber();
        //@formatter:on
    }

}
