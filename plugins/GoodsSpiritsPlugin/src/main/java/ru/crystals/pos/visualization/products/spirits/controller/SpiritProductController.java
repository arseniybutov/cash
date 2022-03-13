package ru.crystals.pos.visualization.products.spirits.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashException;
import ru.crystals.pos.ResExciseValidation;
import ru.crystals.pos.alcocode.CheckAlcoCode;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.catalog.ProductSpiritsBottleEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.catalog.exception.MinPriceException;
import ru.crystals.pos.catalog.limits.ProductLimitsService;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.MinimalPriceAlarmType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.check.exception.PositionalCouponException;
import ru.crystals.pos.egais.EGAISBridge;
import ru.crystals.pos.egais.EGAISBridgeImpl;
import ru.crystals.pos.egais.EgaisException;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResultFactory;
import ru.crystals.pos.egais.excise.validation.ds.ErrorCode;
import ru.crystals.pos.egais.excise.validation.ds.ErrorData;
import ru.crystals.pos.egais.excise.validation.ds.ErrorExciseData;
import ru.crystals.pos.egais.excise.validation.ds.OperationType;
import ru.crystals.pos.exception.ProductLimitException;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.mark.validation.events.ValidationBadMarkEvent;
import ru.crystals.pos.mark.validation.events.ValidationMarkAlreadyExistsInPurchaseEvent;
import ru.crystals.pos.product.events.PriceBelowMrpEvent;
import ru.crystals.pos.stockbalance.StockBalanceChecker;
import ru.crystals.pos.stockbalance.StockBalanceResult;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.pos.visualization.products.spirits.integration.SpiritPluginAdapter;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;
import ru.crystals.pos.visualization.products.spirits.model.ds.ValidationData;
import ru.crystals.pos.visualization.products.spirits.view.ScanFormUIConsumer;
import ru.crystals.pos.visualization.products.spirits.view.SpiritsPluginView;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Контроллер алкогольных товаров
 * Свой процесс создания позиции, проверка акцизных марок
 *
 * @author nbogdanov
 */
@Component
@ConditionalOnProductTypeConfig(typeName = SpiritPluginAdapter.PRODUCT_TYPE)
public class SpiritProductController extends AbstractProductController<SpiritProductModel> {
    private EGAISBridge egaisBridge;
    private ExciseValidation egaisExciseCheckValidation;
    private StockBalanceChecker stockBalanceChecker;
    private CheckAlcoCode checkAlcoCode;
    private ProductLimitsService alcoholLimitService;

    @Autowired
    void setEgaisBridge(EGAISBridge egaisBridge) {
        this.egaisBridge = egaisBridge;
    }

    @Autowired
    void setEgaisExciseCheckValidation(ExciseValidation egaisExciseCheckValidation) {
        this.egaisExciseCheckValidation = egaisExciseCheckValidation;
    }

    @Autowired(required = false)
    void setStockBalanceChecker(StockBalanceChecker stockBalanceChecker) {
        this.stockBalanceChecker = stockBalanceChecker;
    }

    @Autowired
    void setCheckAlcoCode(CheckAlcoCode checkAlcoCode) {
        this.checkAlcoCode = checkAlcoCode;
    }

    @Autowired
    void setAlcoholLimitService(ProductLimitsService alcoholLimitService) {
        this.alcoholLimitService = alcoholLimitService;
    }

    /**
     * Отправка сообщений об ошибке на сервер валидации (например "Цена ниже МРС")
     *
     * @param position      - Позиция
     * @param excise        - Акцизная марка
     * @param errorCode     - Код ошибки
     * @param operationType - Тип операции (продажа / возврат)
     */
    public void sendErrorMessage(PositionSpiritsEntity position, String excise, ErrorCode errorCode, OperationType operationType) {
        if (position == null || excise == null || errorCode == null || operationType == null) {
            return;
        }
        ErrorExciseData exciseData;
        if (getModel().getProduct().isKit()) {
            PurchaseExciseBottleEntity bootle = position.getBottleInKitWithoutExcise();
            String bottleBarcode = bootle != null ? bootle.getBarcode() : null;
            exciseData = new ErrorExciseData(excise, bottleBarcode, position.getBarCode());
        } else {
            exciseData = new ErrorExciseData(excise, position.getBarCode());
        }
        ErrorData data = new ErrorData(Stream.of(exciseData).collect(Collectors.toList()), errorCode, operationType);
        egaisExciseCheckValidation.sendErrorData(data);
    }

