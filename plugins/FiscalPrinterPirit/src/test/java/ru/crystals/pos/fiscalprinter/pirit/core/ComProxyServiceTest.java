package ru.crystals.pos.fiscalprinter.pirit.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComProxyServiceTest {

    @Test
    public void isBinaryPathTest() {
        assertTrue(ComProxyService.isBinaryPath("    BINARY_PATH_NAME   : D:\\Comproxy\\ComProxySrv.exe //RS//ComProxy"));
        assertTrue(ComProxyService.isBinaryPath("        Имя_двоичного_файла  : D:\\Comproxy\\ComProxySrv.exe //RS//ComProxy  "));
    }

    @Test
    public void extractPathTest() {
        testExtract("    BINARY_PATH_NAME   : D:\\Comproxy\\ComProxySrv.exe //RS//ComProxy");
        testExtract("        Имя_двоичного_файла  : D:\\Comproxy\\ComProxySrv.exe //RS//ComProxy  ");
        testExtract("        Имя_двоичного_файла  : \"D:\\Comproxy\\ComProxySrv.exe\" //RS//ComProxy  ");
    }

    private void testExtract(String s) {
        final String actualRuQuotes = ComProxyService.extractInstallPath(s);
        assertEquals("D:\\Comproxy", actualRuQuotes);
    }
}