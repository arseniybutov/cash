package ru.crystals.pos.visualization.products.pieceweight.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.AgeRestrictedAbstractPluginView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterExpirationCodeForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterProductionDateForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.pieceweight.controller.PieceWeightPluginController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by alexey on 17.07.15.
 * <p>
 * Основная вьюшка штучно-весового товара
 */
public class PieceWeightPluginView extends AgeRestrictedAbstractPluginView<PieceWeightPluginController> {
    private final PieceWeightForm viewForm;
    private final PieceWeightEditForm editForm;
    private final PieceWeightEnterQuantityForm enterQuantityForm;
    private final CommonEditOrDeleteForm editOrDeleteForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final EnterProductionDateForm enterProductionDateForm;
    private final EnterExpirationCodeForm enterExpirationCodeForm;

    public PieceWeightPluginView(Properties properties) {
        super(properties);
        enterQuantityForm = new PieceWeightEnterQuantityForm(this);
        editOrDeleteForm = new CommonEditOrDeleteForm(this);
        viewForm = new PieceWeightForm(this);
        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        editForm = new PieceWeightEditForm(this);
        enterProductionDateForm = new EnterProductionDateForm(this);
        enterExpirationCodeForm = new EnterExpirationCodeForm(this);

        this.addPanel(enterQuantityForm);
        this.addPanel(editOrDeleteForm);
        this.addPanel(viewForm);
        this.addPanel(deletePositionForm);
        this.addPanel(editForm);
        this.addPanel(enterProductionDateForm);
        this.addPanel(enterExpirationCodeForm);
    }

    @Override
    protected void afterAgeConfirmed() {
        if (isKitComponent()) {
            addKitProduct();
        } else {
            setCurrentForm(enterQuantityForm);
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        switch (e.getKeyCode()) {
            //по этим клавишам не выходим наверх
            case KeyEvent.VK_ENTER:
                if (currentForm == messageForm) {
                    if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                        getController().getAdapter().dispatchCloseEvent(false);
                    } else {
                        setCurrentForm(enterQuantityForm);
                        getController().getModel().setState(ProductContainer.ProductState.ADD);
                    }
                    return true;
                }

                if (getController().getModel() != null && getController().getModel().getState() == ProductContainer.ProductState.VIEW) {
                    return true;
                }

                if (currentForm == editOrDeleteForm) {
                    if (needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                        getController().getModel().setProduct(getController().getModel().getPosition().getProduct());
                        setCurrentForm(enterProductionDateForm);
                        return true;
                    }

                    if (editOrDeleteForm.isEdit()) {
                        setCurrentForm(editForm);
                    } else {
                        getController().returnPosition(getController().getModel().getPosition());
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
                            if (getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(),
                                    enterProductionDateForm.getProductionDate(), true)) {
                                getController().getAdapter().dispatchCloseEvent(true);
                            }
                            break;
                        case QUICK_EDIT:
                            if (!updateQuantityForPositionProductionDate(enterProductionDateForm)) {
                                return true;
                            }

                            setCurrentForm(enterQuantityForm);
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

                            if (editOrDeleteForm.isEdit()) {
                                setCurrentForm(editForm);
                            } else {
                                getController().returnPosition(getController().getModel().getPosition());
                            }
                            break;
                        default:
                            break;
                    }
                    return true;
                }

                if (isCurrentForm(editForm) || getController().getModel().getState() == ProductState.QUICK_EDIT) {
                    BigDecimal quantity = isCurrentForm(editForm) ? editForm.getQuantity() : enterQuantityForm.getQuantity();
                    boolean checkPermission;
                    try {
                        checkPermission = getController().tryRequestPermissionEditPosition(getController().getModel().getPosition(), quantity,
                                isCurrentForm(editForm));
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

                //если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
                //добавляем позицию в чек.
                if (isCurrentForm(enterQuantityForm)) {
                    if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                        getController().fillDefaultPosition(enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), getController().getModel().getProduct(),
                                getController().getModel().getPosition());
                        if (getController().isPossibleToAddPosition(getController().getModel().getPosition())) {
                            setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                        }
                    } else {
                        if (currentForm.getQuantity() == null || currentForm.getQuantity().compareTo(BigDecimal.ZERO) == 0 ||
                                currentForm.getPrice() == null || currentForm.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                            getController().beepError("PieceWeight. Quantity or price is null. Cannot add or edit position.");
                        }
                        if (getController().getModel() != null && getController().getModel().getState() == ProductContainer.ProductState.ADD) {
                            if (getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(),
                                    true)) {
                                getController().getAdapter().dispatchCloseEvent(true);
                            }
                        }
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
                    if (getController().getModel() != null && getController().getModel().getState() == ProductContainer.ProductState.ADD) {
                        if (getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true)) {
                            getController().getAdapter().dispatchCloseEvent(true);
                        }
                    }
                    return true;
                }

                return false;

