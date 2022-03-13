package ru.crystals.pos.barcodeprocessing.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleId;
import ru.crystals.bundles.BundleRef;
import ru.crystals.bundles.ContextBundle;
import ru.crystals.pos.barcodeprocessing.ProductFinder;
import ru.crystals.pos.barcodeprocessing.processors.BarcodeProcessorPlugin;
import ru.crystals.pos.barcodeprocessing.processors.BarcodeProcessorUtils;
import ru.crystals.pos.barcodeprocessing.processors.config.BarcodeMaskResult;
import ru.crystals.pos.barcodeprocessing.processors.config.ComputableValue;
import ru.crystals.pos.barcodeprocessing.processors.config.FoundProductField;
import ru.crystals.pos.barcodeprocessing.processors.config.PositionAttributes;
import ru.crystals.pos.barcodeprocessing.processors.config.PositionData;
import ru.crystals.pos.barcodeprocessing.processors.config.PositionProperty;
import ru.crystals.pos.barcodeprocessing.processors.config.ProductRef;
import ru.crystals.pos.barcodeprocessing.processors.mask.MatchedMaskResult;
import ru.crystals.pos.barcodeprocessing.processors.mask.PreparedBarcodeField;
import ru.crystals.pos.barcodeprocessing.processors.mask.PreparedBarcodeMask;
import ru.crystals.pos.barcodeprocessing.processors.result.BarcodeProcessResult;
import ru.crystals.pos.barcodeprocessing.transformer.config.TransformerBarcodeProcessorConfig;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.check.InsertType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Плагин ШК-трансформер, позволяющий превращать сложные ШК в позицию в чеке с заданными свойствами
 * <p>
 * Принцип работы:
 * - ищем товар (по EAN13 или коду товара из исходного ШК или из конфига)
 * - устанавливаем цену из ШК или конфига (или оставляем ту же) - важно, что этот плагин не формирует события "Изменения цены"
 * - если нужно устанавливаем атрибуты позиции (из конфига) и добавляем нужные плагинные свойства (из ШК или конфига)
 * - если нужно устанавливаем количество из ШК или конфига (или вообще вычисляем его как и цену из суммы)
 */
@ContextBundle(id = {@BundleId(implemented = BarcodeProcessorPlugin.class, name = "transformer")}, lazy = false, type = "all")
public class TransformerBarcodeProcessor implements BarcodeProcessorPlugin<TransformerBarcodeProcessorConfig> {

    private static final Logger log = LoggerFactory.getLogger(TransformerBarcodeProcessor.class);

    private static final String PLUGIN_NAME = "transformer";

    private static final boolean CAN_CHANGE_QUANTITY_DEFAULT = false;

    private List<PreparedBarcodeMask> barcodeMasks;

    @BundleRef
    private ProductFinder productFinder;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public Class<TransformerBarcodeProcessorConfig> getConfigClass() {
        return TransformerBarcodeProcessorConfig.class;
    }

    @Override
    public void configure(TransformerBarcodeProcessorConfig config) {
        barcodeMasks = BarcodeProcessorUtils.preparedBarcodeMasks(config.getMasks(), this::validateMask);
        log.debug("Prepared barcode masks: {}", barcodeMasks.size());
        if (barcodeMasks.isEmpty()) {
            log.warn("No valid barcode masks configured");
        }
    }

