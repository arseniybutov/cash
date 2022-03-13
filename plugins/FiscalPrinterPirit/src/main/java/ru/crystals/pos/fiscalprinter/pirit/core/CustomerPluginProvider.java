package ru.crystals.pos.fiscalprinter.pirit.core;

import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для клиентского плагина пирита
 * <p>
 * Created by d.borisov on 29.06.2017.
 */
public interface CustomerPluginProvider {
    void setBasePlugin(AbstractPirit ap);

    void fiscalMoneyDocument(Money money) throws Exception;

    void printBankDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException;

    void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException;

    void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException;

    void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException;

    void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException;

    void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException;

    void fiscalizeSum(double sum) throws Exception;

    String getTotalString();

    void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException;

    ExtraRequisiteTypes getCustomerExtraRequisiteType();
}
