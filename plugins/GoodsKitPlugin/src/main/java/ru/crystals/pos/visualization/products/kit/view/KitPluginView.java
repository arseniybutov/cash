package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.kit.controller.KitPluginController;
import ru.crystals.pos.visualization.products.kit.model.KitPluginModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class KitPluginView extends CommonAbstractView<KitPluginController> implements KitComponentListener {
    private final KitEnterQuantityForm enterQuantityForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final KitEditOrDeleteForm editOrDeleteForm;
    private final KitEditForm editForm;
    private final KitViewForm viewForm;
    private final KitComponentContainer kitComponentContainer;
    private final List<ProductEntity> addedComponents = new ArrayList<>();
    /**
     * Флаг который переставляет kitComponentContainer.
     * Передаем в него
     */
    private boolean componentAdded;

    public KitPluginView() {
        enterQuantityForm = new KitEnterQuantityForm(this);
        kitComponentContainer = new KitComponentContainer(this);

        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        editOrDeleteForm = new KitEditOrDeleteForm(this);
        editForm = new KitEditForm(this);
        viewForm = new KitViewForm(this);

        addPanel(enterQuantityForm);
        addPanel(deletePositionForm);
        addPanel(editOrDeleteForm);
        addPanel(editForm);
        addPanel(viewForm);
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscapeKey();
        }
        return false;
    }

    private boolean processEnterKey() {
        if (getController().getModel().getState() == ProductContainer.ProductState.ADD) {
            BigDecimal quantity = enterQuantityForm.getQuantity();
            if (getController().productCheckBeforeAdd(getController().getModel().getProduct(), quantity)) {
                addedComponents.clear();
                processKitComponents(quantity);
                getController().getAdapter().dispatchCloseEvent(true);
            }
            return true;
        }

        if (isCurrentForm(deletePositionForm)) {
            if (deletePositionForm.deleteConfirmed()) {
                String kitItem = getController().getModel().getPosition().getBarCode();
                PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
                PositionEntity positionEntity = check.getPositions().stream()
                        .filter(pos -> kitItem.equals(pos.getKitBarcode())).findFirst().get();
                getController().cashDeletePosition(positionEntity);
            } else {
                getController().getAdapter().dispatchCloseEvent(false);
            }
            return true;
        }

        if (isCurrentForm(enterQuantityForm) || isCurrentForm(editForm)) {
            BigDecimal quantity = isCurrentForm(editForm) ? editForm.getQuantity() : enterQuantityForm.getQuantity();
            if (getController().productCheckBeforeAdd(getController().getModel().getProduct(), quantity)) {
                getController().changeQuantity(getController().getModel().getPosition(), quantity);
            }
            return true;
        }

        if (isCurrentForm(editOrDeleteForm)) {
            if (editOrDeleteForm.isEdit()) {
                setCurrentForm(editForm);
            } else {
                getController().returnPosition(getController().getModel().getPosition());
            }
        }
        return false;
    }


    private boolean processEscapeKey() {
        if (isCurrentForm(editForm)) {
            setCurrentForm(editOrDeleteForm);
            return true;
        }

        return false;
    }

    private void processKitComponents(BigDecimal quantity) {
        kitComponentContainer.setKitQuantity(quantity);

        for (ProductEntity component : getController().getModel().getComponents()) {
            kitComponentContainer.processComponent(component);

            if (componentAdded) {
                addedComponents.add(component);
            } else {
                getController().deleteAlreadyAddedComponents(getController().getModel().getProduct(), addedComponents);
                break;
            }
        }
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        return false;
    }

    @Override
    public boolean dispatchMSREvent(String Track1, String Track2, String Track3, String Track4) {
        return false;
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD:
                processAddState(getController().getModel());
                break;
            case DELETE:
            case QUICK_DELETE:
                setCurrentForm(deletePositionForm);
                break;
            case QUICK_EDIT:
                setCurrentForm(enterQuantityForm);
                break;
            case SHOW_MESSAGE:
                messageForm.setMessage(getController().getModel().getMessage());
                messageForm.setExitState(CommonMessageForm.ExitState.TO_LAST);
                setCurrentForm(messageForm);
                break;
            case ADD_CURRENT:
                processKitComponents(enterQuantityForm.getQuantity());
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            default:
        }
    }

    private void processAddState(KitPluginModel model) {
        setStartAddForm(model);
    }

    private void setStartAddForm(KitPluginModel model) {
        enterQuantityForm.setModel(model);
        setCurrentForm(enterQuantityForm);
    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        return enterQuantityForm.getQuantity().longValue();
    }

    @Override
    public void addComponentResult(boolean success) {
        componentAdded = success;
        if (!success) {
            getController().getAdapter().dispatchCloseEvent(false);
        }
    }

    @Override
    public void showPluginView() {
        Factory.getInstance().getMainWindow().showLockComponent(kitComponentContainer);
    }

    @Override
    public void hidePluginView() {
        Factory.getInstance().getMainWindow().unlockComponent(kitComponentContainer);
    }
}
