package ru.crystals.pos.visualization.products.weight.view;

import ru.crystals.pos.CashException;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.product.events.WeightProductAddedWithScalesEvent;
import ru.crystals.pos.product.events.payload.WeightProductAddedPayload;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.QuestionForm;
import ru.crystals.pos.visualization.commonplugin.view.AgeRestrictedAbstractPluginView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterExpirationCodeForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterProductionDateForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.weight.controller.WeightProductController;
import ru.crystals.pos.visualization.products.weight.model.WeightProductPluginModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 * View весового товара
 * Петеключаеимся между форами весового товара
 */
public class WeightProductView extends AgeRestrictedAbstractPluginView<WeightProductController> {
    private final CommonMessageForm messageForm;
    private final WeightProductEnterWeightForm enterWeightForm;
    private final WeightProductEditOrDeleteForm editOrDeleteForm;
    private final WeightProductViewForm viewForm;
    private final WeightProductSelectTareForm tareForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final EnterProductionDateForm enterProductionDateForm;
    private final QuestionForm questionForm;
    private final EnterExpirationCodeForm enterExpirationCodeForm;

    public WeightProductView(Properties properties) {
        super(properties);
        WeightProductView.this.setName("ru.crystals.pos.visualization.products.weight.view.WeightProductView");
        enterWeightForm = new WeightProductEnterWeightForm(this);
        messageForm = new CommonMessageForm(this);
        editOrDeleteForm = new WeightProductEditOrDeleteForm(this);
        viewForm = new WeightProductViewForm(this);
        tareForm = new WeightProductSelectTareForm(this);
        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        enterProductionDateForm = new EnterProductionDateForm(this);
        questionForm = new QuestionForm(this);
        enterExpirationCodeForm = new EnterExpirationCodeForm(this);

        this.addPanel(enterWeightForm);
        this.addPanel(editOrDeleteForm);
        this.addPanel(messageForm);
        this.addPanel(viewForm);
        this.addPanel(tareForm);
        this.addPanel(deletePositionForm);
        this.addPanel(enterProductionDateForm);
        this.addPanel(questionForm);
        this.addPanel(enterExpirationCodeForm);
    }

    @Override
    protected void afterAgeConfirmed() {
        if (isKitComponent()) {
            addKitProduct();
        } else {
            updateWeightData();
            if (needEnterWeight()) {
                showEnterWeightForm();
            }
        }
    }

