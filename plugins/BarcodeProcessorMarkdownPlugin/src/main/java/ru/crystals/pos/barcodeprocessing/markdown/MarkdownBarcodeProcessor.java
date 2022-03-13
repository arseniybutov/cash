package ru.crystals.pos.barcodeprocessing.markdown;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.barcodeprocessing.ProductFinder;
import ru.crystals.pos.barcodeprocessing.markdown.config.MarkdownBarcodeConfig;
import ru.crystals.pos.barcodeprocessing.processors.BarcodeProcessorPlugin;
import ru.crystals.pos.barcodeprocessing.processors.BarcodeProcessorUtils;
import ru.crystals.pos.barcodeprocessing.processors.config.BarcodeMaskResult;
import ru.crystals.pos.barcodeprocessing.processors.config.InputRestrictions;
import ru.crystals.pos.barcodeprocessing.processors.config.PositionData;
import ru.crystals.pos.barcodeprocessing.processors.config.ProductRef;
import ru.crystals.pos.barcodeprocessing.processors.mask.MatchedMaskResult;
import ru.crystals.pos.barcodeprocessing.processors.mask.PreparedBarcodeField;
import ru.crystals.pos.barcodeprocessing.processors.mask.PreparedBarcodeMask;
import ru.crystals.pos.barcodeprocessing.processors.result.BarcodeProcessResult;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.catalog.exception.MinPriceException;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.techprocess.Reason;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.user.Right;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Плагин ШК уценки
 * <p>
 * Принцип работы:
 * - ищем товар (по EAN13 или коду товара из исходного ШК или из конфига)
 * - устанавливаем цену из ШК или конфига (или оставляем ту же) - генерируем событие "Изменения цены"
 */
@Component("markdown")
public class MarkdownBarcodeProcessor implements BarcodeProcessorPlugin<MarkdownBarcodeConfig> {

    private static final Logger log = LoggerFactory.getLogger(MarkdownBarcodeProcessor.class);

    private static final String PLUGIN_ID = "markdown";

    @Autowired
    private TechProcessInterface techProcess;

    @Autowired
    private ProductFinder productFinder;

    private List<PreparedBarcodeMask> barcodeMasks;

    @Override
    public Class<MarkdownBarcodeConfig> getConfigClass() {
        return MarkdownBarcodeConfig.class;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_ID;
    }

    @Override
    public void configure(MarkdownBarcodeConfig config) {
        barcodeMasks = BarcodeProcessorUtils.preparedBarcodeMasks(config.getMasks(), this::validateMask);
        log.debug("Prepared barcode masks: {}", barcodeMasks.size());
        if (barcodeMasks.isEmpty()) {
            log.warn("No valid barcode masks configured");
        }
    }

