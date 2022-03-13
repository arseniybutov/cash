package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Modifier {
    /**
     * Тип модификатора.
     */
    private ModifierType type;

    /**
     * Наименование модификатора.
     */
    private String name;

    /**
     * Сумма модификатора.
     */
    private BigDecimal sum;

    /**
     * Номера групп налогооблажения.
     */
    private Set<Integer> taxGroupNumbers = new HashSet<>();

    public ModifierType getType() {
        return type;
    }

    public void setType(ModifierType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public Set<Integer> getTaxGroupNumbers() {
        return taxGroupNumbers;
    }

    public void setTaxGroupNumbers(Set<Integer> taxGroupNumbers) {
        this.taxGroupNumbers = taxGroupNumbers;
    }

    public void addTaxGroupNumber(Integer taxGroupNumber) {
        this.taxGroupNumbers.add(taxGroupNumber);
    }

}
