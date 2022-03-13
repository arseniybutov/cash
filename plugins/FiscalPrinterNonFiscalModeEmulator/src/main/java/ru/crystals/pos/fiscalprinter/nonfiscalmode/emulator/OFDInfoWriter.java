package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by v.paydulov on 20.06.2017.
 */
public class OFDInfoWriter {

    public static final Logger logger = LoggerFactory.getLogger(OFDInfoWriter.class);

    private static Map<Integer, String> calculationMethodMap;
    private String OFD_INFO_PATH_FILE;

    public OFDInfoWriter(long index) {
        this.OFD_INFO_PATH_FILE = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + "check_" + index + ".printer.txt";
    }

    static {
        calculationMethodMap = new HashMap<>();
        calculationMethodMap.put(1, "ПРЕДОПЛАТА 100%");
        calculationMethodMap.put(2, "ПРЕДОПЛАТА");
        calculationMethodMap.put(3, "АВАНС");
        calculationMethodMap.put(4, "ПОЛНЫЙ РАСЧЕТ");
        calculationMethodMap.put(5, "ЧАСТИЧНЫЙ РАСЧЕТ И КРЕДИТ");
        calculationMethodMap.put(6, "ПЕРЕДАЧА В КРЕДИТ");
        calculationMethodMap.put(7, "ОПЛАТА КРЕДИТА");
    }

    private String getCalculationMethodValue(Integer key) {
        return calculationMethodMap.get(key) != null ? calculationMethodMap.get(key) : "Неизвестный признак способа расчета";
    }

    public void writeOFDInfo(Check check) {
        if (check != null && check.getGoods() != null && check.getPayments() != null) {
            try (FileWriter fw = new FileWriter(OFD_INFO_PATH_FILE, true)) {
                fw.append("\nИндексы типов оплат (до ФФД 1.0), присутствующие в чеке: ");
                fw.append(check.getPayments().stream().map(Payment::getIndexPayment).collect(Collectors.toList()).toString());
                fw.append("\nИндексы типов оплат (ФФД 1.0), присутствующие в чеке: ");
                fw.append(check.getPayments().stream().map(Payment::getIndexPaymentFDD100).collect(Collectors.toList()).toString());
                fw.append("\n");
                for (Goods good : check.getGoods()) {
                    fw.append("Наименование позиции: ").append(good.getName()).append("\n");
                    fw.append("Признак способа расчета: ").append(getCalculationMethodValue(good.getCalculationMethod())).append("\n");
                    if (good.getCalculationSubject() != null) {
                        fw.append("Признак предмета расчета: ").append(good.getCalculationSubject().toString()).append("\n");
                    }
                    if (good.getAdditionalInfo() != null) {
                        fw.append("Дополнительная информация: ").append('\n').append(good.getAdditionalInfo().toString()).append('\n');
                    }
                    if (StringUtils.isNotBlank(good.getGtdNumber())) {
                        fw.append("Грузовая таможенная декларация товара: ");
                        fw.append(good.getGtdNumber()).append("\n");
                    }

                    if (StringUtils.isNotBlank(good.getRccw())) {
                        fw.append("Код страны происхождения товара: ");
                        fw.append(good.getRccw()).append("\n");
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void writeOFDInfo(Cashier cashier) {
        if (cashier != null && cashier.getInn() != null) {
            try (FileWriter fw = new FileWriter(OFD_INFO_PATH_FILE, true)) {
                fw.append("ИНН кассира: ".concat(cashier.getInn())).append("\n");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
