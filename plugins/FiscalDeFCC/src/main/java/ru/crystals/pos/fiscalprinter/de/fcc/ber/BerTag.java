package ru.crystals.pos.fiscalprinter.de.fcc.ber;

/**
 *
 * @author dalex
 * @param <T>
 *
 * ASN.1 tag reader interface
 */
public interface BerTag<T> {
    T read(BerTagData dataTag);
}
