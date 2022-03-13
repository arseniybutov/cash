package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;

public class ShtrihDriverConnectorTest {

    private ShtrihDriverConnector shtrihDriverConnector = new ShtrihDriverConnector(new ShtrihConfiguration());

    @Test
    public void convertTextForPrintTest() {
        String good1 = "Кефир малиновый с  14.23     *1     =14.23";
        String good2 = "Kafé mimi          27.23     *1     =27.23";
        String good_empty = "";
        String good_null = null;

        String good1ReadyForPrint = "Кефир малиновый с  14.23     *1     =14.23";
        String good2ReadyForPrint = "Kaf? mimi          27.23     *1     =27.23";

        Assert.assertEquals(good1ReadyForPrint, shtrihDriverConnector.convertTextForPrint(good1));
        Assert.assertEquals(good2ReadyForPrint, shtrihDriverConnector.convertTextForPrint(good2));
        Assert.assertEquals(good_empty, shtrihDriverConnector.convertTextForPrint(good_empty));
        Assert.assertNull(shtrihDriverConnector.convertTextForPrint(good_null));
    }

    @Test
    public void getFontSizeTest() {
        shtrihDriverConnector.setUseFontsFromTemplate(true);
        FontLine line = new FontLine("Shopname: Steam");
        line.setFont(null);
        Assert.assertEquals(1, shtrihDriverConnector.getFontSizeForLine(line));

        line = new FontLine("Address: Black Mesa Street 24", Font.DOUBLEHEIGHT);
        Assert.assertEquals(2, shtrihDriverConnector.getFontSizeForLine(line));

        line = new FontLine("Position: Valve Index", Font.SMALL, 4);
        Assert.assertEquals(3, shtrihDriverConnector.getFontSizeForLine(line));

        line = new FontLine("Position: Half-Life Alyx", Font.DOUBLEWIDTH, 1);
        Assert.assertEquals(4, shtrihDriverConnector.getFontSizeForLine(line));

        line = new FontLine("Position: Half-Life 3");
        line.setFont(null);
        line.setConcreteFont(4);
        Assert.assertEquals(4, shtrihDriverConnector.getFontSizeForLine(line));

        line = new FontLine("Position: Portal 3");
        line.setFont(null);
        line.setConcreteFont(0);
        Assert.assertEquals(1, shtrihDriverConnector.getFontSizeForLine(line));
    }

}
