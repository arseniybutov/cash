package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.closedocument.CloseDocumentResponse;

import java.util.Set;

/**
 * Закрытие чека.
 */
public class CloseDocument extends BaseRequest {

    private static final String METHOD_NAME = "closeDocument";

    /**
     * Печатаемый текст.
     */
    private static final String TEXT_PARAM_NAME = "text";

    /**
     * Список платежей.
     */
    private static final String PAYMENTS_PARAM_NAME = "payments";

    /**
     * Группы налогообложения. Налоги на весь чек и налоги
     * на позицию являются взаимоисключающими.
     */
    private static final String TAX_GROUP_NUMBERS_PARAM_NAME = "taxGroupNumbers";

    /**
     * Модификатор на весь чек.
     */
    private static final String MODIFIER_PARAM_NAME = "modifier";

    public CloseDocument(String text, Set<PaymentNFD> payments) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setText(text);
        setPayments(payments);
    }

    public CloseDocument() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return CloseDocumentResponse.class;
    }

    public String getText() {
        return (String) getMethodParam(TEXT_PARAM_NAME);
    }

    public void setText(String text) {
        putMethodParam(TEXT_PARAM_NAME, text);
    }

    public Set<PaymentNFD> getPayments() {
        return (Set<PaymentNFD>) getMethodParam(PAYMENTS_PARAM_NAME);
    }

    public void setPayments(Set<PaymentNFD> payments) {
        putMethodParam(PAYMENTS_PARAM_NAME, payments);
    }

    public Set<Integer> getTaxGroupNumbers() {
        return (Set<Integer>) getMethodParam(TAX_GROUP_NUMBERS_PARAM_NAME);
    }

    public void setTaxGroupNumbers(Set<Integer> taxGroupNumbers) {
        putMethodParam(TAX_GROUP_NUMBERS_PARAM_NAME, taxGroupNumbers);
    }

    public Modifier getModifier() {
        return (Modifier) getMethodParam(MODIFIER_PARAM_NAME);
    }

    public void setModifier(Modifier modifier) {
        putMethodParam(MODIFIER_PARAM_NAME, modifier);
    }
}