            case KeyEvent.VK_ESCAPE:
                if (isCurrentForm(messageForm)) {
                    if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                        getController().getAdapter().dispatchCloseEvent(false);
                    } else {
                        setCurrentForm(enterQuantityForm);
                        getController().getModel().setState(ProductContainer.ProductState.ADD);
                    }
                    return true;
                }

                if (isCurrentForm(editForm)) {
                    setCurrentForm(editOrDeleteForm);
                }
                return false;
        }
        return false;
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
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
    public boolean dispatchMSREvent(String Track1, String Track2, String Track3, String Track4) {
        return false;
    }

    @Override
    public void modelChanged() {
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        switch (getController().getModel().getState()) {
            case ADD:
                boolean hasScannedProductionDate = getController().canUseProductionDate() && getController().hasScannedProductionDate();
                boolean hasScannedExpirationDate = getController().hasScannedExpirationDate();
                boolean isProductionDateValid = getController().getModel().isScannedProductionDateValid();
                boolean isProductExpired = getController().getModel().isCurrentProductExpiredByScannedDate();
                boolean isExpirationDateValid = getController().getModel().isScannedExpirationDateValid(getController().isRefund());
                boolean needCheckExpirationDate = getController().needCheckExpirationDate();
                if (isCurrentForm(waitForm)) {
                    setCurrentFormWithoutAdditionalActions(enterQuantityForm);
                } else if (hasScannedExpirationDate && !isExpirationDateValid && needCheckExpirationDate) {
                    messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
                    messageForm.setMessage(getController().getModel().buildErrorMessageForExpirationDate(getController().isRefund()));
                    setCurrentForm(messageForm);
                    getController().beepError("Incorrect expiration date scanned.");
                } else if (hasScannedProductionDate && (!isProductionDateValid || isProductExpired && needCheckExpirationDate)) {
                    messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
                    messageForm.setMessage(ResBundleVisualization.getString(!isProductionDateValid ? "INCORRECT_PRODUCTION_DATE" : "PRODUCT_HAS_EXPIRED"));
                    setCurrentForm(messageForm);
                    getController().beepError("Incorrect production date scanned.");
                } else if (getController().getModel().getAgeRestriction() != 0) {
                    checkAgePanel.reset();
                    checkAgePanel.setMinAge(getController().getModel().getAgeRestriction());
                    setCurrentPanel(checkAgePanel);
                    getController().onAgeChecking();
                } else {
                    setCurrentForm(enterQuantityForm);
                    PositionEntity position = new PositionEntity();
                    getController().fillDefaultPosition(BigDecimal.ONE, enterQuantityForm.getPrice(), getController().getModel().getProduct(), position);
                    getController().getModel().setPosition(position);
                }
                break;
            case QUICK_EDIT:
                getController().getModel().setPosition(getController().getModel().getPosition());
                getController().getModel().setProduct(getController().getModel().getPosition().getProduct());

                if (!(needEnterProductionDate || getController().getModel().isVeterinaryControl())) {
                    setCurrentForm(enterQuantityForm);
                    getController().fillDefaultPosition(getController().getModel().getPosition().getQntyBigDecimal(),
                            getController().getModel().getPosition().getPriceStartBigDecimal(),
                            getController().getModel().getProduct(), getController().getModel().getPosition());
                } else {
                    setCurrentForm(enterProductionDateForm);
                }
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case DELETE:
            case QUICK_DELETE:
                boolean hasProductionDate = getController().canUseProductionDate() && getController().hasProductionDate();
                if (!(hasProductionDate || getController().getModel().isVeterinaryControl())) {
                    setCurrentForm(deletePositionForm);
                } else {
                    getController().getModel().setPosition(getController().getModel().getPosition());
                    getController().getModel().setProduct(getController().getModel().getPosition().getProduct());
                    setCurrentForm(enterProductionDateForm);
                }
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            case ADD_CURRENT:
                //спец состояние, когда касса сама инициирует добавление по тем данным, которые сейчас на текущей форме
                // (сканирование следующего товара, подитог на форме введения кол-ва)
                if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                    setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                    if (needEnterExpirationDate) {
                        enterExpirationCodeForm.setError(true);
                    } else {
                        enterProductionDateForm.setError(true);
                    }
                    getController().getModel().setState(ProductState.ADD);
                } else if (currentForm == enterQuantityForm) {
                    getController().addPosition(getController().getModel().getProduct(), currentForm.getQuantity(), currentForm.getPrice(), false);
                } else {
                    getController().beepError("Cannot add piece position from this form " + currentForm);
                }
                break;
            case SHOW_MESSAGE:
                messageForm.setMessage(getController().getModel().getMessage());
                messageForm.setExitState(CommonMessageForm.ExitState.TO_LAST);
                setCurrentForm(messageForm);
                break;
            case SHOW_WAIT:
                waitForm.setActionStatusText(getController().getModel().getMessage());
                setCurrentPanel(waitForm);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean checkDataBeforeAdd() {
        if (currentForm == enterQuantityForm || currentForm == enterProductionDateForm || currentForm == enterExpirationCodeForm) {
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
        if (currentForm == enterQuantityForm || isCurrentForm(waitForm)) {
            result = BigDecimalConverter.convertQuantity(enterQuantityForm.getQuantity());
        }
        return result == null ? 0 : result;
    }
}
