package ru.crystals.pos.visualization.products.jewel;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductJewelEntity;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.MeasurePositionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.CheckSumLimitException;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.LeftListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.eventlisteners.RightListener;
import ru.crystals.pos.visualization.products.CommonProductContainer;
import ru.crystals.pos.visualization.products.jewel.controller.JewelPluginController;
import ru.crystals.pos.visualization.products.jewel.model.JewelProductModel;
import ru.crystals.pos.visualization.products.jewel.ret.ValidateReturnJewelForm;
import ru.crystals.pos.visualization.products.jewel.view.JewelAddPositionScanExciseForm;
import ru.crystals.pos.visualization.products.jewel.view.JewelDeletePositionScanExciseForm;
import ru.crystals.pos.visualization.scenaries.DeletePositionScenario;
import ru.crystals.pos.visualization.utils.FormatHelper;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_JEWEL_ENTITY, mainEntity = ProductJewelEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class JewelProductContainer extends CommonProductContainer implements NumberEventListener, EnterEventListener, EscEventListener, DotEventListener,
        LeftListener, RightListener {
    private static final long COUNT_PRECISION = 1000;

    private ProductJewelEntity product = null;
    private JewelProductComponent visualPanel = null;
    private Long quantity = 0L;
    private Long summaR = 0L;
    private Long summaC = 0L;
    private boolean enterSum = false;
    private boolean oneCoin = false;
    private long oldPrice = 0L;
    private boolean emptyPrice = false;

    private boolean editButtonEnabled = false;
    private boolean deleteButtonEnabled = false;

    private enum Buttons {
        EDIT,
        DELETE,
        NONE
    };

    private Buttons selectedButton = Buttons.NONE;
    private boolean emptySum = false;
    PositionEntity pos = new PositionEntity();

    private final JewelPluginController controller;

    /**
     * Форма сканирования марки при добавлении позиции
     */
    private final JewelAddPositionScanExciseForm addPositionScanExciseForm;

    /**
     * Форма сканирования марки при удалении позиции
     */
    private final JewelDeletePositionScanExciseForm deletePositionScanExciseForm;

    /**
     * Форма подтверждения удаления
     */
    private final CommonDeletePositionConfirmForm deletePositionForm;

    /**
     * Форма валидации при возврате по чеку
     */
    private final ValidateReturnJewelForm validateReturnJewelForm;

    private final JewelProductModel model;

    @Autowired
    public JewelProductContainer(JewelPluginController controller, ValidateReturnJewelForm validateReturnJewelForm) {
        this.validateReturnJewelForm = validateReturnJewelForm;
        this.controller = controller;

        this.model = new JewelProductModel();
        this.controller.setModel(model);
        this.controller.setContainer(this);

        XListener listener = new XListener() {
            @Override
            public boolean barcodeScanned(String barcode) {
                return dispatchBarcodeScanned();
            }

            @Override
            public boolean keyPressedNew(XKeyEvent e) {
                return dispatchKeyPressed(e);
            }

            @Override
            public boolean eventMSR(String track1, String track2, String track3, String track4) {
                return false;
            }
        };

        deletePositionForm = new CommonDeletePositionConfirmForm(listener);
        addPositionScanExciseForm = new JewelAddPositionScanExciseForm(listener, controller);
        deletePositionScanExciseForm = new JewelDeletePositionScanExciseForm(listener, controller);
    }

    @Override
    protected JewelProductModel getModel() {
        return model;
    }

    @Override
    protected JewelPluginController getController() {
        return controller;
    }

    /**
     * Проверяем, создана ли панель
     * Заодно создаем ее, раз сюда кто-то залез, значит
     * плагин уже используется
     *
     * @return
     */
    public boolean isVisualPanelCreated() {
        //тут создается компонента, т.к. была ошибка, что при первом обращении ювелирный плагин рисовал форму
        //информации о товаре при любом обращении
        return getVisualPanel() != null;
    }

    public JewelProductComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new JewelProductComponent();
            visualPanel.setAddPositionScanExciseForm(addPositionScanExciseForm);
            visualPanel.setDeletePositionScanExciseForm(deletePositionScanExciseForm);
            visualPanel.setDeletePositionForm(deletePositionForm);
        }
        return visualPanel;
    }

    private Long startQuantity = 0L;

    public Long getStartQuantity() {
        return startQuantity;
    }

    public void setStartQuantity(Long startQuantity) {
        this.startQuantity = startQuantity;
    }

    @Override
    public ProductEntity getProduct() {
        return product;
    }

    @Override
    public void setProduct(ProductEntity product) {
        getCommonProductComponent().clear();

        this.product = (ProductJewelEntity) product;
        getVisualPanel().setProduct(product);

        oldPrice = product.getPrice().getPrice();
        getCommonProductComponent().setSumma(getProduct().getPrice().getPriceBigDecimal());
        reset();
        setStartQuantity(COUNT_PRECISION);
        setQuantity(BigDecimal.valueOf(1L));
        if (product.isFoundByBarcode() && product.getBarCode() != null && product.getBarCode().getPrice() != null) {
            Date now = new Date();
            if ((product.getBarCode().getBeginDate() == null || now.after(product.getBarCode().getBeginDate()))
                    && (product.getBarCode().getEndDate() == null || now.before(product.getBarCode().getEndDate()))) {
                quantity = product.getBarCode().getCount();
                setQuantity(product.getBarCode().getCountBigDecimal());
                setStartQuantity(quantity * COUNT_PRECISION);
            }
        }

        controller.update(product, pos);

        if ((getProductState() == ProductState.ADD || getProductState() == ProductState.REFUND) && controller.needCheckExciseForAddMarkedProduct() && !getCommonProductComponent().isValidExcise()) {
            getCommonProductComponent().showAddPositionScanExciseForm(pos);
        }

        if (getProductState() == ProductState.DELETE || getProductState() == ProductState.QUICK_DELETE) {
            getCommonProductComponent().showDeletePositionForm(pos);
        }

        notifyAddPosition(getProductState() == ProductState.ADD);
        if (getProductState() == ProductState.VIEW) {
            dispatchEventGoodInfo(product);
        }
    }

    protected void setProductPrice(Long price, Long priceRub, Long priceCon) {
        BigDecimal sum = BigDecimal.valueOf(0L, 2);
        getVisualPanel().setSumma(BigDecimal.valueOf(quantity * price, 5));
        pos.setProduct(product);
        pos.setProductType((getProduct().getClass().getSimpleName()));
        pos.setQnty(quantity);
        pos.setNdsClass(product.getNdsClass());
        pos.setNds(product.getNds());
        pos.setPriceStart(price);
        pos.setBarCode(product.getBarCode() == null ? "" : product.getBarCode().getBarCode());
        pos.setItem(product.getItem());
        if (product.getMeasure() != null) {
            pos.setMeasure(new MeasurePositionEntity());
            pos.getMeasure().setCode(product.getMeasure().getCode());
            pos.getMeasure().setName(product.getMeasure().getName());
        }
        pos.setDepartNumber(product.getDepartNumber());
        pos.setName(product.getName());
        pos.setTypePriceNumber(product.getPrice().getNumber());
        pos.setProductSettings(product.getProductConfig());
        pos.setCalculateDiscount(product.getProductConfig().getIsDiscountApplicable());
        pos.setBeforeManualPrice(product.getBeforeManualPrice());

        if (getProductState() == ProductState.ADD || getProductState() == ProductState.REFUND)
            sum = BigDecimal.valueOf(getFactory().getTechProcess().getCheckSum(pos), 2);
        else if (!getPosition().getQnty().toString().equals(pos.getQnty().toString()) && Factory.getTechProcessImpl().getCheck() != null) {
            BigDecimal tQuantity = getPosition().getQntyBigDecimal().subtract(BigDecimal.valueOf(quantity, 3));
            sum = Factory.getTechProcessImpl().getCheck().getCheckSumStartBigDecimal().subtract(tQuantity.multiply(product.getPrice().getPriceBigDecimal()).setScale(2, RoundingMode.HALF_EVEN));
        }

        dispatchUpdateEvent(sum);
    }

    protected void setQuantity(BigDecimal quantity) {
        BigDecimal sum = BigDecimal.valueOf(0L, 2);
        getVisualPanel().setSumma(product.getPrice().getPriceBigDecimal().multiply(quantity).setScale(2, RoundingMode.HALF_EVEN));
        pos.setProduct(product);
        if (getProduct() != null)
            pos.setProductType((getProduct().getClass().getSimpleName()));
        pos.setQntyBigDecimal(quantity);
        pos.setNdsClass(product.getNdsClass());
        pos.setNds(product.getNds());
        pos.setPriceStart(product.getPrice().getPrice());
        pos.setBarCode(product.getBarCode() == null ? "" : product.getBarCode().getBarCode());
        pos.setItem(product.getItem());
        if (product.getMeasure() != null) {
            pos.setMeasure(new MeasurePositionEntity());
            pos.getMeasure().setCode(product.getMeasure().getCode());
            pos.getMeasure().setName(product.getMeasure().getName());
        }
        pos.setDepartNumber(product.getDepartNumber());
        pos.setName(product.getName());
        pos.setTypePriceNumber(product.getPrice().getNumber());
        pos.setProductSettings(product.getProductConfig());
        pos.setCalculateDiscount(product.getProductConfig().getIsDiscountApplicable());

        if (getProductState() == ProductState.ADD || getProductState() == ProductState.REFUND)
            sum = BigDecimal.valueOf(Factory.getTechProcessImpl().getCheckSum(pos), 2);
        else if (!getPosition().getQnty().toString().equals(pos.getQnty().toString()) && Factory.getTechProcessImpl().getCheck() != null) {
            BigDecimal tQuantity = getPosition().getQntyBigDecimal().subtract(quantity);
            sum = Factory.getTechProcessImpl().getCheck().getCheckSumStartBigDecimal().subtract(tQuantity.multiply(product.getPrice().getPriceBigDecimal()).setScale(2, RoundingMode.HALF_EVEN));
        }
        dispatchUpdateEvent(sum);
    }

    @Override
    public void number(Byte num) {

    }

    @Override
    public void enter() {
        if (getVisualPanel().getState() == ProductState.EDIT_OR_DELETE) {
            switch (selectedButton) {
                case EDIT:
                    setProductState(ProductState.EDIT);
                    break;
                case DELETE:
                    new DeletePositionScenario().execute(position, CheckState.EDIT_CHECK);
                    break;
                case NONE:
                    break;
            }
            return;
        }
        if (enterSum && ((summaR == 0L && summaC == 0L) || emptyPrice)) {
            getFactory().getTechProcess().error();
            return;
        }
        doPositionAdd();
    }

    @Override
    public boolean ableToAddPosition() {
        if (!isCallDone() && (quantity == 0L && isReset() || quantity != 0L)) {
            switch (getProductState()) {
                case EDIT_REFUND:
                    return getPosition().getQnty() - quantity * COUNT_PRECISION > 0L;
                case EDIT:
                    return true;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long getCurrentPositionCount() {
        return quantity != null ? quantity : 0;
    }

    @Override
    public void doPositionAdd() {
        if (ableToAddPosition()) {
            switch (getProductState()) {
                case ADD:
                    PositionEntity positionEntity = makeNewPosition(PositionEntity.class);
                    doSetPosition(positionEntity);
                    try {
                        if (getManualAdvertisingActions() != null && getManualAdvertisingActions().get(0) != null) {

                            getManualAdvertisingActions().get(0).setQnty(position.getQnty());
                            getManualAdvertisingActions().get(0).setPosition(positionEntity);

                            positionEntity.setManualAdvertisingActions(getManualAdvertisingActions());
                        }
                        Factory.getTechProcessImpl().addPosition(positionEntity, positionEntity.getInsertType());
                        getFactory().getMainWindow().getCheckContainer().getPositionsListContainer().restoreDisplay();
                        clean();
                        dispatchCloseEvent(true);
                    } catch (CheckSumLimitException e) {
                        positionEntity.setQnty(COUNT_PRECISION);
                        doSetPosition(positionEntity);
                        getFactory().showMessage(e.getMessage());
                        dispatchCloseEvent(false);
                    } catch (Exception e) {
                        getFactory().showMessage(e.getMessage());
                        dispatchCloseEvent(false);
                    }
                    setCallDone(true);
                    break;
                case REFUND:
                    if (enterSum) {

                        if (Factory.getTechProcessImpl().getCheck() == null) {
                            try {
                                Factory.getTechProcessImpl().refund(null);
                                JewelProductContainer.super.setProductState(ProductState.ADD);
                                doPositionAdd();
                                enterSum = false;
                                setCallDone(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (Factory.getTechProcessImpl().getCheck().isSale()) {
                                Factory.getTechProcessImpl().getCheck().setReturn();
                            }

                            super.setProductState(ProductState.ADD);
                            doPositionAdd();
                            enterSum = false;
                            setCallDone(true);
                        }

                    } else {
                        if (quantity == 0L) {
                            quantity = 1000L;
                        }
                        getFactory().getMainWindow().getCheckContainer().setPositionsRefund(true);
                        emptyPrice = false;
                        summaR = product.getPrice().getPrice() / 100;
                        summaC = product.getPrice().getPrice() % 100;
                        setProductPrice(product.getPrice().getPrice(), summaR, summaC);
                        enterSum = true;
                        setReset(false);
                        doPositionAdd();
                    }
                    break;
                case EDIT_REFUND:
                    if (quantity == 0L)
                        quantity = getPosition().getQnty() / COUNT_PRECISION;
                    if (getPosition().getQnty() - quantity * COUNT_PRECISION >= 0L) {
                        PositionEntity position = null;
                        try {
                            position = getPosition().cloneLight();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        position.setQnty(quantity * COUNT_PRECISION);
                        getPosition().setQnty(getPosition().getQnty() - quantity * COUNT_PRECISION);
                        try {
                            Factory.getTechProcessImpl().addPosition(position, position.getInsertType());
                            dispatchCloseEvent(true);
                        } catch (Exception e) {
                            getFactory().showMessage(e.getMessage());
                            dispatchCloseEvent(false);
                        }
                        setPosition(null);
                    }
                    setCallDone(true);
                    break;
                case EDIT:
                    if (quantity == 0L) {
                        quantity = getPosition().getQnty() / COUNT_PRECISION;
                    }
                    getPosition().setQnty(quantity * COUNT_PRECISION);
                    try {
                        getFactory().getTechProcess().changePosition(getPosition());
                        dispatchCloseEvent(true);
                    } catch (Exception e) {
                        getFactory().getTechProcess().error();
                        getFactory().showMessage(e.getMessage());
                    }

                    setPosition(null);
                    setCallDone(true);
                    break;
            }
        } else {
            getFactory().getTechProcess().error();
            quantity = 0L;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void esc() {

        if (getVisualPanel().getState() == ProductState.EDIT_OR_DELETE) {
            getFactory().getMainWindow().getCheckContainer().setState(CheckState.EDIT_CHECK);
            return;
        }

        super.esc();
        if (!isReset()) {
            reset();
        } else {
            setManualAdvertisingActions(null);
        }
    }

    @Override
    public void clean() {
        reset();
        setPosition(null);
        pos = new PositionEntity();
        setManualAdvertisingActions(null);
    }

    @Override
    public void reset() {
        if (!enterSum || (emptySum && emptyPrice)) {
            quantity = 0L;
            enterSum = false;
            setReset(true);

        } else {
            summaR = oldPrice / 100;
            summaC = oldPrice % 100;
            oneCoin = false;
            emptyPrice = true;
            emptySum = true;
        }

    }

    @Override
    public boolean isProductStateAllowed(ProductState state) {
        return state == ProductState.ADD || state == ProductState.VIEW || (state == ProductState.EDIT) || (state == ProductState.EDIT_OR_DELETE)
                || (state == ProductState.EDIT_REFUND);
    }

    public JewelProductComponent getCommonProductComponent() {
        return getVisualPanel();
    }

    @Override
    public void setPosition(PositionEntity position) {
        super.setPosition(position);
        quantity = 0L;

        if (getProductState() == ProductState.EDIT_OR_DELETE) {
            checkRightsForEditAndDelete();
        }
    }

    private void checkRightsForEditAndDelete() {
        editButtonEnabled = (product != null && product.getProductConfig().getChangeQnty()
                && product.getProductPositionData().getCanChangeQuantity().orElse(true)
                && (getFactory().checkUserRight(Right.COUNT_INCREASE)
                || getFactory().checkUserRight(Right.COUNT_REDUCE)));
        deleteButtonEnabled = Factory.getTechProcessImpl().canUserDeletePosition(getPosition());

        if (editButtonEnabled) {
            selectedButton = Buttons.DELETE;
        } else if (deleteButtonEnabled) {
            selectedButton = Buttons.EDIT;
        } else {
            selectedButton = Buttons.NONE;
        }
        changeSelection();
    }

    public void setProductState(ProductState state) {
        super.setProductState(state);
        if (isVisualPanelCreated()) {
            getVisualPanel().setProductState(state);
            /*
             * https://crystals.atlassian.net/browse/SRTB-4732
             * Может быть такое, что сюда попали, когда в плагине нету товара (и позиции).
             * Сценарий:
             * 1) Чек собрался через МЧ
             * 2) Зашли в "Редактирование чека"
             * 3) Жмакнули на ювелирку
             * Позицию позже сетят в CheckContainer и в местном сеттере вызывается эта проверка.
             * Тогда не надо лезть в проверку прав, которая после работы по "недорогим товарам" дергает позицию, ставя кассу раком
             * Не удалено полностью, т.к. уже никто не скажет зачем оно тут вообще было, пусть остается "на всякий случай"
             */
            if (product != null && state == ProductState.EDIT_OR_DELETE) {
                checkRightsForEditAndDelete();
            }
        }
    }

    @Override
    public String getProductMultiple(Long quantity, Long price) {
        String q = "";
        Long qq = quantity / COUNT_PRECISION;
        if (qq != 1L)
            q = "x" + qq.toString().trim() + " ";
        return q + (FormatHelper.formatSumma(qq * price / 100.0).trim());
    }

    @Override
    public PositionEntity makeNewPosition(Class<? extends PositionEntity> positionClass) {
        Long q = quantity;
        if (quantity == 0L)
            q = 1000L;
        PositionEntity positionEntity = super.makeNewPosition(positionClass);
        if (enterSum) {

            if (summaC.toString().length() > 1)
                product.getPrice().setPrice(summaR * 100 + summaC);
            else if (!oneCoin)
                product.getPrice().setPrice(summaR * 100 + summaC * 10);
            else
                product.getPrice().setPrice(summaR * 100 + summaC);

        }

        positionEntity.setProduct(product);

        positionEntity.setPriceStart(product.getPrice().getPrice());
        positionEntity.setProductType(product.getClass().getSimpleName());
        positionEntity.setQnty(q);
        positionEntity.setNdsClass(product.getNdsClass());
        positionEntity.setNds(product.getNds());
        positionEntity.setBarCode(product.getBarCode() == null ? "" : product.getBarCode().getBarCode());
        positionEntity.setItem(product.getItem());
        if (product.getMeasure() != null) {
            positionEntity.setMeasure(new MeasurePositionEntity());
            positionEntity.getMeasure().setCode(product.getMeasure().getCode());
            positionEntity.getMeasure().setName(product.getMeasure().getName());
        }
        positionEntity.setDepartNumber(product.getDepartNumber());
        positionEntity.setName(product.getName());
        positionEntity.setTypePriceNumber(product.getPrice().getNumber());
        positionEntity.setProductSettings(product.getProductConfig());

        final Boolean isDiscountApplicable = product.getIsDiscountApplicable();
        Boolean calculateDiscount = isDiscountApplicable != null ? isDiscountApplicable : product.getProductConfig().getIsDiscountApplicable();
        positionEntity.setCalculateDiscount(calculateDiscount);
        positionEntity.setBeforeManualPrice(product.getBeforeManualPrice());
        positionEntity.setExciseToken(pos.getExciseToken());
        positionEntity.setMarkData(pos.getMarkData());

        if (this.position != null && this.position.isSoftCheckPosition()) {
            positionEntity.setSoftCheckNumber(position.getSoftCheckNumber());
            CheckUtils.copySoftCheckPositionAttributes(positionEntity, this.position);
        }

        return positionEntity;
    }

    @Override
    public String getQuantityString(Double quantity, String measure) {
        return "1 " + measure;
    }

    @Override
    public void dot() {
    }

    private void changeSelection() {

        switch (selectedButton) {
            case EDIT:

                if (deleteButtonEnabled) {
                    selectedButton = Buttons.DELETE;
                    getVisualPanel().changeSelectedButton(false);

                    if (!editButtonEnabled)
                        getVisualPanel().disableEditButton();
                } else {
                    getVisualPanel().changeSelectedButton(true);
                    getVisualPanel().disableDeleteButton();
                }

                break;

            case DELETE:

                if (editButtonEnabled) {
                    selectedButton = Buttons.EDIT;
                    getVisualPanel().changeSelectedButton(true);

                    if (!deleteButtonEnabled)
                        getVisualPanel().disableDeleteButton();
                } else {
                    getVisualPanel().changeSelectedButton(false);
                    getVisualPanel().disableEditButton();
                }
                break;

            case NONE:
                getVisualPanel().disableEditButton();
                getVisualPanel().disableDeleteButton();
                break;
        }
    }

    @Override
    public void left() {
        if (getVisualPanel().getState() == ProductState.EDIT_OR_DELETE) {
            changeSelection();
        }
    }

    @Override
    public void right() {
        if (getVisualPanel().getState() == ProductState.EDIT_OR_DELETE) {
            changeSelection();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private boolean dispatchBarcodeScanned() {
        if (getCommonProductComponent().isCurrentForm(addPositionScanExciseForm)) {
            doPositionAdd();
            return true;
        }

       if (getCommonProductComponent().isCurrentForm(deletePositionScanExciseForm)) {
            doPositionDelete();
            return true;
        }

        return false;
    }

    private boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (getCommonProductComponent().isCurrentForm(deletePositionForm)) {
                if (deletePositionForm.deleteConfirmed()) {
                    if (this.position.getExciseToken() != null) {
                        getCommonProductComponent().showDeletePositionScanExciseForm(this.position);
                    } else {
                        doPositionDelete();
                    }
                } else {
                    resetContainer();
                }
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (getCommonProductComponent().isCurrentForm(addPositionScanExciseForm) && controller.canSkipScanMarkForCurrentPosition()) {
                doPositionAdd();
                return true;
            }

            if (getVisualPanel().getState() == ProductState.EDIT_OR_DELETE
                    || getVisualPanel().getState() == ProductState.DELETE
                    || getVisualPanel().getState() == ProductState.QUICK_DELETE) {
                resetContainer();
                return true;
            }

            getCommonProductComponent().clear();
            clean();
            esc();
            return true;
        }

        return false;
    }

    private void doPositionDelete() {
        boolean checkPermission;
        try {
            checkPermission = controller.tryRequestPermissionDeletePosition(position, getVisualPanel().getState() == ProductState.DELETE);
            Factory.getTechProcessImpl().deletePosition(position, checkPermission);
            resetContainer();
        } catch (Exception ex) {
            Factory.getTechProcessImpl().error(ex.getMessage());
            getFactory().showMessage(ex.getMessage());
        }
    }

    private void resetContainer() {
        CheckContainer checkContainer = getFactory().getMainWindow().getCheckContainer();
        if (checkContainer.getState() != CheckState.SEARCH_PRODUCT) {
            checkContainer.setState(checkContainer.getPreviousState());
        }
        getCommonProductComponent().clear();
        clean();
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn, List<PositionEntity> validatedPositions) {
        return validateReturnJewelForm.validatePositions(returnPurchase, checkReturnPositions, fullReturn, validatedPositions);
    }

}
