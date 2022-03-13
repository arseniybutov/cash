package ru.crystals.pos.barcodeprocessing.transformer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.barcodeprocessing.ProductFinder;
import ru.crystals.pos.barcodeprocessing.transformer.config.TransformerBarcodeProcessorConfig;
import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.util.JsonMappers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransformerBarcodeProcessorTest {

    private static final String ALL_CONFIG = "transformer_config_all_features.json";
    private static final String DECATHLON_CONFIG = "transformer_config_decathlon.json";
    private static final String MINIMAL_CONFIG = "transformer_config_minimal.json";
    private static final String WEIGHT_FROM_SUM = "transformer_config_weight_from_sum.json";
    private static final String SIMPLE_WEIGHT = "transformer_config_simple_weight_barcode.json";
    private static final String WITH_QUANTITY = "transformer_config_barcode_with_quantity.json";

    @Mock
    private ProductFinder pf;

    @InjectMocks
    private TransformerBarcodeProcessor processor;

    private ProductEntity makeProduct(String item, String barcode, String name, long priceValue) {
        return makeProduct(item, barcode, name, priceValue, 1.0);

    }

    private ProductEntity makeProduct(String item, String barcode, String name, long priceValue, double precision) {
        ProductEntity product = new ProductEntity();
        product.setPrecision(precision);
        product.setItem(item);
        product.setName(name);
        if (barcode != null) {
            product.setBarCode(new BarcodeEntity(barcode));
        }
        PriceEntity price = new PriceEntity();
        price.setPrice(priceValue);
        product.setPrice(price);
        product.setProductConfig(new ProductConfig());
        return product;
    }

    /**
     * Проверка ШК мастерской для декатлона
     * Пример ШК: 369621999238059155443311643000
     */
    @Test
    public void processBarcodeDecathlonConfig() throws Exception {
        String productItem = "1000000001";
        when(pf.findProduct(productItem, InsertType.HAND)).thenReturn(makeProduct(productItem, null, "Счет мастерской", 0L));

        configure(DECATHLON_CONFIG);

        String shopNumber = "369621";
        String operationCode = "999";
        String increment = "238059";
        String controlDigit = "1";
        String currency = "643";
        String quantity = "000";
        long price = 55443311L;
        String voucherNumber = shopNumber + operationCode + increment + controlDigit;

        String fullBarcode = voucherNumber + price + currency + quantity;

        ProductEntity product = processor.processBarcode(fullBarcode, InsertType.HAND).getProductEntity();

        assertNotNull("Product expected to be found", product);
        assertEquals(productItem, product.getItem());
        assertEquals(price, product.getPrice().getPrice());
        assertNull("Transformer barcode just sets the price (not changes it)", product.getBeforeManualPrice());
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("BrandOwner", "11");
        expectedProperties.put("WorkshopVoucher", voucherNumber);
        ProductPositionData productPositionData = product.getProductPositionData();
        assertEquals("Invalid mapped properties", expectedProperties, productPositionData.getProperties());
        assertFalse("Position price should not be editable", productPositionData.getCanChangePrice().orElse(true));
        assertFalse("Position quantity should not be editable", productPositionData.getCanChangeQuantity().orElse(true));
        assertFalse("Position should not be discountable", product.getIsDiscountApplicable());
    }

    @Test
    public void processBarcodeMinimalConfig() throws Exception {
        String ean13 = "4903619000002";
        String productItem = "4903619";
        long price = 150_12L;

        when(pf.findProduct(ean13, InsertType.SCANNER)).thenReturn(makeProduct(productItem, ean13, "Товар", price));

        configure(MINIMAL_CONFIG);
        String fullBarcode = "9999" + ean13 + "1";

        ProductEntity product = processor.processBarcode(fullBarcode, InsertType.SCANNER).getProductEntity();

        assertNotNull("Product expected to be found", product);
        assertEquals(productItem, product.getItem());
        assertEquals(price, product.getPrice().getPrice());
        assertNull("Transformer barcode just sets the price (not changes it)", product.getBeforeManualPrice());

        ProductPositionData productPositionData = product.getProductPositionData();
        assertTrue("No properties expected", productPositionData.getProperties().isEmpty());
        assertTrue("Position price should be editable by default (if not changed)", productPositionData.getCanChangePrice().orElse(true));
        assertFalse("Position quantity should not be editable by default", productPositionData.getCanChangeQuantity().orElse(true));
        assertTrue("Position should be discountable by default", product.getIsDiscountApplicable());
    }

    @Test
    public void processBarcodeAllFeaturesConfig() throws Exception {
        String productItem = "1000000001";
        when(pf.findProduct(productItem, InsertType.SCANNER)).thenReturn(makeProduct(productItem, null, "Счет мастерской", 0L));

        configure(ALL_CONFIG);

        String operationCode = "999";
        String increment = "238059";
        long price = 55443311L;

        String fullBarcode = operationCode + increment + price;

        ProductEntity product = processor.processBarcode(fullBarcode, InsertType.SCANNER).getProductEntity();

        assertNotNull("Product expected to be found", product);
        assertEquals(productItem, product.getItem());
        assertEquals(price, product.getPrice().getPrice());
        assertNull("Transformer barcode just sets the price (not changes it)", product.getBeforeManualPrice());
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("Increment", "238059");
        expectedProperties.put("OpCode", "AB999CD");
        ProductPositionData productPositionData = product.getProductPositionData();
        assertEquals("Invalid mapped properties", expectedProperties, productPositionData.getProperties());
        assertTrue("Position price should be editable", productPositionData.getCanChangePrice().orElse(false));
        assertTrue("Position quantity should be editable", productPositionData.getCanChangeQuantity().orElse(false));
        assertFalse("Position should not be discountable", product.getIsDiscountApplicable());
    }

    @Test
    public void simpleWeightBarcode() throws IOException {
        configure(SIMPLE_WEIGHT);
        // префикс 21, код/шк 02504, вес 00125, контрольный разряд 0
        final String weightBarcode = "2102504001250";

        final ProductEntity srcProduct = makeProduct("4903619", "2102504", "Товар", 229L, 0.001);
        when(pf.findProduct("2102504", InsertType.SCANNER)).thenReturn(srcProduct);

        ProductEntity foundProduct = processor.processBarcode(weightBarcode, InsertType.SCANNER).getProductEntity();

        assertNotNull("Product expected to be found", foundProduct);
        assertEquals(srcProduct.getItem(), foundProduct.getItem());
        ProductPositionData productPositionData = foundProduct.getProductPositionData();
        assertEquals((Long) 125L, productPositionData.getWeight());
        assertEquals(229L, foundProduct.getPrice().getPrice());
        assertEquals((Integer) 3, productPositionData.getWeightScale());

        assertTrue("No properties expected", productPositionData.getProperties().isEmpty());
        assertFalse("Position quantity should not be editable (quantity is set by barcode)", productPositionData.getCanChangeQuantity().orElse(true));
        assertTrue("Position price should be editable by default (if not changed)", productPositionData.getCanChangePrice().orElse(true));
        assertTrue("Position should be discountable by default", foundProduct.getIsDiscountApplicable());
    }

    @Test
    public void pieceQuantityBarcode() throws IOException {
        configure(WITH_QUANTITY);
        // префикс 25, код/шк 1234567, кол-во 34 шт, контрольный разряд 0
        final String weightBarcode = "2512345670340";

        final ProductEntity srcProduct = makeProduct("4903619", "1234567", "Товар", 229L);
        when(pf.findProduct("1234567", InsertType.SCANNER)).thenReturn(srcProduct);

        ProductEntity foundProduct = processor.processBarcode(weightBarcode, InsertType.SCANNER).getProductEntity();

        assertNotNull("Product expected to be found", foundProduct);
        assertEquals(srcProduct.getItem(), foundProduct.getItem());
        ProductPositionData productPositionData = foundProduct.getProductPositionData();
        assertEquals(229L, foundProduct.getPrice().getPrice());
        assertEquals((Long) 34L, productPositionData.getWeight());
        assertEquals((Integer) 0, productPositionData.getWeightScale());

        assertFalse("Position quantity should not be editable (quantity is set by barcode)", productPositionData.getCanChangeQuantity().orElse(true));
        assertTrue("Position price should be editable by default (if not changed)", productPositionData.getCanChangePrice().orElse(true));
        assertTrue("Position should be discountable by default", foundProduct.getIsDiscountApplicable());
    }

    @Test
    public void processWeightBarcodeWithSum() throws Exception {
        // префикс 21, код/шк 02504, сумма 00125, контрольный разряд 0
        final String weightBarcodeWithSum = "2102504001250";

        final ProductEntity srcProduct = makeProduct("4903619", "02504", "Товар", 229L, 0.001);
        when(pf.findProduct("02504", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 1_25L;
        final long expectedWeight = 546L;
        final long expectedPrice = 229L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    @Test
    public void processWeightBarcodeWithSumRecalculatedPriceCase1() throws Exception {
        // префикс 21, код/шк 02504, сумма 00137, контрольный разряд 0
        final String weightBarcodeWithSum = "2102504001370";

        final ProductEntity srcProduct = makeProduct("4903619", "02504", "Товар", 7_91L, 0.001);
        when(pf.findProduct("02504", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 1_37L;
        final long expectedWeight = 173L;
        final long expectedPrice = 7_92L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    @Test
    public void processWeightBarcodeWithSumRecalculatedPriceCase2() throws Exception {
        // префикс 21, код/шк 02598, сумма 11234, контрольный разряд 7
        final String weightBarcodeWithSum = "2102598112347";

        final ProductEntity srcProduct = makeProduct("4903619", "02598", "Товар", 29_00L, 0.001);
        when(pf.findProduct("02598", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 112_34L;
        final long expectedWeight = 3_875L;
        final long expectedPrice = 28_99L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    @Test
    public void processWeightBarcodeWithSum_lowSum() throws Exception {
        // префикс 21, код/шк 02929, сумма 00004, контрольный разряд 3
        final String weightBarcodeWithSum = "2102929000043";

        final ProductEntity srcProduct = makeProduct("4903619", "02929", "Товар", 997_99L, 0.001);
        when(pf.findProduct("02929", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 4L;
        final long expectedWeight = 1_000L;
        final long expectedPrice = 4L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    @Test
    public void processWeightBarcodeWithSum_lowSumLowPrice() throws Exception {
        // префикс 21, код/шк 12000, сумма 00001, контрольный разряд 6
        final String weightBarcodeWithSum = "2112000000015";

        final ProductEntity srcProduct = makeProduct("4903619", "12000", "Товар", 9L, 0.001);
        when(pf.findProduct("12000", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 1L;
        final long expectedWeight = 111L;
        final long expectedPrice = 9L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    @Test
    public void processWeightBarcodeWithSum_precision() throws Exception {
        // префикс 22, код/шк 59001, сумма 01184
        final String weightBarcodeWithSum = "2259001011840";

        final ProductEntity srcProduct = makeProduct("4903619", "59001", "Товар", 1184_23L, 0.1);
        when(pf.findProduct("59001", InsertType.SCANNER)).thenReturn(srcProduct);

        final long expectedSum = 11_84L;
        final long expectedWeight = 1_000L;
        final long expectedPrice = 11_84L;

        verifyProcessWeightBarcodeWithSum(weightBarcodeWithSum, srcProduct, expectedSum, expectedWeight, expectedPrice);
    }

    private void verifyProcessWeightBarcodeWithSum(String fullBarcode, ProductEntity srcProduct, long expectedSum, long expectedWeight, long expectedPrice) throws IOException {
        configure(WEIGHT_FROM_SUM);

        ProductEntity foundProduct = processor.processBarcode(fullBarcode, InsertType.SCANNER).getProductEntity();

        assertNotNull("Product expected to be found", foundProduct);
        assertEquals(srcProduct.getItem(), foundProduct.getItem());
        ProductPositionData productPositionData = foundProduct.getProductPositionData();
        assertEquals((Long) expectedSum, CurrencyUtil.getPositionSum(foundProduct.getPrice().getPrice(), productPositionData.getWeight()));
        assertEquals((Long) expectedWeight, productPositionData.getWeight());
        assertEquals(expectedPrice, foundProduct.getPrice().getPrice());
        assertEquals((Integer) 3, productPositionData.getWeightScale());

        assertNull("Transformer barcode just sets the price (not changes it)", foundProduct.getBeforeManualPrice());
        assertTrue("No properties expected", productPositionData.getProperties().isEmpty());
        assertFalse("Position quantity should not be editable (quantity is set by barcode)", productPositionData.getCanChangeQuantity().orElse(true));
        assertFalse("Position price should not be editable by default (price is set by barcode)", productPositionData.getCanChangePrice().orElse(true));
        assertTrue("Position should be discountable by default", foundProduct.getIsDiscountApplicable());
    }

    private void configure(String configName) throws IOException {
        TransformerBarcodeProcessorConfig config = JsonMappers.getDefaultMapper()
                .readValue(this.getClass().getResource("/" + configName), TransformerBarcodeProcessorConfig.class);
        processor.configure(config);
    }

}