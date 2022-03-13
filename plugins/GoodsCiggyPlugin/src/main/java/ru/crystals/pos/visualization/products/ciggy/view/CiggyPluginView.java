package ru.crystals.pos.visualization.products.ciggy.view;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.QuestionForm;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.CheckAgeController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;
import ru.crystals.pos.visualization.products.forms.CheckAgePanel;
import ru.crystals.pos.visualization.products.forms.CheckAgePanelListener;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Отображение в паттерне MVC
 * Задача - отобразить модель в графический интерфейс, изменять модель запрещается
 * Любые изменения - делаются через контроллер. Все внутренние события нажатия обрабатываются тут же, если нужны действия
 * контроллера - следует пробросить событие дальше.
 * Запрещается хранить любое состояние. Управление заходит в визуализацию только через событие изменение модели.
 * <p/>
 * Чтоб поменять форму следует:
 * -> вытащить из предыдущей форма все нужные данные
 * -> вызвать метод контроллера, который поменяет состояние модели
 * -> по изменению модели придет событие, которе обработается этим классом и уже должна показаться новая форма.
 *
 * @author nbogdanov
 */
@SuppressWarnings("serial")
public class CiggyPluginView extends CommonAbstractView<CiggyPluginController> {

    public static final Logger log = LoggerFactory.getLogger(CiggyPluginView.class);

    /**
     * Форма выбора МРЦ
     */
    private final CiggySelectPriceForm selectPriceProductForm;

    /**
     * Форма ввода количества товара
     */
    private final CiggyEnterQuantityForm enterQuantityForm;

    /**
     * Форма изменения количества товара
     */
    private final CiggyEditForm editForm;

    /**
     * Форма удаления/редактиварония товара
     */
    private final CiggyEditOrDeleteForm editOrDeleteForm;

    /**
     * Основная форма отображения товара в чеке
     */
    private final CiggyViewProductForm viewForm;

    /**
     * Форма удаления
     */
    private final CommonDeletePositionConfirmForm deletePositionForm;

    /**
     * Форма подтверждения возраста
     */
    private final CheckAgePanel checkAgePanel;

    /**
     * Форма запроса сканирования акцизной марки при добавлении
     */
    private final CiggyAddPositionScanExciseForm addPositionScanExciseForm;

    /**
     * Форма запроса сканирования акцизной марки при удалении
     */
    private final CiggyDeletePositionScanExciseForm deletePositionScanExciseForm;

    /**
     * Форма вывода различных сообщений
     */
    private final CommonMessageForm messageForm;

    private final QuestionForm questionForm;

    /**
     * Флаг нахождения в дефолтном состоянии
     */
    private boolean reset = true;

    private CommonProductPluginModel lastModelBeforeAgeChange;

    public CiggyPluginView(Properties properties) {
        selectPriceProductForm = new CiggySelectPriceForm(this);
        enterQuantityForm = new CiggyEnterQuantityForm(this);
        currentForm = selectPriceProductForm;
        editOrDeleteForm = new CiggyEditOrDeleteForm(this);
        viewForm = new CiggyViewProductForm(this);
        editForm = new CiggyEditForm(this);
        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        addPositionScanExciseForm = new CiggyAddPositionScanExciseForm(this);
        messageForm = new CommonMessageForm(this);
        questionForm = new QuestionForm(
                new XListener() {
                    @Override
                    public boolean barcodeScanned(String barcode) {
                        return true;
                    }

                    @Override
                    public boolean keyPressedNew(XKeyEvent e) {
                        getController().processQuestion(questionForm.isCommit());
                        return true;
                    }

                    @Override
                    public boolean eventMSR(String track1, String track2, String track3, String track4) {
                        return true;
                    }
                },
                StringUtils.EMPTY,
                ResBundleVisualization.getString("ACCEPT"),
                ResBundleVisualization.getString("CANCEL"));
        deletePositionScanExciseForm = new CiggyDeletePositionScanExciseForm(this);
        checkAgePanel = new CheckAgePanel(properties, new CheckAgePanelListener() {
            @Override
            public void esc() {
                getController().processEscPressEvent();
                getController().onAgeCheckingCompleted();
            }

            @Override
            public void select(boolean confirm, int age, Date birthDate) {
                if (confirm) {
                    if (needScanExcise()) {
                        setCurrentForm(addPositionScanExciseForm);
                        String exciseToken = getController().getModel().getPosition().getExciseToken();
                        if (exciseToken != null && (!isCurrentForm(selectPriceProductForm) || reset)) {
                            boolean dispatched = addPositionScanExciseForm.dispatchBarcodeEvent(exciseToken);
                            if (!dispatched) {
                                dispatchBarcodeScanned(exciseToken);
                            }
                        }
                    } else {
                        checkPriceOrEnterQuantity();
                    }
                } else {
                    esc();
                }
                getController().onAgeCheckingCompleted();
            }
        });

        addPanel(selectPriceProductForm);
        addPanel(enterQuantityForm);
        addPanel(editOrDeleteForm);
        addPanel(editForm);
        addPanel(viewForm);
        addPanel(checkAgePanel);
        addPanel(deletePositionForm);
        addPanel(addPositionScanExciseForm);
        addPanel(messageForm);
        addPanel(questionForm);
        addPanel(deletePositionScanExciseForm);
    }