    private boolean validateMask(PreparedBarcodeMask mask) {
        try {
            BarcodeMaskResult result = mask.getRawMask().getResult();
            PositionData position = result.getPosition();
            ProductRef productRef = result.getProductRef();
            Objects.requireNonNull(position, "'result/position' should be set");
            Objects.requireNonNull(position.getPrice(), "'position/price' should be set");
            Objects.requireNonNull(productRef, "'result/productRef' should be set");
            Set<String> fields = mask.getFields().stream()
                    .map(PreparedBarcodeField::getName)
                    .collect(Collectors.toSet());
            BarcodeProcessorUtils.validateValue(position.getPrice(), fields, "position/price");
            BarcodeProcessorUtils.validateValue(position.getCorrectionReasonCode(), fields, "position/correctionReasonCode");
            BarcodeProcessorUtils.validateValue(productRef.getBarcode(), fields, "productRef/barcode");
            BarcodeProcessorUtils.validateValue(productRef.getItem(), fields, "productRef/item");
            if (productRef.getBarcode() == null && productRef.getItem() == null) {
                throw new IllegalArgumentException("productRef/barcode or productRef/item should be set");
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
    public BarcodeProcessResult processBarcode(String barcode, InsertType insertType) throws Exception {
        final MarkdownBarcode mdBarcode = parse(barcode, insertType);
        if (mdBarcode == null) {
            return BarcodeProcessResult.createNoProductBarcodeProcessResult();
        }

        if (!currentUserAllowedToUseBarcode(mdBarcode)) {
            log.error("User not allowed to use barcode {}", barcode);
            return BarcodeProcessResult.createAccessDeniedBarcodeProcessResult();
        }

        log.debug("Markdown barcode {} parsed: {}", barcode, mdBarcode);
        final String codeToBeFound = StringUtils.defaultString(mdBarcode.getEan13Barcode(), mdBarcode.getProductItem());
        final ProductEntity product = productFinder.findProduct(codeToBeFound, insertType);
        if (product == null) {
            return BarcodeProcessResult.createNoProductBarcodeProcessResult();
        }
        PriceEntity price = product.getPrice();
        product.setBeforeManualPrice(price.getPrice());
        price.setPrice(mdBarcode.getPrice());
        product.setIsDiscountApplicable(false);
        if (mdBarcode.getReasonCode() != null) {
            Reason reason = techProcess.getPriceChangeReasons().stream()
                    .filter(r -> r.getCode() == mdBarcode.getReasonCode())
                    .findFirst()
                    .map(Reason::new)
                    .orElseGet(() -> makeDefaultReason(mdBarcode));
            product.setReasonPriceCorrection(reason);
            product.setIsDiscountApplicable(reason.isNeedApplyDiscount());
        }
        ProductPositionData positionData = product.getProductPositionData();
        boolean canChangeQuantity = false;
        InputRestrictions inputRestrictions = mdBarcode.getInputRestrictions();
        if (inputRestrictions != null) {
            canChangeQuantity = inputRestrictions.canChangeQuantity();
        }
        positionData.setCanChangeQuantity(canChangeQuantity);
        positionData.setCanChangePrice(false);
        checkMrcRestrictions(product);
        return BarcodeProcessResult.createOkBarcodeProcessResult(product);
    }

    /**
     * Проведем проверку на мрц перед тем, как отдать результаты.
     *
     * @param product - продукт
     * @throws MinPriceException - проверка мрц не пройдена, продажа запрещена
     */
    private void checkMrcRestrictions(ProductEntity product) throws MinPriceException {
        long price = Optional.ofNullable(product.getPrice()).map(PriceEntity::getPrice).orElse(0L);
        techProcess.checkProductMinPriceRestrictions(product, price);
    }

    private MarkdownBarcode parse(String barcode, InsertType insertType) {
        MatchedMaskResult matchedMask = BarcodeProcessorUtils.findMatchedMask(barcodeMasks, barcode, insertType);
        if (matchedMask == null) {
            return null;
        }
        try {
            BarcodeMaskResult result = matchedMask.getResult();
            InputRestrictions inputRestrictions = matchedMask.getInputRestrictions();
            String price = matchedMask.computeValue(result.getPosition().getPrice());
            String correctionReasonCode = matchedMask.computeValue(result.getPosition().getCorrectionReasonCode());
            String ean13 = matchedMask.computeValue(result.getProductRef().getBarcode());
            String productItem = matchedMask.computeValue(result.getProductRef().getItem());

            Integer reasonCode = Optional.ofNullable(correctionReasonCode).map(Integer::parseInt).orElse(null);
            return new MarkdownBarcode(barcode, reasonCode, ean13, productItem, Long.parseLong(price), inputRestrictions);
        } catch (Exception e) {
            log.error("Unable to parse markdown barcode {}", barcode, e);
            return null;
        }
    }

    private Reason makeDefaultReason(MarkdownBarcode mdBarcode) {
        return new Reason(mdBarcode.getReasonCode(), "", false);
    }

    public boolean currentUserAllowedToUseBarcode(MarkdownBarcode mdBarcode) {
        InputRestrictions inputRestrictions = mdBarcode.getInputRestrictions();
        Set<String> allowedRights = null != inputRestrictions ? inputRestrictions.getAllowedRights() : new HashSet<>();

        if (allowedRights == null || allowedRights.isEmpty()) {
            return true;
        }

        for (String rightString : allowedRights) {
            try {
                Right right = Right.valueOf(rightString);

                if (techProcess.checkUserRight(right)) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                log.error("Incorrect right name: {}", rightString);
            }
        }

        return false;
    }

}
