package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.addcommodity.AddCommodityResponse;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Указание позиции в открытом чеке. Поля «name» и «code» взаимоисключающие, обязательно указывать хотя бы
 * одно из них.
 */
public class AddCommodity extends BaseRequest {

    private static final String METHOD_NAME = "addCommodity";

    /**
     * Цена товара/услуги.
     */
    private static final String PRICE_PARAM_NAME = "price";

    /**
     * Количество товара/услуги.
     */
    private static final String QUANTITY_PARAM_NAME = "quantity";

    /**
     * Наименование товара/услуги.
     */
    private static final String NAME_PARAM_NAME = "name";

    /**
     * Код товара/услуги.
     */
    private static final String CODE_PARAM_NAME = "code";

    /**
     * Номер секции.
     */
    private static final String SECTION_NUMBER_PARAM_NAME = "sectionNumber";

    /**
     * Группы налогообложения. Налоги на весь чек и налоги
     * на позицию являются взаимоисключающими.
     */
    private static final String TAX_GROUP_NUMBERS_PARAM_NAME = "taxGroupNumbers";

    /**
     * Модификатор (скидка, надбавка) на позицию.
     */
    private static final String MODIFIER_PARAM_NAME = "modifier";

    /**
     * Акцизная марка
     */
    private static final String EXCISE_STAMP = "exciseStamp";


    public AddCommodity() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    public AddCommodity(BigDecimal price, BigDecimal quantity, String name, Long code, Integer sectionNumber, Set<Integer> taxGroupNumbers, Modifier modifier) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setPrice(price);
        setQuantity(quantity);
        setName(name);
        setCode(code);
        setSectionNumber(sectionNumber);
        setTaxGroupNumbers(taxGroupNumbers);
        setModifier(modifier);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return AddCommodityResponse.class;
    }

    public BigDecimal getPrice() {
        return (BigDecimal) getMethodParam(PRICE_PARAM_NAME);
    }

    public void setPrice(BigDecimal price) {
        putMethodParam(PRICE_PARAM_NAME, price);
    }

    public BigDecimal getQuantity() {
        return (BigDecimal) getMethodParam(QUANTITY_PARAM_NAME);
    }

    public void setQuantity(BigDecimal quantity) {
        putMethodParam(QUANTITY_PARAM_NAME, quantity);
    }

    public String getName() {
        return (String) getMethodParam(NAME_PARAM_NAME);
    }

    public void setName(String name) {
        putMethodParam(NAME_PARAM_NAME, name);
    }

    public Long getCode() {
        return (Long) getMethodParam(CODE_PARAM_NAME);
    }

    public void setCode(Long code) {
        putMethodParam(CODE_PARAM_NAME, code);
    }

    public Integer getSectionNumber() {
        return (Integer) getMethodParam(SECTION_NUMBER_PARAM_NAME);
    }

    public void setSectionNumber(Integer sectionNumber) {
        putMethodParam(SECTION_NUMBER_PARAM_NAME, sectionNumber);
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

    public void setExciseStamp(String exciseStamp) {
        putMethodParam(EXCISE_STAMP, exciseStamp);
    }

    public String getExciseStamp() {
        return (String) getMethodParam(EXCISE_STAMP);
    }
}
