package ru.crystals.pos.visualization.products.spirits.model;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Стандартная модель дополнена ошибкой проверки алкогольных ограничений
 * и флагом использования бизнеспроцесса ЕГАИС
 *
 * @author nbogdanov
 */
public class SpiritProductModel extends CommonProductPluginModel {
    private String restrictionMessage;
    private boolean scanExciseLabelsMode;
    private boolean allowChangeQuantityForKit;

    /**
     * Сосканированный ШК. На самом деле звучит как древняя залипень
     */
    private String scannedBarcode;

    /**
     * Результат валидации АМ. Хранится в модели только для работы с бутылками из под всяких событий ШК
     */
    private ExciseValidationResult validationResult;

    /**
     * Залипень из SRTB-3656
     */
    private boolean validationError;

    /**
     * Обработанные бутылки
     */
    private List<PurchaseExciseBottleEntity> processedBottles = new ArrayList<>();

    /**
     * Переопределим, чтобы избавиться от безумного количества кастов
     * Можно избавиться, расширив дженерики в абстракциях, но нужно мноого правок
     */
    @Override
    public ProductSpiritsEntity getProduct() {
        return (ProductSpiritsEntity) super.getProduct();
    }

    /**
     * Переопределим, чтобы избавиться от безумного количества кастов
     * Можно избавиться, расширив дженерики в абстракциях, но нужно мноого правок
     */
    @Override
    public PositionSpiritsEntity getPosition() {
        return (PositionSpiritsEntity) super.getPosition();
    }

    public String getRestrictionMessage() {
        return restrictionMessage;
    }

    public void setRestrictionMessage(String restrictionMessage) {
        this.restrictionMessage = restrictionMessage;
    }

    public boolean isScanExciseLabelsMode() {
        return scanExciseLabelsMode;
    }

    public void setScanExciseLabelsMode(boolean useEgais) {
        this.scanExciseLabelsMode = useEgais;
    }

    public boolean isAllowChangeQuantityForKit() {
        return allowChangeQuantityForKit;
    }

    public void setAllowChangeQuantity(boolean allowChangeQuantity) {
        this.allowChangeQuantityForKit = allowChangeQuantity;
    }

    public String getScannedBarcode() {
        return scannedBarcode;
    }

    public void setScannedBarcode(String scannedBarcode) {
        this.scannedBarcode = scannedBarcode;
    }

    public boolean isValidationError() {
        return validationError;
    }

    public void setValidationError(boolean validationError) {
        this.validationError = validationError;
    }

    public ExciseValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ExciseValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public List<PurchaseExciseBottleEntity> getProcessedBottles() {
        return processedBottles;
    }

    public void setProcessedBottles(List<PurchaseExciseBottleEntity> processedBottles) {
        this.processedBottles = processedBottles;
    }
}
