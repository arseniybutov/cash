package ru.crystals.pos.visualization.products.ciggy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductCiggyController;
import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.exception.MinPriceException;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractCommonMarkedProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.ciggy.integration.CiggyPluginAdapter;
import ru.crystals.pos.visualization.products.ciggy.model.CiggyProductModel;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * User: nbogdanov Date: 24.02.14 Time: 13:17
 */
@Component
@ConditionalOnProductTypeConfig(typeName = CiggyPluginAdapter.PRODUCT_TYPE)
public class CiggyPluginController extends AbstractCommonMarkedProductController<CiggyProductModel> {

    /**
     * Тип для предоставления состояния в которой находится процесс продажи табачной продукции
     */
    public enum State {
        /**
         * Ошибка
         */
        ERROR,
        /**
         * Состояние для выбора цены для табачной продукции
         */
        SELECT_PRICE,
        /**
         * Состояние для подтверждения ввода количества табачной продукции
         */
        ENTER_QUANTITY
    }

    private boolean priceSelected = false;

    /**
     * Признак того, что МРЦ была установлена из сканированной табачной марки,
     * а не из окна выбора МРЦ
     */
    private boolean priceSelectedFromExcise = false;

    private final ProductCiggyController productCiggyController;

    @Autowired
    public CiggyPluginController(ProductCiggyController productCiggyController) {
        this.productCiggyController = productCiggyController;
    }

    public boolean isPriceSelected() {
        return priceSelected;
    }

    public boolean isPriceSelectedFromExcise() {
        return priceSelectedFromExcise;
    }

    @Override
    public void processProductAdd(ProductEntity product) {
        getModel().setMessage(null);
        getModel().setConfirmedProductionDate(false);
        getModel().setPosition(null);
        getModel().setProduct(product);

        PositionEntity positionEntity = new PositionEntity();
        fillDefaultPosition(BigDecimal.ONE, product.getPrice().getPriceBigDecimal(), product, positionEntity);

        if (positionEntity.isCanChangeQnty()) {
            // Для позиций подлежащих "мягкой" маркировке изменять количество нельзя
            positionEntity.setCanChangeQnty(!softMarkedEnabledForCurrentPosition());
        }

        tryToAddMrpFromMark((ProductCiggyEntity) product);
        priceSelected = ((ProductCiggyEntity) product).getAdditionalPrices().isEmpty() || priceSelectedFromExcise;

        getModel().setPosition(positionEntity);
        getModel().setState(ProductContainer.ProductState.ADD);
        getModel().setNeedScanMark(needCheckExciseForAddMarkedProduct());
        getModel().setCanSkipScanMarkForm(canSkipScanMarkForCurrentPosition());

        checkProductMinPriceRestrictionsWithMessage();

        getModel().changed();
    }

    public boolean tryToAddMrpFromMark(ProductCiggyEntity productEntity) {
        Optional<BigDecimal> mrp = productCiggyController.getPriceFromExcise(productEntity);

        if (mrp.isPresent()) {
            setPositionPrice(mrp.get());
            priceSelected = true;
            priceSelectedFromExcise = true;
            return true;
        } else {
            priceSelectedFromExcise = false;
        }

        return false;
    }

    public void selectCiggyPrice(BigDecimal price) {
        priceSelected = true;
        setPositionPrice(price);
        getModel().setState(ProductContainer.ProductState.ADD);

        checkProductMinPriceRestrictionsWithMessage();

        getModel().changed();
    }

    private void setPositionPrice(BigDecimal price) {
        if (getModel().getPosition() == null) {
            getModel().setPosition(new PositionEntity());
        }
        getModel().getPosition().setPriceStartBigDecimal(price);
    }

    public void checkProductMinPriceRestrictionsWithMessage() {
        try {
            checkProductMinPriceRestrictions();
        } catch (MinPriceException ex) {
            if (getModel().isConfirmedProductionDate()) {
                getModel().setMessage(null);
            } else {
                getModel().setMessage(ex.getMessage());
                getModel().getProduct().getProductConfig().processProductionDate(this::showQuestion);
            }
        }
    }

    public void checkProductMinPriceRestrictions() throws MinPriceException {
        if (isRefund()) {
            return;
        }

        Factory.getTechProcessImpl().checkProductMinPriceRestrictions(getModel().getProduct(), getModel().getPosition().getPriceStart());
    }

    /**
     * При выборе другой цены - отправляем на дисплей покупателя
     *
     * @param price
     */
    public void selectionPriceChanged(BigDecimal price) {
        setPositionPrice(price);
        Factory.getTechProcessImpl().getTechProcessEvents().eventUpdateProductOnDisplay(Factory.getTechProcessImpl().getCheckOrNextCheckStub(true),
                getModel().getPosition());
    }

    public void selectionPriceChangedInViewInfo(BigDecimal price) {
        if (getModel().getProduct() != null && getModel().getProduct().getPrice() != null) {
            getModel().getProduct().getPrice().setPrice(price.unscaledValue().longValue());
            eventGoodInfo(getModel().getProduct());
        }
    }

    @Override
    public void fillDefaultPosition(BigDecimal quantity, BigDecimal price, ProductEntity product, PositionEntity positionEntity) {
        super.fillDefaultPosition(quantity, price, product, positionEntity);
        if (product.isFoundByBarcode() && product.getBarCode() != null) {
            BarcodeEntity barcode = product.getBarCode();
            if (barcode.getCount() != 0 && barcode.getCount() != 1000) {
                positionEntity.setQntyBigDecimal(barcode.getCountBigDecimal());
            }
        }
    }

    @Override
    public ProductCiggyEntity getProduct() {
        return (ProductCiggyEntity) super.getProduct();
    }

