package ru.crystals.pos.fiscalprinter.pirit.core;

import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.util.List;
import java.util.Map;

/**
 * Created by d.borisov on 30.06.2017.
 */
public class DefaultPluginProvider implements CustomerPluginProvider {

    protected AbstractPirit basePlugin;

    public DefaultPluginProvider() {
    }

    public DefaultPluginProvider(AbstractPirit ap) {
        this.basePlugin = ap;
    }

    @Override
    public void setBasePlugin(AbstractPirit ap) {
        this.basePlugin = ap;
    }

    @Override
    public void fiscalMoneyDocument(Money money) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putStringValue(money.getCurrency());
        dp.putDoubleValue((double) money.getValue() / basePlugin.PRICE_ORDER);
        basePlugin.sendRequest(PiritCommand.ADD_MONEY_IN_OUT, dp, true);
    }

    @Override
    public void printBankDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        basePlugin.printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        basePlugin.openServiceDocument(document);
                        basePlugin.printLinesList(section.getContent());
                        break;
                    case "slip":
                        basePlugin.printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        basePlugin.closeDocument(true, null);
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        basePlugin.printLinesList(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        basePlugin.printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        basePlugin.openServiceDocument(report);
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        basePlugin.closeDocument(true, null);
                        if (report.isZReport()) {
                            basePlugin.printZReport(report);
                        } else if (report.isXReport()) {
                            basePlugin.printXReport(report);
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        basePlugin.printLinesList(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        basePlugin.printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                            basePlugin.openServiceDocument(money);
                        } else {
                            if (money.isInventoryDocument()) {
                                basePlugin.openServiceDocument(money);
                            } else {
                                basePlugin.openDocument(money);
                            }
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (money.getOperationType() != InventoryOperationType.DECLARATION) {
                            fiscalMoneyDocument(money);
                        }
                        basePlugin.closeDocument(true, null);
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        basePlugin.printLinesList(section.getContent(), money.getOperationType() != InventoryOperationType.DECLARATION);
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        if (basePlugin.piritConfig == null) {
            return;
        }
        try {
            basePlugin.piritConfig.setRequisites(requisites);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check == null) {
            return;
        }
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        if (!check.isDisablePrint()) {
                            basePlugin.printLogo();
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        basePlugin.openDocument(check);
                        break;
                    case "position":
                    case "positionSectionWithGoodSets":
                        basePlugin.processPositionSection(check, section);
                        break;
                    case "payment":
                        basePlugin.processPaymentSection(check.getPayments(), true);
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (check.getType() == CheckType.SALE && basePlugin.config.isPrintStoredImage() && !check.isDisablePrint()) {
                            basePlugin.printStoredImage(true, 1, 1);
                        }

                        if (check.getPrintDocumentSettings().isNeedPrintBarcode() && !check.isDisablePrint()) {
                            basePlugin.printDocumentNumberBarcode(check, true);
                        }
                        basePlugin.putCheckRequisites(check);
                        basePlugin.closeDocument(true, check);
                        break;
                    case "total":
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        basePlugin.printLinesListInCheck(section.getContent(), check);
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check == null) {
            return;
        }
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        if (!check.isDisablePrint()) {
                            basePlugin.printLogo();
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        basePlugin.openDocument(check);
                        break;
                    case "position":
                        basePlugin.printLinesListInCheck(section.getContent(), check);
                        break;
                    case "payment":
                        fiscalizeSum((double) check.getCheckSumEnd() / 100);
                        basePlugin.putPayments(check.getPayments(), true);
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        basePlugin.annulCheck();
                        break;
                    case "total":
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        basePlugin.printLinesListInCheck(section.getContent(), check);
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void fiscalizeSum(double sum) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putStringValue(getTotalString());
        dp.putStringValue("");
        dp.putDoubleValue(1.0);
        dp.putDoubleValue(sum);
        basePlugin.sendRequest(PiritCommand.ADD_ITEM, dp, true);
        basePlugin.sendRequest(PiritCommand.SUBTOTAL, true);
    }

    @Override
    public String getTotalString() {
        return "ИТОГО";
    }

    @Override
    public void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        basePlugin.printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        basePlugin.openServiceDocument(check);
                        basePlugin.printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        basePlugin.printDocumentNumberBarcode(check, false);
                        basePlugin.closeDocument(true, null);
                        break;
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        basePlugin.printLinesList(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ExtraRequisiteTypes getCustomerExtraRequisiteType() {
        return ExtraRequisiteTypes.EMPTY;
    }
}