    private void checkPriceOrEnterQuantity() {
        settingForm();
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        boolean dispatched = dispatchKeyPressed(e);
        if (!dispatched) {
            //если не знаем как разрулить евент, отдаем его внешнему слушателю
            return getController().keyPressedNew(e);
        }
        return dispatched;
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        // если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscKey();
        } else if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
            return true;
        } else {
            return (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar());
        }
    }

    private boolean processEnterKey() {
        if (ProductState.VIEW.equals(getController().getModel().getState())) {
            return true;
        }
        // если мы выбрали цену, но не выбрали количество (товар типа сигарет или ПК)
        if (isCurrentForm(selectPriceProductForm) && ProductState.ADD.equals(getController().getModel().getState())) {
            //Если установлены часы повышенного спроса то в качестве цены берем МРЦ
            getController().selectCiggyPrice(getController().isHighDemandHours() ? selectPriceProductForm.getMRP() : selectPriceProductForm.getPrice());
            return true;
        }

        // если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
        // добавляем позицию в чек.
        if (addPosition()) {
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
            getController().changeQuantity(getController().getModel().getPosition(), quantity, null, checkPermission);
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

                getController().cashDeletePosition(getController().getModel().getPosition(), null, !checkPermission);
            } else {
                getController().getAdapter().dispatchCloseEvent(false);
            }
            return true;
        }

        return false;
    }

    public void showSelectPriceForm() {
        setCurrentForm(selectPriceProductForm);
    }

    private boolean processEscKey() {
        // по нажатию Esc идем назад к форме выбора цены
        if (isCurrentForm(enterQuantityForm) && getController().getModel().getState() == ProductContainer.ProductState.ADD &&
                !((ProductCiggyEntity) getController().getModel().getProduct()).getAdditionalPrices().isEmpty() && !getController().isPriceSelectedFromExcise()) {
            setCurrentForm(selectPriceProductForm);
            return true;
        }

        if (isCurrentForm(addPositionScanExciseForm) && getController().onSkipScanMark()) {
            return true;
        }

        clear();
        return false;
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (isCurrentForm(enterQuantityForm)) {
            if (Factory.getInstance().getBarcodeProcessor().isProduct(barcode)) {
                addPosition();
            }
            return false;
        }
        if (getController().currentProductIsMarked() || getController().getModel().getPosition().getExciseToken() != null) {
            if (isCurrentForm(addPositionScanExciseForm)) {
                if (addPositionScanExciseForm.getPrice() == null) {
                    checkPriceOrEnterQuantity();
                } else {
                    getController().selectCiggyPrice(addPositionScanExciseForm.getPrice());
                }
            } else if (isCurrentForm(deletePositionScanExciseForm)) {
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                            getController().getModel().getState() == ProductState.DELETE);
                } catch (NoPermissionException ex) {
                    getController().beepError(ex.getMessage());
                    return true;
                }
                deletePosition(checkPermission);
            } else {
                getController().beepError("Cannot add ciggy position from this form");
            }
            return true;
        }
        if (!getController().isPriceSelected()) {
            getController().beepError("Cannot process barcode on select ciggy addition price form.");
            return true;   //true - сделает вид, что событие обработано и не пустит его дальше
        }
        return false;
    }

    /**
     * Добавление позиции
     *
     * @return true - если позиция успешно добавлена, в противном случае - false
     */
    private boolean addPosition() {
        if (isStateAddPosition()) {
            if (getController().addPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), true)) {
                clear();
                getController().getAdapter().dispatchCloseEvent(true);
            }
            return true;
        }
        return false;
    }

    /**
     * Удаление позиции из чека
     */
    private void deletePosition(boolean checkPermission) {
        getController().cashDeletePosition(getController().getModel().getPosition(), null, !checkPermission);
        clear();
    }

    /**
     * Сбросить параметры
     */
    private void clear() {
        getController().stopBeepError();
        addPositionScanExciseForm.clear();
        deletePositionScanExciseForm.clear();
    }

    private boolean isStateAddPosition() {
        boolean state = getController().getModel().getState() == ProductState.ADD || getController().getModel().getState() == ProductState.ADD_CURRENT;
        boolean quantityAndPrice = currentForm.getQuantity() != null && currentForm.getPrice() != null;
        return getController().getModel() != null && state && quantityAndPrice;
    }

    @Override
    public boolean dispatchMSREvent(String Track1, String Track2, String Track3, String Track4) {
        return false;
    }

    private void setStartAddForm(CommonProductPluginModel eventModel) {
        settingForm(eventModel.getProduct() != null && ((!isCurrentForm(selectPriceProductForm) && !isCurrentForm(questionForm)) || reset));
    }

    /**
     * Безусловное выполнение отображения нового окна
     */
    private void settingForm() {
        settingForm(true);
    }

    /**
     * Отобразить новое окно в зависимости от условий и состояния продажи табачной продукции
     *
     * @param condition - дополнительное условие, которое следует соблюсти при проверке основных условий
     */
    private void settingForm(boolean condition) {
        CiggyPluginController.State state = getController().checkNextStateOfAddingPosition(condition);
        switch (state) {
            case ERROR:
                setErrorForm(getController().getModel().getMessage());
                break;
            case SELECT_PRICE:
                setCurrentForm(selectPriceProductForm);
                break;
            case ENTER_QUANTITY:
                setCurrentForm(enterQuantityForm);
                break;
            default:
                //  nothing to see here
        }
    }

    public void setErrorForm(String errorMessage) {
        messageForm.setMessage(errorMessage);
        setCurrentForm(messageForm);
        getController().beepError("Cannot add ciggy position");
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD:
                if (isCurrentForm(waitForm)) {
                    setCurrentFormWithoutAdditionalActions(enterQuantityForm);
                } else if (getController().getModel().getMessage() != null) {
                    setErrorForm(getController().getModel().getMessage());
                } else {
                    processAddState(getController().getModel());
                }
                break;
            case QUICK_EDIT:
                processQuickEditState(getController().getModel());
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            case ADD_CURRENT:
                processAddCurrentState();
                break;
            case DELETE:
            case QUICK_DELETE:
                if (getController().getModel().getPosition().getExciseToken() != null) {
                    setCurrentForm(deletePositionScanExciseForm, false);
                    deletePositionScanExciseForm.showForm((ProductCiggyEntity) getController().getModel().getPosition().getProduct(),
                            getController().getModel().getPosition());
                } else {
                    setCurrentForm(deletePositionForm, false);
                }
                break;
            case SHOW_WAIT:
                waitForm.setActionStatusText(getController().getModel().getMessage());
                setCurrentPanel(waitForm);
                break;
            case SHOW_QUESTION:
                processShowQuestion(getController().getModel().getMessage());
                break;
            default:
                break;
        }
    }

    private void processShowQuestion(String message) {
        questionForm.setText(message);
        questionForm.selectNoButton();
        setCurrentPanel(questionForm);
    }

    private void processAddState(CommonProductPluginModel eventModel) {
        if (eventModel.getPosition() == null) {
            PositionEntity position = new PositionEntity();
            getController().fillDefaultPosition(BigDecimal.ONE, eventModel.getProduct().getPrice().getPriceBigDecimal(), eventModel.getProduct(), position);
            eventModel.setPosition(position);
        }

        PositionEntity position = eventModel.getPosition();
        ProductEntity product = eventModel.getProduct();
        int minAge = product.getProductConfig().calculateMinAge(product);

        //needCheckAge(int minAge) дергает метод CheckAgeController.isNeedToConfirmAge который сохраняет в себе состояние, вызываем только когда он точно нужен,
        //иначе можно словить ошибку подобную https://crystals.atlassian.net/browse/SRTB-2769
        if (((!isCurrentForm(selectPriceProductForm) && !isCurrentForm(addPositionScanExciseForm) && !isCurrentForm(questionForm)) || reset)
                && needCheckAge(minAge)) {
            checkAgePanel.reset();
            checkAgePanel.setMinAge(minAge);
            setCurrentPanel(checkAgePanel);
            lastModelBeforeAgeChange = eventModel;
            getController().onAgeChecking();
        } else if (needScanExcise() && !addPositionScanExciseForm.isValidExcise()) {
            setCurrentForm(addPositionScanExciseForm);
            if (position.getExciseToken() != null && (!isCurrentForm(selectPriceProductForm) || reset)) {
                boolean dispatched = addPositionScanExciseForm.dispatchBarcodeEvent(position.getExciseToken());
                if (!dispatched) {
                    dispatchBarcodeScanned(position.getExciseToken());
                }
            }
        } else {
            setStartAddForm(eventModel);
        }
        reset = false;
    }

    private boolean needScanExcise() {
        return getController().getModel().isNeedScanMark();
    }

    private void processQuickEditState(CommonProductPluginModel eventModel) {
        eventModel.setPosition(eventModel.getPosition());
        eventModel.setProduct(eventModel.getPosition().getProduct());
        setCurrentForm(enterQuantityForm);
        getController().fillDefaultPosition(eventModel.getPosition().getQntyBigDecimal(), eventModel.getPosition().getPriceStartBigDecimal(), eventModel.getProduct(),
                eventModel.getPosition());
    }

    private void processAddCurrentState() {
        if (isCurrentForm(enterQuantityForm)) {
            // спец состояние, когда касса сама инициирует добавление по тем данным, которые сейчас на текущей форме
            getController().addPosition(getController().getModel().getProduct(), currentForm.getQuantity(), currentForm.getPrice(), false);
        } else if (isCurrentForm(checkAgePanel)) {
            try {
                PositionEntity position = lastModelBeforeAgeChange.getPosition();
                getController().applySoftCheckAttributes(position);
                Factory.getTechProcessImpl().addPosition(position, InsertType.SCANNER);
                clear();
                getController().getAdapter().dispatchCloseEvent(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getController().beepError("Cannot add ciggy position from this form");
        }
    }

    private boolean needCheckAge(int minAge) {
        return getController().isCheckAge() && CheckAgeController.isNeedToConfirmAge(minAge);
    }

    @Override
    public boolean checkDataBeforeAdd() {
        if (isCurrentForm(enterQuantityForm) || isCurrentForm(addPositionScanExciseForm)) {
            try {
                getController().checkInputDataBeforeAdd(getController().getModel().getProduct(), currentForm.getQuantity(), currentForm.getPrice());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        if (!getController().checkNextStateOfAddingPosition(true).equals(CiggyPluginController.State.ENTER_QUANTITY)) {
            return false;
        }

        return (!getController().getModel().isNeedScanMark() || getController().getModel().getPosition().getExciseToken() != null)
                && checkValidationBeforeAdd();
    }

    /**
     * Попадаем сюда сканируя новый товар на форме проверки возраста
     * Нужно разрешать продажу нового товара только при прохождении валидации подвального
     *
     * @return если можно добавлять в чек
     */
    boolean checkValidationBeforeAdd() {
        if (isCurrentForm(checkAgePanel) && needScanExcise()) {
            String exciseToken = getController().getModel().getPosition().getExciseToken();
            setCurrentForm(addPositionScanExciseForm);
            if (exciseToken != null && addPositionScanExciseForm.dispatchBarcodeEvent(exciseToken)) {
                return false;
            } else {
                setCurrentPanel(checkAgePanel);
            }
        }
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        Long result = 0L;
        if (isCurrentForm(enterQuantityForm) || isCurrentForm(waitForm)) {
            result = BigDecimalConverter.convertQuantity(enterQuantityForm.getQuantity());
        }
        return result == null ? 0L : result;
    }

    public void reset() {
        reset = true;
        clear();
    }
}