    /**
     * Полностью переопределенный productAdd, т.к. надо создать заготовку позиции
     * Т.к. на форме "возраста покупателя" уже можно добавить в чек товар - нужно заранее
     * проверить какое количество поставить
     */
    @Override
    public void processProductAdd(ProductEntity product) {
        getModel().setRestrictionMessage(checkProductInitial(product));
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(product));
        getModel().setAllowChangeQuantity(egaisBridge.allowChangeQuantityForKit(product));

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null && product.getBarCode().getCount() != 1000L;

        setScannedByBarcode(isFoundByBarcode);
        PositionSpiritsEntity posQuick = new PositionSpiritsEntity();
        posQuick.setProduct(product);

        long price = product.getPrice().getPrice();
        if (product.getProductConfig().isSetPriceEqualMinimalPriceRestrictions()) {
            if (price < product.getMinimalPrice()) {
                posQuick.setMinimalPriceAlarm(MinimalPriceAlarmType.PRICE);
                price = product.getMinimalPrice();
                getTechProcessEvents().publishEvent(new PriceBelowMrpEvent(this, true));
            }
        }

        posQuick.setPriceStart(price);
        posQuick.setPriceEnd(price);

        // SRL-848 если товар является набором, для него всегда количество=1 и запрет менять кол-во
        if (product instanceof ProductSpiritsEntity && ((ProductSpiritsEntity) product).isKit()) {
            posQuick.setQnty(1000L);
            posQuick.setCanChangeQnty(false);
        } else {
            if (isFoundByBarcode) {
                if (product.getBarCode().getCount() == 0) {
                    posQuick.setQnty(null);
                    posQuick.setCanChangeQnty(true);
                } else {
                    posQuick.setQnty(product.getBarCode().getCount());
                    posQuick.setCanChangeQnty(false);
                }
            } else {
                if (product.getBarCode() != null && product.getBarCode().getCount() > 0) {
                    posQuick.setQnty(product.getBarCode().getCount());
                } else {
                    posQuick.setQnty(1000L);
                }
            }
        }

        //Проверим на возможность добавления позиции
        try {
            Factory.getTechProcessImpl().isPossibleToAddPosition(posQuick);
            if (!isRefund()) {
                Factory.getTechProcessImpl().checkProductMinPriceRestrictions(product, posQuick.getPriceStart());
            }
        } catch (PositionAddingException e) {
            getModel().setRestrictionMessage(e.getMessage());
        } catch (MinPriceException e) {
            posQuick.setMinimalPriceAlarm(MinimalPriceAlarmType.PRICE);
            getTechProcessEvents().publishEvent(new PriceBelowMrpEvent(this, true));
            getModel().setRestrictionMessage(e.getMessage());
        }

