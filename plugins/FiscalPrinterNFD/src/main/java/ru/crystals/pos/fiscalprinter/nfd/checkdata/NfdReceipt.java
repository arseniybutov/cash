package ru.crystals.pos.fiscalprinter.nfd.checkdata;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.AddCommodity;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains.CommonDomain;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NfdReceipt {

    private TradeOperationType tradeOperationType;

    private CommonDomain domain;

    private Set<PaymentNFD> payments = new HashSet<>();

    private List<String> closeDocumentText = new ArrayList<>();

    private List<NfdReceiptString> receiptText = new ArrayList<>();

    private Set<Integer> taxGroupNumbers = new HashSet<>();

    private List<AddCommodity> commodities = new ArrayList<>();

    private Modifier modifier;

    public NfdReceipt(TradeOperationType tradeOperationType, CommonDomain domain) {
        this.tradeOperationType = tradeOperationType;
        this.domain = domain;
    }

    public TradeOperationType getTradeOperationType() {
        return tradeOperationType;
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        this.tradeOperationType = tradeOperationType;
    }

    public CommonDomain getDomain() {
        return domain;
    }

    public void setDomain(CommonDomain domain) {
        this.domain = domain;
    }

    public List<AddCommodity> getCommodities() {
        return commodities;
    }

    public boolean addCommodities(AddCommodity addCommodity) {
        return commodities.add(addCommodity);
    }

    public boolean addReceiptText(NfdReceiptString nfdReceiptString) {
        return receiptText.add(nfdReceiptString);
    }

    public List<NfdReceiptString> getReceiptText() {
        return receiptText;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public Set<Integer> getTaxGroupNumbers() {
        return taxGroupNumbers;
    }

    public boolean addTaxGroupNumbers(Integer integer) {
        return taxGroupNumbers.add(integer);
    }

    public void setTaxGroupNumbers(Set<Integer> taxGroupNumbers) {
        this.taxGroupNumbers = taxGroupNumbers;
    }

    public List<String> getCloseDocumentText() {
        return closeDocumentText;
    }

    public boolean addCloseDocumentText(String s) {
        return closeDocumentText.add(s);
    }

    public Set<PaymentNFD> getPayments() {
        return payments;
    }

    public boolean addPayment(PaymentNFD payment) {
        return payments.add(payment);
    }

    public void setPayments(Set<PaymentNFD> payments) {
        this.payments = payments;
    }
}
