package ru.crystals.pos.visualization.products.piece.view;

import ru.crystals.pos.catalog.ProductPieceController;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.AgeRestrictedAbstractPluginView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterExpirationCodeForm;
import ru.crystals.pos.visualization.commonplugin.view.form.EnterProductionDateForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.piece.controller.PiecePluginController;
import ru.crystals.pos.visualization.products.piece.model.PiecePluginModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;

public class PiecePluginView extends AgeRestrictedAbstractPluginView<PiecePluginController> {
    private final PieceEnterQuantityForm enterQuantityForm;
    private final PieceEditOrDeleteForm editOrDeleteForm;
    private final PieceViewForm viewForm;
    private final PieceEditForm editForm;
    private final EnterProductionDateForm enterProductionDateForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final ProductPieceController pieceController;
    private final PieceAddPositionScanExciseForm scanExciseForAddForm;
    private final PieceDeletePositionScanExciseForm scanExciseForDeleteForm;
    private final EnterExpirationCodeForm enterExpirationCodeForm;

    public PiecePluginView(Properties properties, ProductPieceController productPieceController) {
        super(properties);
        PiecePluginView.this.setName("ru.crystals.pos.visualization.products.piece.view.PiecePluginView");
        pieceController = productPieceController;
        enterQuantityForm = new PieceEnterQuantityForm(this);
        editOrDeleteForm = new PieceEditOrDeleteForm(this);
        editForm = new PieceEditForm(this);
        viewForm = new PieceViewForm(this);
        enterProductionDateForm = new EnterProductionDateForm(this);

        scanExciseForAddForm = new PieceAddPositionScanExciseForm(this);
        scanExciseForDeleteForm = new PieceDeletePositionScanExciseForm(this);

        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        enterExpirationCodeForm = new EnterExpirationCodeForm(this);

        this.addPanel(enterQuantityForm);
        this.addPanel(editOrDeleteForm);
        this.addPanel(viewForm);
        this.addPanel(editForm);
        this.addPanel(deletePositionForm);
        this.addPanel(enterProductionDateForm);
        this.addPanel(scanExciseForAddForm);
        this.addPanel(scanExciseForDeleteForm);
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
        //если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentForm == messageForm) {
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return false;
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

            if (isCurrentForm(deletePositionForm)) {
                //на форме подтверждения нажали ентер - проанализируем
                if (deletePositionForm.deleteConfirmed()) {
                    if (getController().getModel().getPosition().getExciseToken() != null) {
                        setCurrentFormWithoutAdditionalActions(scanExciseForDeleteForm);
                    } else {
                        if (tryDeletePosition()) {
                            return true;
                        }
                    }
                } else {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            }

            if (getController().getModel() != null &&
                    isCurrentForm(enterQuantityForm) &&
                    getController().getModel().getState() == ProductContainer.ProductState.ADD &&
                    currentForm.getQuantity() != null &&
                    currentForm.getPrice() != null) {
                if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                    getController().fillDefaultPosition(enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), getController().getModel().getProduct(),
                            getController().getModel().getPosition());
                    if (getController().isPossibleToAddPosition(getController().getModel().getPosition())) {
                        setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                    }
                } else {
                    //если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
                    //добавляем позицию в чек.
                    addPosition();
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
                            clear();
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
                }
                return true;
            }