    private boolean validateMask(PreparedBarcodeMask mask) {
        try {
            BarcodeMaskResult result = mask.getRawMask().getResult();
            ProductRef productRef = result.getProductRef();
            PositionData position = result.getPosition();
            Objects.requireNonNull(productRef, "'result/productRef' should be set");
            Objects.requireNonNull(position, "'result/position' should be set");
            Set<String> fields = mask.getFields().stream()
                    .map(PreparedBarcodeField::getName)
                    .collect(Collectors.toSet());
            BarcodeProcessorUtils.validateValue(position.getPrice(), fields, "position/price");
            BarcodeProcessorUtils.validateValue(position.getQuantity(), fields, "position/quantity");
            BarcodeProcessorUtils.validateValue(position.getScale(), fields, "position/scale");
            BarcodeProcessorUtils.validateValue(productRef.getBarcode(), fields, "productRef/barcode");
            BarcodeProcessorUtils.validateValue(productRef.getItem(), fields, "productRef/item");
            if (productRef.getBarcode() == null && productRef.getItem() == null) {
                throw new IllegalArgumentException("productRef/barcode or productRef/item should be set");
            }
            if (position.getProperties() != null) {
                for (PositionProperty property : position.getProperties()) {
                    Objects.requireNonNull(property.getName(), "'property/name' should be set");
                    Objects.requireNonNull(property.getValue(), "'property/value' should be set");
                    BarcodeProcessorUtils.validateValue(property.getValue(), fields, "property/name=" + property.getName());
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Invalid mask {}", mask.getRawMask().getDescription(), e);
        }
        return false;
    }

    @Override
    public boolean supportsBarcode(String barcode) {
        if (barcodeMasks.isEmpty()) {
            return false;
        }
        return barcodeMasks.stream().anyMatch(mask -> mask.matches(barcode));
    }

    @Override
    public BarcodeProcessResult processBarcode(String barcode, InsertType insertType) {
        final MatchedMaskResult matchedMask = BarcodeProcessorUtils.findMatchedMask(barcodeMasks, barcode, insertType);
        if (matchedMask == null) {
            return BarcodeProcessResult.createNoProductBarcodeProcessResult();
        }
        BarcodeMaskResult result = matchedMask.getResult();

        final ProductEntity product = findProduct(insertType, matchedMask, result.getProductRef());
        if (product == null) {
            return BarcodeProcessResult.createNoProductBarcodeProcessResult();
        }
        ProductPositionData productPositionData = product.getProductPositionData();

        Optional.ofNullable(product.getPrice())
                .map(PriceEntity::getPrice)
                .ifPresent(p -> matchedMask.getCtx().put(FoundProductField.PRICE, p.toString()));
        matchedMask.getCtx().put(FoundProductField.PRECISION, String.valueOf(product.getPrecision()));

        PositionData position = result.getPosition();
        fillPriceAndQuantity(matchedMask, product, productPositionData, position);
        fillPluginProperties(matchedMask, productPositionData, position);
        fillAttributes(product, productPositionData, position);
        return BarcodeProcessResult.createOkBarcodeProcessResult(product);
    }

    private void fillAttributes(ProductEntity product, ProductPositionData productPositionData, PositionData position) {
        productPositionData.setCanChangeQuantity(CAN_CHANGE_QUANTITY_DEFAULT);

        PositionAttributes attributes = position.getAttributes();
        if (attributes != null) {
            Optional.ofNullable(attributes.getAllowDiscounts()).ifPresent(product::setIsDiscountApplicable);
            Optional.ofNullable(attributes.getCanChangePrice()).ifPresent(productPositionData::setCanChangePrice);
            Optional.ofNullable(attributes.getCanChangeQuantity()).ifPresent(productPositionData::setCanChangeQuantity);
        }
    }

    private void fillPluginProperties(MatchedMaskResult matchedMask, ProductPositionData productPositionData, PositionData position) {
        if (position.getProperties() != null) {
            position.getProperties().forEach(prop -> productPositionData.addProperty(prop.getName(),
                    matchedMask.computeValue(prop.getValue())));
        }
    }

    private void fillPriceAndQuantity(MatchedMaskResult matchedMask,
                                      ProductEntity product,
                                      ProductPositionData productPositionData,
                                      PositionData position) {
        ComputableValue price = position.getPrice();
        ComputableValue quantity = position.getQuantity();
        if (price != null) {
            long computedPrice = Long.parseLong(matchedMask.computeValue(price));
            PriceEntity priceEntity = new PriceEntity();
            priceEntity.setPrice(computedPrice);
            product.setPrice(priceEntity);
            productPositionData.setCanChangePrice(false);
        }
        if (quantity != null) {
            String computedQuantity = matchedMask.computeValue(quantity);
            String scale = Optional.ofNullable(position.getScale()).map(matchedMask::computeValue).orElse("3");
            productPositionData.setWeight(computedQuantity);
            productPositionData.setWeightScale(scale);
            productPositionData.setCanChangeQuantity(false);
        }
    }

    private ProductEntity findProduct(InsertType insertType, MatchedMaskResult matchedMask, ProductRef productRef) {
        String codeToBeFound = matchedMask.computeValue(productRef.getItem());
        if (codeToBeFound == null) {
            codeToBeFound = matchedMask.computeValue(productRef.getBarcode());
        }
        return productFinder.findProduct(codeToBeFound, insertType);
    }

}