    /**
     * Proxy-метод до продуктового контроллера
     * Создан, чтобы "обезопасить" views от лишних зависимостей
     */
    public boolean isCheckAge() {
        return productCiggyController.isCheckAge();
    }

    /**
     * Proxy-метод до продуктового контроллера
     * Создан, чтобы "обезопасить" views от лишних зависимостей
     */
    public boolean isSellWithoutMRP() {
        return productCiggyController.isSellByFirstPriceWithoutMRP();
    }

    /**
     * Проверка на наличие МРЦ в списке распарсенных данных из АМ
     */
    public boolean isExciseHasMRP() {
        return getProduct().getProductPositionData().getMarkData() != null && getProduct().getProductPositionData().getMarkData().getMinimalRetailPrice() != null;
    }

    /**
     * Проверка на возможность продажи
     * Отсутствует цена в АМ и есть дополнительный список цен табачной продукции, либо если стоит настройка для продажи по первой цене
     */
    public boolean isSellPossible() {
        return !isExciseHasMRP() && (isProductHasAdditionalPrices() || isSellWithoutMRP());
    }

    /**
     * Проверка на наличие дополнительных цен для табачной позиции
     */
    public boolean isProductHasAdditionalPrices() {
        return !((ProductCiggyEntity) getModel().getProduct()).getAdditionalPrices().isEmpty();
    }

    /**
     * Проверяет выбрана ли уже МРЦ
     * Имеет смысл при быстрой продаже, когда МРЦ вытягивается из сканированной марки
     */
    public boolean isSelectedMRP() {
        ProductCiggyEntity productEntity = (ProductCiggyEntity) (getProduct() != null ? getProduct() : getModel().getPosition().getProduct());
        return isPriceSelected() || tryToAddMrpFromMark(productEntity);
    }

    /**
     * (Так как продажа табака усложняется новыми условиями, думаю будет правильно вынести проверку этих условий в отдельный метод)
     * (https://crystals.atlassian.net/browse/SRTB-2890)
     * Вычисление состояния, к которому может перейти текущий процесс добавления в чек позиции табачной продукции
     *
     * @param extraCondition - дополнительное внешнее условие, которое следует соблюсти при проверке основных условий
     * @return состояние, к которому может перейти процесс добавления в чек позиции
     */
    public State checkNextStateOfAddingPosition(boolean extraCondition) {
        State result = State.ERROR;

        try {
            Factory.getTechProcessImpl().isPossibleToAddPosition(getModel().getPosition());
        } catch (PositionAddingException e) {
            getModel().setMessage(e.getMessage());
            return State.ERROR;
        }

        // Маркированный ли табак или присутствует АМ
        boolean isMarked = currentProductIsMarked();
        // Признак того что - табак маркированный и есть список дополнительных цен и выбрана цена(которая есть в АМ) из дополнительного списка цен
        boolean markedProduct = isMarked && isPriceSelectedFromExcise();
        // Признак того что - табак маркированный и отсутствует МРЦ в АМ
        boolean markedProductNoMRP = isMarked && !isExciseHasMRP();
        // Выбрана ли цена из списка дополнительных цен и отсутствует ли цена в АМ
        boolean isPriceNotSelected = !isSelectedMRP() && !isExciseHasMRP();
        // Признак того что - есть список дополнительных цен и (маркированный табак без МРЦ или если цена не выбрана) и дополнительное условие
        boolean notMarkedProduct = isProductHasAdditionalPrices() && (markedProductNoMRP || isPriceNotSelected) && extraCondition;

        if (markedProduct) {
            result = State.ENTER_QUANTITY;
        } else if (notMarkedProduct) {
            result = State.SELECT_PRICE;
        } else if (isSellPossible()) {
            result = State.ENTER_QUANTITY;
        }
        getModel().setMessage(State.ERROR.equals(result) ? ResBundleVisualization.getString("SALE_DENIED") : null);
        return result;
    }

    @Override
    protected boolean checkRightPositionCancel(boolean checkRight) {
        //  проверим привилегию отмены маркированной табачной позиции
        boolean isMarked = getProduct().getBarCode() != null && getProduct().getBarCode().isMarked();
        if (isMarked && Factory.getTechProcessImpl().checkUserRight(Right.ADDITION_MARKED_CIGGY_POSITION_CANCEL)) {
            return true;
        }

        //  если не прошли проверок выше, то посмотрим на базовые привилегии
        return super.checkRightPositionCancel(checkRight);
    }

    @Override
    public void onAgeChecking() {
        getTechProcessEvents().eventWarningStateNotification();
    }

    @Override
    public void onAgeCheckingCompleted() {
        getTechProcessEvents().eventWorkingStateNotification();
    }

    public boolean isHighDemandHours() {
        return productCiggyController.isHighDemandHours();
    }

    public void savePreviousState() {
        getModel().setPrevState(getModel().getState());
        getModel().setPrevMessage(getModel().getMessage());
    }

    public void showQuestion(String message) {
        savePreviousState();
        getModel().setMessage(message);
        getModel().setState(ProductContainer.ProductState.SHOW_QUESTION);
        getModel().changed();
    }

    public void processQuestion(boolean commit) {
        if (commit) {
            getModel().setMessage(null);
            getModel().setConfirmedProductionDate(true);
        } else {
            getModel().setMessage(getModel().getPrevMessage());
            getModel().setConfirmedProductionDate(false);
        }
        getModel().setState(getModel().getPrevState());
        getModel().changed();
    }

    public void applySoftCheckAttributes(PositionEntity position) {
        if (getAdapter().getPosition().isSoftCheckPosition()) {
            position.setSoftCheckNumber(getAdapter().getPosition().getSoftCheckNumber());
            CheckUtils.copySoftCheckPositionAttributes(position, getAdapter().getPosition());
        }
    }
}