    @Override
    public void modelChanged() {
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        switch (getController().getModel().getState()) {
            case ADD:
                tryToSetPosition(getController().getModel());
                break;
            case QUICK_EDIT:
                getController().getModel().setPosition(getController().getModel().getPosition());
                getController().getModel().setProduct(getController().getModel().getPosition().getProduct());
                setCurrentForm(enterWeightForm);
                getController().fillDefaultPosition(getController().getModel().getPosition().getQntyBigDecimal(),
                        getController().getModel().getPosition().getPriceStartBigDecimal(),
                        getController().getModel().getProduct(), getController().getModel().getPosition());
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            case ADD_CURRENT:
                if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                    setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                    if (needEnterExpirationDate) {
                        enterExpirationCodeForm.setError(true);
                    } else {
                        enterProductionDateForm.setError(true);
                    }
                    getController().getModel().setState(ProductState.ADD);
                } else if (currentForm == enterWeightForm) {
                    //спец состояние, когда касса сама инициирует добавление по тем данным, которые сейчас на текущей форме
                    addPosition(currentForm.getQuantity(), currentForm.getPrice(), null, false);
                } else {
                    getController().beepError("Cannot add weight position from this form");
                }
                break;
            case SHOW_MESSAGE:
                messageForm.setMessage(getController().getModel().getMessage());
                messageForm.setExitState(CommonMessageForm.ExitState.TO_LAST);
                setCurrentForm(messageForm);
                break;
            case DELETE:
            case QUICK_DELETE:
                boolean hasProductionDate = getController().canUseProductionDate() && getController().hasProductionDate();
                if (!(hasProductionDate || getController().getModel().isVeterinaryControl())) {
                    setCurrentForm(deletePositionForm, false);
                } else {
                    getController().getModel().setPosition(getController().getModel().getPosition());
                    getController().getModel().setProduct(getController().getModel().getPosition().getProduct());
                    setCurrentForm(enterProductionDateForm);
                }
                break;
            case SHOW_WAIT:
                waitForm.setActionStatusText(getController().getModel().getMessage());
                setCurrentPanel(waitForm);
                break;
            case SHOW_QUESTION:
                getController().getModel().setSavedWeight(enterWeightForm.getQuantity());
                fillQuestionForm(getController().getModel().getMessage());
                setCurrentPanel(questionForm);
                break;
            default:
                break;
        }
    }

    /**
     * В зависимости от текущего состояния плагина и данных, позиция либо будет отображена в подвале,
     * либо будет вызвано сообщения о подтверждении, при весе меньшем чем разрешенный
     *
     * @param eventModel модель данных
     */
    private void tryToSetPosition(WeightProductPluginModel eventModel) {
        boolean hasScannedProductionDate = getController().canUseProductionDate() && getController().hasScannedProductionDate();
        boolean hasScannedExpirationDate = getController().hasScannedExpirationDate();
        boolean isProductionDateValid = eventModel.isScannedProductionDateValid();
        boolean isProductExpired = eventModel.isCurrentProductExpiredByScannedDate();
        boolean isExpirationDateValid = eventModel.isScannedExpirationDateValid(getController().isRefund());
        boolean needCheckExpirationDate = getController().needCheckExpirationDate();
        if (isCurrentForm(waitForm)) {
            setCurrentFormWithoutAdditionalActions(enterWeightForm);
        } else if (hasScannedExpirationDate && !isExpirationDateValid && needCheckExpirationDate) {
            messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
            messageForm.setMessage(eventModel.buildErrorMessageForExpirationDate(getController().isRefund()));
            setCurrentForm(messageForm);
            getController().beepError("Incorrect expiration date scanned.");
        } else if (hasScannedProductionDate && (!isProductionDateValid || isProductExpired && needCheckExpirationDate)) {
            messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
            messageForm.setMessage(ResBundleVisualization.getString(!isProductionDateValid ? "INCORRECT_PRODUCTION_DATE" : "PRODUCT_HAS_EXPIRED"));
            setCurrentForm(messageForm);
            getController().beepError("Incorrect production date scanned.");
        } else if (eventModel.getAgeRestriction() != 0) {
            checkAgePanel.reset();
            checkAgePanel.setMinAge(eventModel.getAgeRestriction());
            setCurrentPanel(checkAgePanel);
            getController().onAgeChecking();
        } else {
            updateWeightData();
            if (needEnterWeight()) {
                showEnterWeightForm();
            }
        }
    }

    private void updateWeightData() {
        ProductWeightEntity product = (ProductWeightEntity) getController().getModel().getProduct();
        long productionWeight = Optional.ofNullable(product.getProductPositionData()).map(ProductPositionData::getWeight).orElse(0L);
        if (productionWeight > 0 && product.useWeightAndScaleSectionFromGS1()) {
            product.setWeightBigDecimal(BigDecimal.valueOf(productionWeight,
                    Optional.ofNullable(product.getProductPositionData()).map(ProductPositionData::getWeightScale).orElse(0)));
        }
        Long weight = product.getWeight();
        getController().setWeightScanned(weight != null && weight > 0);
    }

    private boolean needEnterWeight() {
        return !getController().isWeightScanned()
                || getController().checkAndNotifyForLowWeight(getController().getModel().getProduct(),
                ((ProductWeightEntity) getController().getModel().getProduct()).getWeight());
    }

    private void showEnterWeightForm() {
        setCurrentForm(enterWeightForm);
        PositionEntity position = new PositionEntity();
        getController().fillDefaultPosition(enterWeightForm.getQuantity(), enterWeightForm.getPrice(), getController().getModel().getProduct(), position);
        getController().getModel().setPosition(position);
    }

    private void fillQuestionForm(String message) {
        questionForm.setText(message);
        questionForm.setButtonText(ResBundleVisualization.getString("ACCEPT"),
                ResBundleVisualization.getString("CANCEL"));
        questionForm.selectNoButton();
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP ||
                e.getKeyCode() == KeyEvent.VK_DOWN) {
            return true;
        }

        //если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentForm == messageForm) {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                    return true;
                } else {
                    getController().getModel().setState(ProductState.ADD);
                    setCurrentForm(enterWeightForm);
                    return false;
                }
            }

            if (getController().getModel().getState() == ProductState.SHOW_MESSAGE) {
                getController().getModel().setState(ProductState.ADD);
            }

            if (getController().getModel().getState() == ProductContainer.ProductState.VIEW) {
                return true;
            }

            if (currentForm == editOrDeleteForm) {
                if (needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                    getController().getModel().setProduct(getController().getModel().getPosition().getProduct());
                    setCurrentForm(enterProductionDateForm);
                    return true;
                }

                getController().returnPosition(getController().getModel().getPosition());
                return true;
            } else if (getController().getModel().getState() == ProductState.QUICK_EDIT) {
                BigDecimal quantity = enterWeightForm.getQuantity();
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionEditPosition(getController().getModel().getPosition(), quantity, false);
                } catch (NoPermissionException ex) {
                    getController().beepError(ex.getMessage());
                    return true;
                }
                boolean productionDateEnteredManually =
                        getController().canUseProductionDate() && getController().hasProductionDate() && !getController().hasScannedProductionDate();
                if (productionDateEnteredManually) {
                    getController().changeQuantity(getController().getModel().getPosition(), quantity, enterProductionDateForm.getProductionDate(), checkPermission);
                } else if (!getController().getModel().isVeterinaryControl()) {
                    getController().changeQuantity(getController().getModel().getPosition(), quantity, null, checkPermission);
                } else {
                    getController().changeQuantity(getController().getModel().getPosition(), quantity, enterProductionDateForm.getProductionDate(), checkPermission);
                }
                return true;
            }

            if (isCurrentForm(deletePositionForm)) {
                //на форме подтверждения нажали ентер - проанализируем
                if (deletePositionForm.deleteConfirmed()) {
                    boolean checkPermission;
                    try {
                        checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                                getController().getModel().getState() == ProductState.DELETE);
                    } catch (NoPermissionException ex) {
                        getController().beepError(ex.getMessage());
                        return true;
                    }
                    getController().cashDeletePosition(getController().getModel().getPosition(), enterProductionDateForm.getProductionDate(), !checkPermission);
                } else {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            }

            if (isCurrentForm(enterProductionDateForm)) {
                switch (getController().getModel().getState()) {
                    case ADD:
                        if (getController().getModel().isProductExpired(enterProductionDateForm.getProductionDate())) {
                            getController().beepError("Product has expired");
                            Factory.getInstance().showMessage(ResBundleVisualization.getString("PRODUCT_HAS_EXPIRED"));

                            return false;
                        }
                        addPosition(enterWeightForm.getQuantity(), enterWeightForm.getPrice(), enterProductionDateForm.getProductionDate(), true);
                        break;
                    case QUICK_EDIT:
                        if (!updateQuantityForPositionProductionDate(enterProductionDateForm)) {
                            return true;
                        }

                        setCurrentForm(enterWeightForm);
                        break;
                    case DELETE:
                    case QUICK_DELETE:
                        if (!canDeletePositionWithProductionDate(enterProductionDateForm)) {
                            return true;
                        }

                        setCurrentForm(deletePositionForm);
                        break;
                    case EDIT_OR_DELETE:
                        if (!updateQuantityForPositionProductionDate(enterProductionDateForm)) {
                            return true;
                        }

                        getController().returnPosition(getController().getModel().getPosition());
                        break;
                    default:
                        break;
                }
                return true;
            }

            if (isCurrentForm(enterExpirationCodeForm) && getController().getModel().getState() == ProductState.ADD) {
                Date expirationDate = enterExpirationCodeForm.getExpirationDate();
                if (!getController().getModel().isExpirationDateValid(getController().isRefund(), expirationDate)) {
                    String msg = getController().getModel().buildErrorMessageForExpirationDate(getController().isRefund(), expirationDate);
                    getController().beepError(msg);
                    Factory.getInstance().showMessage(msg);
                    return false;
                }
                getController().getModel().updateExpirationDate(enterExpirationCodeForm.getExpirationCode());
                addPosition();
                return true;
            }

            //если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
            //добавляем позицию в чек.
            if (getController().getModel().getState() == ProductContainer.ProductState.ADD && currentForm.getQuantity() != null &&
                    currentForm.getPrice() != null) {
                if (isCurrentForm(enterWeightForm)) {
                    if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                        getController().fillDefaultPosition(enterWeightForm.getQuantity(), enterWeightForm.getPrice(), getController().getModel().getProduct(),
                                getController().getModel().getPosition());
                        if (getController().isPossibleToAddPosition(getController().getModel().getPosition())) {
                            setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                        }
                    } else {
                        ProductWeightEntity product = (ProductWeightEntity) getController().getModel().getProduct();
                        PositionEntity position = getController().getModel().getPosition();
                        ProductConfig productConfig = product.getProductConfig();
                        if (isTaringRequired(productConfig) && !tareForm.isTareSelected() && !getController().isWeightScanned()) {
                            product.setWeightBigDecimal(enterWeightForm.getQuantity());
                            position.setQntyBigDecimal(enterWeightForm.getQuantity());
                            tareForm.refreshTare(product);
                            if (tareForm.isTareAvailable()) {
                                tareForm.setProduct(product);
                                setCurrentForm(tareForm);
                            } else {
                                addPosition();
                            }
                        } else {
                            addPosition();
                        }
                    }
                } else {
                    if (tareForm.getTare().getWeight().compareTo(BigDecimal.ZERO) == 0) {
                        addPosition();
                    } else {
                        setCurrentForm(enterWeightForm);
                    }
                }
                return true;
            }

            if (isCurrentForm(questionForm)) {
                if (questionForm.isCommit()) {
                    setCurrentForm(enterWeightForm);
                    enterWeightForm.weightChange(getController().getModel().getSavedWeight());
                    getController().lowWeightConfirmed();
                } else {
                    tryToCancelPosition();
                }
                return true;
            }

            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            tareForm.clear();
            if (currentForm == messageForm) {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                } else {
                    setCurrentForm(enterWeightForm);
                }
                return true;
            }
            if (currentForm.equals(tareForm)) {
                setCurrentForm(enterWeightForm);
                getController().sendChangeWeightEvent(enterWeightForm.getQuantity());
                return true;
            }
            if (isCurrentForm(questionForm)) {
                tryToCancelPosition();
                return true;
            }
            getController().getModel().setLowWeightConfirmed(false);
            return false;
        }
        return false;
    }

    private boolean isTaringRequired(ProductConfig productConfig) {
        return productConfig.isTaringEnabled() && getController().getScales() != null && getController().getScales().moduleCheckState();
    }

    private void addPosition(BigDecimal quantity, BigDecimal price, Date productionDate, boolean addManually) {
        ProductEntity product = getController().getModel().getProduct();
        if (getController().addPosition(product, quantity, price, productionDate, addManually)) {
            WeightProductAddedPayload payload = new WeightProductAddedPayload(product.getItem(), quantity, getController().getModel().getPosition().getInsertType());
            // Отправим в умные весы. Если их нет - ничего не произойдёт.
            if (!getController().isWeightScanned()) {
                Factory.getTechProcessImpl().getTechProcessEvents().publishEvent(new WeightProductAddedWithScalesEvent(this, payload));
            }
            getController().getAdapter().dispatchCloseEvent(true);
            getController().getModel().setLowWeightConfirmed(false);
        }
    }

    private void addPosition() {
        addPosition(enterWeightForm.getQuantity(), enterWeightForm.getPrice(), null, true);
        enterWeightForm.clear();
        tareForm.clear();
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (isCurrentForm(questionForm)) {
            getController().beepError("Scan on the weight confirmation window");
            try {
                getController().eventScanOutCheck(barcode);
            } catch (CashException ex) {
                // здесь ничего не делаем, залогировались в методе контроллера
            }
            return true;
        }
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        boolean hasScannedExpirationDate = getController().hasScannedExpirationDate();
        boolean isExpirationDateValid = getController().getModel().isScannedExpirationDateValid(getController().isRefund());
        boolean needCheckExpirationDate = getController().needCheckExpirationDate();

        if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
            setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
            if (needEnterExpirationDate) {
                enterExpirationCodeForm.setError(true);
            } else {
                enterProductionDateForm.setError(true);
            }
            return true;
        } else if (hasScannedExpirationDate && !isExpirationDateValid && needCheckExpirationDate) {
            messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
            messageForm.setMessage(getController().getModel().buildErrorMessageForExpirationDate(getController().isRefund()));
            setCurrentForm(messageForm);
            getController().beepError("Incorrect expiration date scanned.");
            return true;
        }
        return false;
    }

    @Override
    public void setController(WeightProductController controller) {
        super.setController(controller);
        this.enterWeightForm.setController(controller);
        this.viewForm.setController(controller);
        tareForm.setController(controller);
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    public boolean checkDataBeforeAdd() {
        if (currentForm == enterWeightForm || currentForm == tareForm && !tareForm.isTareSelected() || currentForm == enterProductionDateForm || currentForm == enterExpirationCodeForm) {
            try {
                getController().checkInputDataBeforeAdd(getController().getModel().getProduct(), currentForm.getQuantity(), currentForm.getPrice());
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public long getCurrentPositionCount() {
        Long result = 0L;
        if (currentForm == enterWeightForm || currentForm == tareForm && !tareForm.isTareSelected()) {
            result = BigDecimalConverter.convertQuantity(currentForm.getQuantity());
        } else if (isCurrentForm(waitForm)) {
            result = BigDecimalConverter.convertQuantity(enterWeightForm.getQuantity());
        }
        return result == null ? 0 : result;
    }

    public boolean isWeightLessThanMinValue() {
        if (getController().getModel().isLowWeightConfirmed()) {
            return false;
        }
        BigDecimal quantity = enterWeightForm.getQuantity();
        return getController().isLessThatMinValue(quantity);
    }

    private void tryToCancelPosition() {
        if (Factory.getTechProcessImpl().checkUserRight(Right.ADDITION_POSITION_CANCEL)) {
            getController().getAdapter().dispatchCloseEvent(false);
        } else {
            getController().beepError("User has no right to cancel position");
        }
    }
}