            if (isCurrentForm(editForm) || getController().getModel().getState() == ProductState.QUICK_EDIT) {
                BigDecimal quantity = isCurrentForm(editForm) ? editForm.getQuantity() : enterQuantityForm.getQuantity();
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionEditPosition(getController().getModel().getPosition(), quantity, isCurrentForm(editForm));
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

            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (isCurrentForm(scanExciseForAddForm) && getController().onSkipScanMark()) {
                return true;
            }

            if (currentForm == messageForm) {
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return false;
            }

            if (currentForm == editForm) {
                setCurrentForm(editOrDeleteForm);
            }

            clear();
            return false;
        }
        return false;
    }

    /**
     * @return если true, то нужно прервать поток выполнения. Удаление не доступно
     */
    private boolean tryDeletePosition() {
        boolean checkPermission;
        try {
            checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                    getController().getModel().getState() == ProductState.DELETE);
        } catch (NoPermissionException ex) {
            getController().beepError(ex.getMessage());
            return true;
        }
        getController().cashDeletePosition(getController().getModel().getPosition(), enterProductionDateForm.getProductionDate(), !checkPermission);
        clear();
        return false;
    }

    private void addPosition() {
        if (getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true)) {
            clear();
            getController().getAdapter().dispatchCloseEvent(true);
        }
    }

    public void clear() {
        getController().stopBeepError();
        scanExciseForAddForm.clear();
        deletePositionForm.clear();
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

        if (isCurrentForm(scanExciseForAddForm)) {
            processAddState(getController().getModel());
            return true;
        }

        if (isCurrentForm(scanExciseForDeleteForm)) {
            tryDeletePosition();
            return true;
        }

        return false;
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    public void modelChanged() {
        boolean needEnterProductionDate = getController().canUseProductionDate() && getController().needEnterProductionDate();
        boolean needEnterExpirationDate = getController().productNeedExpirationDate() && !getController().hasScannedExpirationDate();
        switch (getController().getModel().getState()) {
            case ADD:
                if (isCurrentForm(waitForm)) {
                    setCurrentFormWithoutAdditionalActions(enterQuantityForm);
                } else {
                    processAddState(getController().getModel());
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
            case VIEW:
                setCurrentForm(viewForm, false);
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
            case ADD_CURRENT:
                //спец состояние, когда касса сама инициирует добавление по тем данным, которые сейчас на текущей форме
                // (сканирование следующего товара, подитог на форме введения кол-ва)
                if (isCurrentForm(checkAgePanel)) {
                    getController().addPosition(getController().getModel().getProduct(), getController().getModel().getPosition().getQntyBigDecimal(),
                            getController().getModel().getPosition().getPriceStartBigDecimal(), false);
                    clear();
                } else if (needEnterExpirationDate || needEnterProductionDate || getController().getModel().isVeterinaryControl()) {
                    setCurrentForm(needEnterExpirationDate ? enterExpirationCodeForm : enterProductionDateForm);
                    if (needEnterExpirationDate) {
                        enterExpirationCodeForm.setError(true);
                    } else {
                        enterProductionDateForm.setError(true);
                    }
                    getController().getModel().setState(ProductState.ADD);
                } else if (isCurrentForm(enterQuantityForm)) {
                    boolean added = getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(),
                            false);
                    clear();
                    if (added && getController().getAdapter().getPosition().isSoftCheckPosition()) {
                        getController().getAdapter().dispatchCloseEvent(true);
                    }
                } else {
                    getController().beepError("Cannot add piece position from this form " + currentForm);
                }
                break;
            case SHOW_WAIT:
                waitForm.setActionStatusText(getController().getModel().getMessage());
                setCurrentPanel(waitForm);
                break;
            default:
                break;
        }
    }

    private void processAddState(PiecePluginModel eventModel) {
        boolean hasScannedProductionDate = getController().canUseProductionDate() && getController().hasScannedProductionDate();
        boolean hasScannedExpirationDate = getController().hasScannedExpirationDate();
        boolean isProductionDateValid = eventModel.isScannedProductionDateValid();
        boolean isProductExpired = eventModel.isCurrentProductExpiredByScannedDate();
        boolean isExpirationDateValid = eventModel.isScannedExpirationDateValid(getController().isRefund());
        boolean needCheckExpirationDate = getController().needCheckExpirationDate();
        if (hasScannedExpirationDate && !isExpirationDateValid && needCheckExpirationDate) {
            messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
            messageForm.setMessage(getController().getModel().buildErrorMessageForExpirationDate(getController().isRefund()));
            setCurrentForm(messageForm);
            getController().beepError("Incorrect expiration date scanned.");
        } else if (hasScannedProductionDate && (!isProductionDateValid || isProductExpired && needCheckExpirationDate)) {
            messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
            messageForm.setMessage(ResBundleVisualization.getString(!isProductionDateValid ? "INCORRECT_PRODUCTION_DATE" : "PRODUCT_HAS_EXPIRED"));
            setCurrentForm(messageForm);
            getController().beepError("Incorrect production date scanned.");
        } else if (isRfidAlreadyInCheck(eventModel)) {
            messageForm.setMessage(ResBundleVisualization.getString("RFID_IN_CHECK"));
            setCurrentForm(messageForm);
        } else if (eventModel.getAgeRestriction() != 0) {
            checkAgePanel.reset();
            checkAgePanel.setMinAge(eventModel.getAgeRestriction());
            setCurrentPanel(checkAgePanel);
            getController().onAgeChecking();
        } else if (isKitComponent()) {
            addKitProduct();
        } else if (eventModel.isNeedScanMark() && !scanExciseForAddForm.isValidExcise()) {
            setCurrentForm(scanExciseForAddForm);
            if (eventModel.getPosition().getExciseToken() != null) {
                boolean dispatched = scanExciseForAddForm.dispatchBarcodeEvent(eventModel.getPosition().getExciseToken());
                if (!dispatched) {
                    dispatchBarcodeScanned(eventModel.getPosition().getExciseToken());
                }
            }
        } else {
            setCurrentForm(enterQuantityForm);
            PositionEntity position = new PositionEntity();
            getController().fillDefaultPosition(BigDecimal.ONE, enterQuantityForm.getPrice(), eventModel.getProduct(), position);
            eventModel.setPosition(position);
        }
    }

    private boolean isRfidAlreadyInCheck(PiecePluginModel eventModel) {
        final PositionEntity position = eventModel.getPosition();
        if (position == null) {
            return false;
        }
        return pieceController.checkForDuplicationRFID(eventModel.getPosition().getProduct());
    }

    @Override
    public boolean checkDataBeforeAdd() {
        if (getController().getModel().getState() != ProductState.ADD) {
            return true;
        }

        if (!getController().getModel().isCanSkipScanMarkForm() && getController().getModel().isNeedScanMark() && getController().getModel().getPosition().getExciseToken() == null) {
            return false;
        }

        if (isCurrentForm(enterQuantityForm)) {
            try {
                getController().checkInputDataBeforeAdd(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice());
                return true;
            } catch (Exception e) {
                return false;
            }
        } else if (isCurrentForm(checkAgePanel) || isKitComponent()) {
            try {
                getController().checkInputDataBeforeAdd(getController().getModel().getProduct(), getController().getModel().getPosition().getQntyBigDecimal(),
                        getController().getModel().getProduct().getPrice().getPriceBigDecimal());
                return true;
            } catch (Exception e) {
                return false;
            }
        } else if (isCurrentForm(enterProductionDateForm) || isCurrentForm(enterExpirationCodeForm)) {
            try {
                getController().checkInputDataBeforeAdd(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice());
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
        if (isCurrentForm(enterQuantityForm) || isCurrentForm(waitForm)) {
            result = BigDecimalConverter.convertQuantity(enterQuantityForm.getQuantity());
        } else if (isCurrentForm(checkAgePanel)) {
            result = BigDecimalConverter.convertQuantity(getController().getModel().getPosition().getQntyBigDecimal());
        }
        return result == null ? 0 : result;
    }
}
