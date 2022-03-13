package ru.crystals.pos.visualization.products.spirits.view;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ExciseValidationErrorType;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;
import ru.crystals.pos.egais.excise.validation.ExciseValidationType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.CheckAgeController;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.forms.CheckAgePanel;
import ru.crystals.pos.visualization.products.forms.CheckAgePanelListener;
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

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
public class SpiritsPluginView extends CommonAbstractView<SpiritProductController> {
    private static final Logger log = LoggerFactory.getLogger(SpiritsPluginView.class);

    private final SpiritScanExciseForm scanExciseForm;
    private final SpiritRestrictionForm restrictionForm;
    private final SpiritEnterQuantityForm enterQuantityForm;
    private final SpiritEditForm editForm;
    private final SpiritEditOrDeleteForm editOrDeleteForm;
    private final SpiritViewProductForm viewForm;
    private final SpiritViewProductRestrictionForm viewRestrictionForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final SpiritDeletePositionForm deletePositionEgaisForm;
    private final SpiritExciseValidationForm exciseValidationForm;
    private final SpiritConnectionWarningForm connectionWarningForm;
    private final CheckAgePanel checkAgePanel;

    public SpiritsPluginView(Properties properties, ExciseValidation egaisExciseCheckValidation) {
        scanExciseForm = new SpiritScanExciseForm(this);

        restrictionForm = new SpiritRestrictionForm(this);
        enterQuantityForm = new SpiritEnterQuantityForm(this);
        editOrDeleteForm = new SpiritEditOrDeleteForm(this);
        viewForm = new SpiritViewProductForm(this);
        editForm = new SpiritEditForm(this);
        deletePositionForm = new CommonDeletePositionConfirmForm(this);
        deletePositionEgaisForm = new SpiritDeletePositionForm(this);

        viewRestrictionForm = new SpiritViewProductRestrictionForm(this);

        exciseValidationForm = new SpiritExciseValidationForm(egaisExciseCheckValidation, this);
        connectionWarningForm = new SpiritConnectionWarningForm(egaisExciseCheckValidation, this);

        checkAgePanel = new CheckAgePanel(properties, new CheckAgePanelListener() {
            @Override
            public void esc() {
                getController().getModel().setConfirmedCustomerBirthDate(null);
                getController().processEscPressEventWithSpecialPrivilege();
                getController().onAgeCheckingCompleted();
            }

            @Override
            public void select(boolean confirm, int age, Date birthDate) {
                if (confirm) {
                    getController().getModel().setConfirmedCustomerBirthDate(birthDate);
                    if (isKitComponent()) {
                        addKitProduct();
                    } else {
                        setStartAddForm();
                    }
                } else {
                    esc();
                }
                getController().onAgeCheckingCompleted();
            }

            @Override
            public boolean isPossibleEnterBirthDate() {
                return true;
            }
        });

        addPanel(scanExciseForm);
        addPanel(restrictionForm);
        addPanel(enterQuantityForm);
        addPanel(editOrDeleteForm);
        addPanel(editForm);
        addPanel(viewForm);
        addPanel(checkAgePanel);
        addPanel(deletePositionForm);
        addPanel(deletePositionEgaisForm);
        addPanel(exciseValidationForm);
        addPanel(connectionWarningForm);
        addPanel(viewRestrictionForm);
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        // если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return processEnterKey();
        } else if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
            return true;
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscKey();
        }
        return false;
    }

    /**
     * Нажали ESC на какой-то форме, что делаем дальше?
     * Если вернуть false - выйдем из плагина
     */
    private boolean processEscKey() {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        if (isCurrentForm(deletePositionEgaisForm)) {
            if (deletePositionEgaisForm.getQuantity().intValue() > 0 &&
                    (getController().getModel().getState() == ProductState.EDIT_OR_DELETE || getController().getModel().getState() == ProductState.QUICK_EDIT)) {
                //если при уменьшении акцизного товара ввели хоть 1 акциз, то при ESC кол-во в позиции изменится
                getController().getModel().getPosition().setInMemoryBottles(deletePositionEgaisForm.getBottles());
                getController().changeQuantity(getController().getModel().getPosition(),
                        getController().getModel().getPosition().getQntyBigDecimal().subtract(deletePositionEgaisForm.getQuantity()));
                return true;
            }
        } else if (isCurrentForm(exciseValidationForm)) {
            setCurrentPanel(scanExciseForm);
            currentForm = scanExciseForm;
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
            return true;
        } else if (isCurrentForm(connectionWarningForm)) {
            setCurrentPanel(scanExciseForm);
            currentForm = scanExciseForm;
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
            return true;
        }
        return false;
    }

    private boolean processEnterKey() {
        if (isCurrentForm(connectionWarningForm)) {
            setCurrentPanel(scanExciseForm);
            currentForm = scanExciseForm;
            if (connectionWarningForm.getFormType() == ExciseValidationType.SetRetail && connectionWarningForm.isYes()) {
                //продолжить
                if (!getController().addBottleWithBarcode(getController().getModel().getScannedBarcode(), scanExciseForm)) {
                    dispatchBarcodeScanned(getController().getModel().getScannedBarcode());
                }
            }
            if (connectionWarningForm.getFormType() == ExciseValidationType.SetRetail && !connectionWarningForm.isYes()) {
                //обновить
                if (!scanExciseForm.dispatchBarcodeEvent(getController().getModel().getScannedBarcode())) {
                    dispatchBarcodeScanned(getController().getModel().getScannedBarcode());
                }
            }
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
            return true;
        }

        if (isCurrentForm(exciseValidationForm)) {
            setCurrentPanel(scanExciseForm);
            currentForm = scanExciseForm;
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();

            if (exciseValidationForm.isYes() && getController().isProductRefund() &&
                    !getController().addBottleWithBarcode(getController().getModel().getScannedBarcode(), scanExciseForm)) {
                dispatchBarcodeScanned(getController().getModel().getScannedBarcode());
            }
            return true;
        }

        if (currentForm == restrictionForm) {
            //сообщение о запрете продажи - нажали ентер - вышли из плагина
            return false;
        }

        if (getController().getModel().getState() == ProductState.VIEW) {
            return true;
        }

        // если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
        // добавляем позицию в чек.
        if (isCurrentForm(enterQuantityForm)
                && getController().getModel() != null && getController().getModel().getState() == ProductState.ADD
                && currentForm.getQuantity() != null && currentForm.getPrice() != null) {
            if (getController().getModel().isScanExciseLabelsMode()) {
                showScanExciseForm();
                //SRTB-3665 Если у продукта есть акцизка, то мы пришли по рельсам быстрой продажи
                String excise = getController().getModel().getProduct().getProductPositionData().getExcise();
                if (StringUtils.isNotEmpty(excise)) {
                    //  отдадим АМ форме сканирования АМ (т.к. именно она ПОЧЕМУ-ТО занимается валидацией)
                    boolean dispatched = scanExciseForm.dispatchBarcodeEvent(excise);
                    if (!dispatched) {
                        dispatchBarcodeScanned(excise);
                    }
                }
            } else {
                getController().addSpiritPosition(
                        getController().getModel().getProduct(),
                        enterQuantityForm.getQuantity(),
                        enterQuantityForm.getPrice(),
                        true,
                        null
                );
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

        if (isCurrentForm(editForm) || isCurrentForm(enterQuantityForm)) {
            if (getController().getModel().isScanExciseLabelsMode()) {
                int q = currentForm.getQuantityDiff().intValue();
                if (q < 0) {
                    setCurrentForm(deletePositionEgaisForm, false);
                    deletePositionEgaisForm.setQuantity(-q);
                    return true;
                } else if (q > 0) {
                    scanExciseForm.setQuantity(BigDecimal.valueOf(q));
                    setCurrentForm(scanExciseForm, false);
                    return true;
                }
                getController().changeQuantity(getController().getModel().getPosition(), currentForm.getQuantity());
            } else {
                BigDecimal quantity = isCurrentForm(editForm) ? editForm.getQuantity() : enterQuantityForm.getQuantity();
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionEditPosition(getController().getModel().getPosition(), quantity, isCurrentForm(editForm));
                } catch (NoPermissionException ex) {
                    getController().beepError(ex.getMessage());
                    return true;
                }
                getController().changeQuantity(getController().getModel().getPosition(), currentForm.getQuantity(), null, checkPermission);
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
                } catch (NoPermissionException e) {
                    getController().beepError(e.getMessage());
                    return true;
                }
                getController().cashDeleteSpiritPosition(getController().getModel().getPosition(), null, !checkPermission);
            } else {
                getController().getAdapter().dispatchCloseEvent(false);
            }
            return true;
        }

        return false;
    }

    private void showScanExciseForm() {
        if (getController().getModel().getProduct().isKit()) {
            // SRL-848 товар является набором, надо сканировать все акцизные марки набора
            scanExciseForm.setKitQuantity(getController().getModel().getProduct().getExciseBottles().size());
        } else {
            scanExciseForm.setQuantity(enterQuantityForm.getQuantity());
        }
        scanExciseForm.setSumm(enterQuantityForm.getSumm());
        setCurrentForm(scanExciseForm, false);
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (isCurrentForm(enterQuantityForm) && getController().getModel().isScanExciseLabelsMode() && getController().isExciseBarcode(barcode)) {
            //  покажем форму сканирования АМ
            showScanExciseForm();

            if (getController().currentProductIsMarked()) {
                //Пришли при сканирование АМ когда в подвале товар добавлен быстрым сканированием
                //надо добавить товар из подвала и отправить АМ на поиск дальше, она не от этого товара
                if (scanExciseForm.dispatchBarcodeEvent(getController().getModel().getProduct().getProductPositionData().getExcise())) {
                    return true;
                }

                if (getController().getModel().isValidationError()) {
                    //Если не смогли добавить позицию, на экране ошибка
                    //Сосканированную АМ никуда передавать не будем
                    return true;
                } else {
                    getController().addSpiritPosition(getController().getModel().getProduct(),
                            enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(),
                            false, getController().getModel().getProcessedBottles());
                    //Передадим АМ дальше
                    return false;
                }
            }

            //  отдадим АМ форме сканирования АМ (т.к. именно она ПОЧЕМУ-ТО занимается валидацией)
            if (scanExciseForm.dispatchBarcodeEvent(barcode)) {
                // Либо что-то не так с переданной АМ, либо позиций больше одной и нужно отсканировать следующие марки
                return true;
            }
        }

        if (isCurrentForm(scanExciseForm)) {
            // сюда можно провалитсья или при добавлении новой позиции, или когда редактируешь существующую и поменял кол-во в
            // сторону увеличения
            if (getController().getModel().getState() == ProductState.ADD) {
                getController().addSpiritPosition(getController().getModel().getProduct(), enterQuantityForm.getQuantity(), enterQuantityForm.getPrice(), false,
                        getController().getModel().getProcessedBottles());
            } else if (getController().getModel().getState() == ProductState.QUICK_EDIT || getController().getModel().getState() == ProductState.EDIT || getController().getModel().getState() == ProductState.EDIT_OR_DELETE) {
                BigDecimal quantity = currentForm.getQuantity().add(getController().getModel().getPosition().getQntyBigDecimal());
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionEditPosition(getController().getModel().getPosition(), quantity,
                            getController().getModel().getState() == ProductState.EDIT_OR_DELETE);
                } catch (NoPermissionException ex) {
                    getController().beepError(ex.getMessage());
                    return true;
                }
                //редактировали позицию и добавили еще currentForm.getQuantity() бутылок
                getController().getModel().getPosition().setInMemoryBottles(getController().getModel().getProcessedBottles());
                getController().changeQuantity(getController().getModel().getPosition(), quantity, null, checkPermission);
            }
            return true;
        } else if (isCurrentForm(deletePositionEgaisForm)) {
            boolean checkPermission;
            try {
                checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                        getController().getModel().getState() == ProductState.DELETE);
            } catch (NoPermissionException ex) {
                getController().beepError(ex.getMessage());
                return true;
            }
            if (EnumSet.of(ProductState.QUICK_DELETE, ProductState.DELETE).contains(getController().getModel().getState())) {
                getController().cashDeleteSpiritPosition(getController().getModel().getPosition(), deletePositionEgaisForm.getBottles(), !checkPermission);
            } else {
                //редактировали позицию и удалили currentForm.getQuantity() бутылок
                getController().getModel().getPosition().setInMemoryBottles(deletePositionEgaisForm.getBottles());
                getController().changeQuantity(getController().getModel().getPosition(),
                        getController().getModel().getPosition().getQntyBigDecimal().subtract(deletePositionEgaisForm.getQuantity()), null, checkPermission);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchMSREvent(String Track1, String Track2, String Track3, String Track4) {
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
                if (getController().getModel().getRestrictionMessage() != null) {
                    restrictionForm.setWarnMessage(getController().getModel().getRestrictionMessage());
                    setCurrentForm(restrictionForm);
                } else if (isCurrentForm(waitForm)) {
                    setCurrentFormWithoutAdditionalActions(enterQuantityForm);
                } else {
                    processAddState();
                }
                break;
            case QUICK_EDIT:
                processQuickEditState(getController().getModel());
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case VIEW:
                if (getController().getModel().getRestrictionMessage() == null) {
                    setCurrentForm(viewForm, false);
                } else {
                    setCurrentForm(viewRestrictionForm, false);
                }
                break;
            case ADD_CURRENT:
                processAddCurrentState();
                break;
            case DELETE:
            case QUICK_DELETE:
                if (getController().getModel().isScanExciseLabelsMode()) {
                    setCurrentForm(deletePositionEgaisForm, false);
                } else {
                    setCurrentForm(deletePositionForm, false);
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

    private void processAddState() {
        ProductEntity product = getController().getModel().getProduct();
        int minAge = product.getProductConfig().calculateMinAge(product);

        if (minAge > 0 && CheckAgeController.isNeedToConfirmAge(minAge)) {
            checkAgePanel.reset();
            checkAgePanel.setMinAge(minAge);
            setCurrentPanel(checkAgePanel);
            getController().onAgeChecking();
        } else if (isKitComponent()) {
            addKitProduct();
        } else {
            setStartAddForm();
        }
    }

    private void processQuickEditState(CommonProductPluginModel eventModel) {
        eventModel.setPosition(eventModel.getPosition());
        eventModel.setProduct(eventModel.getPosition().getProduct());
        enterQuantityForm.setCurrentModel(getController().getModel());
        setCurrentForm(enterQuantityForm);
        getController().fillDefaultPosition(eventModel.getPosition().getQntyBigDecimal(), eventModel.getPosition().getPriceStartBigDecimal(), eventModel.getProduct(),
                eventModel.getPosition());
    }

    private void processAddCurrentState() {
        if (getController().getModel().isScanExciseLabelsMode() && !getController().currentProductIsMarked()) {
            getController().beepError("Cannot add excise spirit by SUBTOTAL");
            getController().getModel().setState(ProductState.ADD);
        } else if (isCurrentForm(enterQuantityForm) || isCurrentForm(scanExciseForm)) {
            getController().addSpiritPosition(getController().getModel().getProduct(), getCurrentQuantity(),
                    getCurrentPrice(), false, getController().getExciseBottles());
        } else if (isCurrentForm(checkAgePanel)) {
            getController().addSpiritPosition(getController().getModel().getProduct(), getController().getModel().getPosition().getQntyBigDecimal(),
                    getController().getModel().getPosition().getPriceStartBigDecimal(), false, getController().getExciseBottles());
        } else {
            getController().beepError("Cannot add spirit position from this form");
        }
    }

    @Override
    public boolean checkDataBeforeAdd() {
        if (getController().getModel().getState() == ProductState.ADD) {
            if (getController().getModel().isScanExciseLabelsMode() && isCurrentForm(checkAgePanel)) {
                return false;
            } else {
                return checkDataBeforeAddInner(true);
            }
        }
        if ((!getController().getModel().isScanExciseLabelsMode() || getController().currentProductIsMarked())
                && (isCurrentForm(enterQuantityForm) || isCurrentForm(checkAgePanel))) {
            return checkDataBeforeAddInner(true);
        } else if (getController().currentProductIsMarked()) {
            return checkDataBeforeAddInner(false);
        }

        return false;
    }

    /**
     * Проверятор перед добавлением алкопозиции в чек
     * Есть нюанс: в чек добавляется не та позиция, что сейчас в модели.
     * Например, в модели у позиции может не быть бутылок, а в добавляемой в чек - они уже проставлены.
     * Это происходит из-за того, что в интерфейсе проверятора конкретно добавляемая позиция - не передается
     *
     * @param validateIfMarked проводить ли валидацию
     * @return можно ли добавить позицию
     */
    private boolean checkDataBeforeAddInner(boolean validateIfMarked) {
        try {
            ProductSpiritsEntity product = getController().getModel().getProduct();
            getController().checkInputDataBeforeAdd(
                    product,
                    getCurrentQuantity(),
                    product.getPrice().getPriceBigDecimal()
            );

            /*
             * Проверка нужна при быстром добавлении. Рельсы - фастскан или МЧ с марками (у них будет excise в ProductPosition).
             * Наборы - мимо
             */
            String fastScanExcise = product.extractRawMarkFromPositionData();
            if (validateIfMarked && StringUtils.isNotEmpty(fastScanExcise)) {
                showScanExciseForm();
                String message = getController().checkExciseBeforeAdd(fastScanExcise, Collections.emptyList());
                if (message != null) {
                    Factory.getTechProcessImpl().startCriticalErrorBeeping();
                    scanExciseForm.showMessage(message, true);
                    return false;
                }

                ExciseValidationResult validationResult = scanExciseForm.egaisExciseValidation(fastScanExcise);
                processValidationResults(validationResult);
                return validationResult.operationPossibility;
            }
            return true;
        } catch (Exception ex) {
            log.error("", ex);
            return false;
        }
    }

    private BigDecimal getCurrentQuantity() {
        return isCurrentForm(checkAgePanel) ? BigDecimal.ONE :
                getController().currentProductIsMarked() ? enterQuantityForm.getQuantity() : currentForm.getQuantity();
    }

    private BigDecimal getCurrentPrice() {
        return getController().currentProductIsMarked() ? enterQuantityForm.getPrice() : currentForm.getPrice();
    }

    @Override
    public long getCurrentPositionCount() {
        Long result = 0L;
        if ((isCurrentForm(enterQuantityForm) || isCurrentForm(checkAgePanel)) || isCurrentForm(waitForm)) {
            BigDecimal quantity = isCurrentForm(checkAgePanel) ? BigDecimal.ONE : enterQuantityForm.getQuantity();
            result = BigDecimalConverter.convertQuantity(quantity);
        }
        return result == null ? 0 : result;
    }

    @Override
    protected void addKitProduct() {
        ProductSpiritsEntity product = getController().getModel().getProduct();
        boolean addingResult = getController().addSpiritPosition(
                product, product.getBarCode().getCountBigDecimal(), product.getPrice().getPriceBigDecimal(), true, null
        );
        getController().getAdapter().dispatchCloseEvent(addingResult);
    }

    /**
     * Отрисует реакцию UI на ошибки валидации АМ
     */
    public void processValidationResults(ExciseValidationResult exciseValidationResult) {
        if (!exciseValidationResult.operationPossibility) {
            if (ExciseValidationErrorType.NO_SERVER_CONNECTION.equals(exciseValidationResult.errorType)) {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                setCurrentForm(connectionWarningForm);
                connectionWarningForm.setMessage(exciseValidationResult.operationMessage);
            } else {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
                setCurrentForm(exciseValidationForm);
                exciseValidationForm.setMessage(exciseValidationResult);
            }
        }
    }
}
