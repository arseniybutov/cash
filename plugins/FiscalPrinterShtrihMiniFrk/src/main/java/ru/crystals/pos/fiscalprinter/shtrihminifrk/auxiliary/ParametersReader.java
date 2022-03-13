package ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary;

import java.math.BigInteger;
import java.util.Collection;

/**
 * Считывает и возвращает {@link ShtrihParameter настроечные параметры} устройств семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public interface ParametersReader {

    /**
     * Считывает и возвращает настройки.
     * 
     * @return не <code>null</code> - в крайнем случае вернет пустую коллекцию; в списке элементов <code>null</code>'ей тоже не будет; также в списке
     *         элементов не будет невалидных значений: т.е., если {@link ShtrihParameter#getFieldType()} == {@link ShtrihFieldType#NUMBER}, то
     *         {@link ShtrihParameter#getValue() значение} этого параметра можно скормить конструктору {@link BigInteger} и ошибок при этом не будет.
     */
    Collection<ShtrihParameter> readParameters();
}