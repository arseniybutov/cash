package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShtrihItemCodeTest {

    @Test
    public void SRTZ_156_FootwearKtnTest() {
        assertEquals(0x1520, new ShtrihItemCode("1520", 4612345678900L, "serialdata").getIntMarking());
        assertEquals(0x0005, new ShtrihItemCode("0005", 4612345678900L, "serialdata").getIntMarking());
    }
}