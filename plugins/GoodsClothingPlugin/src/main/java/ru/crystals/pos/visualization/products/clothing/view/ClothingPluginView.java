package ru.crystals.pos.visualization.products.clothing.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonDialogForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.clothing.controller.ClothingProductController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Отображение в паттерне MVC Задача - отобразить модель в графический
 * интерфейс, изменять модель запрещается Любые изменения - делаются через
 * контроллер. Все внутренние события нажатия обрабатываются тут же, если нужны
 * действия контроллера - следует пробросить событие дальше. Запрещается хранить
 * любое состояние. Управление заходит в визуализацию только через событие
 * изменение модели.
 * <p/>
 * Чтоб поменять форму следует: -> вытащить из предыдущей форма все нужные
 * данные -> вызвать метод контроллера, который поменяет состояние модели -> по
 * изменению модели придет событие, которе обработается этим классом и уже
 * должна показаться новая форма.
 *
 * @author Tatarinov Eduard
 */
@SuppressWarnings("serial")
public class ClothingPluginView extends CommonAbstractView<ClothingProductController> {
    private final ClothingScanKisForm scanCisForm;
    private final ClothingEnterQuantityForm enterQuantityForm;
    private final ClothingEditForm editForm;
    private final ClothingEditOrDeleteForm editOrDeleteForm;
    private final ClothingViewProductForm viewForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;

    public ClothingPluginView() {
        enterQuantityForm = new ClothingEnterQuantityForm(this);
        editOrDeleteForm = new ClothingEditOrDeleteForm(this);
        viewForm = new ClothingViewProductForm(this);
        editForm = new ClothingEditForm(this);
        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        CommonDialogForm exciseDialogForm = new CommonDialogForm(this);
        scanCisForm = new ClothingScanKisForm(this);
        addPanel(scanCisForm);
        addPanel(enterQuantityForm);
        addPanel(editOrDeleteForm);
        addPanel(editForm);
        addPanel(viewForm);
        addPanel(deletePositionForm);
        addPanel(exciseDialogForm);
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        // если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return procesEnterKey();
        } else if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
            return true;
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscKey();
        }
        return false;
    }

    private boolean processEscKey() {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        return false;
    }

    private boolean isStateAddPosition(){
        return getController().getModel() != null && (getController().getModel().getState() == ProductState.ADD || getController().getModel().getState() == ProductState.ADD_CURRENT);
    }

    private boolean procesEnterKey() {

        if (isCurrentForm(scanCisForm) && isStateAddPosition()
                && enterQuantityForm.getQuantity() != null && enterQuantityForm.getPrice() != null) {
            getController().addClothesPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true);
            return true;
        }

        if (getController().getModel().getState() == ProductState.VIEW) {
            return true;
        }

        // если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
        // добавляем позицию в чек.
        if (isCurrentForm(enterQuantityForm)
                && isStateAddPosition()
                && currentForm.getQuantity() != null && currentForm.getPrice() != null) {
            if (getController().getModel().isScanExciseLabelsMode()) {
                scanCisForm.setSumm(enterQuantityForm.getSumm());
                setCurrentForm(scanCisForm, false);
            } else {
                getController().addClothesPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true);
            }
            return true;
        }

        if (isCurrentForm(editOrDeleteForm)) {
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
                getController().cashDeletePosition(getController().getModel().getPosition());
            } else {
                getController().getAdapter().dispatchCloseEvent(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (isCurrentForm(scanCisForm)) {
            addClothesPosition();
            return true;
        } else if (isCurrentForm(enterQuantityForm) && !getController().getModel().isScanExciseLabelsMode()) {
            addClothesPosition();
        } else {
            getController().beepError("Cannot add cis clothing position from this form");
        }
        return false;
    }

    private void addClothesPosition() {
        if (isStateAddPosition()) {
            getController().addClothesPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true);
        }
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    private void setStartAddForm() {
        enterQuantityForm.setCurrentModel(getController().getModel());
        setCurrentForm(enterQuantityForm);
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD:
                setStateAdd();
                break;
            case EDIT_OR_DELETE:
                setStateEditOrDelete();
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            case ADD_CURRENT:
                //состояние, когда касса сама инициирует добавление по тем данным, которые сейчас на текущей форме
                setStateAddCurrent();
                break;
            case DELETE:
            case QUICK_DELETE:
                setCurrentForm(deletePositionForm, false);
                break;
            case SHOW_WAIT:
                waitForm.setActionStatusText(getController().getModel().getMessage());
                setCurrentPanel(waitForm);
                break;
            default:
                break;
        }
    }

    private void setStateAdd() {
        if (isCurrentForm(waitForm)) {
            setCurrentFormWithoutAdditionalActions(enterQuantityForm);
        } else {
            setStartAddForm();
        }
    }

    private void setStateAddCurrent() {
        if (getController().getModel().isScanExciseLabelsMode()) {
            scanCisForm.setSumm(enterQuantityForm.getSumm());
            setCurrentForm(scanCisForm, false);
        } else if (isCurrentForm(enterQuantityForm) && !getController().getModel().isScanExciseLabelsMode()) {
            getController().addClothesPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true);
        } else {
            getController().beepError("Cannot add clothing position from this form");
        }
    }

    private void setStateEditOrDelete() {
        if (getController().getModel().isScanExciseLabelsMode()) {
            getController().returnPosition(getController().getModel().getPosition());
        } else {
            setCurrentForm(editOrDeleteForm);
        }
    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        Long result = 0L;
        if ( isCurrentForm(enterQuantityForm) ||  isCurrentForm(waitForm)) {
            BigDecimal quantity = enterQuantityForm.getQuantity();
            result = BigDecimalConverter.convertQuantity(quantity);
        }
        return result == null ? 0 : result;
    }
}
