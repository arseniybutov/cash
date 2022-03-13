package ru.crystals.pos.barcodeprocessing.markdown;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.barcodeprocessing.ProductFinder;
import ru.crystals.pos.barcodeprocessing.markdown.config.MarkdownBarcodeConfig;
import ru.crystals.pos.barcodeprocessing.processors.result.BarcodeProcessResult;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.techprocess.Reason;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.user.Right;
import ru.crystals.util.JsonMappers;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarkdownBarcodeProcessorTest {

    private static final String EAN13 = "4903619000002";
    private static final long PRICE_VALUE = 9_999_999;
    private static final ProductEntity EAN13_PRODUCT = makeProduct(EAN13, PRICE_VALUE);
    private final Reason REASON_11_WITH_DISCOUNTS = new Reason(11, "Причина со скидками", false, true);
    private final Reason REASON_23_WO_DISCOUNTS = new Reason(23, "Причина без скидок", false, false);

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private TechProcessInterface tp;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private ProductFinder pf;

    @InjectMocks
    private MarkdownBarcodeProcessor processor;

    @Before
    public void setUp() throws Exception {
        configure("markdown_config.json");

        when(pf.findProduct(EAN13, InsertType.SCANNER)).thenReturn(makeProduct(EAN13, PRICE_VALUE));
        when(tp.getPriceChangeReasons()).thenReturn(Arrays.asList(
                REASON_11_WITH_DISCOUNTS,
                REASON_23_WO_DISCOUNTS
        ));
    }

    @Test
    public void processValidBarcodeWithDiscountableReason() throws Exception {
        String mdBarcode = "997" + EAN13 + "111234567891";

        boolean supports = processor.supportsBarcode(mdBarcode);
        assertTrue(supports);

        ProductEntity found = processor.processBarcode(mdBarcode, InsertType.SCANNER).getProductEntity();
        assertEquals(EAN13_PRODUCT, found);

        assertEquals(1234567L, found.getPrice().getPrice());
        assertEquals(PRICE_VALUE, (long) found.getBeforeManualPrice());
        ProductPositionData productPositionData = found.getProductPositionData();
        assertFalse("Position added by markdown should have not editable quantity", productPositionData.getCanChangeQuantity().orElseThrow(AssertionError::new));
        assertFalse("Position added by markdown should have not editable price", productPositionData.getCanChangePrice().orElseThrow(AssertionError::new));
        assertEquals(REASON_11_WITH_DISCOUNTS, found.getReasonPriceCorrection());
        assertTrue("Product should be discountable as per reason", found.getIsDiscountApplicable());
    }

    @Test
    public void processValidBarcodeWithNotDiscountableReason() throws Exception {
        String mdBarcode = "997" + EAN13 + "231234567891";

        boolean supports = processor.supportsBarcode(mdBarcode);
        assertTrue(supports);

        ProductEntity found = processor.processBarcode(mdBarcode, InsertType.SCANNER).getProductEntity();
        assertEquals(EAN13_PRODUCT, found);

        assertEquals(1234567L, found.getPrice().getPrice());
        assertEquals(PRICE_VALUE, (long) found.getBeforeManualPrice());
        ProductPositionData productPositionData = found.getProductPositionData();
        assertFalse("Position added by markdown should have not editable quantity", productPositionData.getCanChangeQuantity().orElseThrow(AssertionError::new));
        assertFalse("Position added by markdown should have not editable price", productPositionData.getCanChangePrice().orElseThrow(AssertionError::new));
        assertEquals(REASON_23_WO_DISCOUNTS, found.getReasonPriceCorrection());
        assertFalse("Product should be not discountable as per reason", found.getIsDiscountApplicable());
    }

    @Test
    public void processValidBarcodeWithUnknownReason() throws Exception {
        String mdBarcode = "997" + EAN13 + "760000123891";

        boolean supports = processor.supportsBarcode(mdBarcode);
        assertTrue(supports);

        ProductEntity found = processor.processBarcode(mdBarcode, InsertType.SCANNER).getProductEntity();
        assertEquals(EAN13_PRODUCT, found);

        assertEquals(123L, found.getPrice().getPrice());
        assertEquals(PRICE_VALUE, (long) found.getBeforeManualPrice());
        ProductPositionData productPositionData = found.getProductPositionData();
        assertFalse("Position added by markdown should have not editable quantity", productPositionData.getCanChangeQuantity().orElseThrow(AssertionError::new));
        assertFalse("Position added by markdown should have not editable price", productPositionData.getCanChangePrice().orElseThrow(AssertionError::new));
        assertEquals(new Reason(76, "", false, false), found.getReasonPriceCorrection());
        assertFalse("Product should be not discountable when no reason", found.getIsDiscountApplicable());

        verify(pf).findProduct(EAN13, InsertType.SCANNER);
    }

    @Test
    public void processValidBarcodeByNotScanner() throws Exception {
        String mdBarcode = "997" + EAN13 + "760000123891";

        boolean supports = processor.supportsBarcode(mdBarcode);
        assertTrue(supports);

        ProductEntity found = processor.processBarcode(mdBarcode, InsertType.HAND).getProductEntity();
        assertNull(found);

        verify(pf, never()).findProduct(anyString(), any(InsertType.class));
    }

    @Test
    public void processInvalidBarcode() {
        assertFalse("Invalid length", processor.supportsBarcode("997" + EAN13 + "760000123891000"));
        assertFalse("Invalid fixed value (last digit)", processor.supportsBarcode("997" + EAN13 + "760000123892"));
        assertFalse("Invalid prefix", processor.supportsBarcode("998" + EAN13 + "760000123891"));
    }

    @Test
    public void processValidBarcodeWithTwoMasks() throws Exception {
        when(pf.findProduct(EAN13, InsertType.HAND)).thenReturn(EAN13_PRODUCT);

        configure("markdown_config_two_masks.json");

        String mdBarcode1 = "997" + EAN13 + "0000123";
        String mdBarcode2 = "99800" + EAN13 + "0000123";

        assertTrue(processor.supportsBarcode(mdBarcode1));
        assertTrue(processor.supportsBarcode(mdBarcode2));

        ProductEntity found1 = processor.processBarcode(mdBarcode1, InsertType.HAND).getProductEntity();
        ProductEntity found2 = processor.processBarcode(mdBarcode2, InsertType.HAND).getProductEntity();
        assertEquals(EAN13_PRODUCT, found1);
        assertEquals(EAN13_PRODUCT, found2);

        assertNull(found1.getReasonPriceCorrection());
        assertNull(found2.getReasonPriceCorrection());

        assertFalse(found1.getIsDiscountApplicable());
        assertFalse(found2.getIsDiscountApplicable());

        verify(pf, times(2)).findProduct(EAN13, InsertType.HAND);
    }

    private void configure(String configName) throws IOException {
        MarkdownBarcodeConfig config = JsonMappers.getDefaultMapper()
                .readValue(this.getClass().getResource("/" + configName), MarkdownBarcodeConfig.class);
        processor.configure(config);
    }

    private static ProductEntity makeProduct(String item, long priceValue) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setItem(item);
        PriceEntity price = new PriceEntity();
        price.setPrice(priceValue);
        productEntity.setPrice(price);
        return productEntity;
    }

    @Test
    public void testProcessorSupports15SignsDiscountBarcode() throws Exception {
        String code = "000004";
        long price = 5000;
        String barcode = "99" + code + "000" + price;
        configure("markdown_config_15signs.json");
        assertTrue(processor.supportsBarcode(barcode));
    }

    @Test
    public void testProcess15SignsDiscountBarcodeWithRights() throws Exception {
        String code = "000004";
        long price = 5000;
        int reasonPriceCorrection = 10;
        String barcode = "99" + code + "000" + price;
        ProductEntity product = makeProduct(code, price);

        when(pf.findProduct(code, InsertType.SCANNER)).thenReturn(product);
        when(tp.checkUserRight(Right.valueOf("SALE_CORRECTION_VALUE"))).thenReturn(true);

        configure("markdown_config_15signs.json");

        ProductEntity found = processor.processBarcode(barcode, InsertType.SCANNER).getProductEntity();
        assertEquals(product, found);
        assertEquals(found.getReasonPriceCorrection().getCode(), reasonPriceCorrection);
        assertFalse(found.getIsDiscountApplicable());
    }

    @Test
    public void testProcess15SignsDiscountBarcodeWithoutRights() throws Exception {
        String code = "000004";
        long price = 5000;
        String barcode = "99" + code + "000" + price;
        ProductEntity product = makeProduct(code, price);

        when(pf.findProduct(code, InsertType.SCANNER)).thenReturn(product);
        when(tp.checkUserRight(Right.valueOf("SALE_CORRECTION_VALUE"))).thenReturn(false);

        configure("markdown_config_15signs.json");

        BarcodeProcessResult processResult = processor.processBarcode(barcode, InsertType.SCANNER);
        assertEquals(processResult.getResultCode(), BarcodeProcessResult.ResultCode.ACCESS_DENIED);
    }

    @Test
    public void testProcessDiscountBarcodeForVerniyWithPrefix() throws Exception {
        String prefix = "0";
        String shortCode = "050011";
        String fullCode = prefix + shortCode;
        long price = 5000;
        String barcode = "99" + shortCode + "000" + price;
        ProductEntity product = makeProduct(fullCode, price);

        when(pf.findProduct(fullCode, InsertType.SCANNER)).thenReturn(product);
        when(tp.checkUserRight(Right.valueOf("SALE_CORRECTION_VALUE"))).thenReturn(true);

        configure("markdown_config_verniy.json");

        assertTrue(processor.supportsBarcode(barcode));
        ProductEntity found = processor.processBarcode(barcode, InsertType.SCANNER).getProductEntity();
        assertEquals(product, found);
    }

}