        getModel().setPosition(posQuick);
        getModel().setProduct(product);
        getModel().setState(ProductContainer.ProductState.ADD);
        getModel().changed();
    }


    private String checkProductBarcodes(ProductEntity product) {
        String restrictionMessage = null;
        if ((product.getBarcodes() == null || product.getBarcodes().isEmpty()) && egaisBridge.isEnabled() && product.isExcise()) {
            restrictionMessage = ResBundleGoodsSpirits.getString("POSITION_DOES_NOT_HAVE_BARCODE");
            Factory.getTechProcessImpl().error("Product does not have barcode.");
            Factory.getTechProcessImpl().startCriticalErrorBeeping();
        }
        return restrictionMessage;
    }

    @Override
    public void processPositionDelete(PositionEntity position, ProductContainer.ProductState state) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        super.processPositionDelete(position, state);
    }

    @Override
    public void processPositionEdit(PositionEntity position) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        //после перезагрузки с незакрытым чеком обнуляется, надо снова засетить
        getModel().setAllowChangeQuantity(egaisBridge.allowChangeQuantityForKit(position.getProduct()));
        super.processPositionEdit(position);
    }

    @Override
    public void processPositionEditOrReturn(PositionEntity position) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        super.processPositionEditOrReturn(position);
    }

    @Override
    public void processProductView(ProductEntity product) {
        getModel().setRestrictionMessage(checkAlcoholLimits(product, false));
        super.processProductView(product);
    }

    /**
     * Вычисление необходимости сканирования акцизных марок
     */
    private boolean isNeedToScanExciseLabels(ProductEntity product) {
        ProductSpiritsEntity spiritProduct = (ProductSpiritsEntity) product;
        // если это акцизный товар или это набор в которм есть акцизные бутылки
        // и ЕГАИС включен
        boolean isEgaisOk = egaisBridge != null && egaisBridge.isEnabled();
        boolean isKitOk = spiritProduct.isKit() && !spiritProduct.getExciseBottles().isEmpty();
        return (product.isExcise() || isKitOk) && isEgaisOk;
    }

    /**
     * Проверяет ограничения на продажу алкоголя.
     */
    private String checkAlcoholLimits(ProductEntity product, boolean needBeeping) {
        if (product instanceof ProductSpiritsEntity) {
            ProductSpiritsEntity sproduct = (ProductSpiritsEntity) product;
            try {
                PurchaseEntity check = Factory.getTechProcessImpl().getCheck();

                boolean refund
                        = (check == null && Factory.getInstance().getMainWindow().getCheckContainer().getState() == CheckState.RETURN_CHECK)// первая позоция
                        // произвольного возврата
                        || (check != null && check.isReturn());// возврат

                if (!refund) {
                    alcoholLimitService.checkSaleAvailable(sproduct.getAlcoholicContent(), sproduct.getVolume(),
                            product.getPrice().getPrice(), sproduct.getProductConfig().getSpiritLimitTimeoutBeforeAddPayment());
                    return null;
                }
            } catch (ProductLimitException e) {
                if (needBeeping) {
                    Factory.getTechProcessImpl().error(e.getMessage());
                    Factory.getTechProcessImpl().getTechProcessEvents().eventAttemptSaleDeniedGoods(product);
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                }
                return e.getMessage();
            }
        }
        return null;
    }

    /**
     * Переопределен метод добавления позиций
     */
    public boolean addSpiritPosition(ProductSpiritsEntity product, BigDecimal quantity, BigDecimal price, boolean addedManually,
                                     List<PurchaseExciseBottleEntity> bottles) {

        PositionSpiritsEntity position = new PositionSpiritsEntity();
        try {
            position.setAddedToCheckManually(addedManually);
            checkInputDataBeforeAdd(product, quantity, price);
            fillDefaultPosition(quantity, price, product, position);

            if (reserveOnStockBalance(position, quantity)) {
                return false;
            }

            position.setAlcoholicContent(product.getAlcoholicContent());
            position.setVolume(product.getVolume());
            position.setAlcoholicType(product.getAlcoholicType());
            position.setMinimalPriceAlarm(getModel().getPosition().getMinimalPriceAlarm());
            boolean isFoundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null;
            boolean isCountOverOneAndFoundByBarcode = isFoundByBarcode && product.getBarCode().getCount() != 0 && product.getBarCode().getCount() != 1000;
            if (isCountOverOneAndFoundByBarcode || isScannedByBarcode()) {
                position.setCanChangeQnty(false);
            }
            // SRL-848 кол-во для наборов c бутылками изменять нельзя
            if (!egaisBridge.allowChangeQuantityForKit(product)) {
                position.setCanChangeQnty(false);
                position.setKit(product.isKit());
            }
            List<PurchaseExciseBottleEntity> exciseBottleForSave = bottles == null ? null : new ArrayList<>(bottles);
            position.setInMemoryBottles(exciseBottleForSave);
            position.setAlcoMinPrice(product.getProductConfig().getAmrc(exciseBottleForSave, product.isKit()).orElse(null));
            applySoftCheckAttributes(position, product);
            boolean isAdded = getAdapter().doPositionAdd(position);
            getAdapter().dispatchCloseEvent(isAdded);
            return isAdded;
        } catch (PositionalCouponException ex) {
            getAdapter().beepError("Cannot add position: " + position.toString() + " error msg:" + ex.getMessage());
            Factory.getInstance().showMessage(ex.getMessage());
            return false;
        } catch (Exception ex) {
            getAdapter().beepError("Cannot add position: " + position.toString() + " error msg:" + ex.getMessage());
            return false;
        }
    }

    public void applySoftCheckAttributes(PositionSpiritsEntity position, ProductSpiritsEntity product) {
        if (getAdapter().getPosition().isSoftCheckPosition()) {
            position.setSoftCheckNumber(getAdapter().getPosition().getSoftCheckNumber());
            CheckUtils.copySoftCheckPositionAttributes(position, getAdapter().getPosition());
            int bottleCount = position.getInMemoryBottles().size();
            long bottlesQuantity = (long) BigDecimalConverter.getQuantityMultiplier() * bottleCount;
            if (product.isExcise() && !product.isKit()) {
                position.setQnty(bottlesQuantity);
            }
        }
    }

    /**
     * Проверка акцизной марки перед добавлением товара в чек
     *
     * @param barcode      штрихкод акцизной марки
     * @param inMemBottles список уже отскариванных бутылок
     * @return Локализованное сообщение об ошибке, либо null, если всё хорошо
     */
    public String checkExciseBeforeAdd(String barcode, List<PurchaseExciseBottleEntity> inMemBottles) {
        if (barcode != null) {
            if (egaisBridge.validateExciseBarcode(barcode)) {
                if (findExistExciseBarcodeBeforeAdd(barcode, inMemBottles) == null) {
                    Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                    return null;
                } else {
                    Factory.getTechProcessImpl().getTechProcessEvents().publishEvent(new ValidationMarkAlreadyExistsInPurchaseEvent(this, barcode));
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                    return ResBundleGoodsSpirits.getString("SCAN_EXCISE_EXISTS");
                }
            } else {
                Factory.getTechProcessImpl().getTechProcessEvents().publishEvent(new ValidationBadMarkEvent(this, barcode));
                return ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM");
            }
        } else {
            Factory.getTechProcessImpl().getTechProcessEvents().publishEvent(new ValidationBadMarkEvent(this, barcode));
            Factory.getTechProcessImpl().startCriticalErrorBeeping();
            return ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM");
        }
    }

    /**
     * Определяем производится ли возврат товара
     *
     * @return true - если возврат, false - иначе
     */
    public boolean isProductRefund() {
        return getAdapter().getProductState() == ProductContainer.ProductState.REFUND;
    }

    private PurchaseExciseBottleEntity findExistExciseBarcodeBeforeAdd(String exciseBarcode, Collection<PurchaseExciseBottleEntity> collection) {
        return egaisExciseCheckValidation.findExistExciseBarcodeForSale(exciseBarcode, collection, true);
    }

    /**
     * Проверка длинны штрихкода акцизной марки с возвратом сообщения ошибки и включением пищалки
     */
    public String checkExciseLength(String exciseBarcode) {
        if (!egaisBridge.validateExciseBarcode(exciseBarcode)) {
            Factory.getTechProcessImpl().startCriticalErrorBeeping();
            return ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM");
        } else {
            return null;
        }
    }

    public PurchaseExciseBottleEntity checkExciseBeforeDelete(String exciseBarcode, PositionSpiritsEntity positionEntity, List<PurchaseExciseBottleEntity>
            inMemBottles) throws Exception {
        TechProcessInterface tpi = Factory.getTechProcessImpl();
        try {
            if (!egaisBridge.validateExciseBarcode(exciseBarcode)) {
                throw new Exception(ResBundleVisualization.getString("WRONG_SPIRIT_STAMP_LENGTH"));
            }
            // сначала всегда ищем в уже сканированных бутылках
            PurchaseExciseBottleEntity bottle = egaisExciseCheckValidation.findExistExciseBarcodeForReturn(exciseBarcode, inMemBottles, false);
            if (bottle != null) {
                // нашли - ругаемся
                throw new Exception(ResBundleVisualization.getString("SPIRIT_STAMP_ALREADY_SCANNED"));
            }

            if (positionEntity.getKit()) {
                // в алко-наборах ищем бутылки только в наборе
                bottle = egaisExciseCheckValidation.findExistExciseBarcodeForReturn(exciseBarcode, positionEntity.getExciseBottles(), false);
                if (bottle != null) {
                    return bottle;
                } else {
                    bottle = egaisExciseCheckValidation.findExistExciseBarcodeForReturn(exciseBarcode, null, true);
                    boolean isBottlesAbsent = inMemBottles == null || inMemBottles.isEmpty();
                    boolean isBottleOk = bottle != null && bottle.getPosition() != null;
                    boolean isKitOk = isBottleOk && bottle.getPosition().getKit() && bottle.getPosition().getItem().equals(positionEntity.getItem());
                    if (isBottlesAbsent && isKitOk) {
                        getModel().setPosition(bottle.getPosition());
                        return bottle;
                    } else {
                        throw new Exception(ResExciseValidation.getString("GOODS_ABSENT_IN_KIT"));
                    }
                }
            } else {
                // ищем в чеке
                bottle = egaisExciseCheckValidation.findExistExciseBarcodeForReturn(exciseBarcode, null, true);
                if (bottle == null) {
                    throw new Exception(ResBundleVisualization.getString("SPIRIT_STAMP_NOT_FOUND"));
                } else if (!bottle.getItem().equals(positionEntity.getItem())) {
                    throw new Exception(ResBundleVisualization.getString("SPIRIT_STAMP_FOR_ANOTHER_PRODUCT"));
                } else {
                    return bottle;
                }
            }
        } catch (Exception e) {
            // врубаем пищалку при ругательствах
            tpi.startCriticalErrorBeeping();
            throw e;
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getGoodsCode() != null) {
            return checkExciseMode(e.getGoodsCode());
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            processEscPressEventWithSpecialPrivilege();
            return true;
        }
        return false;
    }

    private boolean checkExciseMode(String barcode) {
        if (getModel().isScanExciseLabelsMode()
                && !currentProductIsMarked()) {
            Factory.getTechProcessImpl().error("Try scan barcode on spirits plugin with egais");
            try {
                eventScanOutCheck(barcode);
            } catch (CashException ex) {
                Factory.showCriticalMessage(ResBundleVisualization.getString("PRODUCT_NOT_IN_CHECK"));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        boolean foundByBarcode = super.barcodeScanned(barcode);
        if (!foundByBarcode) {
            return checkExciseMode(barcode);
        } else {
            return true;
        }
    }

    public void cashDeleteSpiritPosition(PositionSpiritsEntity position, List<PurchaseExciseBottleEntity> bottles, boolean checkPermission) {
        position.setInMemoryBottles(bottles);
        deleteReserveOnStockBalance(position);
        super.cashDeletePosition(position, null, checkPermission);
    }

    public void processEscPressEventWithSpecialPrivilege() {
        boolean rightExcisePositionCancel = getModel().isScanExciseLabelsMode()
                && Factory.getTechProcessImpl().checkUserRight(Right.ADDITION_EXCISE_POSITION_CANCEL);
        if (!getModel().isScanExciseLabelsMode()) {
            processEscPressEvent();
        } else if (getModel().getState() == ProductContainer.ProductState.VIEW
                || checkRightPositionCancel(getModel().isScanExciseLabelsMode())
                || checkRightFirstPositionCancel(getModel().isScanExciseLabelsMode())
                || rightExcisePositionCancel) {
            processEscPressEvent(false);
        } else {
            if (canRequestPermissionOnPosition()) {
                processEscPressEvent(true);
            } else {
                beepError("User has no rights to cancel position from check ("
                        + Right.ADDITION_EXCISE_POSITION_CANCEL.toString() + " or "
                        + Right.ADDITION_POSITION_CANCEL.toString() + " or "
                        + Right.FIRST_POSITION_CANCEL.toString() + ")");
            }
        }
    }

    /**
     * Проверить продукт на всевозможные условия еще на этапе перед отображение позиции в подвале.
     * Например, есть ли штрихкод или не сработало ли алкогольное ограничение
     */
    private String checkProductInitial(ProductEntity product) {
        String alcoholLimitsMessage = checkAlcoholLimits(product, true);
        if (alcoholLimitsMessage != null) {
            return alcoholLimitsMessage;
        }
        return checkProductBarcodes(product);
    }

    @Override
    protected void checkCouponQuantityRestriction(BigDecimal quantity) throws Exception {
        //Проверка только для неакцизного
        if (!getModel().isScanExciseLabelsMode()) {
            super.checkCouponQuantityRestriction(quantity);
        }
    }

    public boolean compareExcisePositionQuantity(BigDecimal currentQuantity, BigDecimal inputQuantity, boolean stateConditionEquality) {
        boolean result = inputQuantity.intValue() <= currentQuantity.intValue() && inputQuantity.intValue() != 0;
        if (result && Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE) && stateConditionEquality) {
            return true;
        } else if (inputQuantity.intValue() < currentQuantity.intValue() && inputQuantity.intValue() != 0
                && !Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE) && stateConditionEquality) {
            Factory.getTechProcessImpl().error("Spirits. Change quantity not allowed.");
        }
        return result;
    }

    /**
     * Проверяем, что barcode является валиднoй акцизной маркой
     */
    public boolean isExciseBarcode(String barcode) {
        return egaisBridge.validateExciseBarcode(barcode);
    }

    /**
     * По задаче [SRTB-2670] - запрещено увеличивать количество товарной позиции. Только уменьшать.
     * Проверка на увеличение количества товарной позиций в мягком чеке.
     */
    public boolean isPossibleToChangePositionQuantity(BigDecimal defaultQuantity, BigDecimal newQuantity) {
        boolean isSoftCheck = getModel().getPosition().isSoftCheckPosition() || getAdapter().getPosition().isSoftCheckPosition();
        if (isSoftCheck && defaultQuantity.compareTo(newQuantity) < 0) {
            String errorMessage = ResBundleGoodsSpirits.getString("INCREASE_COUNT_NOT_AVAILABLE_FOR_SOFT_CHECK");
            Factory.getTechProcessImpl().error("Cannot change cheque position: " + errorMessage);
            Factory.getInstance().showMessage(errorMessage);
            return false;
        }
        return true;
    }

    /**
     * Сформируем бутылки для позиции при:
     * 1) Быстром добавление через АМ
     * 2) Продаже МЧ с переданными марками
     *
     * @return бутылки
     */
    public List<PurchaseExciseBottleEntity> getExciseBottles() {
        if (getModel().isScanExciseLabelsMode() && currentProductIsMarked()) {
            ProductSpiritsEntity product = getModel().getProduct();
            ProductPositionData productPositionData = product.getProductPositionData();

            List<String> excises = new ArrayList<>();
            Optional.ofNullable(productPositionData.getExcise()).ifPresent(excises::add);
            Optional.ofNullable(productPositionData.getWholesaleExcises()).ifPresent(excises::addAll);

            List<PurchaseExciseBottleEntity> bottles = new ArrayList<>();
            for (String excise : excises) {
                bottles.add(new PurchaseExciseBottleEntity(product,
                        excise,
                        getModel().getPosition().getPriceEnd(),
                        product.getVolume(),
                        null)
                );
            }
            return bottles;
        }
        return null;
    }

    /**
     * Этот метод должен работать иначе, в интерфейсе ProductPluginController сказано,
     * что он должен показывать "маркированность" товара, а не наличие марки в данный момент...
     * Но в своё время завязались на такую реализацию, и это превратилось в макароны. Придется пока оставить
     */
    @Override
    public boolean currentProductIsMarked() {
        ProductPositionData productPositionData = getModel().getProduct().getProductPositionData();
        return StringUtils.isNotEmpty(productPositionData.getExcise()) || CollectionUtils.isNotEmpty(productPositionData.getWholesaleExcises());
    }

    @Override
    public void onAgeChecking() {
        getTechProcessEvents().eventWarningStateNotification();
    }

    @Override
    public void onAgeCheckingCompleted() {
        getTechProcessEvents().eventWorkingStateNotification();
    }

    /**
     * Проверяем и резервируем продукт на складском балансе
     *
     * @return true - ошибка резерва, false - товар зарезервирован
     */
    public boolean reserveOnStockBalance(PositionEntity position, BigDecimal quantity) {
        if (position.getProduct().isCheckForFreeBalance()) {
            if (stockBalanceChecker == null) {
                Factory.showCriticalMessage(ResBundleGoodsSpirits.getString("SALE_FORBBIDEN_STOCK_BALANCE_NOT_ENABLE"));
                return true;
            }

            StockBalanceResult stockBalanceResult = stockBalanceChecker.reserve(position, quantity);
            if (stockBalanceResult.isError()) {
                Factory.showCriticalMessage(stockBalanceResult.getMessage());
                return true;
            }
        }
        return false;
    }

    public void deleteReserveOnStockBalance(PositionEntity position) {
        if (position.getProduct().isCheckForFreeBalance() && stockBalanceChecker != null) {
            stockBalanceChecker.deleteReserve(position);
        }
    }

    /**
     * При продаже алкогольного набора бутылок
     *
     * @return Возвращает true если была найдена бутылка в наборе по ШК и ожидается сканирование акцизной марки для неё
     */
    public boolean isWaitExciseForBottleMode() {
        int bottlesCount = getModel().getProcessedBottles().size();
        return bottlesCount > 0 && StringUtils.isEmpty(getModel().getProcessedBottles().get(bottlesCount - 1).getExciseBarcode());
    }

    public CheckAlcoCode getCheckAlcoCode() {
        return checkAlcoCode;
    }

    /**
     * Валидация АМ. Записывает результаты в модель.
     *
     * @param inputScannedBarcode АМ
     * @return результат валидации АМ
     */
    public ExciseValidationResult egaisExciseValidation(String inputScannedBarcode) {
        ExciseValidationResult exciseValidationResult;
        getModel().setValidationError(false);
        getModel().getPosition().setInMemoryBottles(getModel().getProcessedBottles());

        /*
         * Без этого не работает продажа зарезервированных товаров в МЧ
         * Непонятно, как раньше работало и из-за чего сломалось
         */
        applySoftCheckAttributes(getModel().getPosition(), getModel().getProduct());

        if (isProductRefund()) {
            exciseValidationResult = egaisExciseCheckValidation.isCanReturnExcise(inputScannedBarcode, getModel().getPosition());
        } else {
            exciseValidationResult = egaisExciseCheckValidation.isCanSaleExcise(inputScannedBarcode, getModel().getPosition());
        }
        // Проверки на АМРЦ
        if (!isRefund() && !getModel().getProduct().getProductConfig().checkAmrc(getModel().getPosition(), exciseValidationResult.getAmrc())) {
            exciseValidationResult = ExciseValidationResultFactory.fail(ResBundleGoodsSpirits.getString("PRICE_LESS_AMRC"), exciseValidationResult.errorType);
            // Отправляем данные об ошибке на сервер валидации для дальнейшей отправки в ERP
            sendErrorMessage(getModel().getPosition(), getModel().getScannedBarcode(), ErrorCode.PRICE_LESS_MRC, OperationType.SALE);
        }
        getModel().setValidationResult(exciseValidationResult);
        getModel().setValidationError(!exciseValidationResult.operationPossibility);
        getModel().setScannedBarcode(inputScannedBarcode);
        return exciseValidationResult;
    }

    @Override
    protected SpiritsPluginView getView() {
        return (SpiritsPluginView) super.getView();
    }

    /**
     * Обработка результатов валидации
     *
     * @param validationResult - результат валидации
     */
    public void processValidationResults(ExciseValidationResult validationResult) {
        // Отображает результаты валидации на UI
        getView().processValidationResults(validationResult);
        /*
         * Если у нас запрещена продажа, то надо вычистить переданные фастсканом марки.
         * Дальнейшая работа должна уже идти с "голой" позицией, чтобы кассир мог сосканить другую марку
         */
        if (!validationResult.operationPossibility) {
            getProduct().getProductConfig().clearExciseDataExternally(getProduct());
        }
    }

    /**
     * Добавить бутылку. Использует ScanFormUIConsumer для обработки реакции UI
     *
     * @param scannedBarcode - АМ отсканированной бутылки
     * @param uiConsumer     - интерфейс взаимодействия с текущей формочкой
     * @return добавлена ли бутылка
     */
    public boolean addBottleWithBarcode(String scannedBarcode, ScanFormUIConsumer uiConsumer) {
        String errorMessage = null;
        ValidationData validationData = getModel().getValidationResult() != null ? new ValidationData(getModel().getValidationResult()) : new ValidationData();
        ProductSpiritsEntity productEntity = getModel().getProduct() != null ? getModel().getProduct() : (ProductSpiritsEntity) getModel().getPosition().getProduct();
        PurchaseExciseBottleEntity bottle;
        List<PurchaseExciseBottleEntity> bottles = getModel().getProcessedBottles();
        if (productEntity.isKit()) {
            // это алконабор
            try {
                if (isWaitExciseForBottleMode()) {
                    errorMessage = checkExciseLength(scannedBarcode);
                    if (errorMessage != null) {
                        uiConsumer.showMessage(errorMessage, true);
                        return true;
                    }
                    // бутылка уже есть, просто добавим ей сканкод акцизной марки
                    int bottleId = bottles.size() - 1;
                    if (bottles.get(bottleId).getAlcoCodes() == null) {
                        String alcoCode = EGAISBridgeImpl.getAlcoCode(
                                productEntity, scannedBarcode, bottles.get(bottleId).getBarcode(),
                                validationData.getAlcocode()
                        );
                        bottles.get(bottleId).setAlcoCodes(alcoCode);
                    }
                    bottles.get(bottleId).setExciseBarcode(scannedBarcode);
                    bottles.get(bottleId).setAlcoMinPrice(validationData.getAmrc());
                    uiConsumer.showScanExciseLabel();
                } else {
                    ProductSpiritsBottleEntity bottleInKit = EGAISBridgeImpl.findBottleInKit(
                            productEntity, scannedBarcode,
                            validationData.getAlcocode(), validationData.getBarcode()
                    );
                    String barcode = bottleInKit.isFoundByBarcode() && validationData.getBarcode() == null ?
                            scannedBarcode : EGAISBridgeImpl.getDefaultBarcode(bottleInKit);
                    bottle = new PurchaseExciseBottleEntity(
                            productEntity, scannedBarcode, EGAISBridgeImpl.getActualPriceForBottle(bottleInKit.getPrices()),
                            bottleInKit.getVolume(), bottleInKit.getActualAlcoCode(), validationData.getAmrc()
                    );
                    bottle.setBottleItem(bottleInKit.getBottleItem());
                    bottle.setBarcode(barcode);
                    bottle.setPosition(getModel().getPosition());
                    bottles.add(bottle);
                    Factory.getTechProcessImpl().confirm();
                    if (bottleInKit.isFoundByBarcode() && validationData.getBarcode() == null) {
                        bottle.setExciseBarcode("");
                        uiConsumer.showMessage(ResBundleGoodsSpirits.getString("SCAN_EXCISE_LABEL"), false);
                        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                        return true;
                    }
                }
            } catch (EgaisException e) {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                uiConsumer.showMessage(e.getLocalizedMessage(), true);
                return true;
            }
        } else {
            // это не алко набор
            errorMessage = checkExciseLength(scannedBarcode);
            if (errorMessage == null) {
                errorMessage = EGAISBridgeImpl.containsAlcoCodeInProduct(productEntity, scannedBarcode);
            }
            if (errorMessage == null) {
                PriceEntity priceEntity = getModel().getProduct().getPrice();
                long price = priceEntity != null ? BigDecimalConverter.convertMoney(priceEntity.getPriceBigDecimal()) : 0L;
                bottle = new PurchaseExciseBottleEntity(productEntity, scannedBarcode, price,
                        productEntity.getVolume(), null, validationData.getAmrc());
            } else {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                uiConsumer.showMessage(errorMessage, true);
                return true;
            }
            bottles.add(bottle);
        }
        return uiConsumer.updateQuantity();
    }

    @Override
    public void changeQuantity(PositionEntity position, BigDecimal quantity, Date productionDate, boolean checkPermission) {
        if (quantity.equals(BigDecimalConverter.convertQuantity(0))) {
            Factory.getInstance().showMessage(ResBundleVisualization.getString("INVALID_POSITION_COUNT"));
            return;
        }

        if (reserveOnStockBalance(position, quantity)) {
            return;
        }
        super.changeQuantity(position, quantity, productionDate, checkPermission);
    }
}
