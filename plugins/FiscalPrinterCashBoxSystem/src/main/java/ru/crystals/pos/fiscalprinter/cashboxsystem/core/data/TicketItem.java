package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание позиции чека. Если явно не отключено округление параметром is_round_total,
 * то результирующая сумма позиции считается по формуле:
 * <br><b>round((price * count) - discount_sum + markup_sum)</b></br>
 */
public class TicketItem {

    /**
     * Наименование позиции
     */
    @JsonProperty("name")
    private String name;
    /**
     * Стоимость одной единицы
     */
    @JsonProperty("price")
    private CbsMoney price;
    /**
     * Количество с точностью до 2-ух знаков. Количество передаваемых знаков после запятой желательно урезать до 2-3
     */
    @JsonProperty("count")
    private Double count;
    @JsonProperty("section")
    private Section section;
    /**
     * Процент скидки. Это поле будет игнорироваться, если передается поле discount_sum
     */
    @JsonProperty("discount_percent")
    private Double discountPercent;
    /**
     * Сумма скидки.
     */
    @JsonProperty("discount_sum")
    private CbsMoney discountSum;
    /**
     * Сумма наценки. Это поле будет игнорироваться, если передается поле или discount_sum или discount_percent
     */
    @JsonProperty("markup_sum")
    private CbsMoney markupSum;
    /**
     * роцент наценки. Это поле будет игнорироваться, если передается поле markup_sum или discount_sum или discount_percent
     */
    @JsonProperty("markup_percent")
    private Double markupPercent;
    /**
     * Номер акцизной марки.
     */
    @JsonProperty("excise_stamp")
    private String exciseStamp;
    /**
     * Производить математическое округление позиции (по умолчанию в CBS true)
     */
    @JsonProperty("is_round_total")
    private Boolean isRoundTotal = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CbsMoney getPrice() {
        return price;
    }

    public void setPrice(CbsMoney price) {
        this.price = price;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public CbsMoney getDiscountSum() {
        return discountSum;
    }

    public void setDiscountSum(CbsMoney discountSum) {
        this.discountSum = discountSum;
    }

    public CbsMoney getMarkupSum() {
        return markupSum;
    }

    public void setMarkupSum(CbsMoney markupSum) {
        this.markupSum = markupSum;
    }

    public Double getMarkupPercent() {
        return markupPercent;
    }

    public void setMarkupPercent(Double markupPercent) {
        this.markupPercent = markupPercent;
    }

    public String getExciseStamp() {
        return exciseStamp;
    }

    public void setExciseStamp(String exciseStamp) {
        this.exciseStamp = exciseStamp;
    }

    public boolean isRoundTotal() {
        return isRoundTotal;
    }

    public void setRoundTotal(boolean roundTotal) {
        isRoundTotal = roundTotal;
    }
}
