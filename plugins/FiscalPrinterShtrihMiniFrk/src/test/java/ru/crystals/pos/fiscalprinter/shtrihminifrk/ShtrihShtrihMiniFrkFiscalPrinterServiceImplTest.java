package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.test.MockInjectors;

public class ShtrihShtrihMiniFrkFiscalPrinterServiceImplTest {

    private static final float NDS_12 = 12.0f;
    private static final float NDS_18 = 18.0f;
    private static final float NDS_20 = 20.0f;
    private static final float NDS_18_118 = -18.0f;
    private static final float NDS_20_120 = -20.0f;

    private static final String NDS_18_NAME = "НДС 18%";
    private static final String NDS_20_NAME = "НДС 20%";
    private static final String NDS_18_118_NAME = "НДС 18/118";
    private static final String NDS_20_120_NAME = "НДС 20/120";

    private static final String TAXES_FIELD = "taxes";


    private ShtrihMiniFrkFiscalPrinterServiceImpl service = new ShtrihMiniFrkFiscalPrinterServiceImpl();

    private BaseShtrihConfig config = new BaseShtrihConfig();

    @Before
    public void setUp() throws Exception {
        service.setConfig(config);
    }

    @Test
    public void testGetTax18inFROldTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesOld(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_18);

        Assert.assertEquals(1, taxId);
    }

    @Test
    public void testGetTax118inFROldTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesOld(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_18_118);

        Assert.assertEquals(2, taxId);
    }

    @Test
    public void testGetTax20inFROldTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesOld(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_20);

        Assert.assertEquals(1, taxId);
    }

    @Test
    public void testGetTax120inFROldTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesOld(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_20_120);

        Assert.assertEquals(2, taxId);
    }

    @Test(expected = FiscalPrinterException.class)
    public void testTaxNoFound() throws Exception {

        MockInjectors.injectField(service, getTaxesOld(), TAXES_FIELD);
        service.getTaxId(NDS_12);

    }

    @Test
    public void testGetTax20inFRNewTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesNew(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_20);

        Assert.assertEquals(1, taxId);
    }

    @Test
    public void testGetTax120inFRNewTaxes() throws Exception {
        MockInjectors.injectField(service, getTaxesNew(), TAXES_FIELD);
        int taxId = service.getTaxId(NDS_20_120);

        Assert.assertEquals(2, taxId);
    }

    @Test(expected = FiscalPrinterException.class)
    public void testTaxNoFoundNewTaxes() throws Exception {

        MockInjectors.injectField(service, getTaxesNew(), TAXES_FIELD);
        service.getTaxId(NDS_18);

    }

    private ValueAddedTaxCollection getTaxesOld() {
        ValueAddedTaxCollection collection = new ValueAddedTaxCollection();
        collection.addTax(new ValueAddedTax(0, NDS_18, NDS_18_NAME));
        collection.addTax(new ValueAddedTax(1, NDS_18_118, NDS_18_118_NAME));
        return collection;
    }

    private ValueAddedTaxCollection getTaxesNew() {
        ValueAddedTaxCollection collection = new ValueAddedTaxCollection();
        collection.addTax(new ValueAddedTax(0, NDS_20, NDS_20_NAME));
        collection.addTax(new ValueAddedTax(1, NDS_20_120, NDS_20_120_NAME));
        return collection;
    }

}
